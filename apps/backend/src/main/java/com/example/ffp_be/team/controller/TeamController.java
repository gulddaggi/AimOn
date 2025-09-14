package com.example.ffp_be.team.controller;

import com.example.ffp_be.team.dto.TeamRequestDto;
import com.example.ffp_be.team.dto.TeamResponseDto;
import com.example.ffp_be.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
@Validated
@Tag(name = "Team", description = "팀 관리 API")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @Operation(summary = "팀 등록", description = "게임 ID와 리그 ID를 포함한 팀 정보를 등록합니다.")
    public ResponseEntity<TeamResponseDto> createTeam(@Valid @RequestBody TeamRequestDto request) {
        return ResponseEntity.ok(teamService.createTeam(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID로 팀 조회", description = "팀 ID를 이용해 팀 정보를 조회합니다.")
    public ResponseEntity<TeamResponseDto> getTeamById(
        @Parameter(description = "팀 ID") @PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @GetMapping
    @Operation(summary = "전체 팀 조회", description = "등록된 모든 팀 정보를 조회합니다.")
    public ResponseEntity<List<TeamResponseDto>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/game/{gameId}")
    @Operation(summary = "게임별 팀 조회", description = "게임 ID를 이용해 해당 게임의 팀들을 조회합니다.")
    public ResponseEntity<List<TeamResponseDto>> getTeamsByGame(
        @Parameter(description = "게임 ID") @PathVariable Long gameId) {
        return ResponseEntity.ok(teamService.getTeamsByGameId(gameId));
    }

    @GetMapping("/league/{leagueId}")
    @Operation(summary = "리그별 팀 조회", description = "리그 ID를 이용해 해당 리그의 팀들을 조회합니다.")
    public ResponseEntity<List<TeamResponseDto>> getTeamsByLeague(
        @Parameter(description = "리그 ID") @PathVariable Long leagueId) {
        return ResponseEntity.ok(teamService.getTeamsByLeagueId(leagueId));
    }

    @GetMapping("/search")
    @Operation(summary = "팀명으로 검색", description = "팀명을 포함하는 키워드로 팀을 검색합니다. 대소문자 구분 없음.")
    public ResponseEntity<List<TeamResponseDto>> searchTeamsByName(
        @Parameter(description = "팀명 검색 키워드") @RequestParam("name") String name) {
        return ResponseEntity.ok(teamService.searchTeamsByName(name));
    }

    @PutMapping("/{id}")
    @Operation(summary = "팀 정보 수정", description = "게임 ID와 리그 ID를 포함한 팀 정보를 수정합니다.")
    public ResponseEntity<TeamResponseDto> updateTeam(
        @Parameter(description = "팀 ID") @PathVariable Long id,
        @Valid @RequestBody TeamRequestDto request) {
        return ResponseEntity.ok(teamService.updateTeam(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "팀 삭제", description = "팀 ID를 이용해 팀 정보를 삭제합니다.")
    public ResponseEntity<Void> deleteTeam(
        @Parameter(description = "팀 ID") @PathVariable Long id) {
        teamService.deleteTeamById(id);
        return ResponseEntity.noContent().build();
    }
}
