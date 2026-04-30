package com.recipe.domain.dto.auth;

import lombok.*;

//레디스에 Value로 저장
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenData {
    private String email;
    private TokenPurpose tokenPurpose;
    //timestamp 고민
}
