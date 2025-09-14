package com.example.ffp_be.user.controller;

import com.example.ffp_be.auth.security.CustomUserDetails;
import com.example.ffp_be.user.dto.request.UpdateUserRequest;
import com.example.ffp_be.user.dto.response.UserResponse;
import com.example.ffp_be.user.exception.AuthenticationException;
import com.example.ffp_be.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 프로필 관리 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회")
    public ResponseEntity<UserResponse> getMe(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(userService.getMyProfile(userId));
    }

    @GetMapping("/by-nickname/{nickname}")
    @Operation(summary = "닉네임으로 프로필 조회")
    public ResponseEntity<UserResponse> getByNickname(@PathVariable String nickname) {
        return ResponseEntity.ok(userService.getProfileByNickname(nickname));
    }

    @GetMapping("/by-id/{userId}")
    @Operation(summary = "userId로 프로필 조회")
    public ResponseEntity<UserResponse> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @GetMapping("/search")
    @Operation(summary = "닉네임 키워드 검색")
    public ResponseEntity<List<UserResponse>> searchByNickname(
        @RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(userService.searchByNickname(keyword.trim()));
    }

    @GetMapping("/availability")
    @Operation(summary = "닉네임 사용 가능 여부 확인")
    public ResponseEntity<Boolean> isNicknameAvailable(@RequestParam("nickname") String nickname) {
        return ResponseEntity.ok(userService.isNicknameAvailable(nickname.trim()));
    }

    @PutMapping("/me")
    @Operation(summary = "내 프로필 수정")
    public ResponseEntity<UserResponse> updateMe(
        Authentication authentication,
        @RequestBody UpdateUserRequest request
    ) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PutMapping("/me/profile-image")
    @Operation(summary = "프로필 이미지 수정")
    public ResponseEntity<UserResponse> updateProfileImage(
        Authentication authentication,
        @RequestBody String profileImageUrl
    ) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(userService.updateProfileImage(userId, profileImageUrl));
    }

    @PostMapping("/me/exp")
    @Operation(summary = "경험치 증감(+/-)")
    public ResponseEntity<UserResponse> addExp(Authentication authentication,
        @RequestParam("exp") int expToAdd) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(userService.addExp(userId, expToAdd));
    }

    @PutMapping("/me/level")
    @Operation(summary = "레벨 설정")
    public ResponseEntity<UserResponse> setLevel(Authentication authentication,
        @RequestParam("level") int level) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(userService.setLevel(userId, level));
    }

    private Long getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new AuthenticationException();
    }
}
