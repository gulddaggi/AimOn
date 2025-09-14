package com.example.ffp_be.like.service;

import com.example.ffp_be.like.dto.LikeResponse;
import com.example.ffp_be.post.dto.response.PostListResponse;

import java.util.List;

public interface PostLikeService {

    LikeResponse toggleLike(Long userId, Long postId);


    List<PostListResponse> getLikedPosts(Long userId);
}
