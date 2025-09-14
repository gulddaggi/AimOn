# working/router.py
import os, json, requests
from dotenv import load_dotenv
load_dotenv()

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
CHAT_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"
MODEL = "gpt-4.1-mini"

SYSTEM = (
    "너는 라우터다. 입력 질문을 분석해 아래 JSON만 반환한다. 다른 텍스트는 절대 출력하지 말 것.\n"
    '형식: {"route":"sql|hybrid","task":"TEAM_ROSTER|LEAGUE_TEAMS|PLAYER_TEAM|UNKNOWN",'
    '"entities":{"team":str|null,"league":str|null,"player":str|null},"confidence":0~1,"reason":str}\n'
    "가용 DB 스키마(요약): Game(name), League(name), Team(name,country,leagueId), Player(name,handle,country,position).\n"
    "지원 SQL 태스크: TEAM_ROSTER(팀 선수 명단), LEAGUE_TEAMS(리그 참가팀), PLAYER_TEAM(선수 소속).\n"
    "규칙:\n"
    "- 질문이 위 3가지 태스크와 정확히 일치할 때만 route=sql 로 선택한다.\n"
    "- 비교/최상급/정렬/수치/날짜/최고/최저/가장/첫번째/오래된 등 추가 연산이 필요한 경우에는 DB 컬럼이 부족하므로 route=hybrid 로 설정한다.\n"
    "- 엔티티(team/league/player)가 보이면 entities에 채워라. 확실치 않으면 null.\n"
    "예시:\n"
    "- 'G2 선수 알려줘' → {route:'sql', task:'TEAM_ROSTER', entities:{team:'G2 Esports',league:null,player:null}}\n"
    "- 'VCT EMEA 팀 목록' → {route:'sql', task:'LEAGUE_TEAMS', entities:{team:null,league:'VCT EMEA',player:null}}\n"
    "- 'Alfajer 어느 팀 소속이야?' → {route:'sql', task:'PLAYER_TEAM', entities:{team:null,league:null,player:'Alfajer'}}\n"
    "- 'fnatic 선수중 가장 오래된 선수가 누구야?' → {route:'hybrid', task:'UNKNOWN', entities:{team:'FNATIC',league:null,player:null}}\n"
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