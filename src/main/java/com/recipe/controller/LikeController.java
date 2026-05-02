package com.recipe.controller;

import com.recipe.domain.dto.like.ResponseLikeStatus;
import com.recipe.config.CustomerDetails;
import com.recipe.domain.dto.recipe.RecipeResponseDTO;
import com.recipe.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Log4j2
@RequiredArgsConstructor
@Tag(name = "좋아요", description = "레시피 좋아요 관련 API")
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "레시피 좋아요",
            description = "좋아요 추가 및 행동 로그 기록",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좋아요 성공"),
                    @ApiResponse(responseCode = "409", description = "이미 좋아요 누름")
            })
    @PostMapping("/likes/{recipeId}")
    public ResponseEntity<ResponseLikeStatus> likeToRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal CustomerDetails customer) {
        return ResponseEntity.ok(likeService.likeToRecipe(customer.getUserId(), recipeId));
    }

    @Operation(summary = "좋아요 취소",
            description = "좋아요 취소 및 행동 로그 UNLIKE 처리",
            responses = {
                    @ApiResponse(responseCode = "204", description = "취소 성공"),
                    @ApiResponse(responseCode = "409", description = "취소할 좋아요 없음")
            })
    @DeleteMapping("/likes/{recipeId}")
    public ResponseEntity<Void> deleteLike(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal CustomerDetails customer) {
        likeService.removeLike(customer.getUserId(), recipeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "좋아요 기록 조회",
            description = "사용자가 좋아요를 누른 레시피 목록 반환",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "회원 없음")
            })
    @GetMapping("/likes")
    public ResponseEntity<Page<RecipeResponseDTO>> likesHistory(
            @AuthenticationPrincipal CustomerDetails customer) {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createDate"));
        return ResponseEntity.ok(likeService.likeHistory(customer.getUserId(), pageable));
    }
}