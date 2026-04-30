package com.recipe.domain.dto;

import com.recipe.algorithm.IngredientRecommendAlgorithm.IngredientRecommendationResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class IngredientRecommendationResponseDto {
    private List<RecipeDto> recommendedRecipes;

    public static IngredientRecommendationResponseDto of(List<IngredientRecommendationResult> results) {
        return IngredientRecommendationResponseDto.builder()
                .recommendedRecipes(
                        results.stream()
                                .map(r -> RecipeDto.builder()
                                        .id(r.getRecipeId())
                                        .title(r.getRecipeTitle())
                                        .imageUrl(r.getRecipeImage())
                                        .score(r.getScore())
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Getter
    @Builder
    public static class RecipeDto {
        private Long id;
        private String title;
        private String imageUrl;
        private Double score;
    }
}
