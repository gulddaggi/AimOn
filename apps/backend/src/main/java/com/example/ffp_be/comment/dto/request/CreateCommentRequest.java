package com.example.ffp_be.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateCommentRequest {

    @NotNull
    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Nullable
    @Schema(description = "부모 댓글 ID (일반 댓글은 null)", example = "null", nullable = true)
    private Long parentCommentId;

    @NotBlank
    @Schema(description = "댓글 내용", example = "이건 댓글입니다.")
    private String content;
}
