package com.example.ffp_be.like.service;

import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.like.entity.GameLike;
import com.example.ffp_be.like.repository.GameLikeRepository;
import com.example.ffp_be.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameLikeServiceImpl implements GameLikeService {

    private final GameLikeRepository gameLikeRepository;

    @Override
    @Transactional
    public void likeGame(User user, Game game) {
        if (gameLikeRepository.findByUserAndGame(user, game).isEmpty()) {
            gameLikeRepository.save(GameLike.builder()
                .user(user)
                .game(game)
                .createdAt(LocalDateTime.now())
                .build());
        }
    }

    @Override
    @Transactional
    public void unlikeGame(User user, Game game) {
        gameLikeRepository.deleteByUserAndGame(user, game);
    }

    @Override
    public List<Game> getLikedGames(User user) {
        return gameLikeRepository.findAllByUser(user)
            .stream()
            .map(GameLike::getGame)
            .toList();
    }

    @Override
    @Transactional
    public boolean toggleGameLike(User user, Game game) {
        boolean exists = gameLikeRepository.findByUserAndGame(user, game).isPresent();
        if (exists) {
            gameLikeRepository.deleteByUserAndGame(user, game);
            return false;
        } else {
            gameLikeRepository.save(GameLike.builder()
                .user(user)
                .game(game)
                .createdAt(java.time.LocalDateTime.now())
                .build());
            return true;
        }
    }
}
