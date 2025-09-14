# rag/rag_engine.py
import os, threading
import numpy as np
import faiss, pymysql, requests
from dotenv import load_dotenv

# 하이브리드 검색/SQL 라우팅
from .retrieval import HybridRetriever  # retrieval.HybridRetriever(sql_route, hybrid)

load_dotenv()

# ===== 환경변수 =====
OPENAI_API_KEY  = os.getenv("OPENAI_API_KEY")
EMBEDDING_URL   = os.getenv("EMBEDDING_URL")  # 예: https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings
CHAT_URL        = os.getenv("CHAT_URL")       # 예: https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions
EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "text-embedding-3-large")
CHAT_MODEL      = os.getenv("CHAT_MODEL", "gpt-4.1-mini")
FAISS_PATH      = os.getenv("FAISS_PATH")     # (순수 RAG 용; AUTO는 retrieval 내부 인덱스 사용)
TOP_K_DEFAULT   = int(os.getenv("TOP_K_DEFAULT", "15"))

MYSQL_HOST      = os.getenv("MYSQL_HOST")
MYSQL_USER      = os.getenv("MYSQL_USER")
MYSQL_PASSWORD  = os.getenv("MYSQL_PASSWORD")
MYSQL_DB        = os.getenv("MYSQL_DATABASE")

INTERNAL_TOKEN  = os.getenv("INTERNAL_TOKEN")  # (선택) 내부 보호용

# ===== 전역 리소스 =====
_faiss = None            # (레거시 RAG용)
_db = None               # (레거시 RAG용)
_hybrid = {"retriever": None, "alpha": 0.5, "top_k": TOP_K_DEFAULT}

_lock = threading.Lock()

# ===== 공통 초기화 =====
def _connect_db():
    return pymysql.connect(
        host=MYSQL_HOST, user=MYSQL_USER, password=MYSQL_PASSWORD,
        db=MYSQL_DB, charset="utf8mb4"
    )

def init_once():
    global _faiss, _db
    with _lock:
        # 순수 RAG 경로(호환 용) – 필요 시 유지
        if _faiss is None:
            # 환경변수 지정 경로 우선, 없거나 접근 불가 시 server/normalized_faiss_index/valorant.index로 폴백
            base_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
            candidates = []
            if FAISS_PATH:
                candidates.append(FAISS_PATH)
                if not os.path.isabs(FAISS_PATH):
                    candidates.append(os.path.join(base_dir, FAISS_PATH))
            candidates.append(os.path.join(base_dir, "normalized_faiss_index", "valorant.index"))
            for p in candidates:
                if os.path.exists(p):
                    _faiss = faiss.read_index(p)
                    break
        if _db is None:
            _db = _connect_db()
        # 하이브리드 리트리버
        if _hybrid["retriever"] is None:
            _hybrid["retriever"] = HybridRetriever(alpha=_hybrid["alpha"], top_k=_hybrid["top_k"])

# ===== OpenAI 호출 =====
def _embedding(text: str):
    headers = {"Content-Type": "application/json", "Authorization": f"Bearer {OPENAI_API_KEY}"}
    payload = {"model": EMBEDDING_MODEL, "input": [text]}
    r = requests.post(EMBEDDING_URL, headers=headers, json=payload, timeout=30)
    r.raise_for_status()
    return r.json()["data"][0]["embedding"]

SYSTEM_PROMPT = """너의 이름은 에이몬이다. 너는 발로란트와 e스포츠에 특화된 전문 어시스턴트다.

[목표]
- 제공된 정보가 있으면 최우선, 간결·정확한 한국어.
- 모를 경우에는 확실히 아는 범위에서만 대답. 발로란트와 무관한 질문이면 “흠 잘 모르겠는데, 내 전문 분야인 발로란트를 빗나간 질문이라니, 섭섭한데?”라고 답변.
- 단, 일상적인 인사나 가벼운 대화(예: "안녕", "잘 지냈어?", "뭐해?")에는 섭섭함을 표현하지 않고 반갑게 맞이하며, 발로란트·e스포츠 관련 도움을 줄 수 있음을 안내.

[스타일]
- 한국어만.
- 금지: 출처/번호/대괄호/링크 나열.
- 톤: 전문가+위트, 딱딱하지 않고 친근하게.

[행동 규칙]
1) 아는 정보가 있으면 명확한 문장으로 답변.
2) 발로란트와 무관하거나 모르는 주제 → “흠 잘 모르겠는데, 내 전문 분야인 발로란트를 빗나간 질문이라니, 섭섭한데?”라고 답변. 추측 금지.
3) 인사·가벼운 대화 → 섭섭함 없이 반갑게 맞이 + “나는 발로란트와 e스포츠 전문이야, 필요한 게 있으면 말해!” 같은 안내 추가.
4) 형식: 한 문단 또는 짧은 목록. 금지 표기 준수.
"""

