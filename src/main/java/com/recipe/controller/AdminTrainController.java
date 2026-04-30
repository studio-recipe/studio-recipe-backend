package com.recipe.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/train-bpr")
public class AdminTrainController {

    private final RestClient flaskRestClient;

    @PostMapping
    public ResponseEntity<?> startTrain() {
        try {
            // Flask: POST /api/admin/train-bpr
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

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        try {
            Object res = flaskRestClient.get()
                    .uri("/api/admin/train-bpr/status")
                    .retrieve()
                    .body(Object.class);
            return ResponseEntity.ok(res);
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