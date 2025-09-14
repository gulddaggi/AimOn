package com.example.ffp_be.player.service;

import com.example.ffp_be.player.dto.PlayerRequestDto;
import com.example.ffp_be.player.dto.PlayerResponseDto;
import com.example.ffp_be.player.entity.PlayerEntity;
import com.example.ffp_be.player.repository.PlayerRepository;
import com.example.ffp_be.player.exception.PlayerNotFoundException;
import com.example.ffp_be.player.stats.valorant.dto.ValorantPlayerStatsResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    @Override
    @Transactional
    public PlayerResponseDto createPlayer(PlayerRequestDto request) {
        PlayerEntity saved = playerRepository.save(
            PlayerEntity.builder()
                .teamId(request.getTeamId())
                .gameId(request.getGameId())
                .name(request.getName())
                .handle(request.getHandle())
                .country(request.getCountry())
                .build()
        );

        return toResponse(saved);
    }

    @Override
    @Transactional
    public PlayerResponseDto updatePlayer(Long playerId, PlayerRequestDto request) {
        PlayerEntity entity = playerRepository.findById(playerId)
            .orElseThrow(PlayerNotFoundException::new);

        entity.setTeamId(request.getTeamId());
        entity.setGameId(request.getGameId());
        entity.setName(request.getName());
        entity.setHandle(request.getHandle());
        entity.setCountry(request.getCountry());

        return toResponse(entity);
    }

    @Override
    @Transactional
    public void deletePlayerById(Long playerId) {
        if(!playerRepository.existsById(playerId)) {
            throw new PlayerNotFoundException();
        }
        playerRepository.deleteById(playerId);
    }

    @Override
    public PlayerResponseDto getPlayerById(Long playerId) {
        PlayerEntity entity = playerRepository.findById(playerId)
            .orElseThrow(PlayerNotFoundException::new);
        return toResponse(entity);
    }

    @Override
    public List<PlayerResponseDto> getAllPlayers() {
        return playerRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public List<PlayerResponseDto> getPlayersByTeamId(Long teamId) {
        return playerRepository.findByTeamId(teamId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public List<PlayerResponseDto> searchPlayersByName(String keyword) {
        return playerRepository.findByNameContaining(keyword).stream()
            .map(this::toResponse)
            .toList();
    }

    private PlayerResponseDto toResponse(PlayerEntity e) {
        return PlayerResponseDto.builder()
            .id(e.getId())
            .teamId(e.getTeamId())
            .gameId(e.getGameId())
            .name(e.getName())
            .handle(e.getHandle())
            .country(e.getCountry())
            .imgUrl(e.getImgUrl())
            .valorantStats(e.getValorantStats() == null ? null : ValorantPlayerStatsResponseDto.from(e.getValorantStats()))
            .build();
    }
}
