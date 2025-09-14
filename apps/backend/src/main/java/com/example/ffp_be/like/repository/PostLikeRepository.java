package com.example.ffp_be.like.repository;

import com.example.ffp_be.like.entity.PostLikeEntity;
import com.example.ffp_be.post.entity.PostContentEntity;
import com.example.ffp_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLikeEntity, Long> {

    Optional<PostLikeEntity> findByUserAndPost(User user, PostContentEntity post);

    boolean existsByUserAndPost(User user, PostContentEntity post);

    void deleteByUserAndPost(User user, PostContentEntity post);

    long countByPost(PostContentEntity post);

    List<PostLikeEntity> findAllByUser(User user);

}
