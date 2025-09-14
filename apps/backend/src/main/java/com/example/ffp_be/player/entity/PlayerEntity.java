package com.example.ffp_be.player.entity;

import com.example.ffp_be.player.stats.valorant.entity.ValorantPlayerStatsEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "player")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Long teamId;

    @Column(nullable = true)
    private Long gameId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100, unique = true)
    private String handle;

    @Column(nullable = false, length = 50)
    private String country;

    @Column(name = "img_url", length = 255)
    private String imgUrl;

    @OneToOne(mappedBy = "player", fetch = FetchType.LAZY,
        cascade = CascadeType.ALL, orphanRemoval = true)
    private ValorantPlayerStatsEntity valorantStats;

    public void setValorantStats(ValorantPlayerStatsEntity stats) {
        if(this.valorantStats != null) {
            this.valorantStats.setPlayer(null);
        }
        this.valorantStats = stats;
        if(stats != null) {
            stats.setPlayer(this);
        }

    }
}
