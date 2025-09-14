import requests
import os
import json
import re
from dotenv import load_dotenv
from bs4 import BeautifulSoup
from datetime import datetime, timedelta


def convert_to_sql_datetime(utc_string):
    """
    UTC 날짜 문자열을 SQL DATETIME 형식(YYYY-MM-DD HH:MM:SS)으로 변환
    예: "Fri, 08 Aug 2025 03:00:00 GMT" -> "2025-08-08 03:00:00"
    """
    try:
        # UTC 문자열을 datetime 객체로 파싱
        dt = datetime.strptime(utc_string, "%a, %d %b %Y %H:%M:%S GMT")
        # SQL DATETIME 형식으로 변환
        return dt.strftime("%Y-%m-%d %H:%M:%S")
    except ValueError:
        # 파싱 실패 시 원본 문자열 반환
        return utc_string


load_dotenv()
team_base_url = "https://vlr.orlandomm.net/api/v1/teams/"
teams_all_url = "https://vlr.orlandomm.net/api/v1/teams?limit=all"
ch_team_base_url = "https://vlr.orlandomm.net/api/v1/teams?region=ch"
player_base_url = "https://vlr.orlandomm.net/api/v1/players/"


team_ids = [
    11058, 2, 7386, 5248, 120, 2355, 188, 1034, 15072, 6961, 2406, 2359,
    1001, 2593, 14419, 1184, 397, 2059, 12694, 474, 8877, 4915, 11479, 7035,
    17, 8185, 878, 11060, 6199, 918, 8304, 624, 278, 14, 5448, 466,
    12010, 1120, 731, 12685, 13576, 14137, 13581, 1119, 11981, 11328, 12064, 13790
]

pacific_team_ids = [17, 8185, 878, 11060, 6199, 918, 8304, 624, 278, 14, 5448, 466]
emce_team_ids = [1001, 2593, 14419, 1184, 397, 2059, 12694, 474, 8877, 4915, 11479, 7035]
america_team_ids = [11058, 2, 7386, 5248, 120, 2355, 188, 1034, 15072, 6961, 2406, 2359]
china_team_ids = [12010, 1120, 731, 12685, 13576, 14137, 13581, 1119, 11981, 11328, 12064, 13790]

leagues = [[pacific_team_ids, "VCT Pacific"], [america_team_ids, "VCT Americas"], [emce_team_ids, "VCT EMEA"], [china_team_ids, "VCT China"]]

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


