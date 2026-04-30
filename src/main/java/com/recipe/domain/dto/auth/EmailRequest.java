package com.recipe.domain.dto.auth;

import lombok.Getter;

@Getter
public class EmailRequest {
    String email;
    private TokenPurpose purpose;
}
