package com.example.ffp_be.league.repository;

import com.example.ffp_be.league.entity.League;
import com.example.ffp_be.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeagueRepository extends JpaRepository<League, Long> {

    List<League> findAllByGame(Game game);

    List<League> findByGame_Id(Long gameId);

    List<League> findByNameContainingIgnoreCase(String name);

    boolean existsByNameAndGame_Id(String name, Long gameId);
}
