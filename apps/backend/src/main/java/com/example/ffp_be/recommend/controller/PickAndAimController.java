package com.example.ffp_be.recommend.controller;

import com.example.ffp_be.recommend.dto.response.PickAndAimCandidateResponse;
import com.example.ffp_be.recommend.dto.request.PickAndAimFilterRequest;
import com.example.ffp_be.recommend.dto.response.PickAndAimLeagueResponse;
import com.example.ffp_be.recommend.dto.response.PickAndAimKeywordMetaResponse;
import com.example.ffp_be.recommend.service.PickAndAimService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pick-aim")
@Validated
@Tag(name = "Pick&Aim", description = "선호 팀 추천 API")
public class PickAndAimController {

    private final PickAndAimService pickAndAimService;

    @GetMapping("/leagues")
    @Operation(summary = "제공 게임/리그 목록")
    public ResponseEntity<List<PickAndAimLeagueResponse>> getLeagues() {
        return ResponseEntity.ok(pickAndAimService.getSupportedLeagues());
    }

    @PostMapping("/candidates")
    @Operation(summary = "키워드 기반 선호 팀 후보 3개 반환")
    public ResponseEntity<List<PickAndAimCandidateResponse>> getCandidates(
        @Valid @RequestBody PickAndAimFilterRequest request
    ) {
        return ResponseEntity.ok(pickAndAimService.getCandidates(request));
    }

    @GetMapping("/keywords")
    @Operation(summary = "활성 키워드 목록")
    public ResponseEntity<List<PickAndAimKeywordMetaResponse>> getKeywords() {
        return ResponseEntity.ok(pickAndAimService.getActiveKeywords());
    }
}


