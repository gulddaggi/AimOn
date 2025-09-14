package com.example.ffp_be.match.controller;

import com.example.ffp_be.match.dto.MatchRequestDto;
import com.example.ffp_be.match.dto.MatchResponseDto;
import com.example.ffp_be.match.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matches")
@Validated
@Tag(name = "Match", description = "경기(매치) 관리 API")
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    @Operation(summary = "매치 등록")
    public ResponseEntity<MatchResponseDto> createMatch(@Valid @RequestBody MatchRequestDto request) {
        return ResponseEntity.ok(matchService.createMatch(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID로 매치 조회")
    public ResponseEntity<MatchResponseDto> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @GetMapping
    @Operation(summary = "전체 매치 조회")
    public ResponseEntity<List<MatchResponseDto>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "팀별 매치 조회")
    public ResponseEntity<List<MatchResponseDto>> getMatchesByTeam(
        @Parameter(description = "팀 ID") @PathVariable Long teamId) {
        return ResponseEntity.ok(matchService.getMatchesByTeamId(teamId));
    }

    @GetMapping("/game/{gameId}")
    @Operation(summary = "게임별 매치 조회")
    public ResponseEntity<List<MatchResponseDto>> getMatchesByGame(
        @Parameter(description = "게임 ID") @PathVariable Long gameId) {
        return ResponseEntity.ok(matchService.getMatchesByGameId(gameId));
    }

    @GetMapping("/league/{leagueId}")
    @Operation(summary = "리그별 매치 조회")
    public ResponseEntity<List<MatchResponseDto>> getMatchesByLeague(
        @Parameter(description = "리그 ID") @PathVariable Long leagueId) {
        return ResponseEntity.ok(matchService.getMatchesByLeagueId(leagueId));
    }

    @GetMapping("/date-range")
    @Operation(summary = "날짜 범위로 매치 조회")
    public ResponseEntity<List<MatchResponseDto>> getMatchesByDateRange(
        @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(matchService.getMatchesByDateRange(start, end));
    }

    @GetMapping("/played/{played}")
    @Operation(summary = "경기 진행 여부로 매치 조회")
    public ResponseEntity<List<MatchResponseDto>> getMatchesByPlayed(@PathVariable Boolean played) {
        return ResponseEntity.ok(matchService.getMatchesByPlayed(played));
    }

    @PutMapping("/{id}")
    @Operation(summary = "매치 정보 수정")
    public ResponseEntity<MatchResponseDto> updateMatch(
        @PathVariable Long id,
        @Valid @RequestBody MatchRequestDto request) {
        return ResponseEntity.ok(matchService.updateMatch(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "매치 삭제")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }
}


