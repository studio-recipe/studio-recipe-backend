package com.recipe.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class FlaskRecommendResponse {
    private Long userId;
    private Integer count;
    private List<Recommendation> recommendations;

    @Data
    public static class Recommendation {
        private Long recipeId;
        private Double score;
    }
}