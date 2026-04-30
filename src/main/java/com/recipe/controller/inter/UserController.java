package com.recipe.controller.inter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "User", description = "사용자 관련 API")
public interface UserController {
    @Operation(summary = "회원 상세 페이지",
            description = "회원 자신의 정보를 볼 수 있다.(사용자 정보, 찜한 목록, 사용자 목록)")
    ResponseEntity<Void> myPage(Long userId);

}
