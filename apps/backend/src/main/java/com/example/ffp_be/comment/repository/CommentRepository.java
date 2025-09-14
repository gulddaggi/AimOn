package com.example.ffp_be.comment.repository;

import com.example.ffp_be.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);
}
