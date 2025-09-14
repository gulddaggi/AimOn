package com.example.ffp_be.comment.service;

import com.example.ffp_be.comment.dto.response.CommentResponse;
import com.example.ffp_be.comment.dto.request.CreateCommentRequest;
import com.example.ffp_be.comment.entity.Comment;
import com.example.ffp_be.comment.exception.CommentNotFoundException;
import com.example.ffp_be.comment.exception.NotCommentAuthorException;
import com.example.ffp_be.comment.exception.ParentCommentNotFoundException;
import com.example.ffp_be.comment.repository.CommentRepository;
import com.example.ffp_be.post.entity.PostContentEntity;
import com.example.ffp_be.post.exception.PostNotFoundException;
import com.example.ffp_be.post.repository.PostContentRepository;
import com.example.ffp_be.user.entity.User;
import com.example.ffp_be.user.exception.UserNotFoundException;
import com.example.ffp_be.user.repository.UserRepository;
import com.example.ffp_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostContentRepository postRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public CommentResponse createComment(Long userId, CreateCommentRequest request) {

        PostContentEntity post = postRepository.findById(request.getPostId())
            .orElseThrow(() -> new PostNotFoundException(request.getPostId()));

        User user = userRepository.findById(userId)
            .orElseThrow(UserNotFoundException::new);

        Comment parent = null;
        if (request.getParentCommentId() != null) {
            parent = commentRepository.findById(request.getParentCommentId())
                .orElseThrow(ParentCommentNotFoundException::new);
        }

        Comment comment = Comment.builder()
            .post(post)
            .user(user)
            .parentComment(parent)
            .content(request.getContent())
            .build();

        Comment saved = commentRepository.save(comment);

        post.setCommentCount(post.getCommentCount() + 1);

        userService.addExp(userId, 10);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        List<Comment> commentList = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        List<CommentResponse> responseList = new ArrayList<>();

        for (Comment comment : commentList) {
            CommentResponse response = toResponse(comment);
            responseList.add(response);
        }

        return responseList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByUserId(Long userId) {
        List<Comment> commentList = commentRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<CommentResponse> responseList = new ArrayList<>();

        for (Comment comment : commentList) {
            CommentResponse response = toResponse(comment);
            responseList.add(response);
        }

        return responseList;
    }

    @Override
    public CommentResponse updateComment(Long userId, Long commentId, String newContent) {

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(CommentNotFoundException::new);

        if (!comment.getUser().getId().equals(userId)) {
            throw new NotCommentAuthorException();
        }

        comment.setContent(newContent);

        return toResponse(comment);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(CommentNotFoundException::new);

        if (!comment.getUser().getId().equals(userId)) {
            throw new NotCommentAuthorException();
        }

        commentRepository.delete(comment);
        PostContentEntity post = comment.getPost();
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
    }

    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
            .id(comment.getId())
            .postId(comment.getPost().getId())
            .parentCommentId(
                comment.getParentComment() != null ? comment.getParentComment().getId() : null)
            .content(comment.getContent())
            .authorNickname(comment.getUser().getNickname())
            .authorProfileUrl(comment.getUser().getProfileImageUrl())
            .createdAt(comment.getCreatedAt())
            .build();
    }
}
