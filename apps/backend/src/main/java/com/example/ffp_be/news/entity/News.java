package com.example.ffp_be.news.entity;

import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관 관계: 팀
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // 연관 관계: 게임
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    // 뉴스 제목 (HTML 제거 예정)
    @Column(nullable = false, length = 255)
    private String title;

    // 요약 내용 (HTML 제거 예정)
    @Column(columnDefinition = "TEXT")
    private String content;

    // 원문 링크 (중복 체크 기준)
    @Column(nullable = false, unique = true, length = 1000)
    private String link;

    // 발행일 (pubDate)
    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;
}
