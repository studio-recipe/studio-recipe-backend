package com.recipe.domain.dto.recipe;

import com.recipe.domain.entity.Recipe;
import com.recipe.domain.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRequestDTO {

    @NotBlank(message = "레시피 제목을 입력하세요")
    private String rcpTtl;

    @NotBlank(message = "요리명을 입력하세요")
    private String ckgNm;

    private String ckgMthActoNm;   // 조리방법
    private String ckgMtrlActoNm;  // 재료
    private String ckgKndActoNm;   // 종류
    private String ckgMtrlCn;      // 재료 상세
    private String ckgInbunNm;     // 인분
    private String ckgDodfNm;      // 난이도
    private String ckgTimeNm;      // 조리시간
    private String rcpImgUrl;      // 이미지 URL

    public Recipe toEntity(User author) {
        return Recipe.builder()
                .rcpTtl(rcpTtl)
                .ckgNm(ckgNm)
                .ckgMthActoNm(ckgMthActoNm)
                .ckgMtrlActoNm(ckgMtrlActoNm)
                .ckgKndActoNm(ckgKndActoNm)
                .ckgMtrlCn(ckgMtrlCn)
                .ckgInbunNm(ckgInbunNm)
                .ckgDodfNm(ckgDodfNm)
                .ckgTimeNm(ckgTimeNm)
                .rcpImgUrl(rcpImgUrl)
                .inqCnt(0)
                .rcmmCnt(0)
                .firstRegDt(LocalDateTime.now())
                .author(author)
                .build();
    }
}
