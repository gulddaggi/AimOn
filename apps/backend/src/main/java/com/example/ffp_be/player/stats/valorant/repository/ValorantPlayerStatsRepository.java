package com.example.ffp_be.player.stats.valorant.repository;

import com.example.ffp_be.player.stats.valorant.entity.ValorantPlayerStatsEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValorantPlayerStatsRepository extends JpaRepository<ValorantPlayerStatsEntity, Long> {

    Optional<ValorantPlayerStatsEntity> findByPlayer_Id(Long playerId);
    boolean existsByPlayer_Id(Long playerId);
}
