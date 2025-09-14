from keybert import KeyBERT
from crawling import get_news_title_list, get_total_data

url = "https://www.vlr.gg/team/news/2593/fnatic/"

def keyword_by_title(url):
    doc = get_news_title_list(url)

    kw_model = KeyBERT()
    keywords = kw_model.extract_keywords(doc, keyphrase_ngram_range=(1, 3))
    print(keywords)

def keyword_by_context(url):
    doc = get_total_data(url)
    kw_model = KeyBERT()
    keywords = kw_model.extract_keywords(doc, keyphrase_ngram_range=(1, 3))
    print(keywords)

# keyword_by_context(url)