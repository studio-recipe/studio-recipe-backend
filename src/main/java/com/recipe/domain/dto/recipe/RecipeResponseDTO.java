package com.recipe.domain.dto.recipe;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.recipe.domain.entity.Recipe;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RecipeResponseDTO {
    private Long rcpSno;
    private String rcpTtl;
    private String ckgNm;
    private Integer inqCnt;
    private Integer rcmmCnt;
    private String ckgMthActoNm;
    private String ckgMtrlActoNm;
    private String ckgKndActoNm;
    private String ckgMtrlCn;
    private String ckgInbunNm;
    private String ckgDodfNm;
    private String ckgTimeNm;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime firstRegDt;
    private String rcpImgUrl;

    // 엔티티 -> DTO 변환을 위한 정적 팩토리 메서드
    public static RecipeResponseDTO fromEntity(Recipe recipe) {
        return RecipeResponseDTO.builder()
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
}
