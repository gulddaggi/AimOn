package com.example.ffp_be.team.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponseDto {

    private Long id;
    private Long gameId;
    private Long leagueId;
    private String teamName;
    private String country;
    private Double winRate;
    private Double attackWinRate;
    private Double defenseWinRate;
    private String imgUrl;
    private Integer point;
    private Integer rank;
}
