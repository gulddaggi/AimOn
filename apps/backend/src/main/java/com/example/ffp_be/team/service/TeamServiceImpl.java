package com.example.ffp_be.team.service;

import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.game.repository.GameRepository;
import com.example.ffp_be.league.entity.League;
import com.example.ffp_be.league.repository.LeagueRepository;
import com.example.ffp_be.team.dto.TeamRequestDto;
import com.example.ffp_be.team.dto.TeamResponseDto;
import com.example.ffp_be.team.entity.Team;
import com.example.ffp_be.team.repository.TeamRepository;
import com.example.ffp_be.team.exception.TeamNotFoundException;
import com.example.ffp_be.team.exception.GameNotFoundInTeamContextException;
import com.example.ffp_be.team.exception.LeagueNotFoundInTeamContextException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;
    private final LeagueRepository leagueRepository;

    private TeamResponseDto toDto(Team e) {
        return TeamResponseDto.builder()
            .id(e.getId())
            .gameId(e.getGame().getId())
            .leagueId(e.getLeague().getId())
            .teamName(e.getTeamName())
            .country(e.getCountry())
            .winRate(e.getWinRate())
            .attackWinRate(e.getAttackWinRate())
            .defenseWinRate(e.getDefenseWinRate())
            .imgUrl(e.getImgUrl())
            .point(e.getPoint())
            .build();
    }

    private void apply(Team e, TeamRequestDto r, Game game, League league) {
        e.setGame(game);
        e.setLeague(league);
        e.setTeamName(r.getTeamName());
        e.setCountry(r.getCountry());
        e.setWinRate(r.getWinRate() != null ? r.getWinRate() : 0.0);
        e.setAttackWinRate(r.getAttackWinRate() != null ? r.getAttackWinRate() : 0.0);
        e.setDefenseWinRate(r.getDefenseWinRate() != null ? r.getDefenseWinRate() : 0.0);
        e.setPoint(r.getPoint() != null ? r.getPoint() : 0);
        e.setImgUrl(r.getImgUrl() != null ? r.getImgUrl() : e.getImgUrl());
    }

    @Override
    @Transactional
    public TeamResponseDto createTeam(TeamRequestDto request) {
        Game game = gameRepository.findById(request.getGameId())
            .orElseThrow(GameNotFoundInTeamContextException::new);
        League league = leagueRepository.findById(request.getLeagueId())
            .orElseThrow(LeagueNotFoundInTeamContextException::new);

        Team entity = Team.builder().build();
        apply(entity, request, game, league);
        return toDto(teamRepository.save(entity));
    }

    @Override
    @Transactional
    public TeamResponseDto updateTeam(Long teamId, TeamRequestDto request) {
        Team entity = teamRepository.findById(teamId)
            .orElseThrow(TeamNotFoundException::new);

        Game game = gameRepository.findById(request.getGameId())
            .orElseThrow(GameNotFoundInTeamContextException::new);
        League league = leagueRepository.findById(request.getLeagueId())
            .orElseThrow(LeagueNotFoundInTeamContextException::new);

        apply(entity, request, game, league);
        return toDto(teamRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteTeamById(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new TeamNotFoundException();
        }
        teamRepository.deleteById(teamId);
    }

    @Override
    public TeamResponseDto getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
            .map(this::toDto)
            .orElseThrow(TeamNotFoundException::new);
    }

    @Override
    public List<TeamResponseDto> getAllTeams() {
        return teamRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public List<TeamResponseDto> getTeamsByGameId(Long gameId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(GameNotFoundInTeamContextException::new);
        return teamRepository.findAllByGame(game).stream().map(this::toDto).toList();
    }

    @Override
    public List<TeamResponseDto> getTeamsByLeagueId(Long leagueId) {
        List<Team> teams = teamRepository.findByLeague_Id(leagueId);
        teams.sort((a, b) -> Integer.compare(b.getPoint() != null ? b.getPoint() : 0,
                                             a.getPoint() != null ? a.getPoint() : 0));

        int currentRank = 0;
        Integer lastPoint = null;
        int index = 0;

        List<TeamResponseDto> ranked = new ArrayList<>();
        for (Team team : teams) {
            int point = team.getPoint() != null ? team.getPoint() : 0;
            if (lastPoint == null || point != lastPoint) {
                currentRank = index + 1;
                lastPoint = point;
            }
            index++;
            TeamResponseDto dto = toDto(team);
            dto.setRank(currentRank);
            ranked.add(dto);
        }
        return ranked;
    }

    @Override
    public List<TeamResponseDto> searchTeamsByName(String keyword) {
        return teamRepository.findByTeamNameContainingIgnoreCase(keyword).stream().map(this::toDto)
            .toList();
    }
}
