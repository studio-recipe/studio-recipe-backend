package com.recipe.domain.dto.auth;

//레디스에 저장될 토큰
public enum TokenPurpose {
    FIND_ID,
    RESET_PASSWORD,
}
