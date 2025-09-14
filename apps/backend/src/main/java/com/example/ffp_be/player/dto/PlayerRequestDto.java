package com.example.ffp_be.player.dto;

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
public class PlayerRequestDto {

    private Long teamId;
    private Long gameId;
    private String name;
    private String handle;
    private String country;

}
