package com.example.ffp_be.clip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateClipRequest {

    @NotNull
    private Long gameId;

    @NotNull
    private Long teamId;

    @NotBlank
    private String youtubeUrl;

    @NotBlank
    private String title;

    @NotNull
    private Long userId;   // ✅ 등록하는 유저 ID
}
