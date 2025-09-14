package com.example.ffp_be.news.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsListResponse {

    private Long id;
    private String title;
    private String content;
    private String link;
    private LocalDateTime publishedAt;

    private String teamName;
    private String gameName;
}
