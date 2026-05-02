package com.recipe.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/train-bpr")
@Tag(name = "Admin - BPR 학습", description = "Flask BPR 모델 학습 관련 API")
public class AdminTrainController {

    private final RestClient flaskRestClient;

    @Operation(summary = "BPR 모델 학습 시작",
            description = "Flask 서버에 BPR 모델 학습을 요청합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "요청 성공 또는 Flask 연결 실패 메시지 반환")
            })
    @PostMapping
    public ResponseEntity<?> startTrain() {
        try {
            Object res = flaskRestClient.post()
                    .uri("/api/admin/train-bpr")
                    .retrieve()
                    .body(Object.class);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "ok", false,
                    "message", "Flask 연결 실패: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "BPR 학습 상태 조회",
            description = "Flask 서버의 현재 학습 진행 상태를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            })
    @GetMapping("/status")
    public ResponseEntity<?> status() {
        try {
            return ResponseEntity.ok(flaskRestClient.get()
                    .uri("/api/admin/train-bpr/status")
                    .retrieve()
                    .body(Object.class));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "state", Map.of(
                            "running", false,
                            "last_success", false,
                            "last_error", "Flask 연결 실패: " + e.getMessage(),
                            "last_started_at", (String) null,
                            "last_finished_at", (String) null,
                            "server_time", OffsetDateTime.now().toString()
                    )
            ));
        }
    }
}