package com.example.ffp_be.team.service;

import com.example.ffp_be.team.dto.TeamRequestDto;
import com.example.ffp_be.team.dto.TeamResponseDto;

import java.util.List;

public interface TeamService {

    TeamResponseDto createTeam(TeamRequestDto request);

    TeamResponseDto updateTeam(Long teamId, TeamRequestDto request);

    void deleteTeamById(Long teamId);

    TeamResponseDto getTeamById(Long teamId);

    List<TeamResponseDto> getAllTeams();

    /** 게임 ID로 팀 조회 (Game 엔티티 조회 후 findAllByGame 사용) */
    List<TeamResponseDto> getTeamsByGameId(Long gameId);

    /** 리그 ID로 팀 조회 (현재는 Long FK, 추후 League 연관관계로 전환 예정) */
    List<TeamResponseDto> getTeamsByLeagueId(Long leagueId);

    /** 팀명 부분 검색 (대소문자 무시) */
    List<TeamResponseDto> searchTeamsByName(String keyword);
}
