package com.example.ffp_be.match.repository;

import com.example.ffp_be.match.entity.Match;
import com.example.ffp_be.team.entity.Team;
import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.league.entity.League;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByTeam(Team team);

    List<Match> findByGame(Game game);

    List<Match> findByLeague(League league);

    List<Match> findByMatchDateBetween(LocalDateTime start, LocalDateTime end);

    List<Match> findByPlayed(Boolean played);

    Optional<Match> findByVlrMatchId(Long vlrMatchId);

    boolean existsByLeague_Id(Long leagueId);
}


