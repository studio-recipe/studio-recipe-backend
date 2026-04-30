package com.recipe.domain.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class ResetProcessResponse {
    private String message;
    private String token;
}
