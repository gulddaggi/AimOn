package com.example.ffp_be.post.controller;

import com.example.ffp_be.auth.security.CustomUserDetails;
import com.example.ffp_be.post.dto.request.UpdatePostRequest;
import com.example.ffp_be.post.dto.request.CreatePostRequest;
import com.example.ffp_be.post.dto.response.EditPostResponse;
import com.example.ffp_be.post.dto.response.PostDetailResponse;
import com.example.ffp_be.post.dto.response.PostListResponse;
import com.example.ffp_be.post.service.PostService;
import com.example.ffp_be.user.exception.AuthenticationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Validated
@Tag(name = "Post", description = "게시글 관리 API")
public class PostController {

    private final PostService postService;


    @PostMapping
    @Operation(summary = "게시글 등록")
    public ResponseEntity<PostDetailResponse> createPost(
        @AuthenticationPrincipal CustomUserDetails principal,
        @RequestBody @Valid CreatePostRequest request
    ) {
        if (principal == null) {
            throw new AuthenticationException();
        }
        Long userId = principal.getUserId();
        PostDetailResponse response = postService.createPost(userId, request);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    @Operation(summary = "게시글 목록 조회")
    public ResponseEntity<List<PostListResponse>> listPosts() {
        List<PostListResponse> list = postService.listAllPosts();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/my")
    @Operation(summary = "내가 작성한 게시글 목록")
    public ResponseEntity<List<PostListResponse>> listMyPosts(
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            throw new AuthenticationException();
        }
        Long userId = principal.getUserId();
        List<PostListResponse> list = postService.listMyPosts(userId);
        return ResponseEntity.ok(list);
    }


    @GetMapping("/recent")
    @Operation(summary = "최근 게시글 N건 조회")
    public ResponseEntity<List<PostListResponse>> listRecentPosts(
        @RequestParam(defaultValue = "5") int count
    ) {
        List<PostListResponse> list = postService.listRecentPosts(count);
        return ResponseEntity.ok(list);
    }


    @GetMapping("/search")
    @Operation(summary = "제목으로 게시글 검색")
    public ResponseEntity<List<PostListResponse>> searchPostsByTitle(
        @RequestParam("title")
        @NotBlank(message = "검색어를 입력해주세요.")
        @Size(max = 200, message = "검색어는 최대 200자까지 입력 가능합니다.")
        String keyword
    ) {
        List<PostListResponse> list = postService.searchPostsByTitle(keyword.trim());
        return ResponseEntity.ok(list);
    }


    @GetMapping("/{postId}")
    @Operation(summary = "게시글 상세 조회")
    public ResponseEntity<PostDetailResponse> getPost(
        @PathVariable Long postId
    ) {
        PostDetailResponse response = postService.getPost(postId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{postId}/edit")
    @Operation(summary = "게시글 수정용 데이터 조회")
    public ResponseEntity<EditPostResponse> getPostForEdit(
        @AuthenticationPrincipal CustomUserDetails principal,
        @PathVariable Long postId
    ) {
        if (principal == null) {
            throw new AuthenticationException();
        }
        Long userId = principal.getUserId();
        EditPostResponse response = postService.getPostForEdit(userId, postId);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{postId}")
    @Operation(summary = "게시글 수정")
    public ResponseEntity<PostDetailResponse> updatePost(
        @AuthenticationPrincipal CustomUserDetails principal,
        @PathVariable Long postId,
        @RequestBody @Valid UpdatePostRequest request
    ) {
        if (principal == null) {
            throw new AuthenticationException();
        }
        Long userId = principal.getUserId();
        PostDetailResponse response = postService.updatePost(userId, postId, request);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제")
    public ResponseEntity<Void> deletePost(
        @AuthenticationPrincipal CustomUserDetails principal,
        @PathVariable Long postId
    ) {
        if (principal == null) {
            throw new AuthenticationException();
        }
        Long userId = principal.getUserId();
        postService.deletePost(userId, postId);
        return ResponseEntity.noContent().build();
    }
}
