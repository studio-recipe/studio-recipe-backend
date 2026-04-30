package com.recipe.algorithm;

import com.recipe.domain.entity.Recipe;
import com.recipe.domain.entity.UserReferences;
import com.recipe.repository.RecipeRepository;
import com.recipe.repository.UserReferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecipeRecommendAlgorithm {

    private final UserReferencesRepository userReferencesRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeScoreCalculator scoreCalculator;

    /**
     * 사용자별 추천 레시피 계산
     */
    public List<RecommendationResult> recommendRecipes(Long userId) {
        // 사용자 선호 로그 가져오기
        List<UserReferences> userReferences = userReferencesRepository.findAll().stream()
                .filter(ref -> ref.getUser().getUserId().equals(userId))
                .collect(Collectors.toList());

        if (userReferences.isEmpty()) {
            // 로그가 없을 경우 기본 추천 (좋아요 수 기준)
            return recipeRepository.findTop10ByOrderByRcmmCntDesc().stream()
                    .map(recipe -> new RecommendationResult(recipe, 0.0))
                    .collect(Collectors.toList());
        }

        // 점수 계산
        Map<Long, Double> recipeScores = scoreCalculator.calculateScore(userReferences);

        // 전체 레시피 가져오기
        List<Recipe> allRecipes = recipeRepository.findAll();

        // 점수순 정렬
        return allRecipes.stream()
                .filter(recipe -> recipeScores.containsKey(recipe.getRcpSno()))
                .sorted(Comparator.comparingDouble(
                        (Recipe r) -> recipeScores.getOrDefault(r.getRcpSno(), 0.0)
                ).reversed())
                .limit(10)
                .map(recipe -> new RecommendationResult(recipe, recipeScores.get(recipe.getRcpSno())))
                .collect(Collectors.toList());
    }
}
