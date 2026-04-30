package com.recipe.controller.inter;

import com.recipe.domain.dto.Recipe.RecipeResponseDTO;
import com.recipe.domain.dto.ResponseLikeStatus;
import com.recipe.domain.dto.auth.CustomerDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

@Tag(name = "Like API", description = "좋아요 관련 API")
public interface LikeController {
    @Operation(summary = "레시피 좋아요",
            description = "레시피 좋아요 정보와 사용자 좋아요 기록을 업데이트.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 이벤트 발생"),
                    @ApiResponse(responseCode = "409", description = "좋아요 중복 발생"),
                    @ApiResponse(responseCode = "500", description = "그 외 데이터베이스 제약 조건 위반")
            })
    public ResponseEntity<ResponseLikeStatus>
    likeToRecipe(Long recipeId, CustomerDetails customer);

    @Operation(summary = "레시피 좋아요 기록", description = "사용자는 자신이 좋아요를 눌렀던 레시피들을 볼 수 있다.",
    responses = {
            @ApiResponse(responseCode = "200", description = "정상 반환"),
            @ApiResponse(responseCode = "404", description = "해당 회원을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "403", description = "토큰 관련 문제")
    })
    public ResponseEntity<Page<RecipeResponseDTO>> likesHistory(CustomerDetails customer);

    @Operation(summary = "좋아요 취소", description = "레시피 좋아요 기록 삭제 && 좋아요 취소(레시피 좋아요 -1)",
    responses = {
            @ApiResponse(responseCode = "204", description = "정상 삭제&&취소"),
            @ApiResponse(responseCode = "409", description = "삭제 및 취소할 좋아요가 없음"),
            @ApiResponse(responseCode = "403", description = "토큰 권한 없음")
    })
    public ResponseEntity<Void> deleteLike(Long recipeId, CustomerDetails customer);
}