def save_team_data():
    # 나라는 없는 것도 있어 수 작업 필요.
    url = f"{teams_all_url}"
    response = requests.get(url)
    response.raise_for_status() 
    teams_data = response.json()

    teams_country = {}
    for team in teams_data["data"]:
        teams_country[int(team["id"])] = team["country"]

    results = []

    for league, league_name in leagues:
        for team_id in league:
            team_data = fetch_data(team_base_url, team_id)
            team_name = team_data["data"]["info"]["name"]
            country = teams_country.get(team_id, "Unknown")
            team_score_data = requests.get(f"https://www.vlr.gg/team/{team_id}/")
            team_soup = BeautifulSoup(team_score_data.text, "html.parser")
            
            # class="rating-num"인 div에서 팀 점수 추출
            rating_div = team_soup.find("div", class_="rating-num")
            team_score = rating_div.get_text(strip=True) if rating_div else None

            date_start = (datetime.now() - timedelta(days=60)).strftime("%Y-%m-%d")            
            tema_detail_url = f"https://www.vlr.gg/team/stats/{team_id}/?event_id=all&date_start={date_start}&date_end="
            response = requests.get(tema_detail_url)
            soup = BeautifulSoup(response.text, "html.parser")
            table = soup.find("table")
            
            win_rate = None
            a_win_rate = None
            d_win_rate = None
            
            if table:
                # 헤더 찾기
                header_row = table.find("tr")
                if header_row:
                    headers = [th.get_text(strip=True) for th in header_row.find_all(["th", "td"])]
                    
                    # 전체 승률 계산
                    if "W" in headers and "L" in headers:
                        w_index = headers.index("W")
                        l_index = headers.index("L")
                        
                        rows = table.find_all("tr")[1:]  # 헤더 제외
                        total_w = 0
                        total_l = 0
                        
                        for row in rows:
                            cells = row.find_all(["td", "th"])
                            if len(cells) > max(w_index, l_index):
                                try:
                                    w_text = cells[w_index].get_text(strip=True)
                                    l_text = cells[l_index].get_text(strip=True)
                                    
                                    w_value = int(w_text) if w_text else 0
                                    l_value = int(l_text) if l_text else 0
                                    
                                    total_w += w_value
                                    total_l += l_value
                                except (ValueError, ZeroDivisionError):
                                    pass
                        
                        # 전체 W와 L로 승률 계산
                        total_games = total_w + total_l
                        if total_games > 0:
                            win_rate = int((total_w / total_games) * 100)
                    
                    # 공격 승률 계산 (8번: RW, 9번: RL)
                    if len(headers) > 9:
                        rows = table.find_all("tr")[1:]  # 헤더 제외
                        total_atk_rw = 0
                        total_atk_rl = 0
                        
                        for row in rows:
                            cells = row.find_all(["td", "th"])
                            if len(cells) > 9:
                                try:
                                    rw_text = cells[8].get_text(strip=True)
                                    rl_text = cells[9].get_text(strip=True)
                                    
                                    rw_value = int(rw_text) if rw_text and rw_text != '-' else 0
                                    rl_value = int(rl_text) if rl_text and rl_text != '-' else 0
                                    
                                    total_atk_rw += rw_value
                                    total_atk_rl += rl_value
                                except (ValueError, IndexError):
                                    pass
                        
                        total_atk_rounds = total_atk_rw + total_atk_rl
                        if total_atk_rounds > 0:
                            a_win_rate = int((total_atk_rw / total_atk_rounds) * 100)
                    
                    # 수비 승률 계산 (11번: RW, 12번: RL)
                    if len(headers) > 12:
                        rows = table.find_all("tr")[1:]  # 헤더 제외
                        total_def_rw = 0
                        total_def_rl = 0
                        
                        for row in rows:
                            cells = row.find_all(["td", "th"])
                            if len(cells) > 12:
                                try:
                                    rw_text = cells[11].get_text(strip=True)
                                    rl_text = cells[12].get_text(strip=True)
                                    
                                    rw_value = int(rw_text) if rw_text and rw_text != '-' else 0
                                    rl_value = int(rl_text) if rl_text and rl_text != '-' else 0
                                    
                                    total_def_rw += rw_value
                                    total_def_rl += rl_value
                                except (ValueError, IndexError):
                                    pass
                        
                        total_def_rounds = total_def_rw + total_def_rl
                        if total_def_rounds > 0:
                            d_win_rate = int((total_def_rw / total_def_rounds) * 100)
                    
                    # Agent Compositions 열(13번째)에서 빈 칸이 아닌 행 개수 계산
                    agent_compositions_count = 0
                    if len(headers) > 13:
                        rows = table.find_all("tr")[1:]  # 헤더 제외
                        
                        for row in rows:
                            cells = row.find_all(["td", "th"])
                            if len(cells) > 13:
                                cell = cells[13]  # 13번째 열
                                # mod-supercell 클래스가 있는지 확인
                                if "mod-supercell" in cell.get("class", []):
                                    # supercell 내부의 div.supercell 찾기
                                    supercell_div = cell.find("div", class_="supercell")
                                    if supercell_div:
                                        # supercell_div 내부에 다른 div가 있는지 확인
                                        inner_divs = supercell_div.find_all("div")
                                        if len(inner_divs) > 0:
                                            # 각 div를 개별적으로 확인하고 카운트
                                            for div in inner_divs:
                                                if div.get_text(strip=True):
                                                    agent_compositions_count += 1
            else:
                print(f"팀 {team_id}의 테이블을 찾을 수 없습니다.")
            
            # 첫 번째 팀 정보만 출력하고 중단
            print(f"팀명: {team_name}")
            print(f"국가: {country}")
            print(f"팀 점수: {team_score}")
            print(f"시작 날짜: {date_start}")
            print(f"전체 승률: {win_rate}%")
            print(f"공격 승률: {a_win_rate}%")
            print(f"수비 승률: {d_win_rate}%")
            print(f"Agent Compositions 데이터 개수: {agent_compositions_count}")
            
            results.append({
                "gameId": "Valorant",
                "leagueId": league_name,
                "name": team_name,
                "country": country,
                "score": team_score,
                "win_rate": win_rate,
                "a_win_rate": a_win_rate,
                "d_win_rate": d_win_rate,
                "ac": agent_compositions_count
            })

    with open("DB_JSON/team.json", "w", encoding="utf-8") as f:
        json.dump(results, f, indent=2, ensure_ascii=False)

