package com.example.ffp_be.user.service;

import com.example.ffp_be.user.dto.request.UpdateUserRequest;
import com.example.ffp_be.user.dto.response.UserResponse;
import java.util.List;

public interface UserService {

    UserResponse getProfile(Long userId);

    UserResponse getMyProfile(Long userId);

    UserResponse getProfileByNickname(String nickname);

    List<UserResponse> searchByNickname(String keyword);

    boolean isNicknameAvailable(String nickname);

    UserResponse updateProfile(Long userId, UpdateUserRequest request);

    UserResponse createDefaultProfile(Long userId, String defaultNickname);

    // optional
    UserResponse updateProfileImage(Long userId, String profileImageUrl);

    UserResponse addExp(Long userId, int expToAdd);

    UserResponse setLevel(Long userId, int level);
}