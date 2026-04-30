package com.recipe.domain.dto.user;

import com.recipe.domain.entity.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

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
