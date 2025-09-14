package com.example.ffp_be.player.stats.valorant.dto;

import com.example.ffp_be.player.stats.valorant.entity.ValorantPlayerStatsEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ValorantPlayerStatsResponseDto {

    private Long playerId;
    private int round;
    private double acs;
    private double adr;
    private double apr;
    private double fkpr;
    private double fdpr;
    private double hs;
    private double cl;
    private double kda;

    public static ValorantPlayerStatsResponseDto from(ValorantPlayerStatsEntity entity) {
        return ValorantPlayerStatsResponseDto.builder()
            .playerId(entity.getPlayerId())
            .round(entity.getRound())
            .acs(entity.getAcs())
            .adr(entity.getAdr())
            .apr(entity.getApr())
            .fkpr(entity.getFkpr())
            .fdpr(entity.getFdpr())
            .hs(entity.getHs())
            .cl(entity.getCl())
            .kda(entity.getKda())
            .build();
    }
}
