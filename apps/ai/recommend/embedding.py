import requests
import faiss
import numpy as np
import os
import json
from dotenv import load_dotenv
import tiktoken

load_dotenv()

GMS_KEY = os.getenv("OPENAI_API_KEY")
EMBEDDING_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings"
MODEL = "text-embedding-3-large"
MAX_TOTAL_TOKENS = 8191  # OpenAI 모델 제한

# 🔸 토크나이저 설정
encoding = tiktoken.encoding_for_model(MODEL)

# 🔽 문서 불러오기
docs = []
with open("input_docs.jsonl", "r", encoding="utf-8") as f:
    for line in f:
        docs.append(json.loads(line.strip()))

# 텍스트 추출 + 토큰 수 계산
texts = [f"{doc['title']} {doc['context']}" for doc in docs]
text_token_pairs = [(text, len(encoding.encode(text))) for text in texts]

# 🔁 배치 구성 함수 (총 토큰 수 기준으로 나누기)
def make_batches(pairs, max_tokens=MAX_TOTAL_TOKENS):
    batches = []
    batch = []
    token_sum = 0

    for text, tokens in pairs:
        if tokens > max_tokens:
            print(f"⚠️ 너무 긴 텍스트는 단독 처리 예정 (tokens={tokens})")
            if batch:
                batches.append(batch)
                batch = []
                token_sum = 0
            batches.append([(text, tokens)])  # 단독 처리
            continue

        if token_sum + tokens > max_tokens:
            batches.append(batch)
            batch = [(text, tokens)]
            token_sum = tokens
        else:
            batch.append((text, tokens))
            token_sum += tokens

    if batch:
        batches.append(batch)
    return batches

# 🔁 임베딩 함수
def get_embeddings_batch(text_batch):
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {GMS_KEY}"
    }
    data = {
        "model": MODEL,
        "input": [t for t, _ in text_batch]
    }
    res = requests.post(EMBEDDING_URL, headers=headers, json=data)
    res.raise_for_status()
    return [item["embedding"] for item in res.json()["data"]]

# 🧠 임베딩 실행
embeddings = []
batches = make_batches(text_token_pairs)

print(f"🔹 총 배치 수: {len(batches)}")

for i, batch in enumerate(batches):
    print(f"🔄 {i+1}/{len(batches)} 배치 임베딩 중... (총 {sum(t for _, t in batch)} tokens)")
    try:
        embs = get_embeddings_batch(batch)
        embeddings.extend(embs)
        print(f"✅ 완료: {len(embs)}개")
    except Exception as e:
        print(f"❌ 실패: {e}")

# 💾 FAISS 저장
embeddings = np.array(embeddings).astype("float32")
dimension = embeddings.shape[1]
index = faiss.IndexFlatL2(dimension)
index.add(embeddings)

os.makedirs("faiss_index", exist_ok=True)
faiss.write_index(index, "faiss_index/fnatic_large.index")

with open("faiss_index/rag.json", "w", encoding="utf-8") as f:
    json.dump(docs, f, ensure_ascii=False, indent=2)

print("✅ 임베딩 및 인덱스 저장 완료 (text-embedding-3-large)")
