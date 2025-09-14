package com.example.ffp_be.game.dto.request;

import com.example.ffp_be.game.entity.GameType;
import lombok.Getter;
import jakarta.validation.constraints.NotNull;

@Getter
public class GameRequest {

    @NotNull(message = "게임 이름은 필수입니다.")
    private GameType name;
}
