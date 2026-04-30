package com.recipe.domain.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NicknameAvailabilityResponse {
    private boolean isAvailable;
    private String message;
}
