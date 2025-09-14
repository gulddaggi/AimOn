package com.example.ffp_be.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long postId;
    private Long parentCommentId;
    private String content;
    private String authorNickname;
    private String authorProfileUrl;
    private LocalDateTime createdAt;
}