def save_team_complete_data():
    # 팀 국가 정보 가져오기
    url = f"{teams_all_url}"
    response = requests.get(url)
    response.raise_for_status() 
    teams_data = response.json()

    teams_country = {}
    for team in teams_data["data"]:
        teams_country[int(team["id"])] = team["country"]

    # player_performance.json 파일 읽기
    try:
        with open("DB_JSON/player_performance.json", "r", encoding="utf-8") as f:
            player_performance_data = json.load(f)
        print(f"플레이어 성능 데이터 {len(player_performance_data)}개 로드됨")
    except FileNotFoundError:
        print("player_performance.json 파일을 찾을 수 없습니다.")
        player_performance_data = []
    except json.JSONDecodeError:
        print("player_performance.json 파일 형식이 올바르지 않습니다.")
        player_performance_data = []

    results = []

    for league, league_name in leagues:
        for team_id in league:
            team_data = fetch_data(team_base_url, team_id)
            team_name = team_data["data"]["info"]["name"]
            country = teams_country.get(team_id, "Unknown")
            
            # 팀 점수 가져오기
            team_score_data = requests.get(f"https://www.vlr.gg/team/{team_id}/")
            team_soup = BeautifulSoup(team_score_data.text, "html.parser")
            rating_div = team_soup.find("div", class_="rating-num")
            team_score = rating_div.get_text(strip=True) if rating_div else None

            # 팀 통계 가져오기 (승률, 공격/수비 승률, Agent Compositions)
            date_start = (datetime.now() - timedelta(days=60)).strftime("%Y-%m-%d")
            tema_detail_url = f"https://www.vlr.gg/team/stats/{team_id}/?event_id=all&date_start={date_start}&date_end="
            response = requests.get(tema_detail_url)
            soup = BeautifulSoup(response.text, "html.parser")
            table = soup.find("table")
            
            win_rate = None
            a_win_rate = None
            d_win_rate = None
            agent_compositions_count = 0
            
            if table:
                header_row = table.find("tr")
                if header_row:
                    headers = [th.get_text(strip=True) for th in header_row.find_all(["th", "td"])]
                    
                    # 전체 승률 계산
                    if "W" in headers and "L" in headers:
                        w_index = headers.index("W")
                        l_index = headers.index("L")
                        rows = table.find_all("tr")[1:]
                        total_w = total_l = 0
                        
                        for row in rows:
                            cells = row.find_all(["td", "th"])
                            if len(cells) > max(w_index, l_index):
                                try:
                                    w_text = cells[w_index].get_text(strip=True)
                                    l_text = cells[l_index].get_text(strip=True)
                                    w_value = int(w_text) if w_text else 0
                                    l_value = int(l_text) if l_text else 0
                                    total_w += w_value
                                    total_l += l_value
                                except (ValueError, ZeroDivisionError):
                                    pass
                        
                        total_games = total_w + total_l
                        if total_games > 0:
                            win_rate = int((total_w / total_games) * 100)
                    
                    # 공격 승률 계산 (8번: RW, 9번: RL)
                    if len(headers) > 9:
                        rows = table.find_all("tr")[1:]
                        total_atk_rw = total_atk_rl = 0
                        
                        for row in rows:
                            cells = row.find_all(["td", "th"])
                            if len(cells) > 9:
                                try:
                                    rw_text = cells[8].get_text(strip=True)
                                    rl_text = cells[9].get_text(strip=True)
                                    rw_value = int(rw_text) if rw_text and rw_text != '-' else 0
                                    rl_value = int(rl_text) if rl_text and rl_text != '-' else 0
                                    total_atk_rw += rw_value
                                    total_atk_rl += rl_value
                                except (ValueError, IndexError):
                                    pass
                        
                        total_atk_rounds = total_atk_rw + total_atk_rl
                        if total_atk_rounds > 0:
                            a_win_rate = int((total_atk_rw / total_atk_rounds) * 100)
                    
                    # 수비 승률 계산 (11번: RW, 12번: RL)
                    if len(headers) > 12:
                        rows = table.find_all("tr")[1:]
                        total_def_rw = total_def_rl = 0
                        
                        for row in rows:
                            cells = row.find_all(["td", "th"])
                            if len(cells) > 12:
                                try:
                                    rw_text = cells[11].get_text(strip=True)
                                    rl_text = cells[12].get_text(strip=True)
                                    rw_value = int(rw_text) if rw_text and rw_text != '-' else 0
                                    rl_value = int(rl_text) if rl_text and rl_text != '-' else 0
                                    total_def_rw += rw_value
                                    total_def_rl += rl_value
                                except (ValueError, IndexError):
                                    pass
                        
                        total_def_rounds = total_def_rw + total_def_rl
                        if total_def_rounds > 0:
                            d_win_rate = int((total_def_rw / total_def_rounds) * 100)
                    
                    # Agent Compositions 계산
                    if len(headers) > 13:
                        rows = table.find_all("tr")[1:]
                        for row in rows:
                            cells = row.find_all(["td", "th"])
                            if len(cells) > 13:
                                cell = cells[13]
                                if "mod-supercell" in cell.get("class", []):
                                    supercell_div = cell.find("div", class_="supercell")
                                    if supercell_div:
                                        inner_divs = supercell_div.find_all("div")
                                        if len(inner_divs) > 0:
                                            for div in inner_divs:
                                                if div.get_text(strip=True):
                                                    agent_compositions_count += 1

            # 플레이어 성능 평균 계산
            player_list = [player["user"] for player in team_data["data"]["players"]]
            team_players_data = []
            
            for player in player_list:
                for performance in player_performance_data:
                    if performance.get("player_handle") == player:
                        if performance.get("agents") is not None:
                            team_players_data.append(performance)
                        break
            
            # 평균 계산할 지표들
            metrics = ["APR", "KAST", "FKPR", "HS%", "CL%", "ADR", "FDPR"]
            team_averages = {}
            
            if team_players_data:
                for metric in metrics:
                    values = []
                    for player_data in team_players_data:
                        value = player_data.get(metric)
                        if value is not None:
                            if isinstance(value, str):
                                clean_value = value.replace('%', '').strip()
                                try:
                                    values.append(float(clean_value))
                                except ValueError:
                                    pass
                            else:
                                values.append(float(value))
                        elif metric == "CL%":
                            values.append(0)
                    
                    if values:
                        team_averages[metric] = round(sum(values) / len(values), 2)
                    else:
                        team_averages[metric] = 0
            else:
                for metric in metrics:
                    team_averages[metric] = 0

            # 결과에 추가
            team_result = {
                "gameId": "Valorant",
                "leagueId": league_name,
                "name": team_name,
                "country": country,
                "score": team_score,
                "win_rate": win_rate,
                "a_win_rate": a_win_rate,
                "d_win_rate": d_win_rate,
                "ac": agent_compositions_count
            }
            
            # 플레이어 평균 성능 추가
            for metric in metrics:
                team_result[f"avg_{metric}"] = team_averages[metric]
            
            results.append(team_result)
            # print(f"팀 {team_name} 데이터 완료")
            print(f"팀 {team_name} 데이터 완료 {team_score}")


    # JSON 파일로 저장
    with open("DB_JSON/team.json", "w", encoding="utf-8") as f:
        json.dump(results, f, indent=2, ensure_ascii=False)
    
    print(f"총 {len(results)}개 팀 데이터 저장 완료")

