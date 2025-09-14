package com.example.ffp_be.match.service;

import com.example.ffp_be.match.dto.MatchRequestDto;
import com.example.ffp_be.match.dto.MatchResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchService {
    MatchResponseDto createMatch(MatchRequestDto request);
    MatchResponseDto updateMatch(Long matchId, MatchRequestDto request);
    void deleteMatch(Long matchId);
    MatchResponseDto getMatchById(Long matchId);
    List<MatchResponseDto> getAllMatches();

    List<MatchResponseDto> getMatchesByTeamId(Long teamId);
    List<MatchResponseDto> getMatchesByGameId(Long gameId);
    List<MatchResponseDto> getMatchesByLeagueId(Long leagueId);
    List<MatchResponseDto> getMatchesByDateRange(LocalDateTime start, LocalDateTime end);
    List<MatchResponseDto> getMatchesByPlayed(Boolean played);
}


