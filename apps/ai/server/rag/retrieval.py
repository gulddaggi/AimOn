# retrieval.py
import os
import numpy as np
import faiss
import pymysql
import re
from dotenv import load_dotenv
import requests

load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
EMBEDDING_URL  = os.getenv("EMBEDDING_URL") 
MODEL_NAME     = os.getenv("EMBEDDING_MODEL", "text-embedding-3-large")

# FAISS 경로 해석: 환경변수 우선, 없거나 잘못되면 기본 경로로 폴백
def _resolve_faiss_path(env_path: str | None) -> str:
    base_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))  # server/
    candidates = []
    if env_path:
        candidates.append(env_path)
        if not os.path.isabs(env_path):
            candidates.append(os.path.join(base_dir, env_path))
    # 기본 경로 (server/normalized_faiss_index/valorant.index)
    candidates.append(os.path.join(base_dir, "normalized_faiss_index", "valorant.index"))
    for p in candidates:
        if os.path.exists(p):
            return p
    # 최종 실패 시 후보 경로들을 함께 알려줌
    raise FileNotFoundError(f"FAISS index not found. Tried: {candidates}")

FAISS_PATH = None
if os.getenv("SKIP_RAG_INIT") != "1":
    try:
        FAISS_PATH = _resolve_faiss_path(os.getenv("FAISS_PATH"))
    except FileNotFoundError as e:
        # 초기화 스크립트 실행 시 인덱스 없을 수 있음 → 경고만 출력
        print(f"[retrieval] Warning: {e}")
        FAISS_PATH = None

def get_db():
    return pymysql.connect(
        host=os.getenv("MYSQL_HOST"),
        user=os.getenv("MYSQL_USER"),
        password=os.getenv("MYSQL_PASSWORD"),
        db=os.getenv("MYSQL_DATABASE"),
        charset="utf8mb4"
    )

def get_embedding(text: str):
    headers = {"Content-Type": "application/json","Authorization": f"Bearer {OPENAI_API_KEY}"}
    data = {"model": MODEL_NAME, "input": [text]}
    r = requests.post(EMBEDDING_URL, headers=headers, json=data)
    r.raise_for_status()
    return r.json()["data"][0]["embedding"]

def cosine_from_l2sq(d):  # FAISS IndexFlatL2는 L2^2 반환(정규화벡터 가정)
    # cos = 1 - d/2  (범위 대략 [-1..1])
    return max(0.0, min(1.0, 1.0 - d/2.0))

def minmax_norm(vals):
    if not vals: return {}
    v = list(vals.values())
    lo, hi = min(v), max(v)
    if hi <= lo: return {k: 1.0 for k in vals}  # 모두 같은 점수면 1
    return {k: (x - lo) / (hi - lo) for k, x in vals.items()}

