# query_rag.py
import requests
import faiss
import numpy as np
import os
import json
from dotenv import load_dotenv

load_dotenv()
GMS_KEY = os.getenv("OPENAI_API_KEY")
EMBEDDING_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings"
CHAT_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"

# 로드
index = faiss.read_index("faiss_index/fnatic.index")
with open("faiss_index/fnatic.json", encoding="utf-8") as f:
    docs = json.load(f)

def get_embedding(text):
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {GMS_KEY}"
    }
    data = {
        "model": "text-embedding-3-small",
        "input": text
    }
    res = requests.post(EMBEDDING_URL, headers=headers, json=data)
    return res.json()["data"][0]["embedding"]

def ask_gpt(context, question):
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {GMS_KEY}"
    }
    messages = [
        {"role": "system", "content": "Answer in Korean using only the provided context."},
        {"role": "user", "content": f"Context:\n{context}\n\nQuestion:\n{question}"}
    ]
    data = {
        "model": "gpt-4.1-mini",
        "messages": messages,
        "max_tokens": 1000,
        "temperature": 0.3
    }
    res = requests.post(CHAT_URL, headers=headers, json=data)
    return res.json()["choices"][0]["message"]["content"]

# 🔎 검색 → GPT 질의
def query(question, k=5):
    q_embed = np.array([get_embedding(question)]).astype("float32")
    D, I = index.search(q_embed, k)

    # 상위 문서 연결
    retrieved = "\n---\n".join([f"{docs[i]['title']}: {docs[i]['context']}" for i in I[0]])
    answer = ask_gpt(retrieved, question)
    return answer

# ✨ 실행 예시
if __name__ == "__main__":
    query_text = "Fnatic은 어떤 팀이야?"
    result = query(query_text)
    print("질문 :", query_text)
    print("\n🧠 GPT 응답:\n", result)
