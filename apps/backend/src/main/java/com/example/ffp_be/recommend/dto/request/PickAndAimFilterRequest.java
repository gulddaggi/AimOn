package com.example.ffp_be.recommend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
public class PickAndAimFilterRequest {

    @NotNull
    private Long gameId;

    private Long leagueId;

    @NotEmpty
    private List<String> keywords;
}


