package com.recipe.controller;

import com.recipe.controller.inter.RecipeController;
import com.recipe.domain.dto.PageRequestDTO;
import com.recipe.domain.dto.Recipe.RecipeResponseDTO;
import com.recipe.domain.dto.SortBy;
import com.recipe.domain.dto.auth.CustomerDetails;
import com.recipe.service.RecipeService;
import com.recipe.service.AuthService;
import com.recipe.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
class RecipeControllerImpl implements RecipeController {

    private final RecipeService recipeService;
    private final RecommendationService recommendationService;


    @GetMapping("/recommend-recipes")
    public ResponseEntity<List<RecipeResponseDTO>> recommendRecipes(
            @RequestParam(defaultValue = "10") int k,
            @RequestParam(defaultValue = "0.8") double lambda,
            @RequestParam(required = false) Long seedRecipeId,
            @AuthenticationPrincipal CustomerDetails customer
    ) {
        if (customer == null) return ResponseEntity.status(401).build();

        Long userId = customer.getUserId();
        List<RecipeResponseDTO> result =
                recommendationService.recommendForUser(userId, k, lambda, seedRecipeId);

        return ResponseEntity.ok(result);
    }



    @GetMapping("/main-pages")
    public ResponseEntity<Page<RecipeResponseDTO>> mainPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "CREATED_AT") String sortBy
    ){
        SortBy sortByEnum = SortBy.formString(sortBy);
        if (sortByEnum == null) {
            sortByEnum = SortBy.CREATED_AT; // fallback to default
        }
        
        PageRequestDTO requestPage = PageRequestDTO.builder()
                .page(page)
                .size(size)
                .direction(direction)
                .sortBy(sortByEnum)
                .build();

        Pageable pageable = requestPage.getPageable();
        Page<RecipeResponseDTO> recipePage = recipeService.readRecipePage(pageable);

        return ResponseEntity.ok(recipePage);
    }

    @GetMapping("recipes/{recipeId}")
    public ResponseEntity<RecipeResponseDTO> detailRecipe(
            @PathVariable("recipeId") Long recipeId,
            @AuthenticationPrincipal CustomerDetails customer){

        Long userId = (customer != null) ? customer.getUserId() : null;
        RecipeResponseDTO recipe = recipeService.findOneRecipe(recipeId, userId);
        return ResponseEntity.ok(recipe);
    }
}
