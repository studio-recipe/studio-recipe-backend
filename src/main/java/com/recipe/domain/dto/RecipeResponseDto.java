package com.recipe.domain.dto;

import com.recipe.algorithm.RecommendationResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 추천 결과를 클라이언트에 전달하기 위한 DTO
 */
@Getter
@Builder
public class RecipeResponseDto {

    private List<RecipeDto> recommendedRecipes;

    public static RecipeResponseDto of(List<RecommendationResult> results) {
        return RecipeResponseDto.builder()
                .recommendedRecipes(
                        results.stream()
                                .map(r -> RecipeDto.builder()
                                        .id(r.getRecipe().getRcpSno())          // Recipe 엔티티의 필드명과 매칭
                                        .title(r.getRecipe().getRcpTtl())
                                        .chefName(r.getRecipe().getCkgNm())
                                        .likeCount(r.getRecipe().getRcmmCnt())
                                        .imageUrl(r.getRecipe().getRcpImgUrl())
                                        .score(r.getScore())                    // 알고리즘에서 계산된 점수
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Getter
    @Builder
    public static class RecipeDto {
        private Long id;           // rcpSno
        private String title;      // rcpTtl
        private String chefName;   // ckgNm
        private String name;
        private Integer likeCount; // Cnt
        private String imageUrl;   // rcpImgUrl
        private double score;      // 추천 점수
    }
}
