package com.recipe.service;

import com.recipe.domain.dto.recipe.RecipeResponseDTO;
import com.recipe.domain.entity.Recipe;
import com.recipe.exceptions.recipe.RecipeExceptions;
import com.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserReferencesService referenceService;

    @Transactional
    public Page<RecipeResponseDTO> readRecipePage(Pageable pageable) {
        log.info("Service readRecipePage");
        Page<Recipe> recipePage = recipeRepository.findAll(pageable);
        if(recipePage.getNumberOfElements() <= 0){
            log.info("Request Page page >>>>> {}", recipePage.getTotalElements());
            log.info("Request Page size >>>>> {}", recipePage.getSize());
            log.info("Response Page Total Count >>>>> {}", recipePage.getNumberOfElements());
            throw RecipeExceptions.NOT_FOUND.getRecipeException();
        }
        return recipePage.map(RecipeResponseDTO::fromEntity);
    }

    @Transactional
    public  RecipeResponseDTO findOneRecipe(Long recipeId, Long userId) {
        log.info("Service findOneRecipe");

        Recipe findRecipe = findByRecipeId(recipeId);

        findRecipe.viewCountUp();
        referenceService.userRecipeView(findRecipe, userId);

        return RecipeResponseDTO.fromEntity(findRecipe);
    }

    public Recipe findByRecipeId(Long recipeId) {
        return recipeRepository.findById(recipeId).orElseThrow(
                () -> RecipeExceptions.NOT_FOUND.getRecipeException()
        );
    }
}
