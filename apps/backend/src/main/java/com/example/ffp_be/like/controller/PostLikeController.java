package com.example.ffp_be.like.controller;

import com.example.ffp_be.auth.security.CustomUserDetails;
import com.example.ffp_be.like.dto.LikeResponse;
import com.example.ffp_be.like.service.PostLikeService;
import com.example.ffp_be.post.dto.response.PostListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@Tag(name = "Like - Post", description = "선호 게시글 관리 API")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/posts/{postId}")
    @Operation(summary = "게시글 좋아요 토글")
    public ResponseEntity<LikeResponse> toggleLikeByPath(
        @AuthenticationPrincipal CustomUserDetails principal,
        @PathVariable Long postId
    ) {
        if (principal == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        Long userId = principal.getUserId();
        LikeResponse response = postLikeService.toggleLike(userId, postId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/myposts")
    @Operation(summary = "내가 선호한 게시글 목록")
    public ResponseEntity<List<PostListResponse>> getMyLikedPosts(
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        Long userId = principal.getUserId();
        List<PostListResponse> likedPosts = postLikeService.getLikedPosts(userId);
        return ResponseEntity.ok(likedPosts);
    }
}
