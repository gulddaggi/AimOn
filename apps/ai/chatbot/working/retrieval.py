import os
import re
import json
import numpy as np
import faiss
import pymysql
import requests
from dotenv import load_dotenv

load_dotenv()

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
EMBEDDING_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings"
MODEL_NAME = "text-embedding-3-large"
FAISS_PATH = "normalized_faiss_index/valorant.index"


def get_db_connection():
    return pymysql.connect(
        host=os.getenv("MYSQL_HOST"),
        user=os.getenv("MYSQL_USER"),
        password=os.getenv("MYSQL_PASSWORD"),
        db=os.getenv("NEW_DB"),  # 사용자 지정: NEW_DB 고정 사용
        charset="utf8mb4",
    )


def get_embedding(text: str):
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {OPENAI_API_KEY}",
    }
    data = {"model": MODEL_NAME, "input": [text]}
    response = requests.post(EMBEDDING_URL, headers=headers, json=data)
    response.raise_for_status()
    return response.json()["data"][0]["embedding"]


def cosine_from_l2_squared(l2_sq: float) -> float:
    # IndexFlatL2 + L2 정규화된 벡터 가정 시: cos = 1 - d/2
    cosine = 1.0 - (l2_sq / 2.0)
    if cosine < 0.0:
        return 0.0
    if cosine > 1.0:
        return 1.0
    return cosine


def minmax_normalize(score_map: dict[int, float]) -> dict[int, float]:
    if not score_map:
        return {}
    values = list(score_map.values())
    lo, hi = min(values), max(values)
    if hi <= lo:
        return {k: 1.0 for k in score_map.keys()}
    return {k: (v - lo) / (hi - lo) for k, v in score_map.items()}