def save_player_data():

    results = []

    for league, league_name in leagues:
        for team_id in league:
            team_data = fetch_data(team_base_url, team_id)
            team_name = team_data["data"]["info"]["name"]
            for player in team_data["data"]["players"]:
                user = player["user"]
                name = player["name"]
                img_url = player["img"]
                country = player["country"]
                results.append({
                    "teamId": team_name,
                    "gameId": "Valorant",
                    "name": name,
                    "handle": user,
                    "country": country,
                    "img_url": img_url
                })

    with open("DB_JSON/player.json", "w", encoding="utf-8") as f:
        json.dump(results, f, indent=2, ensure_ascii=False)

def save_players_id_txt() -> None:
    """팀 목록을 순회해 선수 ID를 수집하고 DB_JSON/players_id.txt로 저장한다."""
    players_id = []
    for team_id in team_ids:
        team_data = fetch_data(team_base_url, team_id)
        if team_data:
            for player in team_data.get("data", {}).get("players", []):
                players_id.append({
                    "id": player["id"],
                    "user": player["user"],
                    "country": player["country"]
                })

    os.makedirs("DB_JSON", exist_ok=True)
    with open("DB_JSON/players_id.json", "w", encoding="utf-8") as f:
        json.dump(players_id, f, ensure_ascii=False, indent=2)

