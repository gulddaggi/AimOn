import requests
import json
from datetime import datetime

def extract_date_for_embedding(rfc_datetime: str) -> str:
    dt = datetime.strptime(rfc_datetime, "%a, %d %b %Y %H:%M:%S %Z")
    return dt.strftime("%B %d, %Y")

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
        "1st": "first place",
        "2nd": "second place",
        "3rd": "third place",
        "4th": "fourth place",
    }

    for result in results:
        if " - " not in result:
            if result in ordinal_map:
                sentences += f"{team_name} finished in {ordinal_map[result]} at the {tournament_name}. "
            else:
                sentences += f"{team_name} finished in {result} at the {tournament_name}. "
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
            placement_text = f"{parts[0]} to {parts[1]} place"
        else:
            placement_text = f"{placement} place"

        sentence = f"{team_name} finished in {placement_text} in the {stage} stage of the {tournament_name}. "
        sentences += sentence

    return sentences


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
            result.append({"title": f"Information about {team_name}.", "content": f"{team_name} is a Valorant esports team. They are also referred to as {tag}."})
        else:
            result.append({"title": f"Information about {team_name}.", "content": f"{team_name} is a Valorant esports team."})
        for player in team_data["data"]["players"]:
            player_data = fetch_data(player_base_url, player["id"])
            user = player_data["data"]["info"]["user"]
            name = player_data["data"]["info"]["name"]
            country = player_data["data"]["info"]["country"]
            joined = player_data["data"]["team"]["joined"]
            past_teams = player_data["data"]["pastTeams"]
            sentence = f"{user} is a professional Valorant player from {country}. His full name is {name}."

            if joined:
                sentence += f" He joined {team_name} in {joined}."

            if past_teams:
                team_names = ", ".join([team["name"] for team in past_teams])
                sentence += f" Prior to this, he played for teams such as {team_names}."

            sentence = sentence.strip()

            result.append({
                "title": f"Information about {team_name}'s player {user}.",
                "content": sentence
            })
        for event in team_data["data"]["events"]:
            event_name = event["name"]
            event_year = event["year"]
            event_result = event["results"]
            content = convert_results_to_sentences(team_name, event_name, event_result)
            result.append({"title": f"Information about {team_name}'s event {event_name}, which was held in {event_year}.", "content": f"{content}"})
        for match in team_data["data"]["results"]:
            my_team_point = match["teams"][0]["points"]
            opponent_team = match["teams"][1]["name"]
            opponent_team_point = match["teams"][1]["points"]
            match_event = match["event"]["name"]
            match_date = extract_date_for_embedding(match["utc"])
            if my_team_point > opponent_team_point:
                result.append({"title": f"Information about {team_name}'s match against {opponent_team} in {match_event} on {match_date}.", "content": f"{team_name} won the match {my_team_point}-{opponent_team_point} against {opponent_team}."})
            else:
                result.append({"title": f"Information about {team_name}'s match against {opponent_team} in {match_event} on {match_date}.", "content": f"{team_name} lost the match {my_team_point}-{opponent_team_point} to {opponent_team}."})
        
        print(team_name, "done")


with open("api_data_new.jsonl", "w", encoding="utf-8") as f:
    for item in result:
        f.write(json.dumps(item, ensure_ascii=False) + "\n")

print(f"총 {len(result)}개의 데이터가 team_players_data.jsonl 파일에 저장되었습니다.")
