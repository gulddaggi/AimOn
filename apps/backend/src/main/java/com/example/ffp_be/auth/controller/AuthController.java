package com.example.ffp_be.auth.controller;

import com.example.ffp_be.auth.dto.request.LoginRequest;
import com.example.ffp_be.auth.dto.request.JoinRequest;
import com.example.ffp_be.auth.dto.response.BaseResponse;
import com.example.ffp_be.auth.dto.response.UserInfoResponse;
import com.example.ffp_be.auth.dto.response.LoginResponse;
import com.example.ffp_be.auth.entity.RefreshToken;
import com.example.ffp_be.auth.entity.UserCredential;
import com.example.ffp_be.auth.exception.DuplicatedUsernameException;
import com.example.ffp_be.auth.exception.LoginFailedException;
import com.example.ffp_be.auth.exception.UserNotFoundException;
import com.example.ffp_be.auth.exception.InvalidRefreshTokenException;
import com.example.ffp_be.auth.exception.RefreshTokenExpiredException;
import com.example.ffp_be.auth.service.ResponseService;
import com.example.ffp_be.auth.service.UserCredentialService;
import com.example.ffp_be.auth.service.RefreshTokenService;
import com.example.ffp_be.auth.utils.JwtTokenProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth", description = "인증/인가 관리 API")
public class AuthController {

    private final UserCredentialService userCredentialService;
    private final RefreshTokenService refreshTokenService;
    private final ResponseService responseService;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/join")
    @Operation(summary = "회원 가입")
    public ResponseEntity<BaseResponse> join(@RequestBody JoinRequest joinRequest) {
        try {
            LoginResponse loginRes = userCredentialService.join(joinRequest);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + loginRes.getAccessToken());

            BaseResponse body = responseService.getSingleDataResponse(true, "회원가입 및 자동 로그인 성공",
                loginRes);
            return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(body);
        } catch (DuplicatedUsernameException e) {
            logger.debug(e.getMessage());
            BaseResponse body = responseService.getBaseResponse(false, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public ResponseEntity<BaseResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginRes = userCredentialService.login(loginRequest);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + loginRes.getAccessToken());

            BaseResponse body = responseService.getSingleDataResponse(true, "로그인 성공", loginRes);
            return ResponseEntity.ok().headers(headers).body(body);
        } catch (LoginFailedException e) {
            logger.debug(e.getMessage());
            BaseResponse body = responseService.getBaseResponse(false, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users")
    @Operation(summary = "인증된 사용자 정보 조회")
    public ResponseEntity<BaseResponse> findUserByAuthentication(
        final Authentication authentication) {
        try {
            String username = authentication.getName();
            UserInfoResponse found = userCredentialService.findByUsername(username);
            BaseResponse body = responseService.getSingleDataResponse(true, "조회 성공", found);
            return ResponseEntity.ok(body);
        } catch (UserNotFoundException e) {
            logger.debug(e.getMessage());
            BaseResponse body = responseService.getBaseResponse(false, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 재발급")
    public ResponseEntity<BaseResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshTokenValue = body.get("refreshToken");
        RefreshToken refreshToken = refreshTokenService
            .findByRefreshToken(refreshTokenValue)
            .orElseThrow(() -> new InvalidRefreshTokenException());

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenExpiredException();
        }

        UserCredential user = refreshToken.getUser();

        String newAccessToken = jwtTokenProvider.createToken(
            user.getEmail(),
            Collections.singletonList(user.getRole())
        );

        Map<String, String> data = new HashMap<>();
        data.put("accessToken", newAccessToken);

        BaseResponse bodyRes = responseService.getSingleDataResponse(true, "토큰 재발급 성공", data);
        return ResponseEntity.ok(bodyRes);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public ResponseEntity<BaseResponse> logout(@RequestBody Map<String, String> body) {
        String refreshTokenValue = body.get("refreshToken");
        RefreshToken refreshToken = refreshTokenService
            .findByRefreshToken(refreshTokenValue)
            .orElseThrow(() -> new InvalidRefreshTokenException());

        refreshTokenService.delete(refreshToken);

        BaseResponse bodyRes = responseService.getBaseResponse(true, "로그아웃 성공");
        return ResponseEntity.ok(bodyRes);
    }

    // 테스트용 API - 다양한 예외 상황 테스트
    @GetMapping("/test-exceptions")
    @Operation(summary = "예외 처리 테스트용 API")
    public ResponseEntity<BaseResponse> testExceptions(@RequestParam String type) {
        switch (type) {
            case "duplicated-username":
                throw new DuplicatedUsernameException();
            case "login-failed":
                throw new LoginFailedException();
            case "user-not-found":
                throw new UserNotFoundException();
            case "invalid-refresh-token":
                throw new InvalidRefreshTokenException();
            case "refresh-token-expired":
                throw new RefreshTokenExpiredException();
            case "illegal-state":
                throw new IllegalStateException("테스트용 IllegalStateException");
            case "illegal-argument":
                throw new IllegalArgumentException("테스트용 IllegalArgumentException");
            default:
                throw new RuntimeException("알 수 없는 예외 타입");
        }
    }

}
