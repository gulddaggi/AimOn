package com.example.ffp_be.post.dto.response;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditPostResponse {

    private Long postId;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    private String body;
}
