package com.example.ffp_be.player.service;

import com.example.ffp_be.player.dto.PlayerRequestDto;
import com.example.ffp_be.player.dto.PlayerResponseDto;
import java.util.List;

public interface PlayerService {

    PlayerResponseDto createPlayer(PlayerRequestDto request);

    PlayerResponseDto updatePlayer(Long playerId, PlayerRequestDto request);

    void deletePlayerById(Long playerId);

    PlayerResponseDto getPlayerById(Long playerId);

    List<PlayerResponseDto> getAllPlayers();

    List<PlayerResponseDto> getPlayersByTeamId(Long teamId);

    List<PlayerResponseDto> searchPlayersByName(String keyword);
}
