from openai import AsyncOpenAI
import os
import getpass
import asyncio
from dotenv import load_dotenv

load_dotenv()
if not os.environ.get("OPENAI_API_KEY"):
    os.environ["OPENAI_API_KEY"] = getpass.getpass("GMS KEY를 입력하세요: ")


client = AsyncOpenAI(base_url="https://gms.ssafy.io/gmsapi/api.openai.com/v1")

async def main():
    while True:
        
        user_input = await asyncio.to_thread(input, "\n질문을 입력하세요(q 입력 시 종료): ")
        if user_input.strip().lower() == 'q':
            print("종료합니다.")
            break
        res_text = ""
        stream = await client.chat.completions.create(
            model='gpt-4.1-mini',
            messages=[
                {"role": "user", "content": user_input}
            ],
            max_tokens=1024,
            stream=True,
        )
        async for chunk in stream:
            if chunk.choices[0].delta.content is not None:
                res_text += chunk.choices[0].delta.content
                print(chunk.choices[0].delta.content, end="", flush=True)
        print("\n")

if __name__ == "__main__":
    asyncio.run(main())
