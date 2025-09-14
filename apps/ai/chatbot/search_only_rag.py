
import os
import json
import numpy as np
import faiss
import pymysql
import requests
from dotenv import load_dotenv

# ✅ 환경 변수 로드
load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
EMBEDDING_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings"
MODEL_NAME = "text-embedding-3-large"
FAISS_PATH = "normalized_faiss_index/valorant.index"

# ✅ DB 연결
conn = pymysql.connect(
    host=os.getenv("MYSQL_HOST"),
    user=os.getenv("MYSQL_USER"),
    password=os.getenv("MYSQL_PASSWORD"),
    db=os.getenv("MYSQL_DB"),
    charset="utf8mb4"
)
cursor = conn.cursor()

# ✅ FAISS 인덱스 로드
faiss_index = faiss.read_index(FAISS_PATH)

# ✅ 쿼리 임베딩 함수
def get_embedding(text):
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {OPENAI_API_KEY}"
    }
    data = {
        "model": MODEL_NAME,
        "input": [text]
    }
    response = requests.post(EMBEDDING_URL, headers=headers, json=data)
    response.raise_for_status()
    return response.json()["data"][0]["embedding"]

# ✅ 유사 문서 검색 (DB 조회 기반)
def search_similar(query, top_k=5):
    query_emb = np.array(get_embedding(query), dtype=np.float32).reshape(1, -1)
    faiss.normalize_L2(query_emb)

    D, I = faiss_index.search(query_emb, top_k)
    vector_indices = I[0].tolist()

    placeholders = ','.join(['%s'] * len(vector_indices))
    sql = f"""
        SELECT vector_index, title, content
        FROM embeddings
        WHERE vector_index IN ({placeholders})
        ORDER BY FIELD(vector_index, {placeholders})
    """
    cursor.execute(sql, vector_indices * 2)
    rows = cursor.fetchall()

    return [{"title": row[1], "content": row[2]} for row in rows]

# ✅ 메인 루프
def main():
    print("🔍 질문에 대한 유사 문서 검색기 (GPT 응답 없음)")
    print("🚪 종료하려면 'exit'")

    while True:
        question = input("\n질문을 입력하세요: ")
        if question.strip().lower() == "exit":
            break

        docs = search_similar(question, top_k=20)

        print("\n📚 유사한 문서들:")
        for i, doc in enumerate(docs):
            print(f"\n[{i+1}] {doc['title']}\n{doc['content']}")

if __name__ == "__main__":
    main()
