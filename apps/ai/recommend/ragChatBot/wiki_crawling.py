import wikipediaapi
import json

def extract_sections_recursive(sections, result_list, parent_titles=None):
    """
    재귀적으로 모든 섹션을 탐색하여 title과 text를 추출하는 함수
    parent_titles: 상위 섹션들의 제목들을 누적하는 리스트
    """
    if parent_titles is None:
        parent_titles = []
    
    for section in sections:
        # 현재 섹션의 하위 섹션들을 확인
        subsections = section.sections
        
        # 현재 섹션의 제목을 상위 제목 리스트에 추가
        current_titles = parent_titles + [section.title] if section.title else parent_titles
        
        if not subsections:  # 빈 리스트인 경우 (하위 섹션이 없는 경우)
            # title과 text가 있는 경우만 추가
            if section.title and section.text.strip():
                # 계층 구조를 반영한 제목 생성 (예: "발로란트-요원-타격대")
                hierarchical_title = "-".join(current_titles)
                section_data = {
                    "title": hierarchical_title,
                    "content": section.text.strip()
                }
                result_list.append(section_data)
        else:  # 하위 섹션이 있는 경우 재귀 호출
            extract_sections_recursive(subsections, result_list, current_titles)

e_wiki = wikipediaapi.Wikipedia(
    user_agent='valrorant_community/1.0',
    language='en',
    extract_format=wikipediaapi.ExtractFormat.WIKI,
)

k_wiki = wikipediaapi.Wikipedia(
    user_agent='valrorant_community/1.0',
    language='ko',
    extract_format=wikipediaapi.ExtractFormat.WIKI,
)

# page_py = e_wiki.page('Valorant')
k_list = ['발로란트', '발로란트 챔피언스 투어', '발로란트 챔피언스', '발로란트 챔피언스 투어 마스터스', '2025 e스포츠 월드컵', '1인칭 슈팅 게임']
e_list = ['Valorant', 'Valorant Champions Tour', 'Fnatic']



for word in k_list:
    page_py = k_wiki.page(word)
# 전처리 실행
    processed_data = [{"title": page_py.title, "content": page_py.text.strip()}]
    extract_sections_recursive(page_py.sections, processed_data, [page_py.title])

    # 결과 출력
    print(f"총 {len(processed_data)}개의 섹션을 추출했습니다.")
    for i, item in enumerate(processed_data):
        print(f"\n{i+1}. {item['title']}")
        print(f"내용: {item['content'][:100]}...")  # 처음 100자만 출력

    # JSONL 파일로 저장 (append 모드로 변경하면 데이터가 쌓임)
    with open('wiki_processed_data.jsonl', 'a', encoding='utf-8') as f:
        for item in processed_data:
            json.dump(item, f, ensure_ascii=False)
            f.write('\n')

    print(f"\n전처리된 데이터가 'wiki_processed_data.jsonl' 파일로 저장되었습니다.")