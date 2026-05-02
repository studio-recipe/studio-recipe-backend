package com.recipe.domain.dto.like;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseLikeStatus {
    private boolean liked;
    private int likeCount;
}
