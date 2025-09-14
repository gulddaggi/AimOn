package com.example.ffp_be.clip.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class ClipKeywordServiceImpl implements ClipKeywordService {

    @Value("${youtube.ingest.maxQueriesPerTeam:2}")
    private int maxQueriesPerTeam;

    @Value("${youtube.ingest.includeKorean:true}")
    private boolean includeKorean;

    @Value("${youtube.ingest.includeEnglish:true}")
    private boolean includeEnglish;

    private static final Pattern HANGUL = Pattern.compile("[가-힣]");

    /** 팀 + 게임 조합 검색어 생성 (개수 제한 + 중복 제거 + 한/영 우선순위) */
    @Override
    public List<String> buildQueries(String teamName, Long gameId, String gameDisplayName) {
        String t = safe(teamName);
        if (t.isBlank()) return List.of();

        // ✅ 팀당 쿼리 “항상 1개”로 강제
        final int limit = 1; // Math.min(maxQueriesPerTeam, 1) 과 동일 효과

        List<String> gameNames = gameNameVariants(gameId, gameDisplayName);
        boolean teamIsKorean = HANGUL.matcher(t).find();

        LinkedHashSet<String> queries = new LinkedHashSet<>();

        // 1) 한국어 우선
        if (includeKorean && (teamIsKorean || !includeEnglish)) {
            for (String g : gameNames) {
                if (isKorean(g)) {
                    queries.add((t + " " + g + " 하이라이트").trim());
                    if (queries.size() >= limit) return firstN(queries, limit);
                }
            }
        }

        // 2) 영어
        if (includeEnglish) {
            for (String g : gameNames) {
                if (!isKorean(g)) {
                    queries.add((t + " " + g + " highlight").trim());
                    if (queries.size() >= limit) return firstN(queries, limit);
                }
            }
        }

        // 3) 보조 형태 (실제로는 limit=1이라 여기 도달하기 어려움)
        if (includeKorean && teamIsKorean) {
            for (String g : gameNames) {
                if (isKorean(g)) {
                    queries.add((t + " " + g + " 클립").trim());
                    if (queries.size() >= limit) return firstN(queries, limit);
                }
            }
        }
        if (includeEnglish) {
            for (String g : gameNames) {
                if (!isKorean(g)) {
                    queries.add((t + " " + g + " highlights").trim());
                    if (queries.size() >= limit) return firstN(queries, limit);
                }
            }
        }

        return firstN(queries, limit);
    }

    /** 게임ID/표시명 → 한/영 후보 (중복 제거) */
    private List<String> gameNameVariants(Long gameId, String gameDisplayName) {
        LinkedHashSet<String> base = new LinkedHashSet<>();
        String disp = safe(gameDisplayName);
        if (!disp.isBlank()) base.add(disp);

        if (gameId != null) {
            switch (gameId.intValue()) {
                case 1 -> { base.add("발로란트"); base.add("Valorant"); }
                case 2 -> { base.add("오버워치"); base.add("Overwatch"); }
                default -> { base.add("게임"); base.add("Game"); }
            }
        } else {
            base.add("게임"); base.add("Game");
        }

        List<String> out = new ArrayList<>();
        for (String g : base) {
            if (isKorean(g) && includeKorean) out.add(g);
            if (!isKorean(g) && includeEnglish) out.add(g);
        }
        if (out.isEmpty()) out.add("Game");
        return out;
    }

    private boolean isKorean(String s) {
        return s != null && HANGUL.matcher(s).find();
    }
    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
    private List<String> firstN(Set<String> set, int n) {
        if (set.isEmpty()) return List.of();
        List<String> list = new ArrayList<>(set);
        return list.size() <= n ? list : list.subList(0, n);
    }
}
