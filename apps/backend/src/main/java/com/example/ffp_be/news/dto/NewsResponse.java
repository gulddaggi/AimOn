package com.example.ffp_be.news.dto;

import com.example.ffp_be.news.entity.News;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsResponse {

    private Long id;
    private String title;
    private String content;
    private String link;
    private LocalDateTime publishedAt;


    private String teamName;
    private String gameName;

    public static NewsResponse fromEntity(News news) {
        return NewsResponse.builder()
            .id(news.getId())
            .title(news.getTitle())
            .content(news.getContent())
            .link(news.getLink())
            .publishedAt(news.getPublishedAt())
            .teamName(news.getTeam().getTeamName())   // team FK에서 이름 추출
            .gameName(news.getGame().getName().name())     // game FK에서 이름 추출
            .build();
    }
}
