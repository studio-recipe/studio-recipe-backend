package com.recipe.controller;

import io.swagger.v3.oas.annotations.Hidden;
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
@Hidden // Todo
public class UserController {

    @GetMapping("/my-pages/{userId}")
    public ResponseEntity<Void> myPage(@PathVariable Long userId) {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/my-pages")
    public ResponseEntity<Void> updateMyPage() {
        return ResponseEntity.ok().build();
    }
}