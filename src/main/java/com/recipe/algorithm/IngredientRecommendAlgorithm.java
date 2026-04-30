package com.recipe.algorithm;

import com.recipe.domain.entity.Recipe;
import com.recipe.repository.RecipeRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 사용자의 보유 재료를 기반으로
 * 인기 + 재료 일치율 기반의 정밀 추천 알고리즘
 */
@Component
@RequiredArgsConstructor
public class IngredientRecommendAlgorithm {

    private final RecipeRepository recipeRepository;

    /**
     * 사용자 입력 재료 기반 추천
     */
    public List<IngredientRecommendationResult> recommendByIngredients(List<String> userIngredients) {
        List<Recipe> allRecipes = recipeRepository.findAll();
        Map<Long, Double> recipeScores = new HashMap<>();

        for (Recipe recipe : allRecipes) {
            String materialText = recipe.getCkgMtrlCn();
            if (materialText == null || materialText.isBlank()) continue;

            // 재료 파싱
            List<String> recipeIngredients = Arrays.stream(materialText.split("[,\\n]"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            // 일치 개수 계산
            long matchedCount = recipeIngredients.stream()
                    .filter(ri -> userIngredients.stream().anyMatch(ui -> ri.contains(ui)))
                    .count();

            double ingredientMatchRate = recipeIngredients.isEmpty()
                    ? 0.0
                    : (double) matchedCount / recipeIngredients.size();

            double popularityScore = recipe.getRcmmCnt() != null ? recipe.getRcmmCnt() : 0;

            // 최종 점수 계산 (정밀 추천)
            double finalScore = ingredientMatchRate * 0.7 + (popularityScore / 1000.0) * 0.3;

            recipeScores.put(recipe.getRcpSno(), finalScore);
        }

        // 정렬 및 상위 10개 선택
        return allRecipes.stream()
                .filter(r -> recipeScores.containsKey(r.getRcpSno()))
                .sorted(Comparator.comparingDouble(
                        (Recipe r) -> recipeScores.getOrDefault(r.getRcpSno(), 0.0)
                ).reversed())
                .limit(10)
                .map(r -> new IngredientRecommendationResult(
                        r.getRcpSno(),
                        r.getRcpTtl(),
                        r.getRcpImgUrl(),
                        recipeScores.get(r.getRcpSno())
                ))
                .collect(Collectors.toList());
    }

    /**
     * 추천 결과 DTO
     */
    @Getter
    public static class IngredientRecommendationResult {
        private final Long recipeId;
        private final String recipeTitle;
        private final String recipeImage;
        private final Double score;

        public IngredientRecommendationResult(Long recipeId, String recipeTitle, String recipeImage, Double score) {
            this.recipeId = recipeId;
            this.recipeTitle = recipeTitle;
            this.recipeImage = recipeImage;
            this.score = score;
        }
    }
}
