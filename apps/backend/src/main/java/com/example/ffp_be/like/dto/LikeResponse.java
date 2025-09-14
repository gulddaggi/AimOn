package com.example.ffp_be.like.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikeResponse {
    private Long postId;
    private boolean liked;
    private long likeCount;
}