class HybridRetriever:
    def __init__(self, alpha=0.5, top_k=15):
        self.alpha = alpha
        self.top_k = top_k
        self.conn = get_db()
        self.cursor = self.conn.cursor()
        # FULLTEXT 인덱스 존재 보장(없으면 생성 시도)
        self._ensure_fulltext_index()
        if os.getenv("SKIP_RAG_INIT") != "1" and FAISS_PATH:
            self.faiss_index = faiss.read_index(FAISS_PATH)
        else:
            self.faiss_index = None
        self._load_entity_cache()

    def _ensure_fulltext_index(self):
        try:
            # 동일 이름 인덱스 존재 여부 확인
            self.cursor.execute(
                "SHOW INDEX FROM embeddings WHERE Key_name=%s",
                ("ft_title_content",),
            )
            exists = self.cursor.fetchone() is not None
            if not exists:
                self.cursor.execute(
                    "ALTER TABLE embeddings ADD FULLTEXT INDEX ft_title_content (title, content)"
                )
                self.conn.commit()
        except Exception:
            # 권한/버전 문제로 실패해도 검색 파이프라인은 계속 진행 (FAISS로 폴백)
            try:
                self.conn.rollback()
            except Exception:
                pass

    def _load_entity_cache(self):
        # Team/League/Player 명칭 캐시 → 엔티티 인식과 질의 정규화용
        self.teams = set()
        self.leagues = set()
        self.players = set()
        # 존재할 때만 로딩 (없어도 동작)
        try:
            # 실제 스키마: team(team_name)
            self.cursor.execute("SELECT team_name FROM team")
            self.teams = {r[0] for r in self.cursor.fetchall() if r and r[0]}
        except: pass
        try:
            # 실제 스키마: league(name)
            self.cursor.execute("SELECT name FROM league")
            self.leagues = {r[0] for r in self.cursor.fetchall() if r and r[0]}
        except: pass
        try:
            # 실제 스키마: player(handle)
            self.cursor.execute("SELECT handle FROM player")
            self.players = {r[0] for r in self.cursor.fetchall() if r and r[0]}
        except: pass

    def _detect_entities(self, q: str):
        # 간단 매칭(대소문자 무시)
        ql = q.lower()
        hit_team = next((t for t in self.teams if t.lower() in ql), None)
        hit_league = next((l for l in self.leagues if l.lower() in ql), None)
        hit_player = next((p for p in self.players if p and p.lower() in ql), None)
        return hit_team, hit_league, hit_player

    def sql_route(self, q: str):
        team, league, player = self._detect_entities(q)
        # 규칙 기반 라우팅(필요시 추가)
        if team and re.search(r"(선수|명단|로스터|멤버)", q):
            # 실제 스키마: player.team_id → team.id, team.team_name
            sql = """
              SELECT p.name, p.handle, p.country
              FROM player p
              JOIN team t ON p.team_id=t.id
              WHERE t.team_name=%s
              ORDER BY p.handle
              LIMIT 100
            """
            self.cursor.execute(sql, (team,))
            rows = self.cursor.fetchall()
            if rows:
                lines = [f"- {r[1]} ({r[0]}) / {r[2]}" for r in rows]
                return {
                    "type": "sql",
                    "title": f"{team} 선수 명단",
                    "content": "\n".join(lines)
                }
        if league and re.search(r"(팀|소속|참가|목록)", q):
            # 실제 스키마: team.team_name, team.country, team.league_id → league.id
            sql = """
              SELECT t.team_name, t.country
              FROM team t
              JOIN league l ON t.league_id=l.id
              WHERE l.name=%s
              ORDER BY t.team_name
              LIMIT 200
            """
            self.cursor.execute(sql, (league,))
            rows = self.cursor.fetchall()
            if rows:
                lines = [f"- {r[0]} / {r[1]}" for r in rows]
                return {
                    "type": "sql",
                    "title": f"{league} 참가 팀",
                    "content": "\n".join(lines)
                }
        if player and re.search(r"(어느 팀|소속|팀|어디)", q):
            # 실제 스키마 반영 + LEFT JOIN으로 누락 방지
            sql = """
              SELECT p.handle, t.team_name AS team, l.name AS league
              FROM player p
              LEFT JOIN team t ON p.team_id=t.id
              LEFT JOIN league l ON t.league_id=l.id
              WHERE p.handle=%s
              LIMIT 1
            """
            self.cursor.execute(sql, (player,))
            row = self.cursor.fetchone()
            if row:
                return {
                    "type": "sql",
                    "title": f"{row[0]} 소속",
                    "content": f"팀: {row[1]} / 리그: {row[2]}"
                }
        return None

    def bm25_search(self, q: str, k: int, team=None, league=None):
        # MySQL FULLTEXT(자연어 모드)
        where = "MATCH(title, content) AGAINST (%s IN NATURAL LANGUAGE MODE)"
        params = [q]
        if team:
            where += " AND title LIKE %s"
            params.append(f"%{team}%")
        if league:
            where += " AND title LIKE %s"
            params.append(f"%{league}%")
        sql = f"""
          SELECT vector_index, title, content,
                 MATCH(title, content) AGAINST (%s IN NATURAL LANGUAGE MODE) AS score
          FROM embeddings
          WHERE {where}
          ORDER BY score DESC
          LIMIT {k}
        """
        # params에 q를 한 번 더 넣어야 SELECT와 WHERE 모두 채움
        full_params = [q] + params
        try:
            self.cursor.execute(sql, full_params)
            return self.cursor.fetchall()  # (vi, title, content, score)
        except pymysql.err.OperationalError as e:
            # FULLTEXT 인덱스 미존재(1191) 등으로 실패 시 BM25는 비활성화하고 FAISS만 사용
            return []

    def faiss_search(self, q: str, k: int):
        if not self.faiss_index:
            return []
        qv = np.array(get_embedding(q), dtype=np.float32).reshape(1, -1)
        faiss.normalize_L2(qv)
        D, I = self.faiss_index.search(qv, k)
        vi = I[0].tolist()
        d  = D[0].tolist()
        if not vi: return []
        ph = ",".join(["%s"] * len(vi))
        sql = f"""
          SELECT vector_index, title, content
          FROM embeddings
          WHERE vector_index IN ({ph})
        """
        self.cursor.execute(sql, vi)
        rows = self.cursor.fetchall()
        meta = {row[0]: (row[1], row[2]) for row in rows}
        results = []
        for idx, dist in zip(vi, d):
            if idx in meta:
                title, content = meta[idx]
                results.append((idx, title, content, cosine_from_l2sq(dist)))
        return results  # (vi, title, content, sim)

    def hybrid(self, q: str, top_k: int = None):
        top_k = top_k or self.top_k
        team, league, _ = self._detect_entities(q)
        vres = self.faiss_search(q, max(top_k, 20))
        bres = self.bm25_search(q, max(top_k, 20), team=team, league=league)

        v_scores = {vi: s for (vi, _, _, s) in vres}
        b_scores = {vi: s for (vi, _, _, s) in bres}

        v_norm = minmax_norm(v_scores)
        b_norm = minmax_norm(b_scores)

        # 가중합
        combined = {}
        meta = {}
        for (vi, t, c, _) in vres:
            meta[vi] = (t, c)
        for (vi, t, c, _) in bres:
            meta[vi] = (t, c)
        for vi in meta:
            combined[vi] = self.alpha * v_norm.get(vi, 0.0) + (1 - self.alpha) * b_norm.get(vi, 0.0)

        ranked = sorted(combined.items(), key=lambda x: x[1], reverse=True)[:top_k]
        out = [{"title": meta[vi][0], "content": meta[vi][1], "score": score} for vi, score in ranked]
        return out