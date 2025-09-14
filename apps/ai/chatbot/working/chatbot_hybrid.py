import os
import requests
from dotenv import load_dotenv

from .retrieval import HybridRetriever
from .router import route as route_query

load_dotenv()

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
CHAT_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"

retriever = HybridRetriever(alpha=0.5, top_k=15)


def ask_openai(context: str, question: str) -> str:
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {OPENAI_API_KEY}",
    }
    messages = [
        {
            "role": "system",
            "content": "다음 근거를 우선 사용해 간결하고 정확하게 한국어로 답하세요. 모르면 모른다고 답하세요.",
        },
        {
            "role": "user",
            "content": f"근거:\n{context}\n\n질문: {question}\n규칙: - 근거에 없는 추측 금지 - 필요 시 근거 출처 제목 표기",
        },
    ]
    data = {"model": "gpt-4.1-mini", "messages": messages, "temperature": 0.2}
    r = requests.post(CHAT_URL, headers=headers, json=data)
    r.raise_for_status()
    return r.json()["choices"][0]["message"]["content"]


def main():
    print("🎮 Valorant Hybrid RAG 챗봇 시작 (종료하려면 'exit')")
    while True:
        q = input("\n질문을 입력하세요: ")
        if q.strip().lower() == "exit":
            break

        # 0) LLM 라우터로 경로 결정
        decision = route_query(q)
        route_type = decision.get("route", "hybrid")
        task = decision.get("task", "UNKNOWN")
        entities = decision.get("entities", {})
        conf = float(decision.get("confidence", 0.0) or 0.0)

        # 1) SQL 경로(임계치 적용), 실패 시 폴백
        if route_type == "sql" and conf >= 0.55:
            res = retriever.run_sql_task(task, entities)
            if res:
                print("\n[경로] LLM Router → SQL")
                print(f"\n📚 {res['title']}\n{res['content']}")
                continue

        # 2) 하이브리드 검색
        docs = retriever.hybrid(q, top_k=15)
        context = "\n\n".join([f"[{i+1}] {d['title']}\n{d['content']}" for i, d in enumerate(docs)])
        print("\n[경로] LLM Router → Hybrid")
        print("\n💬 챗봇 답변:\n", ask_openai(context, q))


if __name__ == "__main__":
    main()


