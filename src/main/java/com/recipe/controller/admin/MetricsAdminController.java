package com.recipe.controller.admin;

import com.recipe.domain.dto.admin.MetricsResponseDTO;
import com.recipe.domain.entity.RecommendMetrics;
import com.recipe.repository.RecommendMetricsRepository;
import com.recipe.service.admin.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin - 추천 지표", description = "추천 시스템 성능 지표 관련 API")
public class MetricsAdminController {

    private final MetricsService metricsService;
    private final RestClient flaskRestClient;
    private final RecommendMetricsRepository metricsRepository;

    @Operation(summary = "최근 추천 지표 조회",
            description = "DB에 저장된 가장 최근 추천 성능 지표를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            })
    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponseDTO> metrics() {
        log.info("[ADMIN][METRICS] GET /admin/metrics requested");
        MetricsResponseDTO latest = metricsService.getLatest();
        log.info("[ADMIN][METRICS] latest={}", latest);
        return ResponseEntity.ok(latest);
    }

    @Operation(summary = "추천 지표 재계산",
            description = "Flask에서 지표를 가져와 DB에 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "재계산 성공 또는 Flask 연결 실패 메시지 반환")
            })
    @PostMapping("/metrics/recompute")
    public ResponseEntity<?> recompute() {
        // 기존 로직 그대로 유지
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

        double recall = pickDouble(body, "recallAt10", "recall_at_10", "recall@10", "recall10", "recall");
        double ndcg   = pickDouble(body, "ndcgAt10", "ndcg_at_10", "ndcg@10", "ndcg10", "ndcg");
        double hit    = pickDouble(body, "hitRateAt10", "hit_rate_at_10", "hit@10", "hit10", "hitRate", "hit");
        double cov    = pickDouble(body, "coverage", "cov", "coverageRate", "coverage_rate", "itemCoverage", "item_coverage");

        RecommendMetrics saved = metricsRepository.save(
                RecommendMetrics.builder()
                        .recallAt10(recall).ndcgAt10(ndcg)
                        .hitRateAt10(hit).coverage(cov)
                        .createdAt(LocalDateTime.now())
                        .build());

        long tookMs = System.currentTimeMillis() - started;
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "saved", Map.of("recallAt10", recall, "ndcgAt10", ndcg, "hitRateAt10", hit, "coverage", cov),
                "raw", body,
                "tookMs", tookMs
        ));
    }

    private double pickDouble(Map body, String... keys) {
        if (body == null) return 0.0;
        for (String k : keys) {
            Double parsed = toDoubleOrNull(body.get(k));
            if (parsed != null) return parsed;
        }
        return 0.0;
    }

    private Double toDoubleOrNull(Object v) {
        if (v == null) return null;
        try {
            String s = String.valueOf(v).trim();
            return s.isEmpty() ? null : Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }
}