package com.example.ffp_be.match.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResponseDto {
    private Long id;
    private Long teamId;
    private String opTeam;
    private Long gameId;
    private Long leagueId;
    private LocalDateTime matchDate;
    private Integer myScore;
    private Integer opScore;
    private Boolean isPlayed;
    private Long vlrMatchId;
}


