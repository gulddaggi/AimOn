package com.example.ffp_be.news.service;

import com.example.ffp_be.news.dto.NewsResponse;
import com.example.ffp_be.news.entity.News;
import com.example.ffp_be.news.repository.NewsRepository;
import com.example.ffp_be.team.entity.Team;
import com.example.ffp_be.team.repository.TeamRepository;
import com.example.ffp_be.game.repository.GameRepository;
import com.example.ffp_be.like.repository.TeamLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final TeamLikeRepository teamLikeRepository;
    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;
    private final NaverNewsClient naverNewsClient;

    /**
     * 모든 팀에 대해 키워드 기반 뉴스 수집 & 저장
     */
    @Override
    public void fetchAndSaveNewsForAllGames() {
        List<Team> teams = teamRepository.findAll();

        for (Team team : teams) {
            String teamName = team.getTeamName();                  // DRX, T1 등
            String gameName = team.getGame().getName().name();     // 예: VALORANT, LOL 등 enum 값
            String keyword = gameName + " " + teamName + " 이스포츠 뉴스";

            Long teamId = team.getId();
            Long gameId = team.getGame().getId();

            naverNewsClient.searchAndSaveNews(keyword, teamId, gameId);
        }
    }

    /**
     * 전체 최신 뉴스 상위 20개 조회
     */
    @Override
    public List<NewsResponse> getLatestNews() {
        List<News> latestNews = newsRepository.findTop20ByOrderByPublishedAtDesc();
        return latestNews.stream()
            .map(NewsResponse::fromEntity)
            .toList();
    }

    /**
     * 특정 팀 ID 리스트에 해당하는 뉴스 최신순 조회
     */
    @Override
    public List<NewsResponse> getNewsByTeamsAndGames(List<Long> teamIds, List<Long> gameIds) {
        List<News> newsList = newsRepository.findByTeam_IdInAndGame_IdInOrderByPublishedAtDesc(teamIds, gameIds);
        return newsList.stream()
            .map(NewsResponse::fromEntity)
            .toList();
    }
}
