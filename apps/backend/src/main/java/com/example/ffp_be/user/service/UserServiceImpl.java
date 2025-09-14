package com.example.ffp_be.user.service;

import com.example.ffp_be.auth.entity.UserCredential;
import com.example.ffp_be.user.dto.request.UpdateUserRequest;
import com.example.ffp_be.user.dto.response.UserResponse;
import com.example.ffp_be.user.entity.User;
import com.example.ffp_be.user.exception.DuplicatedNicknameException;
import com.example.ffp_be.user.exception.InvalidLevelException;
import com.example.ffp_be.user.exception.UserProfileNotFoundException;
import com.example.ffp_be.user.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findByUser_Id(userId)
            .orElseThrow(() -> new UserProfileNotFoundException(userId));
        return toDto(user);
    }

    @Override
    public UserResponse getMyProfile(Long userId) {
        User user = userRepository.findByUser_Id(userId)
            .orElseThrow(() -> new UserProfileNotFoundException(userId));
        return UserResponse.builder()
            .id(user.getId())
            .nickname(user.getNickname())
            .email(user.getUser().getEmail())
            .profileImageUrl(user.getProfileImageUrl())
            .level(user.getLevel())
            .exp(user.getExp())
            .build();
    }

    @Override
    public UserResponse getProfileByNickname(String nickname) {
        User user = userRepository.findByNickname(nickname)
            .orElseThrow(() -> new UserProfileNotFoundException(
                "닉네임 '" + nickname + "'에 해당하는 프로필을 찾을 수 없습니다."));
        return toDto(user);
    }

    @Override
    public List<UserResponse> searchByNickname(String keyword) {
        return userRepository.findAllByNicknameContaining(keyword).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateUserRequest request) {
        User user = userRepository.findByUser_Id(userId)
            .orElseThrow(() -> new UserProfileNotFoundException(userId));

        if (request.getNickname() != null && !Objects.equals(user.getNickname(),
            request.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new DuplicatedNicknameException(request.getNickname());
            }
            user.setNickname(request.getNickname());
        }

        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Override
    @Transactional
    public UserResponse createDefaultProfile(Long userId, String defaultNickname) {
        if (userRepository.findByUser_Id(userId).isPresent()) {
            return toDto(userRepository.findByUser_Id(userId).orElseThrow());
        }

        if (userRepository.existsByNickname(defaultNickname)) {
            throw new DuplicatedNicknameException(defaultNickname);
        }

        UserCredential ref = UserCredential.builder().id(userId).build();

        User profile = User.builder()
            .user(ref)
            .nickname(defaultNickname)
            .profileImageUrl(null)
            .level(0)
            .exp(0)
            .build();

        User saved = userRepository.save(profile);
        return toDto(saved);
    }

    @Override
    @Transactional
    public UserResponse updateProfileImage(Long userId, String profileImageUrl) {
        User user = userRepository.findByUser_Id(userId)
            .orElseThrow(() -> new UserProfileNotFoundException(userId));
        user.setProfileImageUrl(profileImageUrl);
        return toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse addExp(Long userId, int expToAdd) {
        if (expToAdd == 0) {
            return getProfile(userId);
        }
        User user = userRepository.findByUser_Id(userId)
            .orElseThrow(() -> new UserProfileNotFoundException(userId));
        int updatedExp = user.getExp() + expToAdd;
        if (updatedExp < 0) {
            updatedExp = 0;
        }

        int updatedLevel = user.getLevel();
        while (updatedExp >= 100) {
            updatedLevel += 1;
            updatedExp -= 100;
        }

        user.setLevel(updatedLevel);
        user.setExp(updatedExp);
        return toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse setLevel(Long userId, int level) {
        if (level < 0) {
            throw new InvalidLevelException();
        }
        User user = userRepository.findByUser_Id(userId)
            .orElseThrow(() -> new UserProfileNotFoundException(userId));
        user.setLevel(level);
        return toDto(userRepository.save(user));
    }

    private UserResponse toDto(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .nickname(user.getNickname())
            .profileImageUrl(user.getProfileImageUrl())
            .level(user.getLevel())
            .exp(user.getExp())
            .build();
    }
}
