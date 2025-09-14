package com.example.ffp_be.team.entity;

import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.league.entity.League;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "team")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @Column(name = "team_name", nullable = false, length = 100)
    private String teamName;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "win_rate", nullable = false)
    private Double winRate;

    @Column(name = "a_win_rate", nullable = false)
    private Double attackWinRate;

    @Column(name = "d_win_rate", nullable = false)
    private Double defenseWinRate;

    @Column(name = "img_url", length = 255)
    private String imgUrl;

    @Column(name = "point", nullable = false)
    private Integer point;
}
