package com.example.ffp_be.like.service;

import com.example.ffp_be.like.dto.LikeResponse;
import com.example.ffp_be.like.entity.PostLikeEntity;
import com.example.ffp_be.like.repository.PostLikeRepository;
import com.example.ffp_be.like.exception.ResourceNotFoundException;
import com.example.ffp_be.post.dto.response.PostListResponse;
import com.example.ffp_be.post.entity.PostContentEntity;
import com.example.ffp_be.post.repository.PostContentRepository;
import com.example.ffp_be.user.entity.User;
import com.example.ffp_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostContentRepository postContentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LikeResponse toggleLike(Long userId, Long postId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("사용자 없음"));

        PostContentEntity post = postContentRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("게시글 없음"));

        boolean alreadyLiked = postLikeRepository.existsByUserAndPost(user, post);

        if (alreadyLiked) {
            postLikeRepository.deleteByUserAndPost(user, post);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        } else {
            PostLikeEntity like = PostLikeEntity.builder()
                .user(user)
                .post(post)
                .build();
            postLikeRepository.save(like);

            post.setLikeCount(post.getLikeCount() + 1);
        }
        postContentRepository.save(post);

        long likeCount = post.getLikeCount();

        return LikeResponse.builder()
            .postId(post.getId())
            .liked(!alreadyLiked)
            .likeCount(likeCount)
            .build();
    }

    @Override
    public List<PostListResponse> getLikedPosts(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("사용자 없음"));

        List<PostLikeEntity> likeList = postLikeRepository.findAllByUser(user);
        List<PostListResponse> result = new ArrayList<>();

        for (PostLikeEntity like : likeList) {
            PostContentEntity post = like.getPost();

            PostListResponse dto = PostListResponse.builder()
                .postId(post.getId())
                .authorId(post.getUser().getId())
                .authorNickname(post.getUser().getNickname())
                .title(post.getTitle())
                .createdAt(post.getCreatedAt())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .build();

            result.add(dto);
        }

        return result;
    }
}
