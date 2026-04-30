package com.recipe.controller;

import com.recipe.controller.inter.LikeController;
import com.recipe.domain.dto.Recipe.RecipeResponseDTO;
import com.recipe.domain.dto.ResponseLikeStatus;
import com.recipe.domain.dto.auth.CustomerDetails;
import com.recipe.service.LikeService;
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
public class LikeControllerImpl implements LikeController {

    private final LikeService likeService;


    @PostMapping("/likes/{recipeId}")
    public ResponseEntity<ResponseLikeStatus> likeToRecipe(
            @PathVariable("recipeId") Long recipeId,
            @AuthenticationPrincipal CustomerDetails customer) {
        Long userId = customer.getUserId();
        log.info("Controller 좋아요 이벤트!");
        log.info("customer: {}", customer);
        log.info("userId:{}", customer.getUserId());
        ResponseLikeStatus likeStatus = likeService.likeToRecipe(userId, recipeId);

        return ResponseEntity.ok().body(likeStatus);
    }

    @DeleteMapping("/likes/{recipeId}")
    public ResponseEntity<Void> deleteLike(
            @PathVariable("recipeId") Long recipeId,
            @AuthenticationPrincipal CustomerDetails customer) {
        Long userId = customer.getUserId();

        likeService.removeLike(userId, recipeId);
        return ResponseEntity.noContent().build();
    }

    //사용자 좋아요 기록 반환
    @GetMapping("/likes")
    public ResponseEntity<Page<RecipeResponseDTO>> likesHistory(@AuthenticationPrincipal CustomerDetails customer) {
        Long userId = customer.getUserId();

        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createDate"));

        Page<RecipeResponseDTO> page = likeService.likeHistory(userId, pageable);
        return ResponseEntity.ok(page);
    }
}
