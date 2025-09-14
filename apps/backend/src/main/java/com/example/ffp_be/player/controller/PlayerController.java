package com.example.ffp_be.player.controller;

import com.example.ffp_be.player.dto.PlayerRequestDto;
import com.example.ffp_be.player.dto.PlayerResponseDto;
import com.example.ffp_be.player.service.PlayerService;
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
@RequestMapping("/players")
@Validated
@Tag(name = "Player", description = "선수 관리 API")
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    @Operation(summary = "선수 등록")
    public ResponseEntity<PlayerResponseDto> createPlayer(
        @Valid @RequestBody PlayerRequestDto request
    ) {
        PlayerResponseDto created = playerService.createPlayer(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID로 선수 검색")
    public ResponseEntity<PlayerResponseDto> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    @GetMapping
    @Operation(summary = "전체 선수 조회")
    public ResponseEntity<List<PlayerResponseDto>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }


    @GetMapping("/team/{teamId}")
    @Operation(summary = "팀으로 선수 검색")
    public ResponseEntity<List<PlayerResponseDto>> getPlayersByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(playerService.getPlayersByTeamId(teamId));
    }


    @GetMapping("/search")
    @Operation(summary = "이름으로 선수 검색")
    public ResponseEntity<List<PlayerResponseDto>> searchPlayersByName(
        @RequestParam("name") String name
    ) {
        return ResponseEntity.ok(playerService.searchPlayersByName(name));
    }

    @PutMapping("/{id}")
    @Operation(summary = "선수 정보 수정")
    public ResponseEntity<PlayerResponseDto> updatePlayer(
        @PathVariable Long id,
        @Valid @RequestBody PlayerRequestDto request
    ) {
        return ResponseEntity.ok(playerService.updatePlayer(id, request));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "선수 삭제")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayerById(id);
        return ResponseEntity.noContent().build();
    }
}
