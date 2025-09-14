import os
import json
import numpy as np
import faiss
import pymysql
from dotenv import load_dotenv

# ✅ 환경 변수 로드
load_dotenv()
FAISS_PATH = "faiss_index/valorant.index"

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

# ✅ 쿼리 임베딩 함수 (API 호출 대신 수동 입력 받기)
def mock_embedding_vector():
    print("\n❗ 현재 API는 호출되지 않으며, 임베딩 벡터를 수동 입력받습니다.")
    vec = input("🔢 쉼표로 구분된 float 리스트를 입력하세요 (예: 0.01,0.02,...):\n")
    numbers = np.array([float(x) for x in vec.strip().split(",")], dtype=np.float32).reshape(1, -1)
    faiss.normalize_L2(numbers)
    return numbers

# ✅ 유사 문서 검색
def search_similar_manual(query_vector, top_k=5):
    D, I = faiss_index.search(query_vector, top_k)
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
    print("🔍 유사 문서 검색 전용 (API 호출 없음)")
    print("🚪 종료하려면 'exit'")

    while True:
        question = input("\n질문을 입력하세요: ")
        if question.strip().lower() == "exit":
            break

        # 실제 API 호출 없이 벡터 수동 입력받음
        query_vector = mock_embedding_vector()
        docs = search_similar_manual(query_vector, top_k=15)

        print("\n📚 유사한 문서들:")
        for i, doc in enumerate(docs):
            print(f"\n[{i+1}] {doc['title']}\n{doc['content']}")

if __name__ == "__main__":
    main()
