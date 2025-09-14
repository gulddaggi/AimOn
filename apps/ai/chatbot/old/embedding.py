import json
import os
import requests
import numpy as np
import faiss
import pymysql
from dotenv import load_dotenv
from tqdm import tqdm
import tiktoken  # ✅ 정확한 토크나이저

# ✅ 환경 설정
load_dotenv()
API_KEY = os.getenv("OPENAI_API_KEY")
EMBEDDING_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings"
MODEL_NAME = "text-embedding-3-large"
DIMENSION = 8192
TOKEN_LIMIT = 8192

# ✅ 토큰 카운팅 함수
encoding = tiktoken.get_encoding("cl100k_base")
def count_tokens(text):
    return len(encoding.encode(text))

# ✅ DB 연결
conn = pymysql.connect(
    host=os.getenv("MYSQL_HOST"),
    user=os.getenv("MYSQL_USER"),
    password=os.getenv("MYSQL_PASSWORD"),
    db=os.getenv("MYSQL_DB"),
    charset="utf8mb4"
)
cursor = conn.cursor()

# ✅ FAISS index (실제 차원으로 초기화)
# 첫 번째 임베딩을 받은 후 올바른 차원으로 재초기화
faiss_index = None
meta_data = []

# ✅ 임베딩 API
def get_embeddings_batch(texts):
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {API_KEY}"
    }
    data = {
        "model": MODEL_NAME,
        "input": texts
    }
    
    print(f"🔍 API 호출: {len(texts)}개 텍스트, 총 토큰: {sum(count_tokens(t) for t in texts)}")
    
    response = requests.post(EMBEDDING_URL, headers=headers, json=data)
    
    if response.status_code != 200:
        print(f"❌ API 오류: {response.status_code}")
        print(f"   응답: {response.text}")
        response.raise_for_status()
    
    result = response.json()
    print(f"✅ API 성공: {len(result.get('data', []))}개 임베딩 반환")
    
    return [item["embedding"] for item in result["data"]]

# ✅ JSONL 처리 및 임베딩
batch = []
batch_token_sum = 0

with open("api_data.jsonl", "r", encoding="utf-8") as f:
    for line in tqdm(f, desc="Processing"):
        item = json.loads(line.strip())
        title, content = item["title"], item["content"]
        full_text = f"{title}\n{content}"
        token_len = count_tokens(full_text)

        if token_len > TOKEN_LIMIT:
            print(f"⚠️ {title} → {token_len} tokens (too long, skipped)")
            continue

        if batch_token_sum + token_len > TOKEN_LIMIT:
            try:
                texts = [x["text"] for x in batch]
                embeddings = get_embeddings_batch(texts)

                # 첫 번째 임베딩으로 FAISS 인덱스 초기화
                if faiss_index is None and embeddings:
                    first_emb = embeddings[0]
                    actual_dimension = len(first_emb)
                    print(f"🔧 FAISS 인덱스 초기화: {actual_dimension}차원")
                    faiss_index = faiss.IndexFlatL2(actual_dimension)

                for i, emb in enumerate(embeddings):
                    vec = np.array(emb, dtype=np.float32)
                    # print(f"   임베딩 {i+1}: {len(vec)}차원")
                    
                    # FAISS 인덱스에 추가
                    faiss_index.add(np.array([vec]))

                    cursor.execute(
                        "INSERT INTO embeddings (title, content, embedding) VALUES (%s, %s, %s)",
                        (batch[i]["title"], batch[i]["content"], json.dumps(emb))
                    )
                    meta_data.append({
                        "title": batch[i]["title"],
                        "content": batch[i]["content"],
                        "embedding": emb
                    })

                conn.commit()
            except Exception as e:
                print("❌ Batch 처리 실패:", str(e))
                print(f"   배치 크기: {len(batch)}")
                print(f"   배치 토큰 합계: {batch_token_sum}")
                import traceback
                traceback.print_exc()

            # 새 배치 초기화
            batch = []
            batch_token_sum = 0

        batch.append({
            "title": title,
            "content": content,
            "text": full_text
        })
        batch_token_sum += token_len

# ✅ 마지막 배치 처리
if batch:
    try:
        texts = [x["text"] for x in batch]
        embeddings = get_embeddings_batch(texts)

        # 첫 번째 임베딩으로 FAISS 인덱스 초기화 (아직 초기화되지 않은 경우)
        if faiss_index is None and embeddings:
            first_emb = embeddings[0]
            actual_dimension = len(first_emb)
            print(f"🔧 FAISS 인덱스 초기화: {actual_dimension}차원")
            faiss_index = faiss.IndexFlatL2(actual_dimension)

        for i, emb in enumerate(embeddings):
            vec = np.array(emb, dtype=np.float32)
            print(f"   임베딩 {i+1}: {len(vec)}차원")
            
            # FAISS 인덱스에 추가
            faiss_index.add(np.array([vec]))

            cursor.execute(
                "INSERT INTO embeddings (title, content, embedding) VALUES (%s, %s, %s)",
                (batch[i]["title"], batch[i]["content"], json.dumps(emb))
            )
            meta_data.append({
                "title": batch[i]["title"],
                "content": batch[i]["content"],
                "embedding": emb
            })

        conn.commit()
    except Exception as e:
        print("❌ 마지막 배치 처리 실패:", str(e))
        print(f"   배치 크기: {len(batch)}")
        print(f"   배치 토큰 합계: {batch_token_sum}")
        import traceback
        traceback.print_exc()

cursor.close()
conn.close()

# ✅ 결과 저장
os.makedirs("faiss_index", exist_ok=True)
faiss.write_index(faiss_index, "faiss_index/valorant.index")
with open("faiss_index/valorant_meta.json", "w", encoding="utf-8") as f:
    json.dump(meta_data, f, ensure_ascii=False, indent=2)
