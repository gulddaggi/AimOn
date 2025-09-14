package com.example.ffp_be.game.service;

import com.example.ffp_be.game.dto.request.GameRequest;
import com.example.ffp_be.game.dto.response.GameResponse;
import com.example.ffp_be.game.entity.GameType;

public interface GameService {
    GameResponse createGame(GameRequest dto);
    GameResponse updateGame(Long gameId, GameRequest dto);
    void deleteGame(Long gameId);
    GameResponse findById(Long id);
    GameResponse findByName(GameType name);
    java.util.List<GameResponse> findAll();
}
