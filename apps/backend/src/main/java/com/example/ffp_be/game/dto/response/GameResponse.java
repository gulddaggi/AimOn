package com.example.ffp_be.game.dto.response;

import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.game.entity.GameType;
import lombok.Getter;

@Getter
public class GameResponse {
    private final Long id;
    private final GameType name;

    public GameResponse(Game game) {
        this.id = game.getId();
        this.name = game.getName();
    }
}
