package com.recipe.domain.dto.auth;

import lombok.Builder;
import lombok.Getter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@Builder
@Getter
public class TokenResponseDTO {

    private String accessToken;
    private Long accessTokenExpiresIn;
    private String refreshToken;
    private Long refreshTokenExpiresIn;
    private String role;
}
