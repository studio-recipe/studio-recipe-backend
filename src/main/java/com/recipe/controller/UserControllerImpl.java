package com.recipe.controller;

import com.recipe.controller.inter.AuthController;
import com.recipe.controller.inter.UserController;
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
public class UserControllerImpl implements UserController{

    @GetMapping("/my-pages/{userId}")
    public ResponseEntity<Void> myPage(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "회원 상세 페이지 수정",
            description = "회원이 수정한 데이터로 회원 테이블 수정")
    @PutMapping("/my-pages")
    public ResponseEntity<Void> updateMyPage(/*@RequestBody*/) {
        return ResponseEntity.ok().build();
    }

    //탈퇴
    
}
