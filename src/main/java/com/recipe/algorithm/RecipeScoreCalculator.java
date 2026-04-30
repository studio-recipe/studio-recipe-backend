package com.recipe.algorithm;

import com.recipe.domain.entity.UserReferences;
import com.recipe.domain.entity.enums.PreferenceType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 사용자의 행동(UserReferences)을 기반으로 레시피 점수를 계산하는 클래스
 */
@Component
public class RecipeScoreCalculator {

    /**
     * 행동별 가중치 (가장 중요)
     */
    private static final double VIEW_WEIGHT = 0.5;
    private static final double LIKE_WEIGHT = 3.0;

    /**
     * 사용자 선호(UserReferences) 리스트로부터 레시피별 점수를 계산
     */
    public Map<Long, Double> calculateScore(List<UserReferences> references) {
        Map<Long, Double> recipeScores = new HashMap<>();

        for (UserReferences ref : references) {
            Long recipeId = ref.getRecipe().getRcpSno();
            double score = 0.0;

            // 행동 유형에 따른 점수 부여
            if (ref.getPreference() == PreferenceType.LIKE) {
                score += LIKE_WEIGHT;
            } else if (ref.getPreference() == PreferenceType.VIEW) {
                score += VIEW_WEIGHT;
            }

            recipeScores.put(recipeId, recipeScores.getOrDefault(recipeId, 0.0) + score);
        }

        return recipeScores;
    }
}
