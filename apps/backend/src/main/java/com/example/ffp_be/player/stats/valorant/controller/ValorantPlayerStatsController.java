package com.example.ffp_be.player.stats.valorant.controller;

import com.example.ffp_be.player.stats.valorant.dto.ValorantPlayerStatsRequestDto;
import com.example.ffp_be.player.stats.valorant.dto.ValorantPlayerStatsResponseDto;
import com.example.ffp_be.player.stats.valorant.service.ValorantPlayerStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players/{playerId}/valorant-stats")
@RequiredArgsConstructor
@Tag(name = "ValorantStats", description = "발로란트 스탯 관리 API")
public class ValorantPlayerStatsController {

    private final ValorantPlayerStatsService valorantPlayerStatsService;

    @GetMapping
    @Operation(summary = "선수 ID로 스탯 조회")
    public ResponseEntity<ValorantPlayerStatsResponseDto> getValorantStatsByPlayerId(
        @PathVariable Long playerId
    ) {
        ValorantPlayerStatsResponseDto response =
            valorantPlayerStatsService.getValorantStatsByPlayerId(playerId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(summary = "선수 스탯 생성/수정")
    public ResponseEntity<ValorantPlayerStatsResponseDto> createOrUpdateValorantStats(
        @PathVariable Long playerId,
        @RequestBody @Valid ValorantPlayerStatsRequestDto request
    ) {
        ValorantPlayerStatsResponseDto response =
            valorantPlayerStatsService.createOrUpdateValorantStats(playerId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping
    @Operation(summary = "선수 스탯 삭제")
    public ResponseEntity<Void> deleteValorantStatsByPlayerId(
        @PathVariable Long playerId
    ) {
        valorantPlayerStatsService.deleteValorantStatsByPlayerId(playerId);
        return ResponseEntity.noContent().build();
    }
}
