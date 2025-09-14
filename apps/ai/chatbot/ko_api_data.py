import requests
import json
from datetime import datetime

def extract_date_for_embedding(date_str: str) -> str:
    try:
        # 문자열을 datetime 객체로 파싱
        dt = datetime.strptime(date_str, "%a, %d %b %Y %H:%M:%S %Z")
        # 원하는 형식으로 포맷
        return f"{dt.year}년 {dt.month}월 {dt.day}일"
    except ValueError:
        return "잘못된 날짜 형식입니다."

def fetch_data(base_url, id):
    """
    특정 팀 ID에 대한 데이터를 API에서 가져옵니다.
    
    Args:
        team_id (int): 팀 ID
        
    Returns:
        dict: 팀 데이터 (JSON 형태)
    """
    try:
        url = f"{base_url}{id}"
        response = requests.get(url)
        response.raise_for_status()  # HTTP 오류가 있으면 예외 발생
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"API 요청 중 오류 발생: {e}")
        return None

def convert_results_to_sentences(team_name: str, tournament_name: str, results: list[str]) -> list[str]:
    sentences = ""
    ordinal_map = {
        "1st": "우승",
        "2nd": "준우승",
        "3rd": "3위",
        "4th": "4위",
    }

    for result in results:
        if " - " not in result:
            if result in ordinal_map:
                sentences += f"{team_name}는 {ordinal_map[result]}으로 {tournament_name}대회를 마무리했다. "
            else:
                sentences += f"{team_name}는 {result}으로 {tournament_name}대회를 마무리했다. "
            continue

        parts = [s.strip() for s in result.split(" - ")]
        if len(parts) == 2:
            stage, placement = parts
        elif len(parts) > 2:
            stage = " - ".join(parts[:-1])
            placement = parts[-1]
        else:
            raise ValueError("Invalid format: less than 2 parts")

        if placement in ordinal_map:
            placement_text = ordinal_map[placement]
        elif "–" in placement:  # 예: 1st–4th
            parts = placement.split("–")
            placement_text = f"{parts[0]}에서 {parts[1]}으로"
        else:
            placement_text = f"{placement}위"

        sentence = f"{team_name}는 {stage} 스테이지에서 {placement_text}으로 {tournament_name}대회를 마무리했다. "
        sentences += sentence

    return sentences

def convert_joined_date(text):
    # 월 이름을 숫자로 변환하는 딕셔너리
    month_map = {
        "January": 1, "February": 2, "March": 3, "April": 4,
        "May": 5, "June": 6, "July": 7, "August": 8,
        "September": 9, "October": 10, "November": 11, "December": 12
    }

    # 예: "joined in February 2021"
    parts = text.strip().split()
    try:
        month_name = parts[-2]  # "February"
        year = parts[-1]        # "2021"
        month_num = month_map[month_name]
        return f"{year}년 {month_num}월"
    except (IndexError, KeyError):
        return "잘못된 입력 형식입니다."



result = []

team_ids = [
    11058, 2, 7386, 5248, 120, 2355, 188, 1034, 15072, 6961, 2406, 2359,
    1001, 2593, 14419, 1184, 397, 2059, 12694, 474, 8877, 4915, 11479, 7035,
    17, 8185, 878, 11060, 6199, 918, 8304, 624, 278, 14, 5448, 466,
    12010, 1120, 731, 12685, 13576, 14137, 13581, 1119, 11981, 11328, 12064, 13790
]
team_base_url = "https://vlr.orlandomm.net/api/v1/teams/"
player_base_url = "https://vlr.orlandomm.net/api/v1/players/"

for team_id in team_ids:
    team_data = fetch_data(team_base_url, team_id)
    
    if team_data:
        team_name = team_data["data"]["info"]["name"]
        tag = team_data["data"]["info"]["tag"]
        if tag:
            result.append({"title": f"{team_name}에 대한 정보.", "content": f"{team_name}는 발로란트 esports 팀이다. {team_name}는 {tag}라고도 불린다."})
        else:
            result.append({"title": f"{team_name}에 대한 정보.", "content": f"{team_name}는 발로란트 esports 팀이다."})
        for player in team_data["data"]["players"]:
            player_data = fetch_data(player_base_url, player["id"])
            user = player_data["data"]["info"]["user"]
            name = player_data["data"]["info"]["name"]
            country = player_data["data"]["info"]["country"]
            joined = player_data["data"]["team"]["joined"]
            past_teams = player_data["data"]["pastTeams"]
            sentence = f"{user}는 발로란트 프로게이머이고 {country}나라 사람이다. 그의 이름은 {name}이다."

            if joined:
                joined_date = convert_joined_date(joined)
                sentence += f" {user}는 {joined_date}부터 {team_name}에서 선수를 하고 있다."

            if past_teams:
                team_names = ", ".join([team["name"] for team in past_teams])
                sentence += f" 이 팀에 오기 전에 {user}는 {team_names}에서 선수를 했다."

            sentence = sentence.strip()

            result.append({
                "title": f"{team_name}의 선수 {user}에 대한 정보.",
                "content": sentence
            })
        for event in team_data["data"]["events"]:
            event_name = event["name"]
            event_year = event["year"]
            event_result = event["results"]
            content = convert_results_to_sentences(team_name, event_name, event_result)
            result.append({"title": f"{event_year}에 열린 {event_name} 대회에서 {team_name}의 결과.", "content": f"{content}"})
        for match in team_data["data"]["results"]:
            my_team_point = match["teams"][0]["points"]
            opponent_team = match["teams"][1]["name"]
            opponent_team_point = match["teams"][1]["points"]
            match_event = match["event"]["name"]
            match_date = extract_date_for_embedding(match["utc"])
            if my_team_point > opponent_team_point:
                result.append({"title": f"{team_name} 팀의 {opponent_team} 팀과의 {match_date}에 열린 {match_event} 대회에서의 결과.", "content": f"{team_name} 팀이 {opponent_team} 팀을 {my_team_point}-{opponent_team_point} 로 이겼다."})
            else:
                result.append({"title": f"{team_name} 팀의 {opponent_team} 팀과의 {match_date}에 열린 {match_event} 대회에서의 결과.", "content": f"{team_name} 팀이 {opponent_team} 팀에 {my_team_point}-{opponent_team_point} 로 졌다."})
        
        print(team_name, "done")


with open("api_data_ko.jsonl", "w", encoding="utf-8") as f:
    for item in result:
        f.write(json.dumps(item, ensure_ascii=False) + "\n")

print(f"총 {len(result)}개의 데이터가 team_players_data.jsonl 파일에 저장되었습니다.")
