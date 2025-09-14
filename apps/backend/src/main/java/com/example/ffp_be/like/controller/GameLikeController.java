package com.example.ffp_be.like.controller;

import com.example.ffp_be.auth.security.CustomUserDetails;
import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.game.repository.GameRepository;
import com.example.ffp_be.like.service.GameLikeService;
import com.example.ffp_be.game.dto.response.GameResponse;
import com.example.ffp_be.user.entity.User;
import com.example.ffp_be.user.repository.UserRepository;
import com.example.ffp_be.like.exception.UserRequiredException;
import com.example.ffp_be.like.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/likes/games")
@RequiredArgsConstructor
@Tag(name = "Like - Game", description = "선호 게임 관리 API")
public class GameLikeController {

    private final GameLikeService gameLikeService;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    @PostMapping("/{gameId}")
    @Operation(summary = "게임 좋아요 토글")
    public ResponseEntity<Boolean> toggleGameLike(
        @AuthenticationPrincipal CustomUserDetails principal,
        @PathVariable Long gameId
    ) {
        if (principal == null) {
            throw new UserRequiredException();
        }
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new ResourceNotFoundException("게임 없음"));
        User user = userRepository.findByUser_Id(principal.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("사용자 정보 없음"));
        boolean liked = gameLikeService.toggleGameLike(user, game);
        return ResponseEntity.ok(liked);
    }

    @GetMapping("/me")
    @Operation(summary = "내가 선호한 게임 목록")
    public ResponseEntity<List<GameResponse>> getMyLikedGames(
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            throw new UserRequiredException();
        }
        User user = userRepository.findByUser_Id(principal.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("사용자 정보 없음"));
        List<GameResponse> result = gameLikeService.getLikedGames(user)
            .stream()
            .map(GameResponse::new)
            .toList();
        return ResponseEntity.ok(result);
    }
}


