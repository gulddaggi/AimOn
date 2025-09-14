package com.example.ffp_be.league.service;

import com.example.ffp_be.league.dto.LeagueRequestDto;
import com.example.ffp_be.league.dto.LeagueResponseDto;

import java.util.List;

public interface LeagueService {

    LeagueResponseDto createLeague(LeagueRequestDto request);

    LeagueResponseDto updateLeague(Long leagueId, LeagueRequestDto request);

    void deleteLeagueById(Long leagueId);

    LeagueResponseDto getLeagueById(Long leagueId);

    List<LeagueResponseDto> getAllLeagues();

    List<LeagueResponseDto> getLeaguesByGameId(Long gameId);

    List<LeagueResponseDto> searchLeaguesByName(String keyword);
}
