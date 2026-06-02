package com.recipe.controller;

import com.recipe.config.CustomerDetails;
import com.recipe.domain.dto.recipe.*;
import com.recipe.service.ImageUploadService;
import com.recipe.service.RecipeService;
import com.recipe.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
@Tag(name = "레시피", description = "레시피 관련 API")
public class RecipeController {

    private final RecipeService recipeService;
    private final RecommendationService recommendationService;
    private final ImageUploadService imageUploadService;

    @GetMapping("/main-pages")
    public ResponseEntity<Page<RecipeResponseDTO>> mainPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "CREATED_AT") String sortBy) {

        PageRequestDTO requestPage = PageRequestDTO.builder()
                .page(page).size(size).direction(direction)
                .sortBy(SortBy.formString(sortBy)).build();
        Pageable pageable = requestPage.getPageable();
        return ResponseEntity.ok(recipeService.readRecipePage(pageable));
    }

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

    @GetMapping("/recipes/{recipeId}")
    public ResponseEntity<RecipeResponseDTO> detailRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal CustomerDetails customer) {

        Long userId = (customer != null) ? customer.getUserId() : null;
        return ResponseEntity.ok(recipeService.findOneRecipe(recipeId, userId));
    }

    @Operation(summary = "레시피 등록",
            description = "로그인한 회원이 레시피와 이미지를 함께 등록합니다. (multipart/form-data)",
            responses = {
                    @ApiResponse(responseCode = "201", description = "등록 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            })
    @PostMapping(value = "/recipes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeResponseDTO> createRecipe(
            @RequestPart("data") @Valid RecipeRequestDTO request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomerDetails customer) {

        // 이미지 저장 후 URL을 DTO에 세팅
        String imageUrl = imageUploadService.upload(image);
        request.setRcpImgUrl(imageUrl);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipeService.createRecipe(request, customer.getUserId()));
    }

    @Operation(summary = "레시피 수정",
            description = "본인이 등록한 레시피만 수정 가능합니다. (multipart/form-data)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "레시피 없음")
            })
    @PutMapping(value = "/recipes/{recipeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeResponseDTO> updateRecipe(
            @PathVariable Long recipeId,
            @RequestPart("data") @Valid RecipeRequestDTO request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomerDetails customer) {

        // 이미지가 있으면 새로 저장, 없으면 기존 URL 유지
        if (image != null && !image.isEmpty()) {
            String imageUrl = imageUploadService.upload(image);
            request.setRcpImgUrl(imageUrl);
        }

        return ResponseEntity.ok(
                recipeService.updateRecipe(recipeId, request, customer.getUserId()));
    }

    @Operation(summary = "레시피 삭제",
            description = "본인이 등록한 레시피만 삭제 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "레시피 없음")
            })
    @DeleteMapping("/recipes/{recipeId}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal CustomerDetails customer) {

        recipeService.deleteRecipe(recipeId, customer.getUserId());
        return ResponseEntity.noContent().build();
    }
}
