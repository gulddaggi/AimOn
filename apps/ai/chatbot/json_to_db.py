# json_to_db.py
import os
import json
import pymysql
from dotenv import load_dotenv

load_dotenv()

DB_HOST = os.getenv("MYSQL_HOST")
DB_USER = os.getenv("MYSQL_USER")
DB_PASSWORD = os.getenv("MYSQL_PASSWORD")
DB_NAME = os.getenv("NEW_DB")

conn = pymysql.connect(
    host=DB_HOST,
    user=DB_USER,
    password=DB_PASSWORD,
    db=DB_NAME,
    charset="utf8mb4",
    autocommit=False,
)
cursor = conn.cursor()

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, "DB_JSON")

# 캐시
game_cache = {}                 # name -> id
league_cache = {}               # (game_id, league_name) -> id
team_cache_by_key = {}          # (game_id, league_id, team_name) -> id
team_cache_by_name = {}         # (game_id, team_name) -> id  (player용 조회)

def get_or_create_game(name: str) -> int:
    if name in game_cache:
        return game_cache[name]
    cursor.execute("SELECT id FROM Game WHERE name=%s", (name,))
    row = cursor.fetchone()
    if row:
        game_id = row[0]
    else:
        cursor.execute("INSERT INTO Game(name) VALUES(%s)", (name,))
        game_id = cursor.lastrowid
    game_cache[name] = game_id
    return game_id

def get_or_create_league(game_id: int, name: str) -> int:
    key = (game_id, name)
    if key in league_cache:
        return league_cache[key]
    cursor.execute("SELECT id FROM League WHERE gameId=%s AND name=%s", (game_id, name))
    row = cursor.fetchone()
    if row:
        league_id = row[0]
    else:
        cursor.execute(
            "INSERT INTO League(gameId, name) VALUES(%s, %s)",
            (game_id, name),
        )
        league_id = cursor.lastrowid
    league_cache[key] = league_id
    return league_id

def get_or_create_team(game_id: int, league_id: int, name: str, country: str) -> int:
    key = (game_id, league_id, name)
    if key in team_cache_by_key:
        return team_cache_by_key[key]
    cursor.execute(
        "SELECT id FROM Team WHERE gameId=%s AND leagueId=%s AND name=%s",
        (game_id, league_id, name),
    )
    row = cursor.fetchone()
    if row:
        team_id = row[0]
    else:
        cursor.execute(
            "INSERT INTO Team(gameId, leagueId, name, country) VALUES(%s, %s, %s, %s)",
            (game_id, league_id, name, country),
        )
        team_id = cursor.lastrowid
    team_cache_by_key[key] = team_id
    team_cache_by_name[(game_id, name)] = team_id
    return team_id

def get_or_create_player(team_id: int, game_id: int, name: str, handle: str, country: str, position: str | None) -> int:
    # 중복 기준: 같은 팀에서 같은 핸들(handle)이면 동일 선수로 간주
    cursor.execute(
        "SELECT id FROM Player WHERE teamId=%s AND handle=%s",
        (team_id, handle),
    )
    row = cursor.fetchone()
    if row:
        return row[0]
    cursor.execute(
        "INSERT INTO Player(teamId, gameId, name, handle, country, position) VALUES(%s, %s, %s, %s, %s, %s)",
        (team_id, game_id, name, handle, country, position),
    )
    return cursor.lastrowid

def load_json(filename: str):
    path = os.path.join(DATA_DIR, filename)
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)

def import_leagues():
    data = load_json("league.json")
    created = 0
    for item in data:
        game_name = item["gameId"]              # ex) "Valorant"
        league_name = item["name"]              # ex) "VCT Pacific"
        game_id = get_or_create_game(game_name)
        _ = get_or_create_league(game_id, league_name)
        created += 1
    return created

def import_teams():
    data = load_json("team.json")
    created = 0
    for item in data:
        game_name = item["gameId"]
        league_name = item["leagueId"]          # 문자열 리그명
        team_name = item["name"]
        country = item.get("country", None)

        game_id = get_or_create_game(game_name)
        league_id = get_or_create_league(game_id, league_name)
        _ = get_or_create_team(game_id, league_id, team_name, country)
        created += 1
    return created

def import_players():
    data = load_json("player.json")
    created = 0
    skipped_missing_team = 0
    for item in data:
        game_name = item["gameId"]
        team_name = item["teamId"]              # 문자열 팀명
        name = item["name"]
        handle = item["handle"]
        country = item.get("country", None)
        role = item.get("role", None)
        position = (role or None) if role != "" else None

        game_id = get_or_create_game(game_name)
        team_id = team_cache_by_name.get((game_id, team_name))
        if team_id is None:
            # 팀이 먼저 있어야 함. 팀이 없으면 건너뛰고 카운트.
            skipped_missing_team += 1
            continue

        _ = get_or_create_player(team_id, game_id, name, handle, country, position)
        created += 1
    return created, skipped_missing_team

def main():
    try:
        leagues = import_leagues()
        teams = import_teams()
        players, skipped = import_players()
        conn.commit()
        print(f"완료: League={leagues}, Team={teams}, Player={players}, (팀 없음으로 스킵={skipped})")
    except Exception as e:
        conn.rollback()
        raise
    finally:
        cursor.close()
        conn.close()

if __name__ == "__main__":
    main()