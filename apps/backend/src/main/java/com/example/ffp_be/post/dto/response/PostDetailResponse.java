package com.example.ffp_be.post.dto.response;

import lombok.*;
import java.util.List;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDetailResponse {

    private Long postId;
    private Long authorId;
    private String authorNickname;
    private String title;
    private String body;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer commentCount;
    private List<String> images;
}