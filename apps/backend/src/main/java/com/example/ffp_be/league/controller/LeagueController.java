package com.example.ffp_be.league.controller;

import com.example.ffp_be.league.dto.LeagueRequestDto;
import com.example.ffp_be.league.dto.LeagueResponseDto;
import com.example.ffp_be.league.service.LeagueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/leagues")
@Validated
@Tag(name = "League", description = "리그 관리 API")
public class LeagueController {

    private final LeagueService leagueService;

    @PostMapping
    @Operation(summary = "리그 등록")
    public ResponseEntity<LeagueResponseDto> createLeague(@Valid @RequestBody LeagueRequestDto request) {
        return ResponseEntity.ok(leagueService.createLeague(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID로 리그 조회")
    public ResponseEntity<LeagueResponseDto> getLeagueById(@PathVariable Long id) {
        return ResponseEntity.ok(leagueService.getLeagueById(id));
    }

    @GetMapping
    @Operation(summary = "전체 리그 조회")
    public ResponseEntity<List<LeagueResponseDto>> getAllLeagues() {
        return ResponseEntity.ok(leagueService.getAllLeagues());
    }

    @GetMapping("/game/{gameId}")
    @Operation(summary = "게임별 리그 조회")
    public ResponseEntity<List<LeagueResponseDto>> getLeaguesByGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(leagueService.getLeaguesByGameId(gameId));
    }

    @GetMapping("/search")
    @Operation(summary = "리그명으로 검색")
    public ResponseEntity<List<LeagueResponseDto>> searchLeaguesByName(@RequestParam("name") String name) {
        return ResponseEntity.ok(leagueService.searchLeaguesByName(name));
    }

    @PutMapping("/{id}")
    @Operation(summary = "리그 정보 수정")
    public ResponseEntity<LeagueResponseDto> updateLeague(
        @PathVariable Long id,
        @Valid @RequestBody LeagueRequestDto request
    ) {
        return ResponseEntity.ok(leagueService.updateLeague(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "리그 삭제")
    public ResponseEntity<Void> deleteLeague(@PathVariable Long id) {
        leagueService.deleteLeagueById(id);
        return ResponseEntity.noContent().build();
    }
}
