package com.example.ffp_be.like.entity;

import com.example.ffp_be.team.entity.Team;
import com.example.ffp_be.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_like")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
