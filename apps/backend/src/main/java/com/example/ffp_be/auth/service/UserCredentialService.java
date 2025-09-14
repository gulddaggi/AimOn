package com.example.ffp_be.auth.service;

import com.example.ffp_be.auth.dto.request.LoginRequest;
import com.example.ffp_be.auth.dto.request.JoinRequest;
import com.example.ffp_be.auth.dto.response.UserInfoResponse;
import com.example.ffp_be.auth.dto.response.LoginResponse;
import com.example.ffp_be.auth.entity.UserCredential;
import com.example.ffp_be.auth.entity.RefreshToken;
import com.example.ffp_be.auth.exception.DuplicatedUsernameException;
import com.example.ffp_be.auth.exception.LoginFailedException;
import com.example.ffp_be.auth.exception.UserNotFoundException;
import com.example.ffp_be.auth.utils.JwtTokenProvider;
import com.example.ffp_be.auth.repository.UserCredentialRepository;
import com.example.ffp_be.user.service.UserService;
import com.example.ffp_be.user.repository.UserRepository;
import com.example.ffp_be.team.dto.TeamResponseDto;
import com.example.ffp_be.user.entity.User;
import com.example.ffp_be.like.service.TeamLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCredentialService {

    private final UserCredentialRepository userCredentialRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final TeamLikeService teamLikeService;

    @Transactional
    public LoginResponse join(JoinRequest joinRequest) {
        if (userCredentialRepository.findByEmail(joinRequest.getUsername()).isPresent()) {
            throw new DuplicatedUsernameException("이미 가입된 유저입니다");
        }
        String nickname = joinRequest.getNickname() == null ? joinRequest.getUsername()
            : joinRequest.getNickname().trim();
        if (!nickname.isEmpty() && userRepository.existsByNickname(nickname)) {
            throw new DuplicatedUsernameException("이미 사용 중인 닉네임입니다");
        }
        UserCredential user = UserCredential.builder()
            .email(joinRequest.getUsername())
            .password(passwordEncoder.encode(joinRequest.getPassword()))
            .role("USER")
            .build();
        UserCredential savedUser = userCredentialRepository.save(user);

        userService.createDefaultProfile(savedUser.getId(),
            nickname.isEmpty() ? savedUser.getEmail() : nickname);

        String accessToken = jwtTokenProvider.createToken(
            savedUser.getEmail(),
            Collections.singletonList(savedUser.getRole())
        );

        String refreshTokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        RefreshToken refreshToken = RefreshToken.builder()
            .user(savedUser)
            .refreshToken(refreshTokenValue)
            .expiresAt(expiresAt)
            .build();
        refreshTokenService.saveOrUpdateToken(refreshToken);

        User profile = userRepository.findByUser_Id(
                savedUser.getId())
            .orElseThrow(() -> new UserNotFoundException("프로필이 없습니다"));
        var likedTeams = teamLikeService.getLikedTeams(profile).stream()
            .map(t -> TeamResponseDto.builder()
                .id(t.getId())
                .gameId(t.getGame().getId())
                .leagueId(t.getLeague().getId())
                .teamName(t.getTeamName())
                .country(t.getCountry())
                .winRate(t.getWinRate())
                .attackWinRate(t.getAttackWinRate())
                .defenseWinRate(t.getDefenseWinRate())
                .imgUrl(t.getImgUrl())
                .point(t.getPoint())
                .build())
            .toList();

        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshTokenValue)
            .likedTeams(likedTeams)
            .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        UserCredential user = userCredentialRepository.findByEmail(loginRequest.getUsername())
            .orElseThrow(() -> new LoginFailedException("잘못된 아이디입니다"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new LoginFailedException("잘못된 비밀번호입니다");
        }

        String accessToken = jwtTokenProvider.createToken(
            user.getEmail(),
            Collections.singletonList(user.getRole())
        );

        String refreshTokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .refreshToken(refreshTokenValue)
            .expiresAt(expiresAt)
            .build();

        refreshTokenService.saveOrUpdateToken(refreshToken);

        // 선호 팀 조회
        User profile = userRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new UserNotFoundException("프로필이 없습니다"));

        var likedTeams = teamLikeService.getLikedTeams(profile).stream()
            .map(t -> TeamResponseDto.builder()
                .id(t.getId())
                .gameId(t.getGame().getId())
                .leagueId(t.getLeague().getId())
                .teamName(t.getTeamName())
                .country(t.getCountry())
                .winRate(t.getWinRate())
                .attackWinRate(t.getAttackWinRate())
                .defenseWinRate(t.getDefenseWinRate())
                .imgUrl(t.getImgUrl())
                .point(t.getPoint())
                .build())
            .toList();

        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshTokenValue)
            .likedTeams(likedTeams)
            .build();
    }

    public UserInfoResponse findByUsername(String username) {
        UserCredential user = userCredentialRepository.findByEmail(username)
            .orElseThrow(() -> new UserNotFoundException("없는 유저입니다."));

        return UserInfoResponse.builder()
            .userId(user.getId())
            .username(user.getEmail())
            .role(user.getRole())
            .build();
    }
}
