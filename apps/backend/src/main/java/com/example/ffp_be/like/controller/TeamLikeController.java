package com.example.ffp_be.like.controller;

import com.example.ffp_be.auth.security.CustomUserDetails;
import com.example.ffp_be.like.service.TeamLikeService;
import com.example.ffp_be.team.dto.TeamResponseDto;
import com.example.ffp_be.team.entity.Team;
import com.example.ffp_be.team.repository.TeamRepository;
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
@RequestMapping("/likes/teams")
@RequiredArgsConstructor
@Tag(name = "Like - Team", description = "선호 팀 관리 API")
public class TeamLikeController {

    private final TeamLikeService teamLikeService;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @PostMapping("/{teamId}")
    @Operation(summary = "팀 좋아요 토글")
    public ResponseEntity<Boolean> toggleTeamLike(
        @AuthenticationPrincipal CustomUserDetails principal,
        @PathVariable Long teamId
    ) {
        if (principal == null) {
            throw new UserRequiredException();
        }
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new ResourceNotFoundException("팀 없음"));
        User user = userRepository.findByUser_Id(principal.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("사용자 정보 없음"));
        boolean liked = teamLikeService.toggleTeamLike(user, team);
        return ResponseEntity.ok(liked);
    }

    @GetMapping("/me")
    @Operation(summary = "내가 선호한 팀 목록")
    public ResponseEntity<List<TeamResponseDto>> getMyLikedTeams(
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            throw new UserRequiredException();
        }
        User user = userRepository.findByUser_Id(principal.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("사용자 정보 없음"));
        List<TeamResponseDto> result = teamLikeService.getLikedTeams(user)
            .stream()
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
        return ResponseEntity.ok(result);
    }
}


