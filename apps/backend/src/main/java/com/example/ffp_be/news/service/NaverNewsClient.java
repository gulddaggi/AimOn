package com.example.ffp_be.news.service;

import com.example.ffp_be.news.entity.News;
import java.util.List;

/**
 * 네이버 뉴스 API 클라이언트 인터페이스
 * 키워드로 뉴스 검색 후 DB에 저장
 */
public interface NaverNewsClient {



    List<News> searchAndSaveNews(String keyword, Long teamId, Long gameId);
}
