package com.recipe.controller;

import com.recipe.domain.dto.PageRequestDTO;
import com.recipe.domain.dto.SortBy;
import com.recipe.domain.dto.auth.CustomerDetails;
import com.recipe.domain.dto.recipe.RecipeResponseDTO;
import com.recipe.service.RecipeService;
import com.recipe.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
@Tag(name = "레시피", description = "레시피 관련 API")
public class RecipeController {

    private final RecipeService recipeService;
    private final RecommendationService recommendationService;

    @Operation(summary = "메인 페이지",
            description = "전체 레시피를 조건에 따라 페이지 반환",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "데이터 없음")
            })
    @GetMapping("/main-pages")
    public ResponseEntity<Page<RecipeResponseDTO>> mainPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "CREATED_AT") String sortBy) {

        PageRequestDTO requestPage = PageRequestDTO.builder()
                .page(page)
                .size(size)
                .direction(direction)
                .sortBy(SortBy.formString(sortBy))
                .build();
        Pageable pageable = requestPage.getPageable();
        return ResponseEntity.ok(recipeService.readRecipePage(pageable));
    }

    @Operation(summary = "AI 추천 레시피",
            description = "Flask BPR+MMR 기반 개인화 추천 레시피 반환 (로그인 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "추천 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            })
    @GetMapping("/recommend-recipes")
    public ResponseEntity<List<RecipeResponseDTO>> recommendRecipes(
            @RequestParam(defaultValue = "10") int k,
            @RequestParam(defaultValue = "0.8") double lambda,
            @RequestParam(required = false) Long seedRecipeId,
            @AuthenticationPrincipal CustomerDetails customer) {

        if (customer == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(
                recommendationService.recommendForUser(customer.getUserId(), k, lambda, seedRecipeId));
    }

    @Operation(summary = "레시피 상세",
            description = "레시피 단건 조회, 조회수 증가 및 행동 로그 기록",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "레시피 없음")
            })
    @GetMapping("/recipes/{recipeId}")
    public ResponseEntity<RecipeResponseDTO> detailRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal CustomerDetails customer) {

        Long userId = (customer != null) ? customer.getUserId() : null;
        return ResponseEntity.ok(recipeService.findOneRecipe(recipeId, userId));
    }
}