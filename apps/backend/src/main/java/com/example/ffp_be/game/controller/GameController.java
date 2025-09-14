package com.example.ffp_be.game.controller;

import com.example.ffp_be.game.dto.request.GameRequest;
import com.example.ffp_be.game.dto.response.GameResponse;
import com.example.ffp_be.game.entity.GameType;
import com.example.ffp_be.game.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
@Tag(name = "Game", description = "게임 관리 API")
public class GameController {

    private final GameService gameService;

    @PostMapping
    @Operation(summary = "게임 등록")
    public ResponseEntity<GameResponse> create(@Valid @RequestBody GameRequest dto) {
        GameResponse created = gameService.createGame(dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/{name}")
    @Operation(summary = "이름으로 게임 조회")
    public ResponseEntity<GameResponse> findByName(@PathVariable GameType name) {
        return ResponseEntity.ok(gameService.findByName(name));
    }

    @GetMapping
    @Operation(summary = "전체 게임 조회")
    public ResponseEntity<java.util.List<GameResponse>> findAll() {
        return ResponseEntity.ok(gameService.findAll());
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "ID로 게임 조회")
    public ResponseEntity<GameResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "게임 수정")
    public ResponseEntity<GameResponse> update(@PathVariable Long id,
        @Valid @RequestBody GameRequest dto) {
        return ResponseEntity.ok(gameService.updateGame(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "게임 삭제")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}
