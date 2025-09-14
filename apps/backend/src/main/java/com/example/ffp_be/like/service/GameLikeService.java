package com.example.ffp_be.like.service;

import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.user.entity.User;

import java.util.List;

public interface GameLikeService {

    void likeGame(User user, Game game);

    void unlikeGame(User user, Game game);

    List<Game> getLikedGames(User user);

    boolean toggleGameLike(User user, Game game);
}
