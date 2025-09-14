package com.example.ffp_be.game.service;

import com.example.ffp_be.game.dto.request.GameRequest;
import com.example.ffp_be.game.dto.response.GameResponse;
import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.game.entity.GameType;
import com.example.ffp_be.game.exception.GameAlreadyExistsException;
import com.example.ffp_be.game.exception.GameNotFoundException;
import com.example.ffp_be.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    @Override
    @Transactional
    public GameResponse createGame(GameRequest dto) {
        if (gameRepository.existsByName(dto.getName())) {
            throw new GameAlreadyExistsException();
        }

        Game game = Game.builder().name(dto.getName()).build();

        try {
            return new GameResponse(gameRepository.save(game));
        } catch (DataIntegrityViolationException e) {
            throw new GameAlreadyExistsException();
        }
    }

    @Override
    @Transactional
    public GameResponse updateGame(Long gameId, GameRequest dto) {
        Game entity = gameRepository.findById(gameId).orElseThrow(GameNotFoundException::new);

        if (!entity.getName().equals(dto.getName()) && gameRepository.existsByName(dto.getName())) {
            throw new GameAlreadyExistsException();
        }

        entity = Game.builder().id(entity.getId()).name(dto.getName()).build();

        try {
            return new GameResponse(gameRepository.save(entity));
        } catch (DataIntegrityViolationException e) {
            throw new GameAlreadyExistsException();
        }
    }

    @Override
    @Transactional
    public void deleteGame(Long gameId) {
        if (!gameRepository.existsById(gameId)) {
            throw new GameNotFoundException();
        }
        gameRepository.deleteById(gameId);
    }

    @Override
    @Transactional(readOnly = true)
    public GameResponse findById(Long id) {
        Game game = gameRepository.findById(id).orElseThrow(GameNotFoundException::new);
        return new GameResponse(game);
    }

    @Override
    @Transactional(readOnly = true)
    public GameResponse findByName(GameType name) {
        Game game = gameRepository.findByName(name).orElseThrow(GameNotFoundException::new);
        return new GameResponse(game);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameResponse> findAll() {
        return gameRepository.findAll().stream().map(GameResponse::new).toList();
    }
}

