package com.recipe.service;

import com.recipe.algorithm.RecipeRecommendAlgorithm;
import com.recipe.algorithm.RecommendationResult;
import com.recipe.domain.dto.RecipeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 추천 알고리즘을 호출하여 DTO로 변환하는 서비스
 */
@Service
@RequiredArgsConstructor
public class RecipeRecommendService {

    private final RecipeRecommendAlgorithm recommendAlgorithm;

    public RecipeResponseDto getRecommendedRecipes(Long userId) {
        List<RecommendationResult> results = recommendAlgorithm.recommendRecipes(userId);
        return RecipeResponseDto.of(results);
    }
}
