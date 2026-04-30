package com.recipe.service.admin;

import com.recipe.domain.dto.admin.MetricsResponseDTO;
import com.recipe.domain.entity.RecommendMetrics;
import com.recipe.repository.RecommendMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final RecommendMetricsRepository repo;

    public MetricsResponseDTO getLatest() {
        RecommendMetrics m = repo.findTopByOrderByCreatedAtDesc()
                .orElseGet(() -> new RecommendMetrics(0.0, 0.0, 0.0, 0.0));

        return MetricsResponseDTO.builder()
                .recallAt10(m.getRecallAt10())
                .ndcgAt10(m.getNdcgAt10())
                .hitRateAt10(m.getHitRateAt10())
                .coverage(m.getCoverage())
                .build();
    }
}