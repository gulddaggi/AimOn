package com.example.ffp_be.recommend.service;

import com.example.ffp_be.game.repository.GameRepository;
import com.example.ffp_be.recommend.domain.PickKeyword;
import com.example.ffp_be.recommend.dto.response.PickAndAimKeywordMetaResponse;
import com.example.ffp_be.recommend.repository.PickKeywordMetaRepository;
import com.example.ffp_be.recommend.exception.InvalidKeywordException;
import com.example.ffp_be.recommend.exception.TooManyKeywordsException;
import com.example.ffp_be.league.entity.League;
import com.example.ffp_be.league.repository.LeagueRepository;
import com.example.ffp_be.player.entity.PlayerEntity;
import com.example.ffp_be.player.repository.PlayerRepository;
import com.example.ffp_be.player.stats.valorant.entity.ValorantPlayerStatsEntity;
import com.example.ffp_be.player.stats.valorant.repository.ValorantPlayerStatsRepository;
import com.example.ffp_be.recommend.dto.response.PickAndAimCandidateResponse;
import com.example.ffp_be.recommend.dto.request.PickAndAimFilterRequest;
import com.example.ffp_be.recommend.dto.response.PickAndAimLeagueResponse;
import com.example.ffp_be.team.entity.Team;
import com.example.ffp_be.team.repository.TeamRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PickAndAimServiceImpl implements PickAndAimService {

    private final GameRepository gameRepository;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final ValorantPlayerStatsRepository valorantStatsRepository;
    private final PickKeywordMetaRepository keywordMetaRepository;

    @Override
    public List<PickAndAimLeagueResponse> getSupportedLeagues() {
        return leagueRepository.findAll().stream()
            .map(l -> PickAndAimLeagueResponse.builder()
                .gameId(l.getGame().getId())
                .gameName(l.getGame().getName().name())
                .leagueId(l.getId())
                .leagueName(l.getName())
                .build())
            .toList();
    }

    @Override
    public List<PickAndAimCandidateResponse> getCandidates(PickAndAimFilterRequest request) {
        Long gameId = request.getGameId();
        Long leagueId = request.getLeagueId();
        List<String> keywords = request.getKeywords();

        if (keywords != null && keywords.size() > 3) {
            throw new TooManyKeywordsException(keywords.size());
        }

        List<Team> teams;
        if (leagueId != null) {
            teams = teamRepository.findByLeague_Id(leagueId);
        } else {
            teams = teamRepository.findAllByGame(gameRepository.findById(gameId)
                .orElseThrow());
        }

        if (teams.isEmpty()) {
            return List.of();
        }

        Map<Long, Double> teamToScore = new HashMap<>();

        Map<Long, Team> teamMap = teams.stream()
            .collect(Collectors.toMap(Team::getId, t -> t));

        List<Long> teamIds = teams.stream().map(Team::getId).toList();
        List<PlayerEntity> players = playerRepository.findByTeamIdIn(teamIds);
        Map<Long, List<PlayerEntity>> teamToPlayers = players.stream()
            .collect(Collectors.groupingBy(PlayerEntity::getTeamId));

        Map<Long, ValorantPlayerStatsEntity> statsByPlayer = players.stream()
            .map(p -> valorantStatsRepository.findByPlayer_Id(p.getId()).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(ValorantPlayerStatsEntity::getPlayerId, s -> s));

        for (String keyword : keywords) {
            PickKeyword k;
            try {
                k = PickKeyword.fromKey(keyword);
            } catch (IllegalArgumentException e) {
                throw new InvalidKeywordException(keyword);
            }
            switch (k) {
                case TOP_TIER -> rankScore(teamMap, teamToScore,
                    Comparator.comparing(Team::getPoint).reversed(),
                    new int[]{12, 10, 8, 6, 4, 2});
                case HIGH_WIN_RATE -> rankScore(teamMap, teamToScore,
                    Comparator.comparing((Team t) -> t.getWinRate() == null ? 0.0 : t.getWinRate())
                        .reversed(),
                    new int[]{12, 10, 8, 6, 4, 2});
                case ATK_STRONG -> rankScore(teamMap, teamToScore,
                    Comparator.comparing(
                            (Team t) -> t.getAttackWinRate() == null ? 0.0 : t.getAttackWinRate())
                        .reversed(),
                    new int[]{12, 10, 8, 6, 4, 2});
                case DEF_STRONG -> rankScore(teamMap, teamToScore,
                    Comparator.comparing(
                            (Team t) -> t.getDefenseWinRate() == null ? 0.0 : t.getDefenseWinRate())
                        .reversed(),
                    new int[]{12, 10, 8, 6, 4, 2});
                case TEAMPLAY_APR -> averageStatScore(teamToPlayers, statsByPlayer, teamToScore,
                    ValorantPlayerStatsEntity::getApr);
                case CONSISTENT_KAST -> averageStatScore(teamToPlayers, statsByPlayer, teamToScore,
                    ValorantPlayerStatsEntity::getKast);
                case FIRST_KILL -> averageStatScore(teamToPlayers, statsByPlayer, teamToScore,
                    ValorantPlayerStatsEntity::getFkpr);
                case HEADSHOT -> averageStatScore(teamToPlayers, statsByPlayer, teamToScore,
                    ValorantPlayerStatsEntity::getHs);
                case ACE_PLAYERS -> acsTopNBonus(players, statsByPlayer, teamToScore, 10);
                case CLUTCH -> averageStatScore(teamToPlayers, statsByPlayer, teamToScore,
                    ValorantPlayerStatsEntity::getCl);
                case FIREPOWER_ADR -> averageStatScore(teamToPlayers, statsByPlayer, teamToScore,
                    ValorantPlayerStatsEntity::getAdr);
                case CHALLENGE -> challengeBonus(teamMap, teamToScore);
                case SAFETY -> inverseAverageStatScore(teamToPlayers, statsByPlayer, teamToScore,
                    ValorantPlayerStatsEntity::getFdpr);
                case BALANCED -> balanceScore(teamMap, teamToScore);
                default -> {
                }
            }
        }

        List<Map.Entry<Long, Double>> ordered = teamToScore.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(3)
            .toList();

        // 리그별 등수 캐시
        Map<Long, Map<Long, Integer>> leagueRankCache = new HashMap<>();

        List<PickAndAimCandidateResponse> result = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++) {
            Map.Entry<Long, Double> e = ordered.get(i);
            Double score = e.getValue();
            Team t = teamMap.get(e.getKey());
            League l = t.getLeague();

            Map<Long, Integer> ranks = leagueRankCache.computeIfAbsent(l.getId(), this::computeLeagueRanks);
            Integer leagueRank = ranks.getOrDefault(t.getId(), 0);

            result.add(PickAndAimCandidateResponse.builder()
                .teamId(t.getId())
                .teamName(t.getTeamName())
                .leagueId(l.getId())
                .leagueName(l.getName())
                .totalScore(score)
                .rank(leagueRank)
                .build());
        }
        return result;
    }

    @Override
    public List<PickAndAimKeywordMetaResponse> getActiveKeywords() {
        return keywordMetaRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc()
            .stream()
            .map(m -> PickAndAimKeywordMetaResponse.of(m.getKeywordKey(), m.getDisplayName(),
                m.getDescription()))
            .toList();
    }

    private void rankScore(Map<Long, Team> teamMap, Map<Long, Double> teamToScore,
        Comparator<Team> comparator, int[] buckets) {
        List<Team> ordered = new ArrayList<>(teamMap.values());
        ordered.sort(comparator);
        int idx = 0;
        for (Team t : ordered) {
            int bucketIdx = Math.min(idx, buckets.length - 1);
            teamToScore.merge(t.getId(), (double) buckets[bucketIdx], Double::sum);
            idx++;
        }
    }

    private interface DoubleGetter {

        double get(ValorantPlayerStatsEntity s);
    }

    private void averageStatScore(
        Map<Long, List<PlayerEntity>> teamToPlayers,
        Map<Long, ValorantPlayerStatsEntity> statsByPlayer,
        Map<Long, Double> teamToScore,
        DoubleGetter getter
    ) {
        Map<Long, Double> averages = new HashMap<>();
        teamToPlayers.forEach((teamId, plist) -> {
            double avg = plist.stream()
                .map(p -> statsByPlayer.get(p.getId()))
                .filter(Objects::nonNull)
                .mapToDouble(getter::get)
                .average().orElse(0.0);
            averages.put(teamId, avg);
        });
        applyRankBuckets(averages, teamToScore, new int[]{12, 10, 8, 6, 4, 2});
    }

    private void inverseAverageStatScore(
        Map<Long, List<PlayerEntity>> teamToPlayers,
        Map<Long, ValorantPlayerStatsEntity> statsByPlayer,
        Map<Long, Double> teamToScore,
        DoubleGetter getter
    ) {
        Map<Long, Double> averages = new HashMap<>();
        teamToPlayers.forEach((teamId, plist) -> {
            double avg = plist.stream()
                .map(p -> statsByPlayer.get(p.getId()))
                .filter(Objects::nonNull)
                .mapToDouble(getter::get)
                .average().orElse(Double.MAX_VALUE);
            averages.put(teamId, avg);
        });
        applyRankBuckets(averages, teamToScore, new int[]{12, 10, 8, 6, 4, 2}, true);
    }

    private void applyRankBuckets(Map<Long, Double> valueByTeam, Map<Long, Double> score,
        int[] buckets) {
        applyRankBuckets(valueByTeam, score, buckets, false);
    }

    private void applyRankBuckets(Map<Long, Double> valueByTeam, Map<Long, Double> score,
        int[] buckets, boolean ascending) {
        List<Map.Entry<Long, Double>> ordered = new ArrayList<>(valueByTeam.entrySet());
        ordered.sort(Map.Entry.comparingByValue());
        if (!ascending) {
            Collections.reverse(ordered);
        }
        int idx = 0;
        for (Map.Entry<Long, Double> e : ordered) {
            int bucketIdx = Math.min(idx, buckets.length - 1);
            score.merge(e.getKey(), (double) buckets[bucketIdx], Double::sum);
            idx++;
        }
    }

    // 리그 내 팀들의 포인트 기반 순위를 계산(동점은 같은 순위, 다음 순위는 건너뜀)
    private Map<Long, Integer> computeLeagueRanks(Long leagueId) {
        List<Team> leagueTeams = teamRepository.findByLeague_Id(leagueId);
        leagueTeams.sort((a, b) -> Integer.compare(
            b.getPoint() != null ? b.getPoint() : 0,
            a.getPoint() != null ? a.getPoint() : 0
        ));

        Map<Long, Integer> result = new HashMap<>();
        int currentRank = 0;
        Integer lastPoint = null;
        int index = 0;
        for (Team t : leagueTeams) {
            int point = t.getPoint() != null ? t.getPoint() : 0;
            if (lastPoint == null || point != lastPoint) {
                currentRank = index + 1;
                lastPoint = point;
            }
            index++;
            result.put(t.getId(), currentRank);
        }
        return result;
    }

    private void acsTopNBonus(List<PlayerEntity> players,
        Map<Long, ValorantPlayerStatsEntity> statsByPlayer,
        Map<Long, Double> score, int topN) {
        List<ValorantPlayerStatsEntity> ordered = players.stream()
            .map(p -> statsByPlayer.get(p.getId()))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingDouble(ValorantPlayerStatsEntity::getAcs).reversed())
            .limit(topN)
            .toList();
        double base = 30.0;
        double step = 2.0;
        for (int i = 0; i < ordered.size(); i++) {
            ValorantPlayerStatsEntity s = ordered.get(i);
            Long playerId = s.getPlayerId();
            PlayerEntity p = players.stream().filter(pp -> Objects.equals(pp.getId(), playerId))
                .findFirst().orElse(null);
            if (p == null) {
                continue;
            }
            Long teamId = p.getTeamId();
            if (teamId == null) {
                continue;
            }
            score.merge(teamId, base - step * i, Double::sum);
        }
    }

    private void challengeBonus(Map<Long, Team> teamMap, Map<Long, Double> score) {
        List<Team> ordered = new ArrayList<>(teamMap.values());
        ordered.sort(Comparator.comparing(Team::getPoint));
        int[] bonuses = new int[]{40, 35, 30, 0, 0, 0};
        int idx = 0;
        for (Team t : ordered) {
            int bucketIdx = Math.min(idx, bonuses.length - 1);
            score.merge(t.getId(), (double) bonuses[bucketIdx], Double::sum);
            idx++;
        }
    }

    private void balanceScore(Map<Long, Team> teamMap, Map<Long, Double> score) {
        Map<Long, Double> diff = teamMap.values().stream().collect(Collectors.toMap(
            Team::getId,
            t -> {
                double atk = t.getAttackWinRate() == null ? 0.0 : t.getAttackWinRate();
                double def = t.getDefenseWinRate() == null ? 0.0 : t.getDefenseWinRate();
                return Math.abs(atk - def);
            }
        ));
        applyRankBuckets(diff, score, new int[]{12, 10, 8, 6, 4, 2}, true);
    }
}


