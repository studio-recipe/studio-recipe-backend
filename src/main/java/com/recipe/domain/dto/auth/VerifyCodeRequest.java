package com.recipe.domain.dto.auth;

import lombok.Getter;

@Getter
public class VerifyCodeRequest {
    private String email;
    private String verificationCode;
    private TokenPurpose purpose;
}
