package com.example.ffp_be.league.service;

import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.game.repository.GameRepository;
import com.example.ffp_be.common.exception.ErrorCode;
import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.league.exception.DuplicatedLeagueException;
import com.example.ffp_be.league.exception.LeagueHasMatchesException;
import com.example.ffp_be.league.exception.LeagueHasTeamsException;
import com.example.ffp_be.league.exception.LeagueNotFoundException;
import com.example.ffp_be.league.dto.LeagueRequestDto;
import com.example.ffp_be.league.dto.LeagueResponseDto;
import com.example.ffp_be.league.entity.League;
import com.example.ffp_be.league.repository.LeagueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeagueServiceImpl implements LeagueService {

    private final LeagueRepository leagueRepository;
    private final GameRepository gameRepository;
    private final com.example.ffp_be.team.repository.TeamRepository teamRepository;
    private final com.example.ffp_be.match.repository.MatchRepository matchRepository;

    private LeagueResponseDto toDto(League e) {
        return LeagueResponseDto.builder()
            .id(e.getId())
            .gameId(e.getGame().getId())
            .name(e.getName())
            .build();
    }

    private void apply(League e, LeagueRequestDto r, Game game) {
        e.setGame(game);
        e.setName(r.getName());
    }

    @Override
    @Transactional
    public LeagueResponseDto createLeague(LeagueRequestDto request) {
        Game game = gameRepository.findById(request.getGameId())
            .orElseThrow(com.example.ffp_be.game.exception.GameNotFoundException::new);

        if (leagueRepository.existsByNameAndGame_Id(request.getName(), request.getGameId())) {
            throw new DuplicatedLeagueException(request.getName());
        }

        League entity = League.builder().build();
        apply(entity, request, game);
        return toDto(leagueRepository.save(entity));
    }

    @Override
    @Transactional
    public LeagueResponseDto updateLeague(Long leagueId, LeagueRequestDto request) {
        League entity = leagueRepository.findById(leagueId)
            .orElseThrow(LeagueNotFoundException::new);
        Game game = gameRepository.findById(request.getGameId())
            .orElseThrow(com.example.ffp_be.game.exception.GameNotFoundException::new);

        if (!entity.getName().equals(request.getName()) || !entity.getGame().getId()
            .equals(request.getGameId())) {
            if (leagueRepository.existsByNameAndGame_Id(request.getName(), request.getGameId())) {
                throw new DuplicatedLeagueException(request.getName());
            }
        }

        apply(entity, request, game);
        return toDto(leagueRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteLeagueById(Long leagueId) {
        leagueRepository.findById(leagueId).orElseThrow(LeagueNotFoundException::new);

        if (teamRepository.existsByLeague_Id(leagueId)) {
            throw new LeagueHasTeamsException();
        }
        if (matchRepository.existsByLeague_Id(leagueId)) {
            throw new LeagueHasMatchesException();
        }
        leagueRepository.deleteById(leagueId);
    }

    @Override
    public LeagueResponseDto getLeagueById(Long leagueId) {
        return leagueRepository.findById(leagueId).map(this::toDto)
            .orElseThrow(LeagueNotFoundException::new);
    }

    @Override
    public List<LeagueResponseDto> getAllLeagues() {
        return leagueRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public List<LeagueResponseDto> getLeaguesByGameId(Long gameId) {
        return leagueRepository.findByGame_Id(gameId).stream().map(this::toDto).toList();
    }

    @Override
    public List<LeagueResponseDto> searchLeaguesByName(String keyword) {
        return leagueRepository.findByNameContainingIgnoreCase(keyword).stream().map(this::toDto)
            .toList();
    }
}
