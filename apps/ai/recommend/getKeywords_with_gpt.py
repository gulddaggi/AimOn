import asyncio
from crawling import get_news_title_list, get_total_data
from openai import AsyncOpenAI
import os
import getpass

if not os.environ.get("OPENAI_API_KEY"):
    os.environ["OPENAI_API_KEY"] = getpass.getpass("GMS KEY를 입력하세요: ")

client = AsyncOpenAI(base_url="https://gms.ssafy.io/gmsapi/api.openai.com/v1")
url = "https://www.vlr.gg/team/news/2593/fnatic/"

async def main():
    input_data = get_total_data(url)
    response = await client.chat.completions.create(
        model='gpt-4.1-nano',
        messages=[
            {
                "role": "system",
                "content": "다음 뉴스 기사 제목을 바탕으로 핵심 키워드 5개를 쉼표로 구분하여 추출해주세요. 기사 제목은 '^'으로 구분되어 있습니다."
            },
            {"role": "user", "content": input_data}
        ],
        max_tokens=100
    )
    keywords = response.choices[0].message.content.strip()
    print(keywords)

if __name__ == "__main__":
    asyncio.run(main())
