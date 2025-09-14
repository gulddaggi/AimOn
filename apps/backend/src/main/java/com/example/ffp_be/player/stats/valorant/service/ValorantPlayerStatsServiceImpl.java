package com.example.ffp_be.player.stats.valorant.service;

import com.example.ffp_be.player.entity.PlayerEntity;
import com.example.ffp_be.player.repository.PlayerRepository;
import com.example.ffp_be.player.stats.valorant.dto.ValorantPlayerStatsRequestDto;
import com.example.ffp_be.player.stats.valorant.dto.ValorantPlayerStatsResponseDto;
import com.example.ffp_be.player.stats.valorant.entity.ValorantPlayerStatsEntity;
import com.example.ffp_be.player.stats.valorant.repository.ValorantPlayerStatsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ValorantPlayerStatsServiceImpl implements ValorantPlayerStatsService {

    private final PlayerRepository playerRepository;
    private final ValorantPlayerStatsRepository statsRepository;

    @Override
    @Transactional
    public ValorantPlayerStatsResponseDto createOrUpdateValorantStats(Long playerId, ValorantPlayerStatsRequestDto request) {
        PlayerEntity player = playerRepository.findById(playerId)
            .orElseThrow(() -> new EntityNotFoundException("플레이어가 존재하지 않습니다: " + playerId));

        ValorantPlayerStatsEntity stats = player.getValorantStats();
        if (stats == null) {
            stats = request.toEntity();
            player.setValorantStats(stats);
        } else {
            stats.setRound(request.getRound());
            stats.setAcs(request.getAcs());
            stats.setAdr(request.getAdr());
            stats.setApr(request.getApr());
            stats.setFkpr(request.getFkpr());
            stats.setFdpr(request.getFdpr());
            stats.setHs(request.getHs());
            stats.setCl(request.getCl());
            stats.setKda(request.getKda());
        }

        PlayerEntity saved = playerRepository.save(player);
        return ValorantPlayerStatsResponseDto.from(saved.getValorantStats());
    }

    @Override
    public ValorantPlayerStatsResponseDto getValorantStatsByPlayerId(Long playerId) {
        ValorantPlayerStatsEntity stats = statsRepository.findById(playerId)
            .orElseThrow(() -> new EntityNotFoundException("스탯이 존재하지 않습니다: " + playerId));
        return ValorantPlayerStatsResponseDto.from(stats);
    }

    @Override
    @Transactional
    public void deleteValorantStatsByPlayerId(Long playerId) {
        if (!statsRepository.existsById(playerId)) {
            throw new EntityNotFoundException("스탯이 존재하지 않습니다: " + playerId);
        }
        statsRepository.deleteById(playerId);
    }
}
