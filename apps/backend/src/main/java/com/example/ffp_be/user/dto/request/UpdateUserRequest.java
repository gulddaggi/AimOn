package com.example.ffp_be.user.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserRequest {

    private String nickname;
    private String profileImageUrl;
}
