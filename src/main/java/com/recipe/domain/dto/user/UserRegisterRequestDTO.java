package com.recipe.domain.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.recipe.domain.entity.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRegisterRequestDTO {
    @NotBlank(message = "아이디를 입력하세요")
    @Size(min = 8, max = 16, message = "아이디는 최소 8자 최대 16자입니다.")
    private String id;

    @NotBlank(message = "비밀번호를 입력하세요")
//    @Size(min = 8, max = 32, message = "비밀번호는 최소 8자 최대 32자입니다.")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])(?=\\S+$).{8,32}$",
            message = "비밀번호는 8자 이상 32자 이하이며, 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다.")
    private String password;

    @NotBlank(message = "이름을 입력하세요")
//    @Size
    private String name;

    @NotBlank(message = "닉네임을 입력하세요")
//    @Size
    private String nickname;

    @NotBlank(message = "이메일을 입력하세요")
    @Email(message = "옳바른 이메일 형식이 아닙니다.")
    private String email;

    @NotNull(message = "생년월일은 필수 입니다.")
    @Past(message = "생년월일은 현재를 넘길 수 없습니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)//역질렬화 되지 않으면 definition error
    private LocalDate birth;

    @NotNull(message = "성별은 필수 값입니다.")
    private Gender gender;
}
