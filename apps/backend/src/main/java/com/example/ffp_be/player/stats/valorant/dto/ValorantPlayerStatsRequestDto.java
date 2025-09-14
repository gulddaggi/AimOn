package com.example.ffp_be.player.stats.valorant.dto;

import com.example.ffp_be.player.stats.valorant.entity.ValorantPlayerStatsEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ValorantPlayerStatsRequestDto {

    private int round;
    private double acs;
    private double adr;
    private double apr;
    private double fkpr;
    private double fdpr;
    private double hs;
    private double cl;
    private double kda;

    public ValorantPlayerStatsEntity toEntity() {
        return ValorantPlayerStatsEntity.builder()
            .round(this.round)
            .acs(this.acs)
            .adr(this.adr)
            .apr(this.apr)
            .fkpr(this.fkpr)
            .fdpr(this.fdpr)
            .hs(this.hs)
            .cl(this.cl)
            .kda(this.kda)
            .build();
    }
}
