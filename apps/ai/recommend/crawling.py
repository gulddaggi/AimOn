import requests
from bs4 import BeautifulSoup
import json

def get_news_title_list(url):
    html = requests.get(url).text
    bs = BeautifulSoup(html, "html.parser")
    new_title_list = ""

    for parent in bs.find_all("a", class_="wf-module-item"):

        for child in parent.find_all("div", class_=None):
            new_title_list+= child.get_text(strip=True) + "\n"
    return new_title_list

def get_news_url_list(url):
    base_url = "https://www.vlr.gg"
    url_list = []
    html = requests.get(url).text
    bs = BeautifulSoup(html, "html.parser")
    links = bs.find_all("a", class_="wf-module-item")

    # 3. 각 a 태그의 href 속성 추출
    for link in links:
        href = link.get("href")
        url_list.append(base_url + href)
    return url_list
        
# def get_news_detail(url):
#     html = requests.get(url).text
#     bs = BeautifulSoup(html, "html.parser")
#     new_title_list = []
#     article = bs.find("div", class_="wf-card mod-article")
#     titles = bs.find_all("h1")
#     for title in titles:
#         new_title_list = new_title_list + title.get_text(strip=True) + ' '

#     content = article.find_all("p")
#     for p in content:
#         for span in p.find_all('span', class_="wf-hover-card mod-article article-ref-card"):
#             span.decompose()
#         new_title_list = new_title_list + p.get_text(strip=True) + ' '
#     return new_title_list

def get_news_detail(url, keywords):
    html = requests.get(url).text
    bs = BeautifulSoup(html, "html.parser")
    
    new_context = []
    article = bs.find("div", class_="wf-card mod-article")

    if article is None:
        return []

    collecting = False
    context = ''
    for tag in article.find_all(["h1", "p"], recursive=True):
        if tag.name == "h1":
            if context:
                new_context.append(context)
                context = ''
            h1_text = tag.get_text(strip=True)
            # 키워드가 h1 텍스트에 있으면 이후 <p>를 수집하겠다
            if any(keyword in h1_text for keyword in keywords):
                collecting = True
                context += h1_text + '. '
            else:
                collecting = False
        elif tag.name == "p" and collecting:
            # 불필요한 span 제거
            for span in tag.find_all('span', class_="wf-hover-card mod-article article-ref-card"):
                span.decompose()
            context += tag.get_text(strip=True) + ' '
    
    return new_context


# get_news_url_list("https://www.vlr.gg/team/news/2593/fnatic/")
# print(get_news_detail("https://www.vlr.gg/516549/team-heretics-ends-finals-curse-at-ewc-reverse-sweeps-fnatic",["FNATIC"]))
def get_total_data(url):
    url_list = get_news_url_list(url)
    # print(url_list)
    total_new_data = []
    for i in range(5):
        news_detail = get_news_detail(url_list[i], ["FNATIC"])
        if news_detail:
            total_new_data.append(news_detail)
    return total_new_data

def news_data_for_embedding(url):
    html = requests.get(url).text
    bs = BeautifulSoup(html, "html.parser")
    article = bs.find("div", class_="wf-card mod-article")

    if article is None:
        return []
    for tag in article.find_all(["h1", "p"], recursive=True):
        if tag.name == "h1":
            # if context:
            #     chunk.append(context)
            #     context = ''
            h1_text = tag.get_text(strip=True)
        elif tag.name == "p":
            # 불필요한 span 제거
            for span in tag.find_all('span', class_="wf-hover-card mod-article article-ref-card"):
                span.decompose()
            for em in tag.find_all('em'):
                em.decompose()
            p_text = tag.get_text(strip=True)
            if p_text:
                new_doc = {"title" : h1_text, "context" : p_text}
                with open("input_docs.jsonl", "a", encoding="utf-8") as f:
                    f.write(json.dumps(new_doc, ensure_ascii=False) + "\n")


# print(news_data_for_embedding("https://www.vlr.gg/516549/team-heretics-ends-finals-curse-at-ewc-reverse-sweeps-fnatic"))

fnatic_url = 'https://www.vlr.gg/team/news/2593/fnatic/'
# url_list = get_news_url_list(fnatic_url)

# for url in url_list:
#     news_data_for_embedding(url)
namu_url = "https://namu.wiki/w/%EB%B0%9C%EB%A1%9C%EB%9E%80%ED%8A%B8%20%EC%B1%94%ED%94%BC%EC%96%B8%EC%8A%A4%20%ED%88%AC%EC%96%B4"
