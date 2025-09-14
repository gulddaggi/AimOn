package com.example.ffp_be.post.service;

import com.example.ffp_be.post.dto.request.UpdatePostRequest;
import com.example.ffp_be.post.dto.request.CreatePostRequest;
import com.example.ffp_be.post.dto.response.EditPostResponse;
import com.example.ffp_be.post.dto.response.PostDetailResponse;
import com.example.ffp_be.post.dto.response.PostListResponse;
import java.util.List;

public interface PostService {

    PostDetailResponse createPost(Long authorId, CreatePostRequest request);

    List<PostListResponse> listPosts(int page, int size);

    PostDetailResponse getPost(Long postId);

    PostDetailResponse updatePost(Long authorId, Long postId, UpdatePostRequest request);

    void deletePost(Long authorId, Long postId);

    EditPostResponse getPostForEdit(Long authorId, Long postId);

    List<PostListResponse> listAllPosts();

    List<PostListResponse> listRecentPosts(int count);

    List<PostListResponse> searchPostsByTitle(String keyword);

    List<PostListResponse> listMyPosts(Long userId);
}
