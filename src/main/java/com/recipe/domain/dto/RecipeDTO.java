package com.recipe.domain.dto;

import com.recipe.domain.entity.Recipe;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecipeDTO {

    private Long rcpSno;                // 레시피 고유 번호
    private String rcpTtl;              // 레시피 제목
    private String ckgNm;               // 요리명
    private Integer inqCnt;             // 조회수
    private Integer rcmmCnt;            // 추천(좋아요) 수
    private String ckgMthActoNm;        // 조리 방법 대분류
    private String ckgMtrlActoNm;       // 재료 대분류
    private String ckgKndActoNm;        // 음식 종류 대분류
    private String ckgMtrlCn;           // 재료 내용
    private String ckgInbunNm;          // 인분
    private String ckgDodfNm;           // 난이도
    private String ckgTimeNm;           // 조리 시간
    private LocalDateTime firstRegDt;   // 등록일
    private String rcpImgUrl;           // 이미지 URL

    // ✅ Entity → DTO 변환
    public static RecipeDTO fromEntity(Recipe recipe) {
        if (recipe == null) return null;
        return RecipeDTO.builder()
                .rcpSno(recipe.getRcpSno())
                .rcpTtl(recipe.getRcpTtl())
                .ckgNm(recipe.getCkgNm())
                .inqCnt(recipe.getInqCnt())
                .rcmmCnt(recipe.getRcmmCnt())
                .ckgMthActoNm(recipe.getCkgMthActoNm())
                .ckgMtrlActoNm(recipe.getCkgMtrlActoNm())
                .ckgKndActoNm(recipe.getCkgKndActoNm())
                .ckgMtrlCn(recipe.getCkgMtrlCn())
                .ckgInbunNm(recipe.getCkgInbunNm())
                .ckgDodfNm(recipe.getCkgDodfNm())
                .ckgTimeNm(recipe.getCkgTimeNm())
                .firstRegDt(recipe.getFirstRegDt())
                .rcpImgUrl(recipe.getRcpImgUrl())
                .build();
    }

    // ✅ DTO → Entity 변환
    public Recipe toEntity() {
        return Recipe.builder()
                .rcpSno(rcpSno)
                .rcpTtl(rcpTtl)
                .ckgNm(ckgNm)
                .inqCnt(inqCnt)
                .rcmmCnt(rcmmCnt)
                .ckgMthActoNm(ckgMthActoNm)
                .ckgMtrlActoNm(ckgMtrlActoNm)
                .ckgKndActoNm(ckgKndActoNm)
                .ckgMtrlCn(ckgMtrlCn)
                .ckgInbunNm(ckgInbunNm)
                .ckgDodfNm(ckgDodfNm)
                .ckgTimeNm(ckgTimeNm)
                .firstRegDt(firstRegDt)
                .rcpImgUrl(rcpImgUrl)
                .build();
    }
}