def crawl_player_performance():
    result = []
    id_list = json.load(open("DB_JSON/players_id.json", "r", encoding="utf-8"))

    # total_url = "https://www.vlr.gg/stats"
    total_url = "https://www.vlr.gg/stats/?event_group_id=all&region=all&min_rounds=100&min_rating=1000&agent=all&map_id=all&timespan=60d"
    response = requests.get(total_url)
    soup = BeautifulSoup(response.text, "html.parser")

    table = soup.find("table")
    if not table:
        print("테이블을 찾을 수 없습니다.")
        return result
        
    header_row = table.find("tr")
    if not header_row:
        print("헤더 행을 찾을 수 없습니다.")
        return result
    
    headers = [th.get_text(strip=True) for th in header_row.find_all(["th", "td"])]
    print("헤더:", headers)
    
    # 필요한 열의 인덱스 찾기
    required_columns = ["Player", "Agents", "Rnd", "ACS", "KAST", "ADR", "APR", "FKPR", "FDPR", "HS%", "CL%", "K", "A", "D"]
    column_indices = {}
    
    for col in required_columns:
        if col in headers:
            column_indices[col] = headers.index(col)
        else:
            print(f"'{col}' 열을 찾을 수 없습니다.")
    
    # 테이블의 모든 데이터 행을 먼저 파싱
    rows = table.find_all("tr")[1:]  # 헤더 제외
    table_players = {}  # {(handle, country): row_data} 형태로 저장
    
    for row in rows:
        cells = row.find_all(["td", "th"])
        if len(cells) < len(headers):
            continue
            
        # Player 열에서 플레이어 핸들과 국가 정보 추출
        if "Player" in column_indices:
            player_cell = cells[column_indices["Player"]]
            
            # div class="text-of"에서 플레이어 핸들 추출
            text_of_div = player_cell.find("div", class_="text-of")
            player_handle = text_of_div.get_text(strip=True) if text_of_div else ""
            
            # <i> 태그에서 국가 코드 추출
            flag_element = player_cell.find("i", class_=lambda x: x and "flag mod-" in x)
            country_code = ""
            if flag_element:
                class_list = flag_element.get("class", [])
                for class_name in class_list:
                    if class_name.startswith("mod-"):
                        country_code = class_name.split("mod-")[1]
                        break
            
            if player_handle and country_code:
                row_data = [cell.get_text(strip=True) for cell in cells]
                table_players[(player_handle, country_code)] = row_data
    
    print(f"테이블에서 {len(table_players)}명의 플레이어를 파싱했습니다.")
    
    # JSON의 각 플레이어를 테이블에서 찾기
    not_found_players = []
    
    for player in id_list:
        player_handle = player["user"]
        country_code = player["country"]
        player_id = player["id"]
        
        # 기본 데이터 구조 (모든 값 NULL로 초기화)
        performance_data = {
            "player_handle": player_handle,
            "agents": None,
            "Rnd": None,
            "ACS": None,
            "KAST": None,
            "ADR": None,
            "APR": None,
            "FKPR": None,
            "FDPR": None,
            "HS%": None,
            "CL%": None,
            "KDA": None
        }
        
        # 테이블에서 해당 플레이어 찾기
        if (player_handle, country_code) in table_players:
            row_data = table_players[(player_handle, country_code)]
            
            # Agents 열에서 에이전트 이름들 추출
            if "Agents" in column_indices:
                agents_cell = None
                # row_data에서 해당 셀을 찾기 위해 다시 파싱 (복잡하므로 테이블에서 다시 찾기)
                for row in rows:
                    cells = row.find_all(["td", "th"])
                    if len(cells) > column_indices["Player"]:
                        player_cell = cells[column_indices["Player"]]
                        text_of_div = player_cell.find("div", class_="text-of")
                        current_handle = text_of_div.get_text(strip=True) if text_of_div else ""
                        
                        flag_element = player_cell.find("i", class_=lambda x: x and "flag mod-" in x)
                        current_country = ""
                        if flag_element:
                            class_list = flag_element.get("class", [])
                            for class_name in class_list:
                                if class_name.startswith("mod-"):
                                    current_country = class_name.split("mod-")[1]
                                    break
                        
                        if current_handle == player_handle and current_country == country_code:
                            agents_cell = cells[column_indices["Agents"]]
                            agent_imgs = agents_cell.find_all("img")
                            agents_list = []
                            
                            for img in agent_imgs:
                                src = img.get("src", "")
                                if src:
                                    agent_name = src.split("/")[-1].replace(".png", "")
                                    if agent_name:
                                        agents_list.append(agent_name)
                            
                            performance_data["agents"] = agents_list if agents_list else None
                            break
            
            # 다른 열들의 데이터 추출
            for col in ["Rnd", "ACS", "KAST", "ADR", "APR", "FKPR", "FDPR", "HS%", "CL%"]:
                if col in column_indices:
                    value = row_data[column_indices[col]]
                    performance_data[col] = value if value and value.strip() else None
            
            # KDA 계산 (K + A) / D
            try:
                if all(col in column_indices for col in ["K", "A", "D"]):
                    k_value = row_data[column_indices["K"]]
                    a_value = row_data[column_indices["A"]]
                    d_value = row_data[column_indices["D"]]
                    
                    if k_value and a_value and d_value:
                        k_float = float(k_value)
                        a_float = float(a_value)
                        d_float = float(d_value)
                        
                        if d_float != 0:
                            kda_ratio = (k_float + a_float) / d_float
                            performance_data["KDA"] = round(kda_ratio, 2)
            except (ValueError, ZeroDivisionError):
                pass  # KDA는 None으로 유지
        else:
            not_found_players.append(player)
        
        # 모든 플레이어를 결과에 추가 (찾은 사람도, 못 찾은 사람도)
        result.append(performance_data)
    
    # 찾지 못한 선수들 출력
    if not_found_players:
        print(f"\n테이블에서 찾을 수 없는 선수들 ({len(not_found_players)}명):")
        for player in not_found_players:
            print(f"- {player['user']} (ID: {player['id']}, 국가: {player['country']})")
    
    print(f"\n총 {len(result)}명의 플레이어 데이터를 처리했습니다. (매칭: {len(result) - len(not_found_players)}명, 미매칭: {len(not_found_players)}명)")
    
    # 결과를 JSON 파일로 저장
    with open("DB_JSON/player_performance.json", "w", encoding="utf-8") as f:
        json.dump(result, f, indent=2, ensure_ascii=False)
    
    print(f"총 {len(result)}명의 플레이어 성능 데이터를 수집했습니다.")
    return result

