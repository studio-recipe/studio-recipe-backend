package com.recipe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "사용자", description = "사용자 관련 API (미구현)")
public class UserController {

    @Operation(summary = "마이페이지 조회", description = "미구현")
    @GetMapping("/my-pages/{userId}")
    public ResponseEntity<Void> myPage(@PathVariable Long userId) {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "마이페이지 수정", description = "미구현")
    @PutMapping("/my-pages")
    public ResponseEntity<Void> updateMyPage() {
        return ResponseEntity.ok().build();
    }
}