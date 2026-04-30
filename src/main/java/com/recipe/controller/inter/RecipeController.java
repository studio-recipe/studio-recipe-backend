package com.recipe.controller.inter;

import com.recipe.domain.dto.Recipe.RecipeResponseDTO;
import com.recipe.domain.dto.auth.CustomerDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

@Tag(name = "레시피", description = "레시피에 대한 API 명세서")
public interface RecipeController {


    @Operation(summary = "메인 페이지",
            description = "전체 레시피 조건에 따라 페이지 반환",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 Page or Size 전달로 조회된 데이터 없음")
            })
    public ResponseEntity<Page<RecipeResponseDTO>> mainPage(int page, int size, String direction, String sortBy);



}
