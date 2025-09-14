package com.example.ffp_be.auth.dto.response;

import com.example.ffp_be.team.dto.TeamResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private List<TeamResponseDto> likedTeams;
}


