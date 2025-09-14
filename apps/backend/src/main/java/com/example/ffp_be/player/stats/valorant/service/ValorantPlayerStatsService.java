package com.example.ffp_be.player.stats.valorant.service;

import com.example.ffp_be.player.stats.valorant.dto.ValorantPlayerStatsRequestDto;
import com.example.ffp_be.player.stats.valorant.dto.ValorantPlayerStatsResponseDto;

public interface ValorantPlayerStatsService {

    ValorantPlayerStatsResponseDto createOrUpdateValorantStats(Long playerId, ValorantPlayerStatsRequestDto request);

    ValorantPlayerStatsResponseDto getValorantStatsByPlayerId(Long playerId);

    void deleteValorantStatsByPlayerId(Long playerId);
}