def _chat(context: str, question: str) -> str:
    headers = {"Content-Type": "application/json", "Authorization": f"Bearer {OPENAI_API_KEY}"}
    messages = [
        {
            "role": "system",
            "content": SYSTEM_PROMPT
        },
        {
            "role": "user",
            "content": f"근거:\n{context}\n\n질문: {question}\n"
                       f"규칙:\n- 근거에 없는 추측 금지\n- 출처/번호/대괄호 표기 금지"
        }
    ]
    payload = {"model": CHAT_MODEL, "messages": messages, "temperature": 0.2}
    r = requests.post(CHAT_URL, headers=headers, json=payload, timeout=60)
    r.raise_for_status()
    return r.json()["choices"][0]["message"]["content"]

# ===== (호환) 순수 RAG 경로 =====
def _fetch_rows_by_indices(vector_indices):
    if not vector_indices:
        return []
    placeholders = ",".join(["%s"] * len(vector_indices))
    sql = f"""
        SELECT vector_index, title, content
        FROM embeddings
        WHERE vector_index IN ({placeholders})
        ORDER BY FIELD(vector_index, {placeholders})
    """
    cur = _db.cursor()
    cur.execute(sql, vector_indices + vector_indices)
    rows = cur.fetchall()
    cur.close()
    return rows

def _search_similar_rag(query: str, top_k: int):
    vec = np.array(_embedding(query), dtype=np.float32).reshape(1, -1)
    faiss.normalize_L2(vec)
    D, I = _faiss.search(vec, top_k)
    indices = [int(x) for x in I[0] if x >= 0]
    rows = _fetch_rows_by_indices(indices)
    docs = [{"title": r[1], "content": r[2]} for r in rows]
    return docs

def answer_question(question: str, top_k: int | None = None):
    """(레거시) 순수 RAG만 수행 – 기존 호환용"""
    init_once()
    k = top_k or TOP_K_DEFAULT
    docs = _search_similar_rag(question, k)
    context = "\n\n".join([f"{d['title']}\n{d['content']}" for i, d in enumerate(docs)])
    answer = _chat(context, question)
    sources = [{"title": d["title"]} for d in docs[:5]]
    return {"answer": answer, "sources": sources, "mode": "rag", "routed": False}

# ===== (신규 기본) AUTO: SQL 라우팅 → 실패 시 하이브리드 =====
def answer_auto(question: str, top_k: int | None = None):
    """
    항상 AUTO 흐름:
      1) SQL 라우팅 시도 (성공 시 그 결과로 답변)
      2) 실패하면 하이브리드(BM25 + FAISS) 검색으로 폴백
    """
    init_once()
    retriever = _hybrid["retriever"]
    k = top_k or _hybrid["top_k"]

    # 1) DB 라우팅
    routed = retriever.sql_route(question)
    if routed:
        context = f"{routed['title']}\n{routed['content']}"
        answer = _chat(context, question)
        return {
            "mode": "auto",
            "routed": True,
            "route_type": "sql",
            "answer": answer,
            "context_preview": context[:1200],
            "sources": [{"title": routed["title"]}],
        }

    # 2) 하이브리드(BM25 + FAISS)
    docs = retriever.hybrid(question, top_k=k)
    context = "\n\n".join([f"{d['title']}\n{d['content']}" for i, d in enumerate(docs)])
    answer = _chat(context, question)
    sources = [{"title": d["title"], "score": round(d.get("score", 0.0), 4)} for d in docs[:5]]
    return {
        "mode": "auto",
        "routed": False,
        "answer": answer,
        "context_preview": context[:1200],
        "sources": sources,
    }

# ===== 보안 토큰 =====
def check_token(token: str | None) -> bool:
    if not INTERNAL_TOKEN:  # 토큰 사용 안 하면 항상 통과
        return True
    return token == INTERNAL_TOKEN
