package com.example.ffp_be.news.repository;

import com.example.ffp_be.news.entity.News;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {

    // 중복 확인용: 링크 기준
    Optional<News> findByLink(String link);

    // 선호 팀 뉴스 최신순 조회
    List<News> findByTeam_IdInAndGame_IdInOrderByPublishedAtDesc(List<Long> teamIds, List<Long> gameIds);

    // (선택) 전체 최신 뉴스 상위 20개
    List<News> findTop20ByOrderByPublishedAtDesc();
}