def crawling_match_data():
    result = []
    url = "https://vlr.orlandomm.net/api/v1/teams/"
    for league, league_name in leagues:
        for team_id in league:
            data = fetch_data(url, team_id)
            isplayed_data = data["data"]["results"]
            upcoming_data = data["data"]["upcoming"]
            
            for match in isplayed_data:
                if "VCT" in match["event"]["name"]:
                    result.append({
                        "team_id": match["teams"][0]["name"],
                        "matchDate": convert_to_sql_datetime(match["utc"]),
                        "op_team": match["teams"][1]["name"],
                        "my_score": match["teams"][0]["points"],
                        "op_score": match["teams"][1]["points"],
                        "doesWin": True if match["teams"][0]["points"] > match["teams"][1]["points"] else False,
                        "is_played": True
                    })
            
            for match in upcoming_data:
                if "VCT" in match["event"]["name"]:
                    result.append({
                        "team_id": match["teams"][0]["name"],
                        "matchDate": convert_to_sql_datetime(match["utc"]),
                        "op_team": match["teams"][1]["name"],
                        "my_score": None,
                        "op_score": None,
                        "doesWin": None,
                        "is_played": False
                    })
    with open("DB_JSON/match.json", "w", encoding="utf-8") as f:
        json.dump(result, f, indent=2, ensure_ascii=False)
            
