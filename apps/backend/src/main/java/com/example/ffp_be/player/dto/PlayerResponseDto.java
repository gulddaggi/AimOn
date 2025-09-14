package com.example.ffp_be.player.dto;

import com.example.ffp_be.player.stats.valorant.dto.ValorantPlayerStatsResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerResponseDto {

    private Long id;
    private Long teamId;
    private Long gameId;
    private String name;
    private String handle;
    private String country;
    private String imgUrl;
    private ValorantPlayerStatsResponseDto valorantStats;
}
