package com.example.ffp_be.post.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePostRequest {

    @Size(max = 100)
    private String title;

    private String body;

    private List<String> imageKeys;
}