class HybridRetriever:
    def __init__(self, alpha: float = 0.5, top_k: int = 15):
        self.alpha = alpha
        self.top_k_default = top_k
        self.conn = get_db_connection()
        self.cursor = self.conn.cursor()
        self.faiss_index = faiss.read_index(FAISS_PATH)
        self._load_entity_cache()
        self._build_normalized_maps_and_aliases()

    def __del__(self):
        try:
            if getattr(self, "cursor", None):
                self.cursor.close()
            if getattr(self, "conn", None):
                self.conn.close()
        except Exception:
            pass

    def _safe_fetch_all_to_set(self, sql: str) -> set[str]:
        try:
            self.cursor.execute(sql)
            return {row[0] for row in self.cursor.fetchall() if row and row[0]}
        except Exception:
            return set()

    def _load_entity_cache(self):
        # 엔티티 캐시(없어도 동작)
        self.games: set[str] = self._safe_fetch_all_to_set("SELECT name FROM Game")
        self.leagues: set[str] = self._safe_fetch_all_to_set("SELECT name FROM League")
        self.teams: set[str] = self._safe_fetch_all_to_set("SELECT name FROM Team")
        self.players: set[str] = self._safe_fetch_all_to_set("SELECT handle FROM Player")

    def _normalize_text(self, s: str) -> str:
        if not s:
            return ""
        s = s.lower().strip()
        # 영문/숫자/밑줄/한글만 남기고 제거
        s = re.sub(r"[^\w\u3131-\u3163\uac00-\ud7a3]", "", s)
        # 단수화 간단 처리: 끝이 s로 끝나면 제거(Esports -> esport)
        if s.endswith("s"):
            s = s[:-1]
        return s

    def _build_normalized_maps_and_aliases(self):
        # 정규화 맵: normalized -> original
        self.team_norm_map: dict[str, str] = {self._normalize_text(t): t for t in self.teams}
        self.league_norm_map: dict[str, str] = {self._normalize_text(l): l for l in self.leagues}
        self.player_norm_map: dict[str, str] = {self._normalize_text(p): p for p in self.players}

        # 한글/축약 별칭
        self.team_alias_to_canonical: dict[str, str] = {
            # 영어 축약/오타
            "g2": "G2 Esports",
            "g2esport": "G2 Esports",
            "g2esports": "G2 Esports",
            "paperrex": "Paper Rex",
            "drx": "DRX",
            "t1": "T1",
            "c9": "Cloud9",
            # 한글 표기 흔한 별칭
            "젠지": "Gen.G",
            "페이퍼렉스": "Paper Rex",
            "페이퍼렉": "Paper Rex",
            "디알엑스": "DRX",
            "클라우드9": "Cloud9",
            "지투": "G2 Esports",
        }

    def _resolve_team(self, query: str) -> str | None:
        qn = self._normalize_text(query)
        # 1) 별칭 우선
        for alias, canon in self.team_alias_to_canonical.items():
            if alias in qn:
                return canon if canon in self.teams else None
        # 2) 정규화 포함 매칭(팀명이 질의에 포함)
        for norm_name, original in self.team_norm_map.items():
            if norm_name and norm_name in qn:
                return original
        # 3) DB LIKE 간단 탐색(짧은 키워드만 추출)
        # 예: g2 -> %g2%
        try:
            if len(qn) >= 2:
                self.cursor.execute("SELECT name FROM Team WHERE LOWER(name) LIKE %s LIMIT 1", (f"%{qn}%",))
                row = self.cursor.fetchone()
                if row:
                    return row[0]
        except Exception:
            pass
        return None

    def _resolve_league(self, query: str) -> str | None:
        qn = self._normalize_text(query)
        for norm_name, original in self.league_norm_map.items():
            if norm_name and norm_name in qn:
                return original
        try:
            if len(qn) >= 3:
                self.cursor.execute("SELECT name FROM League WHERE LOWER(name) LIKE %s LIMIT 1", (f"%{qn}%",))
                row = self.cursor.fetchone()
                if row:
                    return row[0]
        except Exception:
            pass
        return None

    def _resolve_player(self, query: str) -> str | None:
        qn = self._normalize_text(query)
        for norm_name, original in self.player_norm_map.items():
            if norm_name and norm_name in qn:
                return original
        try:
            if len(qn) >= 2:
                self.cursor.execute("SELECT handle FROM Player WHERE LOWER(handle) LIKE %s LIMIT 1", (f"%{qn}%",))
                row = self.cursor.fetchone()
                if row:
                    return row[0]
        except Exception:
            pass
        return None

    def _detect_entities(self, query: str):
        # 우선 간단 해석 후, 해상 실패 시 정교한 resolve로 재시도
        ql = query.lower()
        simple_team = next((t for t in self.teams if t and t.lower() in ql), None)
        simple_league = next((l for l in self.leagues if l and l.lower() in ql), None)
        simple_player = next((p for p in self.players if p and p.lower() in ql), None)
        team = simple_team or self._resolve_team(query)
        league = simple_league or self._resolve_league(query)
        player = simple_player or self._resolve_player(query)
        return team, league, player

    def sql_route(self, query: str):
        team, league, player = self._detect_entities(query)

        # 팀 선수 명단
        if team and re.search(r"(선수|명단|로스터|멤버)", query):
            sql = (
                """
                SELECT p.name, p.handle, p.country
                FROM Player p
                JOIN Team t ON p.teamId = t.id
                WHERE t.name = %s
                ORDER BY p.handle
                LIMIT 100
                """
            )
            self.cursor.execute(sql, (team,))
            rows = self.cursor.fetchall()
            if rows:
                lines = [f"- {handle} ({name}) / {country}" for (name, handle, country) in rows]
                return {
                    "type": "sql",
                    "title": f"{team} 선수 명단",
                    "content": "\n".join(lines),
                }

        # 리그 참가 팀
        if league and re.search(r"(팀|소속|참가|목록)", query):
            sql = (
                """
                SELECT t.name, t.country
                FROM Team t
                JOIN League l ON t.leagueId = l.id
                WHERE l.name = %s
                ORDER BY t.name
                LIMIT 200
                """
            )
            self.cursor.execute(sql, (league,))
            rows = self.cursor.fetchall()
            if rows:
                lines = [f"- {name} / {country}" for (name, country) in rows]
                return {
                    "type": "sql",
                    "title": f"{league} 참가 팀",
                    "content": "\n".join(lines),
                }

        # 선수 소속
        if player and re.search(r"(어느 팀|소속|팀|어디)", query):
            sql = (
                """
                SELECT p.handle, t.name AS team, l.name AS league
                FROM Player p
                JOIN Team t ON p.teamId = t.id
                JOIN League l ON t.leagueId = l.id
                WHERE p.handle = %s
                LIMIT 1
                """
            )
            self.cursor.execute(sql, (player,))
            row = self.cursor.fetchone()
            if row:
                handle, team_name, league_name = row
                return {
                    "type": "sql",
                    "title": f"{handle} 소속",
                    "content": f"팀: {team_name} / 리그: {league_name}",
                }

        return None

    def run_sql_task(self, task: str, entities: dict):
        team = (entities or {}).get("team")
        league = (entities or {}).get("league")
        player = (entities or {}).get("player")

        if task == "TEAM_ROSTER" and team:
            sql = (
                """
                SELECT p.name, p.handle, p.country
                FROM Player p
                JOIN Team t ON p.teamId = t.id
                WHERE t.name = %s
                ORDER BY p.handle
                LIMIT 100
                """
            )
            self.cursor.execute(sql, (team,))
            rows = self.cursor.fetchall()
            if rows:
                lines = [f"- {h} ({n}) / {c}" for (n, h, c) in rows]
                return {"type": "sql", "title": f"{team} 선수 명단", "content": "\n".join(lines)}

        if task == "LEAGUE_TEAMS" and league:
            sql = (
                """
                SELECT t.name, t.country
                FROM Team t
                JOIN League l ON t.leagueId = l.id
                WHERE l.name = %s
                ORDER BY t.name
                LIMIT 200
                """
            )
            self.cursor.execute(sql, (league,))
            rows = self.cursor.fetchall()
            if rows:
                lines = [f"- {n} / {c}" for (n, c) in rows]
                return {"type": "sql", "title": f"{league} 참가 팀", "content": "\n".join(lines)}

        if task == "PLAYER_TEAM" and player:
            sql = (
                """
                SELECT p.handle, t.name AS team, l.name AS league
                FROM Player p
                JOIN Team t ON p.teamId = t.id
                JOIN League l ON t.leagueId = l.id
                WHERE p.handle = %s
                LIMIT 1
                """
            )
            self.cursor.execute(sql, (player,))
            row = self.cursor.fetchone()
            if row:
                handle, team_name, league_name = row
                return {"type": "sql", "title": f"{handle} 소속", "content": f"팀: {team_name} / 리그: {league_name}"}

        return None

    def bm25_search(self, query: str, k: int, team: str | None = None, league: str | None = None):
        # MySQL FULLTEXT: SELECT와 WHERE 모두 AGAINST 사용
        base_select = (
            "SELECT vector_index, title, content, "
            "MATCH(title, content) AGAINST (%s IN NATURAL LANGUAGE MODE) AS score "
            "FROM embeddings "
        )
        where_clauses = [
            "MATCH(title, content) AGAINST (%s IN NATURAL LANGUAGE MODE)",
        ]
        params: list[str] = [query, query]
        if team:
            where_clauses.append("title LIKE %s")
            params.append(f"%{team}%")
        if league:
            where_clauses.append("title LIKE %s")
            params.append(f"%{league}%")
        where_sql = " WHERE " + " AND ".join(where_clauses)
        order_limit = f" ORDER BY score DESC LIMIT {int(k)}"
        sql = base_select + where_sql + order_limit
        self.cursor.execute(sql, params)
        return self.cursor.fetchall()  # (vi, title, content, score)

    def faiss_search(self, query: str, k: int):
        qv = np.array(get_embedding(query), dtype=np.float32).reshape(1, -1)
        faiss.normalize_L2(qv)
        D, I = self.faiss_index.search(qv, k)
        ids = I[0].tolist()
        dists = D[0].tolist()
        if not ids:
            return []
        placeholders = ",".join(["%s"] * len(ids))
        sql = (
            f"SELECT vector_index, title, content FROM embeddings "
            f"WHERE vector_index IN ({placeholders})"
        )
        self.cursor.execute(sql, ids)
        rows = self.cursor.fetchall()
        meta = {vi: (title, content) for (vi, title, content) in rows}
        results = []
        for vi, d in zip(ids, dists):
            if vi in meta:
                title, content = meta[vi]
                results.append((vi, title, content, cosine_from_l2_squared(d)))
        return results  # (vi, title, content, sim)

    def hybrid(self, query: str, top_k: int | None = None):
        k = top_k or self.top_k_default
        team, league, _ = self._detect_entities(query)

        v_results = self.faiss_search(query, max(k, 20))
        b_results = self.bm25_search(query, max(k, 20), team=team, league=league)

        v_scores = {vi: sim for (vi, _, _, sim) in v_results}
        b_scores = {vi: score for (vi, _, _, score) in b_results}

        v_norm = minmax_normalize(v_scores)
        b_norm = minmax_normalize(b_scores)

        combined: dict[int, float] = {}
        meta: dict[int, tuple[str, str]] = {}
        for (vi, title, content, _) in v_results:
            meta[vi] = (title, content)
        for (vi, title, content, _) in b_results:
            meta[vi] = (title, content)

        for vi in meta.keys():
            combined[vi] = self.alpha * v_norm.get(vi, 0.0) + (1.0 - self.alpha) * b_norm.get(vi, 0.0)

        ranked = sorted(combined.items(), key=lambda x: x[1], reverse=True)[:k]
        return [
            {"title": meta[vi][0], "content": meta[vi][1], "score": score}
            for vi, score in ranked
        ]


