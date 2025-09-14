package com.example.ffp_be.clip.dto;

import com.example.ffp_be.clip.entity.Clip;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClipResponse {
    private Long id;
    private String videoId;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private String channelTitle;
    private LocalDateTime publishedAt;
    private Long gameId;
    private Long teamId;
    private Long likeCount;
    private Long viewCount;
    private Long userId;       // ✅

    public static ClipResponse fromEntity(Clip clip) {
        return ClipResponse.builder()
            .id(clip.getId())
            .videoId(clip.getVideoId())
            .title(clip.getTitle())
            .description(clip.getDescription())
            .videoUrl(clip.getVideoUrl())
            .thumbnailUrl(clip.getThumbnailUrl())
            .channelTitle(clip.getChannelTitle())
            .publishedAt(clip.getPublishedAt())
            .gameId(clip.getGameId())
            .teamId(clip.getTeamId())
            .likeCount(clip.getLikeCount())
            .viewCount(clip.getViewCount())
            .userId(clip.getUserId())
            .build();
    }
}
