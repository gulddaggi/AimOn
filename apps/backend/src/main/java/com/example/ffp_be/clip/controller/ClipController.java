package com.example.ffp_be.clip.controller;

import com.example.ffp_be.clip.dto.ClipResponse;
import com.example.ffp_be.clip.dto.CreateClipRequest;
import com.example.ffp_be.clip.service.ClipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clip")
@RequiredArgsConstructor
@Tag(name = "Clip", description = "클립 게시판 관리 API")
public class ClipController {

    private final ClipService clipService;

    @PostMapping("/fetch")
    @Operation(summary = "유튜브 클립 수집")
    public ResponseEntity<String> fetchClipsFromYoutube() {
        clipService.ingestAllClips();
        return ResponseEntity.ok("전체 게임/팀 클립 수집 및 저장이 완료되었습니다.");
    }

    @PostMapping
    @Operation(summary = "클립 등록")
    public ResponseEntity<ClipResponse> registerClip(@Valid @RequestBody CreateClipRequest request) {
        return ResponseEntity.ok(clipService.registerUserClip(request));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "유저별 클립 조회")
    public ResponseEntity<List<ClipResponse>> getUserClips(@PathVariable Long userId) {
        return ResponseEntity.ok(clipService.getClipsByUserId(userId));
    }

    @DeleteMapping("/{clipId}")
    @Operation(summary = "클립 삭제")
    public ResponseEntity<Void> deleteClip(@PathVariable Long clipId) {
        clipService.deleteClip(clipId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/game/{gameId}")
    @Operation(summary = "게임별 클립 조회")
    public ResponseEntity<List<ClipResponse>> getClipsByGameId(@PathVariable Long gameId) {
        return ResponseEntity.ok(clipService.getClipsByGameId(gameId));
    }

    @GetMapping("/game/{gameId}/latest")
    @Operation(summary = "게임별 최신 클립 조회")
    public ResponseEntity<List<ClipResponse>> getLatestClipsByGameId(@PathVariable Long gameId) {
        return ResponseEntity.ok(clipService.getLatestClipsByGameId(gameId));
    }

    @GetMapping("/{clipId}")
    @Operation(summary = "클립 상세 조회")
    public ResponseEntity<ClipResponse> getClipById(@PathVariable Long clipId) {
        return ResponseEntity.ok(clipService.getClipById(clipId));
    }

    @GetMapping("/user/{userId}/preferred")
    @Operation(summary = "유저 선호팀 클립 조회")
    public ResponseEntity<List<ClipResponse>> getPreferredClips(@PathVariable Long userId) {
        return ResponseEntity.ok(clipService.getPreferredTeamClips(userId)); // 항상 6개
    }
}
