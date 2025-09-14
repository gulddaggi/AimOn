package com.example.ffp_be.comment.controller;

import com.example.ffp_be.auth.security.CustomUserDetails;
import com.example.ffp_be.comment.dto.response.CommentResponse;
import com.example.ffp_be.comment.dto.request.CreateCommentRequest;
import com.example.ffp_be.comment.service.CommentService;
import com.example.ffp_be.comment.dto.request.UpdateCommentRequest;
import com.example.ffp_be.user.exception.AuthenticationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Validated
@Tag(name = "Comment", description = "댓글 관리 API")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "댓글 작성")
    public ResponseEntity<CommentResponse> createComment(
        @AuthenticationPrincipal CustomUserDetails principal,
        @RequestBody CreateCommentRequest request
    ) {
        if (principal == null) {
            throw new AuthenticationException();
        }
        Long userId = principal.getUserId();
        CommentResponse response = commentService.createComment(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "게시글 별 댓글 조회")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable Long postId) {
        List<CommentResponse> responseList = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/my")
    @Operation(summary = "내가 작성한 댓글 조회")
    public ResponseEntity<List<CommentResponse>> getMyComments(
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            throw new AuthenticationException();
        }
        Long userId = principal.getUserId();
        List<CommentResponse> responseList = commentService.getCommentsByUserId(userId);
        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정")
    public ResponseEntity<CommentResponse> updateComment(
        @AuthenticationPrincipal CustomUserDetails principal,
        @PathVariable Long commentId,
        @RequestBody UpdateCommentRequest request
    ) {
        if (principal == null) {
            throw new AuthenticationException();
        }
        Long userId = principal.getUserId();
        CommentResponse updated = commentService.updateComment(userId, commentId, request.getContent());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제")
    public ResponseEntity<Void> deleteComment(
        @AuthenticationPrincipal CustomUserDetails principal,
        @PathVariable Long commentId
    ) {
        if (principal == null) {
            throw new AuthenticationException();
        }
        Long userId = principal.getUserId();
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}
