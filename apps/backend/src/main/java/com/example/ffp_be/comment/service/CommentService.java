package com.example.ffp_be.comment.service;

import com.example.ffp_be.comment.dto.response.CommentResponse;
import com.example.ffp_be.comment.dto.request.CreateCommentRequest;

import java.util.List;

public interface CommentService {

    CommentResponse createComment(Long userId, CreateCommentRequest request);

    List<CommentResponse> getCommentsByPostId(Long postId);

    List<CommentResponse> getCommentsByUserId(Long userId);

    CommentResponse updateComment(Long userId, Long commentId, String newContent);

    void deleteComment(Long userId, Long commentId);
}
