package com.example.ffp_be.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateCommentRequest {

    @NotBlank
    @Schema(description = "수정할 댓글 내용", example = "수정된 댓글 내용")
    private String content;
}


