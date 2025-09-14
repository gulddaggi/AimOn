# ✅ embedding_pipeline.py (메타데이터 없이 DB + FAISS만 사용하는 구조)

import os
import json
import numpy as np
import faiss
import pymysql
import requests
import tiktoken
from dotenv import load_dotenv
from tqdm import tqdm

# ✅ 환경 설정
load_dotenv()
API_KEY = os.getenv("OPENAI_API_KEY")
EMBEDDING_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings"
MODEL_NAME = "text-embedding-3-large"
TOKEN_LIMIT = 8192
FAISS_PATH = "normalized_faiss_index/valorant.index"
JSONL_PATH = "api_data_ko.jsonl"
# JSONL_PATH = "api_data_new.jsonl"
# JSONL_PATH = "integrated_data.jsonl"
# JSONL_PATH = "wiki_processed_data.jsonl"

# ✅ 토큰 카운터
encoding = tiktoken.get_encoding("cl100k_base")
def count_tokens(text):
    return len(encoding.encode(text))

# ✅ DB 연결
conn = pymysql.connect(
    host=os.getenv("MYSQL_HOST"),
    user=os.getenv("MYSQL_USER"),
    password=os.getenv("MYSQL_PASSWORD"),
    # db=os.getenv("MYSQL_DB"),
    db=os.getenv("NEW_DB"),
    charset="utf8mb4"
)
cursor = conn.cursor()

# ✅ 기존 인덱스 불러오기 또는 새로 만들기
if os.path.exists(FAISS_PATH):
    print("📥 기존 FAISS 인덱스 로드")
    faiss_index = faiss.read_index(FAISS_PATH)
else:
    print("🆕 새로운 FAISS 인덱스 생성 예정")
    faiss_index = None

# ✅ 임베딩 API

def add_embedding_to_faiss(faiss_index, emb):
    vec = np.array(emb, dtype=np.float32).reshape(1, -1)
    faiss.normalize_L2(vec)
    faiss_index.add(vec)
    return vec

def get_embeddings_batch(texts):
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {API_KEY}"
    }
    data = {
        "model": MODEL_NAME,
        "input": texts
    }
    response = requests.post(EMBEDDING_URL, headers=headers, json=data)
    response.raise_for_status()
    return [item["embedding"] for item in response.json()["data"]]

# ✅ 임베딩 및 저장
batch = []
batch_token_sum = 0
with open(JSONL_PATH, "r", encoding="utf-8") as f:
    for line in tqdm(f, desc="Processing"):
        item = json.loads(line.strip())
        title, content = item["title"], item["content"]
        full_text = f"{title}\n{content}"
        token_len = count_tokens(full_text)

        if token_len > TOKEN_LIMIT:
            print(f"⚠️ {title} → {token_len} tokens (skip)")
            continue

        batch.append({"title": title, "content": content, "text": full_text})
        batch_token_sum += token_len

        if batch_token_sum > TOKEN_LIMIT:
            texts = [x["text"] for x in batch]
            embeddings = get_embeddings_batch(texts)

            if faiss_index is None:
                dim = len(embeddings[0])
                faiss_index = faiss.IndexFlatL2(dim)

            current_idx = faiss_index.ntotal

            for i, emb in enumerate(embeddings):
                vec = np.array(emb, dtype=np.float32).reshape(1, -1)
                faiss.normalize_L2(vec)
                faiss_index.add(vec)

                cursor.execute(
                    "INSERT INTO embeddings (vector_index, title, content, embedding) VALUES (%s, %s, %s, %s)",
                    (current_idx + i, batch[i]["title"], batch[i]["content"], json.dumps(emb))
                )

            conn.commit()
            batch, batch_token_sum = [], 0

# ✅ 마지막 배치
if batch:
    texts = [x["text"] for x in batch]
    embeddings = get_embeddings_batch(texts)

    if faiss_index is None:
        dim = len(embeddings[0])
        faiss_index = faiss.IndexFlatL2(dim)

    current_idx = faiss_index.ntotal

    for i, emb in enumerate(embeddings):
        vec = np.array(emb, dtype=np.float32).reshape(1, -1)
        faiss.normalize_L2(vec)
        faiss_index.add(vec)

        cursor.execute(
            "INSERT INTO embeddings (vector_index, title, content, embedding) VALUES (%s, %s, %s, %s)",
            (current_idx + i, batch[i]["title"], batch[i]["content"], json.dumps(emb))
        )

    conn.commit()

cursor.close()
conn.close()

# ✅ 인덱스 저장
os.makedirs(os.path.dirname(FAISS_PATH), exist_ok=True)
faiss.write_index(faiss_index, FAISS_PATH)
print("✅ FAISS 저장 완료")
