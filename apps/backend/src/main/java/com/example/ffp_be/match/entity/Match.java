package com.example.ffp_be.match.entity;

import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.league.entity.League;
import com.example.ffp_be.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "op_team", nullable = false, length = 100)
    private String opponentTeamName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @Column(name = "match_date", nullable = false)
    private LocalDateTime matchDate;

    @Column(name = "my_score")
    private Integer myScore;

    @Column(name = "op_score")
    private Integer opponentScore;

    @Column(name = "is_played", nullable = false)
    private Boolean played;

    @Column(name = "vlr_match_id", unique = true)
    private Long vlrMatchId;
}


