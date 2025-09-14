from keybert import KeyBERT
from crawling import get_total_data, get_news_title_list
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity

url1 = "https://www.vlr.gg/team/news/2593/fnatic/"
url2 = "https://www.vlr.gg/team/news/8877/karmine-corp/"


doc1 = get_news_title_list(url1)
doc2 = get_news_title_list(url2)
word1 = 'strong'
word2 = 'weak'
model = SentenceTransformer("all-MiniLM-L6-v2")  # 한국어면 kobert 가능
inputs = [doc1, doc2, word1, word2]
embeddings = model.encode(inputs)
sim1 = cosine_similarity([embeddings[0]], [embeddings[2]])[0][0]
sim2 = cosine_similarity([embeddings[1]], [embeddings[2]])[0][0]
print(f"fnatic : {sim1}")
print(f"team-heretics : {sim2}")
