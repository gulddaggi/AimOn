package com.example.ffp_be.league.entity;

import com.example.ffp_be.game.entity.Game;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "league")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어떤 게임의 리그인지 (ERD: game_id NOT NULL) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    /** 리그 이름 (ERD: name, varchar(100) NOT NULL) */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
}
