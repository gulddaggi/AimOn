package com.example.ffp_be.game.repository;

import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.game.entity.GameType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByName(GameType name);
    boolean existsByName(GameType name);

}
