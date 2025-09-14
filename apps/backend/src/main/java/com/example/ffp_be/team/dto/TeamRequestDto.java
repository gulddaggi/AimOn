package com.example.ffp_be.team.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRequestDto {

    @NotNull
    private Long gameId;

    @NotNull
    private Long leagueId;

    @NotBlank
    @Size(max = 100)
    private String teamName;

    @NotBlank
    @Size(max = 100)
    private String country;
    private Double winRate;
    private Double attackWinRate;
    private Double defenseWinRate;
    private Integer point;
    private String imgUrl;
}
