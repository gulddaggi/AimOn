package com.example.ffp_be.recommend.dto.response;

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
public class PickAndAimLeagueResponse {

    private Long gameId;
    private String gameName;
    private Long leagueId;
    private String leagueName;
}


