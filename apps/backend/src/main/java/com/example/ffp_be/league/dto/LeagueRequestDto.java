package com.example.ffp_be.league.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueRequestDto {

    @NotNull
    private Long gameId;

    @NotBlank
    @Size(min = 2, max = 100, message = "리그명은 2-100자 사이여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s]+$", message = "리그명에 특수문자는 사용할 수 없습니다.")
    private String name;
}
