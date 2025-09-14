import os
import json
import pymysql
from dotenv import load_dotenv
from datetime import datetime

# 환경변수 로드 (.env가 없어도 컨테이너 env로 동작)
load_dotenv()

class JsonToDatabase:
    def __init__(self):
        """환경변수에서 데이터베이스 연결 정보 로드"""
        self.host = os.getenv("MYSQL_HOST")
        self.user = os.getenv("MYSQL_USER")
        self.password = os.getenv("MYSQL_PASSWORD")
        # MYSQL_DB 또는 MYSQL_DATABASE 둘 다 지원
        self.database = os.getenv("MYSQL_DB") or os.getenv("MYSQL_DATABASE")
        self.connection = None

        # 전체 리프레시 모드 (배포마다 최신 JSON으로 덮어쓰기)
        self.full_refresh = os.getenv("FULL_REFRESH", "1") == "1"

    def connect(self):
        """데이터베이스 연결"""
        try:
            self.connection = pymysql.connect(
                host=self.host,
                user=self.user,
                password=self.password,
                db=self.database,
                charset="utf8mb4",
                autocommit=False,
            )
            print(f"MySQL 데이터베이스 '{self.database}'에 성공적으로 연결되었습니다.")
            return True
        except Exception as e:
            print(f"데이터베이스 연결 오류: {e}")
            return False

    def disconnect(self):
        """데이터베이스 연결 해제"""
        if self.connection:
            self.connection.close()
            print("MySQL 연결이 해제되었습니다.")

    def load_json_file(self, file_path):
        """JSON 파일 로드"""
        try:
            with open(file_path, "r", encoding="utf-8") as file:
                return json.load(file)
        except Exception as e:
            print(f"JSON 파일 로드 오류 ({file_path}): {e}")
            return None

    def clear_domain_tables(self):
        """
        FK 충돌 없이 도메인 테이블만 안전하게 초기화.
        자식 -> 부모 순서 준수. (유저/커뮤니티 등 사용자 데이터는 건드리지 않음)
        """
        print("\n=== Seed 전 도메인 테이블 초기화(자식→부모) ===")
        tables_in_order = [
            # 자식 먼저
            "valorant_player_stats",
            "matches",
            "player",
            "team_like",
            "game_like",
            # 부모 테이블
            "team",
            "league",
            "game",
            "embeddings",
        ]
        cur = self.connection.cursor()
        try:
            cur.execute("SELECT @@FOREIGN_KEY_CHECKS")
            old_fk = cur.fetchone()[0] if cur.rowcount else 1
            cur.execute("SET FOREIGN_KEY_CHECKS=0")

            for t in tables_in_order:
                try:
                    cur.execute(f"TRUNCATE TABLE {t}")
                    print(f" - truncated: {t} (AUTO_INCREMENT reset)")
                except Exception as e:
                    print(f"   (skip {t}: {e})")

            cur.execute("SET FOREIGN_KEY_CHECKS=%s", (old_fk,))
            self.connection.commit()
            print("초기화 완료.")
        except Exception as e:
            self.connection.rollback()
            print(f"초기화 중 오류: {e}")
            raise
        finally:
            cur.close()
    # --------------------------------------------------- #

    def insert_games(self):
        """1. game"""
        print("\n=== Game 데이터 삽입 중... ===")
        data = self.load_json_file("DB_JSON/game.json")
        if not data:
            return False
        cur = self.connection.cursor()
        try:
            # 기존: DELETE FROM game  ← 제거. 초기화는 clear_domain_tables에서 수행.
            for row in data:
                # name UNIQUE
                cur.execute(
                    "INSERT INTO game (name) VALUES (%s) "
                    "ON DUPLICATE KEY UPDATE name = VALUES(name)",
                    (row["name"],),
                )
            self.connection.commit()
            print(f"Game 데이터 {len(data)}개 삽입/갱신 완료")
            return True
        except Exception as e:
            print(f"Game 데이터 삽입 오류: {e}")
            self.connection.rollback()
            return False
        finally:
            cur.close()

    def insert_leagues(self):
        """2. league"""
        print("\n=== League 데이터 삽입 중... ===")
        data = self.load_json_file("DB_JSON/league.json")
        if not data:
            return False

        cur = self.connection.cursor()
        try:
            inserted, existed = 0, 0
            for row in data:
                game_name = row.get("gameId")
                league_name   = row.get("name")

                if not (game_name and league_name):
                    print(f"경고: league 레코드 필수값 누락: {row}")
                    continue
                
                cur.execute("SELECT id FROM game WHERE name = %s", (game_name,))
                g = cur.fetchone()
                if not g:
                    print(f"경고: 게임을 찾지 못함 (game='{game_name}', league='{league_name}')")
                    continue

                game_id = g[0]
                cur.execute(
                    "SELECT id FROM league WHERE game_id = %s AND name = %s",
                    (game_id, league_name),
                )
                found = cur.fetchone()

                if found:
                    existed += 1
                else:
                    cur.execute(
                        "INSERT INTO league (game_id, name) VALUES (%s, %s)",
                        (game_id, league_name),
                    )
                    inserted += 1
                
            self.connection.commit()
            print(f"League 삽입 {inserted}건, 이미 존재 {existed}건")
            return True
        except Exception as e:
            self.connection.rollback()
            print(f"League 데이터 삽입 오류: {e}")
            return False
        finally:
            cur.close()

    def insert_teams(self):
        """
        3. team
        - team.json 기본 정보 + team_detail.json 매칭해 win_rate / a_win_rate / d_win_rate 채움
        - (team_name, league_id) 존재 시 UPDATE, 없으면 INSERT  (유니크 키 없어도 동작)
        """

        print("\n=== Team 데이터 삽입/갱신 중... ===")
        teams = self.load_json_file("DB_JSON/team.json")
        details = self.load_json_file("DB_JSON/team_detail.json") or []
        if not teams:
            return False

        # team_detail.json을 팀 이름 기준으로 빠르게 참조할 수 있도록 맵 구성 (대소문자 무시)
        def _to_float(v, default=0.0):
            try:
                if v is None or v == "":
                    return default
                return float(v)
            except Exception:
                return default

        def _safe_int(v, default=0):
            try:
                return int(v)
            except (TypeError, ValueError):
                return default
        
        detail_by_name = {}
        for d in details:
            name_key = str(d.get("name", "")).strip().lower()
            if name_key:
                detail_by_name[name_key] = d

        cur = self.connection.cursor()
        try:
            inserted, updated, skipped = 0, 0, 0

            for row in teams:
                game_name   = row.get("gameId")
                league_name = row.get("leagueId")
                team_name   = row.get("name")
                country     = row.get("country", "Unknown")
                img_url     = row.get("img_url")
                point = _safe_int(row.get("point"), 0)

                if not (game_name and league_name and team_name):
                    print(f"경고: 필수값 누락(team): {row}")
                    skipped += 1
                    continue

                # FK 조회
                cur.execute("SELECT id FROM game WHERE name = %s", (game_name,))
                g = cur.fetchone()
                if not g:
                    print(f"경고: '{team_name}' 팀의 game을을 찾을 수 없습니다. (game={game_name}")
                    skipped += 1
                    continue

                game_id = g[0]

                cur.execute(
                    "SELECT id FROM league WHERE name = %s AND game_id = %s",
                    (league_name, game_id)
                )

                l = cur.fetchone()
                if not l:
                    print(f"경고: league '{league_name}' (game='{game_name}')를 찾을 수 없습니다.")
                    skipped += 1
                    continue
                
                league_id = l[0]

                # 상세 매칭
                d = detail_by_name.get(team_name.strip().lower(), {})
                win_rate = _to_float(d.get("win_rate"), 0.0)
                a_win_rate     = _to_float(d.get("a_win_rate"), 0.0)
                d_win_rate     = _to_float(d.get("d_win_rate"), 0.0)

                # 존재 여부 (team_name, league_id)
                cur.execute(
                    "SELECT id FROM team WHERE team_name = %s AND league_id = %s",
                    (team_name, league_id),
                )
                found = cur.fetchone()

                if found:
                    team_id = found[0]
                    try:
                        cur.execute(
                            """
                            UPDATE team
                                SET country = %s,
                                    team_name = %s,
                                    img_url = %s,
                                    game_id = %s,
                                    league_id = %s,
                                    win_rate = %s,
                                    a_win_rate = %s,
                                    d_win_rate = %s,
                                    point = %s
                            WHERE id = %s
                        """,
                        (country, team_name, img_url, game_id, league_id,
                        win_rate, a_win_rate, d_win_rate, point, team_id),
                    )
                    except pymysql.err.ProgrammingError as pe:
                        cur.execute(
                            """
                            UPDATE team
                                SET country = %s,
                                    team_name = %s,
                                    img_url = %s,
                                    game_id = %s,
                                    league_id = %s,
                                    win_rate = %s,
                                    a_win_rate = %s,
                                    d_win_rate = %s
                            WHERE id = %s
                        """,
                        (country, team_name, img_url, game_id, league_id,
                        win_rate, a_win_rate, d_win_rate, team_id),
                        )
                    updated += 1
                else:
                    try:
                        # point 포함 INSERT
                        cur.execute(
                            """
                            INSERT INTO team
                                (country, team_name, img_url, game_id, league_id,
                                win_rate, a_win_rate, d_win_rate, point)
                            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                            """,
                            (country, team_name, img_url, game_id, league_id,
                            win_rate, a_win_rate, d_win_rate, point),
                        )
                    except pymysql.err.ProgrammingError as pe:
                        # point 컬럼이 없을 때: point 없이 INSERT
                        cur.execute(
                            """
                            INSERT INTO team
                                (country, team_name, img_url, game_id, league_id,
                                win_rate, a_win_rate, d_win_rate)
                            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                            """,
                            (country, team_name, img_url, game_id, league_id,
                            win_rate, a_win_rate, d_win_rate),
                        )
                    inserted += 1

            self.connection.commit()
            print(f"Team 삽입 {inserted}건, 갱신 {updated}건, 스킵 {skipped}건")
            return True
        except Exception as e:
            self.connection.rollback()
            print(f"Team 데이터 삽입/갱신 오류: {e}")
            return False
        finally:
            cur.close()

    def insert_players(self):
        """4. player"""
        print("\n=== Player 데이터 삽입 중... ===")
        data = self.load_json_file("DB_JSON/player.json")
        if not data:
            return False
        cur = self.connection.cursor()
        try:
            for row in data:
                team_id = None
                if row.get("teamId"):
                    cur.execute("SELECT id FROM team WHERE team_name = %s", (row["teamId"],))
                    t = cur.fetchone()
                    team_id = t[0] if t else None
                game_id = None
                if row.get("gameId"):
                    cur.execute("SELECT id FROM game WHERE name = %s", (row["gameId"],))
                    g = cur.fetchone()
                    game_id = g[0] if g else None

                # handle UNIQUE → upsert로 갱신 허용
                cur.execute(
                    """
                    INSERT INTO player (team_id, game_id, name, handle, country, img_url)
                    VALUES (%s, %s, %s, %s, %s, %s)
                    ON DUPLICATE KEY UPDATE
                      team_id=VALUES(team_id), game_id=VALUES(game_id),
                      name=VALUES(name), country=VALUES(country), img_url=VALUES(img_url)
                    """,
                    (team_id, game_id, row["name"], row["handle"], row["country"], row["img_url"]),
                )
            self.connection.commit()
            print(f"Player 데이터 {len(data)}개 삽입/갱신 완료")
            return True
        except Exception as e:
            print(f"Player 데이터 삽입 오류: {e}")
            self.connection.rollback()
            return False
        finally:
            cur.close()

    def insert_player_performance(self):
        """5. valorant_player_stats"""
        print("\n=== Player Performance 데이터 삽입 중... ===")
        data = self.load_json_file("DB_JSON/player_performance.json")
        if not data:
            return False
        cur = self.connection.cursor()

        def safe_float(v, default=0.0):
            if v in (None, "", "null"):
                return default
            try:
                return float(v)
            except (ValueError, TypeError):
                return default

        def safe_int(v, default=0):
            if v in (None, "", "null"):
                return default
            try:
                return int(v)
            except (ValueError, TypeError):
                return default

        def safe_percentage(v, default=0.0):
            if v in (None, "", "null"):
                return default
            try:
                return float(str(v).replace("%", "")) / 100.0
            except (ValueError, TypeError):
                return default

        try:
            # 기존: DELETE FROM valorant_player_stats  ← 제거 (full_refresh 시 clear 단계에서 처리)
            for row in data:
                cur.execute("SELECT id FROM player WHERE handle = %s", (row["player_handle"],))
                p = cur.fetchone()
                if not p:
                    print(f"경고: '{row['player_handle']}' 플레이어를 찾을 수 없습니다.")
                    continue

                round_val = safe_int(row.get("Rnd"))
                acs  = safe_float(row.get("ACS"))
                adr  = safe_float(row.get("ADR"))
                apr  = safe_float(row.get("APR"))
                fkpr = safe_float(row.get("FKPR"))
                fdpr = safe_float(row.get("FDPR"))
                hs   = safe_percentage(row.get("HS%"))
                cl   = safe_percentage(row.get("CL%"))
                kast = safe_percentage(row.get("KAST"))
                kda  = safe_float(row.get("KDA"))

                # PK(player_id)라 upsert 형태로 갱신
                cur.execute(
                    """
                    INSERT INTO valorant_player_stats
                    (player_id, round, acs, adr, apr, fkpr, fdpr, hs, cl, kast, kda)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    ON DUPLICATE KEY UPDATE
                      round=VALUES(round), acs=VALUES(acs), adr=VALUES(adr), apr=VALUES(apr),
                      fkpr=VALUES(fkpr), fdpr=VALUES(fdpr), hs=VALUES(hs), cl=VALUES(cl),
                      kast=VALUES(kast), kda=VALUES(kda)
                    """,
                    (p[0], round_val, acs, adr, apr, fkpr, fdpr, hs, cl, kast, kda),
                )
            self.connection.commit()
            print(f"Player Performance 데이터 {len(data)}개 삽입/갱신 완료")
            return True
        except Exception as e:
            print(f"Player Performance 데이터 삽입 오류: {e}")
            self.connection.rollback()
            return False
        finally:
            cur.close()

    def insert_matches(self):
        """6. matches"""
        print("\n=== Match 데이터 삽입 중... ===")
        data = self.load_json_file("DB_JSON/match.json")
        if not data:
            return False
        cur = self.connection.cursor()
        try:
            # 기존: DELETE FROM matches  ← 제거
            for row in data:
                cur.execute("SELECT id, game_id, league_id FROM team WHERE team_name = %s", (row["team_id"],))
                t = cur.fetchone()
                if not t:
                    print(f"경고: '{row['team_id']}' 팀을 찾을 수 없습니다.")
                    continue

                team_id, game_id, league_id = t
                match_date = datetime.strptime(row["matchDate"], "%Y-%m-%d %H:%M:%S")
                my_score = int(row["my_score"]) if row["my_score"] is not None else None
                op_score = int(row["op_score"]) if row["op_score"] is not None else None
                is_played = bool(row["is_played"])

                # vlr_match_id UNIQUE이면 upsert도 고려 가능. 여기선 일반 insert.
                cur.execute(
                    """
                    INSERT INTO matches
                    (team_id, op_team, game_id, league_id, match_date, my_score, op_score, is_played)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                    """,
                    (team_id, row["op_team"], game_id, league_id, match_date, my_score, op_score, is_played),
                )
            self.connection.commit()
            print(f"Match 데이터 {len(data)}개 삽입 완료")
            return True
        except Exception as e:
            print(f"Match 데이터 삽입 오류: {e}")
            self.connection.rollback()
            return False
        finally:
            cur.close()

    def run_all_imports(self):
        """전체 실행"""
        print("=== JSON 데이터를 DB에 삽입 시작 ===")
        if not self.connect():
            return False

        try:
            if self.full_refresh:
                self.clear_domain_tables()

            ok = True
            ok = ok and self.insert_games()
            ok = ok and self.insert_leagues()
            ok = ok and self.insert_teams()
            ok = ok and self.insert_players()
            ok = ok and self.insert_player_performance()
            ok = ok and self.insert_matches()

            try:
                cur = self.connection.cursor()
                cur.execute(
                    "UPDATE game "
                    "SET name = CONVERT(UNHEX('EBB09CEBA19CEB9E80ED8AB8') USING utf8mb4) "
                    "WHERE id = 1"
                )
                self.connection.commit()
                print("게임 이름을 '발로란트'로 강제 업데이트 완료")
                cur.close()
            except Exception as e:
                self.connection.rollback()
                print(f"게임 이름 강제 업데이트 오류: {e}")

            if ok:
                print("\n=== 모든 데이터 삽입이 성공적으로 완료되었습니다! ===")
            else:
                print("\n=== 데이터 삽입 중 일부 오류가 발생했습니다. ===")
            return ok
        finally:
            self.disconnect()

def main():
    db_importer = JsonToDatabase()
    db_importer.run_all_imports()

if __name__ == "__main__":
    main()