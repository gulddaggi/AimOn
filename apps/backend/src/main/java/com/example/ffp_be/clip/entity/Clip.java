package com.example.ffp_be.clip.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "clip",
    indexes = {
        @Index(name = "idx_clip_video_id", columnList = "video_id"),
        @Index(name = "idx_clip_game_published", columnList = "game_id,published_at"),
        @Index(name = "idx_clip_team_published", columnList = "team_id,published_at"),
        @Index(name = "idx_clip_user_created", columnList = "user_id,created_at")
    }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id", nullable = false, length = 50)
    private String videoId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "video_url", nullable = false, length = 255)
    private String videoUrl;

    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    @Column(name = "channel_title", length = 255)
    private String channelTitle;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "team_id")
    private Long teamId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "like_count")
    private Long likeCount;

    @Column(name = "view_count")
    private Long viewCount;

    /** ✅ 등록 유저 */
    @Column(name = "user_id")
    private Long userId;
}
