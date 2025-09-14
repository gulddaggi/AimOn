import requests
from bs4 import BeautifulSoup
import json


def crawl_player_performance_old():
    result = []
    id_list = json.load(open("DB_JSON/players_id.json", "r", encoding="utf-8"))

    for id in id_list:
        api_url = f"https://vlr.orlandomm.net/api/v1/players/{id}/"
        response = requests.get(api_url)
        data = response.json()
        user = data["data"]["info"]["user"]

        detail_url = f"https://www.vlr.gg/player/{id}/"
        response = requests.get(detail_url)
        soup = BeautifulSoup(response.text, "html.parser")
        
        # 첫 번째 테이블 찾기
        table = soup.find("table")
        if not table:
            print(f"player_id={id}: 테이블을 찾을 수 없습니다.")
            break
        
        # 헤더에서 RND, ACS 열 인덱스 찾기
        header_row = table.find("tr")
        if not header_row:
            print(f"player_id={id}: 헤더 행을 찾을 수 없습니다.")
            break
        
        headers = [th.get_text(strip=True) for th in header_row.find_all(["th", "td"])]
        
        try:
            rnd_index = headers.index("RND")
            acs_index = headers.index("ACS")
            k_index = headers.index("K")
            d_index = headers.index("D")
            a_index = headers.index("A")
        except ValueError:
            print(f"player_id={id}: 필요한 열(RND, ACS, K, D, A)을 찾을 수 없습니다.")
            break
        
        # 데이터 행들 처리
        data_rows = table.find_all("tr")[1:]  # 헤더 제외
        rnd_values = []
        acs_values = []
        k_values = []  # Kill 값들
        d_values = []  # Death 값들
        a_values = []  # Assist 값들
        image_alts = []  # 이미지 alt 값들
        
        for i, row in enumerate(data_rows):
            cells = row.find_all(["td", "th"])
            if len(cells) > max(rnd_index, acs_index, k_index, d_index, a_index):
                # 첫 번째 열의 이미지 alt 값 추출 (상위 3개만)
                if i < 3 and len(cells) > 0:
                    first_cell = cells[0]
                    img = first_cell.find("img")
                    if img:
                        alt_value = img.get("alt", "")
                        image_alts.append(alt_value)
                    else:
                        image_alts.append("이미지 없음")
                
                # RND 값 추출 (정수)
                rnd_text = cells[rnd_index].get_text(strip=True)
                try:
                    rnd_values.append(int(rnd_text))
                except ValueError:
                    print(f"RND 값을 정수로 변환할 수 없음: '{rnd_text}'")
                
                # ACS 값 추출 (실수)
                acs_text = cells[acs_index].get_text(strip=True)
                try:
                    acs_values.append(float(acs_text))
                except ValueError:
                    print(f"ACS 값을 실수로 변환할 수 없음: '{acs_text}'")
                
                # K 값 추출 (정수)
                k_text = cells[k_index].get_text(strip=True)
                try:
                    k_values.append(int(k_text))
                except ValueError:
                    print(f"K 값을 정수로 변환할 수 없음: '{k_text}'")
                
                # D 값 추출 (정수)
                d_text = cells[d_index].get_text(strip=True)
                try:
                    d_values.append(int(d_text))
                except ValueError:
                    print(f"D 값을 정수로 변환할 수 없음: '{d_text}'")
                
                # A 값 추출 (정수)
                a_text = cells[a_index].get_text(strip=True)
                try:
                    a_values.append(int(a_text))
                except ValueError:
                    print(f"A 값을 정수로 변환할 수 없음: '{a_text}'")
        
        # 결과 계산 및 출력
        rnd_sum = sum(rnd_values)
        acs_avg = sum(acs_values) / len(acs_values) if acs_values else 0
        k_sum = sum(k_values)
        d_sum = sum(d_values)
        a_sum = sum(a_values)
        kda_ratio = (k_sum + a_sum) / d_sum if d_sum > 0 else 0

        result.append({
            "player_id": user,
            "round": rnd_sum,
            "acs": acs_avg,
            "kda": kda_ratio,
            "most_playing_agent": image_alts
        })

    # 결과를 JSON 파일로 저장
    with open("DB_JSON/player_performance.json", "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

    print(f"\n총 {len(result)}명의 플레이어 데이터를 player_performance.json에 저장했습니다.")



def crawl_player_performance():
    result = []
    id_list = json.load(open("DB_JSON/players_id.json", "r", encoding="utf-8"))

    total_url = "https://www.vlr.gg/stats"
    response = requests.get(total_url)
    soup = BeautifulSoup(response.text, "html.parser")

    table = soup.find("table")
    if not table:
        print("테이블을 찾을 수 없습니다.")
    header_row = table.find("tr")
    if not header_row:
        print(f"player_id={id}: 헤더 행을 찾을 수 없습니다.")
    
    headers = [th.get_text(strip=True) for th in header_row.find_all(["th", "td"])]
    print(headers)

crawl_player_performance()