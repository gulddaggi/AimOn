import os
import json
import numpy as np
import faiss
import requests
from dotenv import load_dotenv

# 환경 변수 로드
load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
EMBEDDING_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings"
CHAT_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"
MODEL_NAME = "text-embedding-3-large"

# 임베딩 생성 함수 (embedding.py와 동일)
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

# FAISS 인덱스 및 메타데이터 로드
faiss_index = faiss.read_index("faiss_index/valorant.index")
with open("faiss_index/valorant_meta.json", "r", encoding="utf-8") as f:
    meta_data = json.load(f)

def search_similar(query, top_k=5):
    # 쿼리 임베딩
    query_emb = np.array(get_embedding(query), dtype=np.float32).reshape(1, -1)
    # FAISS로 유사한 문서 top_k개 검색
    faiss.normalize_L2(query_emb) 
    D, I = faiss_index.search(query_emb, top_k)
    return [meta_data[i] for i in I[0]]

def ask_openai(context, question):
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {OPENAI_API_KEY}"
    }
    # context를 system prompt로 활용
    messages = [
        {"role": "system", "content": f"다음 정보를 참고해서 질문에 답변하세요.\n\n{context}"},
        {"role": "user", "content": question}
    ]
    data = {
        "model": "gpt-4.1-mini",  # 또는 gpt-4 등
        "messages": messages,
        "temperature": 0.2
    }
    response = requests.post(CHAT_URL, headers=headers, json=data)
    response.raise_for_status()
    return response.json()["choices"][0]["message"]["content"]

def main():
    print("Valorant RAG 챗봇에 오신 것을 환영합니다! (종료: exit)")
    while True:
        question = input("\n질문을 입력하세요: ")
        if question.strip().lower() == "exit":
            break
        # 1. 유사 문서 검색
        docs = search_similar(question, top_k=10)
        context = "\n\n".join([f"[{i+1}] {doc['title']}\n{doc['content']}" for i, doc in enumerate(docs)])
        # 2. OpenAI 챗봇 호출
        answer = ask_openai(context, question)
        print("\n[챗봇 답변]\n", answer)

if __name__ == "__main__":
    main()