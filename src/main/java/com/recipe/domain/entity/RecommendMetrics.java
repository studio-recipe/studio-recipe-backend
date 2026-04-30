package com.recipe.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommend_metrics")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendMetrics {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "recall_at_10", nullable = false)
    private Double recallAt10;

    @Column(name = "ndcg_at_10", nullable = false)
    private Double ndcgAt10;

    @Column(name = "hit_rate_at_10", nullable = false)
    private Double hitRateAt10;

    @Column(name = "coverage", nullable = false)
    private Double coverage;

    public RecommendMetrics(Double recallAt10, Double ndcgAt10, Double hitRateAt10, Double coverage) {
        this.recallAt10 = recallAt10;
        this.ndcgAt10 = ndcgAt10;
        this.hitRateAt10 = hitRateAt10;
        this.coverage = coverage;
    }
}