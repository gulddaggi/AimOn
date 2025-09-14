package com.example.ffp_be.recommend.domain;

import java.util.Arrays;

public enum PickKeyword {
    TOP_TIER("상위권", "리그 랭킹 기준 상위권 팀"),
    HIGH_WIN_RATE("높은 승률", "전체 경기 승률이 높은 팀"),
    ATK_STRONG("공격 라운드 강자", "공격 라운드 승률이 높은 팀"),
    DEF_STRONG("수비 라운드 강자", "수비 라운드 승률이 높은 팀"),
    TEAMPLAY_APR("팀플레이", "평균 APR이 높은 팀"),
    CONSISTENT_KAST("꾸준함", "평균 KAST 비율이 높은 팀"),
    FIRST_KILL("기선제압(첫 킬)", "평균 라운드 첫 킬 비율(FKPR)이 높은 팀"),
    HEADSHOT("헤드샷", "평균 헤드샷률이 높은 팀"),
    ACE_PLAYERS("에이스 보유", "평균 전투 점수(ACS)가 높은 선수를 많이 보유한 팀"),
    CLUTCH("클러치 달인", "평균 클러치 성공률이 높은 팀"),
    FIREPOWER_ADR("화력", "평균 ADR이 높은 팀"),
    CHALLENGE("도전", "리그 랭킹 기준 하위권 팀"),
    SAFETY("안전", "라운드 첫 사망 비율(FDPR)이 낮은 팀"),
    BALANCED("균형적인", "공격/수비 라운드 승률 차이가 적은 팀"),
    DIVERSE_COMPS("다양한 조합", "사용하는 에이전트 조합 종류가 많은 팀");

    private final String label;
    private final String description;

    PickKeyword(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public static PickKeyword fromKey(String key) {
        return Arrays.stream(values())
            .filter(k -> k.name().equalsIgnoreCase(key))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown keyword: " + key));
    }
}


