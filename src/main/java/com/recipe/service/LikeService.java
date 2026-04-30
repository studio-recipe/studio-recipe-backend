package com.recipe.service;

import com.recipe.domain.dto.Recipe.RecipeResponseDTO;
import com.recipe.domain.dto.ResponseLikeStatus;
import com.recipe.domain.entity.Like;
import com.recipe.domain.entity.Recipe;
import com.recipe.domain.entity.User;
import com.recipe.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserService userService;
    private final RecipeService recipeService;
    private final UserReferencesService referencesService;

    @Transactional
    public ResponseLikeStatus likeToRecipe(Long userId, Long recipeId) {
        Recipe recipe = recipeService.findByRecipeId(recipeId);
        User user = userService.findByUser(userId);
        recipe.likeToCountUp(); 

        //레시피에 좋아요 증가 전에 악의적인 접근 및 검증, 좋아요 기록에 있는지
        Optional<Like> checkTheLike = likeRepository.findByUserAndRecipe(user, recipe);
        if(checkTheLike.isPresent()) {
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        }

        referencesService.userLikeToRecipe(recipe, user);

        Like like = Like.builder()
                .user(user)
                .recipe(recipe)
                .build();
        likeRepository.save(like);

        return ResponseLikeStatus.builder()
                .liked(true)
                .likeCount(recipe.getRcmmCnt())
                .build();
    }

    public Page<RecipeResponseDTO> likeHistory(Long userId, Pageable pageable) {
        User user = userService.findByUser(userId); //404
        Page<Like> likePage = likeRepository.findByUser(user, pageable);

        List<RecipeResponseDTO> pageResult = likePage.stream()
                .map(Like::getRecipe)
                .map(RecipeResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(pageResult, pageable, likePage.getTotalElements());
    }

    @Transactional
    public void removeLike(Long userId, Long recipeId) {
        User user = userService.findByUser(userId);
        Recipe recipe = recipeService.findByRecipeId(recipeId);

        Like like = likeRepository.findByUserAndRecipe(user, recipe)
                .orElseThrow(() -> new IllegalStateException("삭제할 기록이 없습니다."));

        referencesService.deleteByReference(recipe, user);
        likeRepository.delete(like);

        recipe.likeToCountDown();
    }
}
