package com.recipe.repository;

import com.recipe.domain.entity.RecommendMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecommendMetricsRepository extends JpaRepository<RecommendMetrics, Long> {
    Optional<RecommendMetrics> findTopByOrderByCreatedAtDesc();
}