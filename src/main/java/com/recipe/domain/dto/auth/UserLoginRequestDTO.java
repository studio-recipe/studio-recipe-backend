package com.recipe.domain.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserLoginRequestDTO {

    @NotBlank(message = "아이디를 입력하세요")
    private String id;

    @NotBlank(message = "비밀번호를 입력하세요")
    private String password;

}
