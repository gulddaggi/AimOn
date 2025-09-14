package com.example.ffp_be.league.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueResponseDto {
    private Long id;
    private Long gameId;
    private String name;
}
