package com.recipe.controller.admin;

import com.recipe.domain.dto.admin.MetricsResponseDTO;
import com.recipe.domain.entity.RecommendMetrics;
import com.recipe.repository.RecommendMetricsRepository;
import com.recipe.service.admin.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/admin")
public class MetricsAdminController {

    private final MetricsService metricsService;
    private final RestClient flaskRestClient; // baseUrl = http://127.0.0.1:5000
    private final RecommendMetricsRepository metricsRepository;

    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponseDTO> metrics() {
        log.info("[ADMIN][METRICS] GET /admin/metrics requested");
        MetricsResponseDTO latest = metricsService.getLatest();

        // DTO 구조가 다를 수 있어서 안전하게 toString 기반으로 남김
        log.info("[ADMIN][METRICS] latest={}", latest);

        return ResponseEntity.ok(latest);
    }

    /**
     * 프론트 "지표 재계산" 버튼이 호출
     * - Flask: GET /api/admin/metrics
     * - 받아온 raw key들을 최대한 흡수해서 DB 저장
     * - raw / saved를 응답에도 포함 (프론트 디버그 가능)
     */
    @PostMapping("/metrics/recompute")
    public ResponseEntity<?> recompute() {
        long started = System.currentTimeMillis();
        log.info("[ADMIN][METRICS] POST /admin/metrics/recompute START at={}", LocalDateTime.now());

        Map body;
        try {
            body = flaskRestClient.get()
                    .uri("/api/admin/metrics")
                    .retrieve()
                    .body(Map.class);

            log.info("[ADMIN][METRICS] Flask raw response={}", body);

        } catch (Exception e) {
            log.error("[ADMIN][METRICS] Flask call FAILED: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "ok", false,
                    "message", "Flask 연결 실패: " + e.getMessage()
            ));
        }

        double recall = pickDouble(body,
                "recallAt10", "recall_at_10", "recall@10", "recall10", "recall");
        double ndcg = pickDouble(body,
                "ndcgAt10", "ndcg_at_10", "ndcg@10", "ndcg10", "ndcg");
        double hit = pickDouble(body,
                "hitRateAt10", "hit_rate_at_10", "hit@10", "hit10", "hitRate", "hit");
        double cov = pickDouble(body,
                "coverage", "cov", "coverageRate", "coverage_rate", "itemCoverage", "item_coverage");

        log.info("[ADMIN][METRICS] parsed -> recallAt10={}, ndcgAt10={}, hitRateAt10={}, coverage={}",
                recall, ndcg, hit, cov);

        RecommendMetrics saved = metricsRepository.save(
                RecommendMetrics.builder()
                        .recallAt10(recall)
                        .ndcgAt10(ndcg)
                        .hitRateAt10(hit)
                        .coverage(cov)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        long tookMs = System.currentTimeMillis() - started;
        log.info("[ADMIN][METRICS] DB saved id={}, createdAt={}, tookMs={}",
                (saved != null ? saved.getId() : null),
                (saved != null ? saved.getCreatedAt() : null),
                tookMs);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "saved", Map.of(
                        "recallAt10", recall,
                        "ndcgAt10", ndcg,
                        "hitRateAt10", hit,
                        "coverage", cov
                ),
                "raw", body,
                "tookMs", tookMs
        ));
    }

    private double pickDouble(Map body, String... keys) {
        if (body == null) return 0.0;
        for (String k : keys) {
            Object v = body.get(k);
            Double parsed = toDoubleOrNull(v);
            if (parsed != null) return parsed;
        }
        return 0.0;
    }

    private Double toDoubleOrNull(Object v) {
        if (v == null) return null;
        try {
            String s = String.valueOf(v).trim();
            if (s.isEmpty()) return null;
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }
}
