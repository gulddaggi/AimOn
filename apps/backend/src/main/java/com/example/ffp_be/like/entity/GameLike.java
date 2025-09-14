package com.example.ffp_be.like.entity;

import com.example.ffp_be.game.entity.Game;
import com.example.ffp_be.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_like")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
