package com.example.ffp_be.clip.service;

import java.util.List;

public interface ClipKeywordService {
    /**
     * 팀 + 게임 조합 검색어 생성 (개수 제한 + 중복 제거 + 한/영 우선순위)
     * @param teamName 팀명
     * @param gameId   게임 ID (null 가능)
     * @param gameDisplayName 게임 표시명(예: "발로란트", "Valorant")
     * @return 검색 쿼리 리스트
     */
    List<String> buildQueries(String teamName, Long gameId, String gameDisplayName);
}
