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
CHAT_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"
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

    # DB에서 vector_index 기준으로 title, content 조회
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

# ✅ OpenAI에 프롬프트 요청
def ask_openai(context, question):
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {OPENAI_API_KEY}"
    }
    messages = [
        {"role": "system", "content": f"다음 정보를 참고해서 질문에 답변하세요:\n\n{context}"},
        {"role": "user", "content": question}
    ]
    data = {
        "model": "gpt-4.1-mini",
        "messages": messages,
        "temperature": 0.2
    }
    response = requests.post(CHAT_URL, headers=headers, json=data)
    response.raise_for_status()
    return response.json()["choices"][0]["message"]["content"]

# ✅ 메인 루프
def main():
    print("🎮 Valorant RAG 챗봇 시작 (종료하려면 'exit')")

    while True:
        question = input("\n질문을 입력하세요: ")
        if question.strip().lower() == "exit":
            break

        docs = search_similar(question, top_k=15)
        context = "\n\n".join([f"[{i+1}] {doc['title']}\n{doc['content']}" for i, doc in enumerate(docs)])
        answer = ask_openai(context, question)

        print("\n💬 챗봇 답변:\n", answer)

if __name__ == "__main__":
    main()
