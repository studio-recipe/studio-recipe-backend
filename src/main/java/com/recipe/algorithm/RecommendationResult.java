package com.recipe.algorithm;

import com.recipe.domain.entity.Recipe;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecommendationResult {
    private  Recipe recipe;
    private  double score;
}
