package com.example.ffp_be.news.service;

import com.example.ffp_be.news.dto.NewsResponse;
import java.util.List;

public interface NewsService {
    void fetchAndSaveNewsForAllGames();

    List<NewsResponse> getLatestNews();

    List<NewsResponse> getNewsByTeamsAndGames(List<Long> teamIds, List<Long> gameIds);
}
