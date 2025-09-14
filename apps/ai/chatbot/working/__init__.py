# Package marker for working
# working/router.py
import os, json, requests
from dotenv import load_dotenv
load_dotenv()

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
CHAT_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"
MODEL = "gpt-4.1-mini"

SYSTEM = (
    "너는 라우터다. 입력 질문을 분석해 아래 JSON 형식만 반환해.\n"
    '형식: {"route":"sql|hybrid","task":"TEAM_ROSTER|LEAGUE_TEAMS|PLAYER_TEAM|UNKNOWN",'
    '"entities":{"team":str|null,"league":str|null,"player":str|null},"confidence":0~1,"reason":str}\n'
    "- SQL이 정확한 경우: TEAM_ROSTER(팀 선수 명단), LEAGUE_TEAMS(리그 참가팀), PLAYER_TEAM(선수 소속)\n"
    "- 그 외는 hybrid\n"
    "- 반드시 올바른 JSON만, 다른 텍스트 금지"
)

def route(question: str) -> dict:
    headers = {"Content-Type":"application/json","Authorization":f"Bearer {OPENAI_API_KEY}"}
    data = {
        "model": MODEL,
        "messages": [
            {"role":"system","content": SYSTEM},
            {"role":"user","content": question}
        ],
        "temperature": 0.0
    }
    r = requests.post(CHAT_URL, headers=headers, json=data, timeout=30)
    r.raise_for_status()
    txt = r.json()["choices"][0]["message"]["content"]
    try:
        obj = json.loads(txt)
        # 필수 필드 검증
        if obj.get("route") in ["sql","hybrid"] and "task" in obj and "entities" in obj:
            return obj
    except Exception:
        pass
    # 파싱 실패 시 hybrid로 폴백
    return {"route":"hybrid","task":"UNKNOWN","entities":{"team":None,"league":None,"player":None},"confidence":0.0,"reason":"parse-fail"}