package com.example.ffp_be.player.stats.valorant.entity;

import com.example.ffp_be.player.entity.PlayerEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "valorant_player_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValorantPlayerStatsEntity {

    @Id
    @Column(name = "player_id")
    private Long playerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private PlayerEntity player;

    @Column(name = "round")
    private int round;

    @Column(name = "acs")
    private double acs;

    @Column(name = "adr")
    private double adr;

    @Column(name = "apr")
    private double apr;

    @Column(name = "kast")
    private double kast;

    @Column(name = "fkpr")
    private double fkpr;

    @Column(name = "fdpr")
    private double fdpr;

    @Column(name = "hs")
    private double hs;

    @Column(name = "cl")
    private double cl;

    @Column(name = "kda")
    private double kda;
}
