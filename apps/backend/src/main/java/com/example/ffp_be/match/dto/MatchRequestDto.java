package com.example.ffp_be.match.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRequestDto {

    @NotNull
    private Long teamId;

    @NotBlank
    @Size(max = 100)
    private String opTeam;

    @NotNull
    private Long gameId;

    @NotNull
    private Long leagueId;

    @NotNull
    private LocalDateTime matchDate;

    @PositiveOrZero
    private Integer myScore;   // null 허용

    @PositiveOrZero
    private Integer opScore;   // null 허용

    private Boolean isPlayed;  // null이면 기본 false로 처리

    private Long vlrMatchId;   // optional
}


