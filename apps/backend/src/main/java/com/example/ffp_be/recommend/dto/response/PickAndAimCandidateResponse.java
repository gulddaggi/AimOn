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
public class PickAndAimCandidateResponse {

    private Long teamId;
    private String teamName;
    private Long leagueId;
    private String leagueName;
    private double totalScore;
    private Integer rank;
}


