package com.example.ffp_be.team.repository;

import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findAllByGame(Game game);

    List<Team> findByLeague_Id(Long leagueId);

    List<Team> findByTeamNameContainingIgnoreCase(String teamName);

    boolean existsByLeague_Id(Long leagueId);
}
