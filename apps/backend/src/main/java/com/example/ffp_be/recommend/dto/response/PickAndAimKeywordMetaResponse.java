package com.example.ffp_be.recommend.dto.response;

import com.example.ffp_be.recommend.domain.PickKeyword;
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
public class PickAndAimKeywordMetaResponse {

    private String key;
    private String displayName;
    private String description;

    public static PickAndAimKeywordMetaResponse of(PickKeyword key, String displayName,
        String description) {
        return PickAndAimKeywordMetaResponse.builder()
            .key(key.name())
            .displayName(displayName)
            .description(description)
            .build();
    }
}


