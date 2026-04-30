package com.recipe.domain.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MetricsResponseDTO {
    private double recallAt10;
    private double ndcgAt10;
    private double hitRateAt10;
    private double coverage;
}