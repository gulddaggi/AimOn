package com.example.ffp_be.post.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostListResponse {

    private Long postId;
    private Long authorId;
    private String authorNickname;
    private String title;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer commentCount;
}