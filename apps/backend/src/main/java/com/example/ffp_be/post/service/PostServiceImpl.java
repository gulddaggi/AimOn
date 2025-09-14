package com.example.ffp_be.post.service;

import com.example.ffp_be.post.dto.request.UpdatePostRequest;
import com.example.ffp_be.post.dto.request.CreatePostRequest;
import com.example.ffp_be.post.dto.response.EditPostResponse;
import com.example.ffp_be.post.dto.response.PostDetailResponse;
import com.example.ffp_be.post.dto.response.PostListResponse;
import com.example.ffp_be.post.entity.PostContentEntity;
import com.example.ffp_be.post.repository.PostContentRepository;
import com.example.ffp_be.user.entity.User;
import com.example.ffp_be.user.repository.UserRepository;
import com.example.ffp_be.user.service.UserService;
import com.example.ffp_be.post.exception.PostNotFoundException;
import com.example.ffp_be.post.exception.NotPostAuthorException;
import com.example.ffp_be.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostContentRepository postRepo;
    private final UserRepository userRepo;
    private final UserService userService;

    @Override
    public PostDetailResponse createPost(Long authorId, CreatePostRequest request) {
        User user = userRepo.findById(authorId)
            .orElseThrow(UserNotFoundException::new);

        PostContentEntity post = PostContentEntity.builder()
            .user(user)
            .title(request.getTitle())
            .body(request.getBody())
            .imageKeys(request.getImageKeys())
            .build();
        PostContentEntity saved = postRepo.save(post);

        userService.addExp(authorId, 10);

        return mapToDetail(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostListResponse> listPosts(int page, int size) {
        return postRepo.findAllWithUser(PageRequest.of(page, size))
            .getContent().stream()
            .map(this::toListResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailResponse getPost(Long postId) {
        PostContentEntity post = postRepo.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));
        return mapToDetail(post);
    }

    @Override
    public PostDetailResponse updatePost(Long authorId, Long postId, UpdatePostRequest request) {
        PostContentEntity post = postRepo.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));

        if (!post.getUser().getId().equals(authorId)) {
            throw new NotPostAuthorException();
        }
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getBody() != null) {
            post.setBody(request.getBody());
        }
        if (request.getImageKeys() != null) {
            post.setImageKeys(request.getImageKeys());
        }

        PostContentEntity updated = postRepo.save(post);
        return mapToDetail(updated);
    }

    @Override
    public void deletePost(Long authorId, Long postId) {
        PostContentEntity post = postRepo.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));

        if (!post.getUser().getId().equals(authorId)) {
            throw new NotPostAuthorException();
        }
        postRepo.deleteById(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public EditPostResponse getPostForEdit(Long authorId, Long postId) {
        PostContentEntity post = postRepo.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));

        if (!post.getUser().getId().equals(authorId)) {
            throw new NotPostAuthorException();
        }
        return EditPostResponse.builder()
            .postId(post.getId())
            .title(post.getTitle())
            .body(post.getBody())
            .build();
    }

    private PostDetailResponse mapToDetail(PostContentEntity post) {
        return PostDetailResponse.builder()
            .postId(post.getId())
            .authorId(post.getUser().getId())
            .authorNickname(post.getUser().getNickname())
            .title(post.getTitle())
            .body(post.getBody())
            .createdAt(post.getCreatedAt())
            .likeCount(post.getLikeCount())
            .commentCount(post.getCommentCount())
            // 조회 시에는 그대로 key를 내려주고, 클라이언트가 필요 시 /s3/posts/download-url로 URL 발급
            .images(post.getImageKeys())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostListResponse> listRecentPosts(int count) {
        return postRepo.findAllWithUser(
                PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "createdAt"))
            )
            .getContent().stream()
            .map(this::toListResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostListResponse> searchPostsByTitle(String keyword) {
        return postRepo.findByTitleContainingIgnoreCaseWithUser(
                keyword,
                Sort.by(Sort.Direction.DESC, "createdAt")
            ).stream()
            .map(this::toListResponse)
            .collect(Collectors.toList());
    }

    private PostListResponse toListResponse(PostContentEntity p) {
        return PostListResponse.builder()
            .postId(p.getId())
            .authorId(p.getUser().getId())
            .authorNickname(p.getUser().getNickname())
            .title(p.getTitle())
            .createdAt(p.getCreatedAt())
            .likeCount(p.getLikeCount())
            .commentCount(p.getCommentCount())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostListResponse> listAllPosts() {
        return postRepo.findAllWithUser(Sort.by(Sort.Direction.DESC, "createdAt"))
            .stream()
            .map(this::toListResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostListResponse> listMyPosts(Long userId) {
        return postRepo.findAllByUserId(userId, Sort.by(Sort.Direction.DESC, "createdAt"))
            .stream()
            .map(this::toListResponse)
            .collect(Collectors.toList());
    }
}
