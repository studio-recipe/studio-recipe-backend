package com.recipe.service;

import com.recipe.domain.dto.recipe.RecipeRequestDTO;
import com.recipe.domain.dto.recipe.RecipeResponseDTO;
import com.recipe.domain.entity.Recipe;
import com.recipe.domain.entity.User;
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
    private final UserService userService;
    private final ViewCountService viewCountService;

    @Transactional
    public Page<RecipeResponseDTO> readRecipePage(Pageable pageable) {
        log.info("Service readRecipePage");
        Page<Recipe> recipePage = recipeRepository.findAll(pageable);
        if (recipePage.getNumberOfElements() <= 0) {
            throw RecipeExceptions.NOT_FOUND.getRecipeException();
        }
        return recipePage.map(RecipeResponseDTO::fromEntity);
    }

    @Transactional
    public RecipeResponseDTO findOneRecipe(Long recipeId, Long userId) {
        Recipe findRecipe = findByRecipeId(recipeId);

        // Redis에 쌓인 조회수 합산
        Long redisCount = viewCountService.getRedisViewCount(recipeId);
        int totalInqCnt = (findRecipe.getInqCnt() == null ? 0 : findRecipe.getInqCnt())
                + redisCount.intValue();

        // 조회수 증가 → Redis -> Batch
        viewCountService.incrementViewCount(recipeId);

        // 행동 로그 → 로그인한 사용자만
        referenceService.userRecipeView(findRecipe, userId);

        // inqCnt를 합산값으로 override해서 반환
        return RecipeResponseDTO.fromEntity(findRecipe, totalInqCnt);
    }

    @Transactional
    public RecipeResponseDTO createRecipe(RecipeRequestDTO request, Long userId) {
        log.info("Service createRecipe userId={}", userId);
        User author = userService.findByUser(userId);
        Recipe recipe = request.toEntity(author);
        return RecipeResponseDTO.fromEntity(recipeRepository.save(recipe));
    }

    @Transactional
    public RecipeResponseDTO updateRecipe(Long recipeId, RecipeRequestDTO request, Long userId) {
        log.info("Service updateRecipe recipeId={} userId={}", recipeId, userId);
        Recipe recipe = findByRecipeId(recipeId);
        if (!recipe.isAuthor(userId)) {
            throw RecipeExceptions.FORBIDDEN.getRecipeException();
        }
        recipe.update(request);
        return RecipeResponseDTO.fromEntity(recipe);
    }

    @Transactional
    public void deleteRecipe(Long recipeId, Long userId) {
        log.info("Service deleteRecipe recipeId={} userId={}", recipeId, userId);
        Recipe recipe = findByRecipeId(recipeId);
        if (!recipe.isAuthor(userId)) {
            throw RecipeExceptions.FORBIDDEN.getRecipeException();
        }
        recipeRepository.delete(recipe);
    }

    public Recipe findByRecipeId(Long recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> RecipeExceptions.NOT_FOUND.getRecipeException());
    }
}
