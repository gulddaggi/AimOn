import requests
import faiss
import numpy as np
import os
import json
import pymysql
import tiktoken
from dotenv import load_dotenv

# 🔐 Load environment variables
load_dotenv()
GMS_KEY = os.getenv("OPENAI_API_KEY")
EMBEDDING_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings"
MODEL = "text-embedding-3-large"
MAX_TOTAL_TOKENS = 8191

# 🔌 MySQL
MYSQL_HOST = os.getenv("MYSQL_HOST", "localhost")
MYSQL_USER = os.getenv("MYSQL_USER", "root")
MYSQL_PASSWORD = os.getenv("MYSQL_PASSWORD", "")
MYSQL_DB = os.getenv("MYSQL_DB", "your_db_name")

conn = pymysql.connect(
    host=MYSQL_HOST,
    user=MYSQL_USER,
    password=MYSQL_PASSWORD,
    db=MYSQL_DB,
    charset="utf8mb4"
)
cursor = conn.cursor()

# 🔽 Load JSONL
docs = []
with open("wiki_processed_data.jsonl", "r", encoding="utf-8") as f:
    for line in f:
        docs.append(json.loads(line.strip()))

# 🔢 Insert into DB
for i, doc in enumerate(docs):
    try:
        cursor.execute(
            "INSERT INTO documents (id, title, context) VALUES (%s, %s, %s)",
            (i, doc["title"], doc["context"])
        )
    except Exception as e:
        print(f"❌ 문서 {i} 저장 실패: {e}")
conn.commit()
print(f"✅ MySQL에 {len(docs)}개 문서 저장 완료")

# 🔡 Prepare text + tokens
encoding = tiktoken.encoding_for_model(MODEL)
texts = [f"{doc['title']} {doc['context']}" for doc in docs]
text_token_pairs = [(i, text, len(encoding.encode(text))) for i, text in enumerate(texts)]

# 🔁 Token-aware batching
def make_batches(pairs, max_tokens=MAX_TOTAL_TOKENS):
    batches = []
    batch = []
    token_sum = 0
    for idx, text, tokens in pairs:
        if tokens > max_tokens:
            print(f"⚠️ 단독 처리 필요 (tokens={tokens})")
            if batch:
                batches.append(batch)
                batch = []
                token_sum = 0
            batches.append([(idx, text, tokens)])
            continue
        if token_sum + tokens > max_tokens:
            batches.append(batch)
            batch = [(idx, text, tokens)]
            token_sum = tokens
        else:
            batch.append((idx, text, tokens))
            token_sum += tokens
    if batch:
        batches.append(batch)
    return batches

# 🧠 임베딩 호출
def get_embeddings(text_batch):
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {GMS_KEY}"
    }
    data = {
        "model": MODEL,
        "input": [t for _, t, _ in text_batch]
    }
    res = requests.post(EMBEDDING_URL, headers=headers, json=data)
    res.raise_for_status()
    return [item["embedding"] for item in res.json()["data"]]

# 🔄 임베딩 수행
embeddings = []
batches = make_batches(text_token_pairs)
print(f"🔹 총 배치 수: {len(batches)}")

for i, batch in enumerate(batches):
    print(f"🔄 {i+1}/{len(batches)} 배치 임베딩 중... (tokens: {sum(t for _, _, t in batch)})")
    try:
        batch_embeddings = get_embeddings(batch)
        embeddings.extend(batch_embeddings)
        print(f"✅ 완료: {len(batch_embeddings)}개")
    except Exception as e:
        print(f"❌ 실패: {e}")

# 💾 FAISS 저장
embeddings_np = np.array(embeddings).astype("float32")
dimension = embeddings_np.shape[1]
index = faiss.IndexFlatL2(dimension)
index.add(embeddings_np)

os.makedirs("faiss_index", exist_ok=True)
faiss.write_index(index, "faiss_index/rag.index")
print("✅ FAISS 인덱스 저장 완료")

cursor.close()
conn.close()
