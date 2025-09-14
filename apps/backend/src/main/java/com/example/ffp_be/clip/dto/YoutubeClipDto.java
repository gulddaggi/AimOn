package com.example.ffp_be.clip.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class YoutubeClipDto {
    private String videoId;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private String channelTitle;
    private LocalDateTime publishedAt;
    private Long likeCount;   // ✅ 추가
    private Long viewCount;   // ✅ 추가
}