def team_data_final():
    url = f"{teams_all_url}"
    response = requests.get(url)
    response.raise_for_status() 
    teams_data = response.json()
    point_url = "https://www.vlr.gg/vct-2025/standings"
    point_response = requests.get(point_url)
    point_soup = BeautifulSoup(point_response.text, "html.parser")
    
    # VLR.gg에서 팀 점수 정보 추출
    team_points = {}
    
    # 4개의 테이블을 찾기 (각 리그별 순위표)
    tables = point_soup.find_all("table")
    
    for table in tables:
        rows = table.find_all("tr")
        for row in rows:
            # 팀 링크와 점수 찾기
            team_link = row.find("a", href=lambda x: x and x.startswith("/team/"))
            if team_link:
                # href="/team/{team_id}/{team_name}" 형식에서 team_id 추출
                href = team_link.get("href")
                team_id = href.split("/")[2] if len(href.split("/")) > 2 else None
                
                if team_id:
                    # 점수 찾기 (예: "19 point", "15 point" 등)
                    point_text = row.get_text()
                    point_match = re.search(r'(\d+)\s*point', point_text)
                    if point_match:
                        points = int(point_match.group(1))
                        team_points[team_id] = points
    
    teams_country = {}
    for team in teams_data["data"]:
        teams_country[int(team["id"])] = team["country"]

    results = []

    for league, league_name in leagues:
        for team_id in league:
            team_data = fetch_data(team_base_url, team_id)
            team_name = team_data["data"]["info"]["name"]
            country = teams_country.get(team_id, "Unknown")
            team_img = team_data["data"]["info"]["logo"]
            
            # 팀 ID를 문자열로 변환하여 점수 딕셔너리에서 검색
            team_id_str = str(team_id)
            points = team_points.get(team_id_str, 0)  # 점수가 없으면 0

            results.append({
                "gameId": "Valorant",
                "leagueId": league_name,
                "name": team_name,
                "country": country,
                "img_url": team_img,
                "point": points
            })
    
    with open("DB_JSON/team_final.json", "w", encoding="utf-8") as f:
        json.dump(results, f, indent=2, ensure_ascii=False)
    
    print(f"총 {len(results)}개 팀의 데이터를 수집했습니다.")

# crawl_player_performance()
# save_players_id_txt()
# save_team_data()
# save_team_detail_data()
# save_team_complete_data()
# crawling_match_data()
team_data_final()
# save_player_data()