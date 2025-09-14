package com.example.ffp_be.match.service;

import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.game.repository.GameRepository;
import com.example.ffp_be.league.entity.League;
import com.example.ffp_be.league.repository.LeagueRepository;
import com.example.ffp_be.match.dto.MatchRequestDto;
import com.example.ffp_be.match.dto.MatchResponseDto;
import com.example.ffp_be.match.entity.Match;
import com.example.ffp_be.match.repository.MatchRepository;
import com.example.ffp_be.team.entity.Team;
import com.example.ffp_be.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;
    private final LeagueRepository leagueRepository;

    private MatchResponseDto toDto(Match e) {
        return MatchResponseDto.builder()
            .id(e.getId())
            .teamId(e.getTeam().getId())
            .opTeam(e.getOpponentTeamName())
            .gameId(e.getGame().getId())
            .leagueId(e.getLeague().getId())
            .matchDate(e.getMatchDate())
            .myScore(e.getMyScore())
            .opScore(e.getOpponentScore())
            .isPlayed(e.getPlayed())
            .vlrMatchId(e.getVlrMatchId())
            .build();
    }

    private void apply(Match e, MatchRequestDto r, Team team, Game game, League league) {
        e.setTeam(team);
        e.setOpponentTeamName(r.getOpTeam());
        e.setGame(game);
        e.setLeague(league);
        e.setMatchDate(r.getMatchDate());
        e.setMyScore(r.getMyScore());
        e.setOpponentScore(r.getOpScore());
        e.setPlayed(Boolean.TRUE.equals(r.getIsPlayed()) ? Boolean.TRUE : Boolean.FALSE);
        e.setVlrMatchId(r.getVlrMatchId());
    }

    @Override
    @Transactional
    public MatchResponseDto createMatch(MatchRequestDto request) {
        Team team = teamRepository.findById(request.getTeamId())
            .orElseThrow(com.example.ffp_be.team.exception.TeamNotFoundException::new);
        Game game = gameRepository.findById(request.getGameId())
            .orElseThrow(com.example.ffp_be.team.exception.GameNotFoundInTeamContextException::new);
        League league = leagueRepository.findById(request.getLeagueId())
            .orElseThrow(
                com.example.ffp_be.team.exception.LeagueNotFoundInTeamContextException::new);

        if ((request.getMyScore() != null && request.getOpScore() == null)
            || (request.getMyScore() == null && request.getOpScore() != null)) {
            throw new com.example.ffp_be.match.exception.InvalidScorePairException();
        }

        Match entity = Match.builder().build();
        apply(entity, request, team, game, league);
        return toDto(matchRepository.save(entity));
    }

    @Override
    @Transactional
    public MatchResponseDto updateMatch(Long matchId, MatchRequestDto request) {
        Match entity = matchRepository.findById(matchId)
            .orElseThrow(com.example.ffp_be.match.exception.MatchNotFoundException::new);
        Team team = teamRepository.findById(request.getTeamId())
            .orElseThrow(com.example.ffp_be.team.exception.TeamNotFoundException::new);
        Game game = gameRepository.findById(request.getGameId())
            .orElseThrow(com.example.ffp_be.team.exception.GameNotFoundInTeamContextException::new);
        League league = leagueRepository.findById(request.getLeagueId())
            .orElseThrow(
                com.example.ffp_be.team.exception.LeagueNotFoundInTeamContextException::new);

        if ((request.getMyScore() != null && request.getOpScore() == null)
            || (request.getMyScore() == null && request.getOpScore() != null)) {
            throw new com.example.ffp_be.match.exception.InvalidScorePairException();
        }

        apply(entity, request, team, game, league);
        return toDto(matchRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteMatch(Long matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw new com.example.ffp_be.match.exception.MatchNotFoundException();
        }
        matchRepository.deleteById(matchId);
    }

    @Override
    public MatchResponseDto getMatchById(Long matchId) {
        return matchRepository.findById(matchId)
            .map(this::toDto)
            .orElseThrow(com.example.ffp_be.match.exception.MatchNotFoundException::new);
    }

    @Override
    public List<MatchResponseDto> getAllMatches() {
        return matchRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public List<MatchResponseDto> getMatchesByTeamId(Long teamId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(com.example.ffp_be.team.exception.TeamNotFoundException::new);
        return matchRepository.findByTeam(team).stream().map(this::toDto).toList();
    }

    @Override
    public List<MatchResponseDto> getMatchesByGameId(Long gameId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(com.example.ffp_be.team.exception.GameNotFoundInTeamContextException::new);
        return matchRepository.findByGame(game).stream().map(this::toDto).toList();
    }

    @Override
    public List<MatchResponseDto> getMatchesByLeagueId(Long leagueId) {
        League league = leagueRepository.findById(leagueId)
            .orElseThrow(
                com.example.ffp_be.team.exception.LeagueNotFoundInTeamContextException::new);
        return matchRepository.findByLeague(league).stream().map(this::toDto).toList();
    }

    @Override
    public List<MatchResponseDto> getMatchesByDateRange(LocalDateTime start, LocalDateTime end) {
        return matchRepository.findByMatchDateBetween(start, end).stream().map(this::toDto)
            .toList();
    }

    @Override
    public List<MatchResponseDto> getMatchesByPlayed(Boolean played) {
        return matchRepository.findByPlayed(played).stream().map(this::toDto).toList();
    }
}


