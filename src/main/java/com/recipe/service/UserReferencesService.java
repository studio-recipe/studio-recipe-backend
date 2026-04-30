package com.recipe.service;

import com.recipe.domain.entity.Recipe;
import com.recipe.domain.entity.User;
import com.recipe.domain.entity.UserReferences;
import com.recipe.domain.entity.enums.PreferenceType;
import com.recipe.repository.UserReferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserReferencesService {

    private final UserReferencesRepository referenceRepository;
    private final UserService userService;

    @Transactional
    public void upsertPreference(Recipe recipe, Long userId, PreferenceType newType) {
        User user = userService.findByUser(userId);

        UserReferences ref = referenceRepository
                .findByUserAndRecipe(user, recipe)
                .orElseGet(() -> UserReferences.builder()
                        .user(user)
                        .recipe(recipe)
                        .preference(PreferenceType.VIEW) // 초기값은 VIEW로
                        .build()
                );

        PreferenceType current = ref.getPreference();

        // 1) VIEW가 들어오면:
        //    현재 VIEW면 (조회 갱신)
        //    현재 LIKE면 유지
        //    현재 UNLIKE면 VIEW로
        // 2) LIKE가 들어오면 LIKE로
        // 3) UNLIKE가 들어오면 UNLIKE로 변경
        switch (newType) {
            case VIEW -> {
                if (current == PreferenceType.VIEW) {
                    ref.updateModifiedDate();
                } else if (current == PreferenceType.UNLIKE) {
                    ref.changePreference(PreferenceType.VIEW);
                }
            }
            case LIKE -> {
                if (current != PreferenceType.LIKE) {
                    ref.changePreference(PreferenceType.LIKE);
                } else {
                    ref.updateModifiedDate();
                }
            }
            case UNLIKE -> {
                if (current != PreferenceType.UNLIKE) {
                    ref.changePreference(PreferenceType.UNLIKE);
                } else {
                    ref.updateModifiedDate();
                }
            }
        }

        referenceRepository.save(ref);
    }

    @Transactional
    public void userRecipeView(Recipe recipe, Long userId) {
        if (userId != null) {
            upsertPreference(recipe, userId, PreferenceType.VIEW);
        }
    }

    @Transactional
    public void userLikeToRecipe(Recipe recipe, User user) {
        upsertPreference(recipe, user.getUserId(), PreferenceType.LIKE);
    }

    /*
    - 좋아요 취소 시 삭제 시키지 않고 UNLIKE로 남김
     */
    @Transactional
    public void deleteByReference(Recipe recipe, User user) {
        upsertPreference(recipe, user.getUserId(), PreferenceType.UNLIKE);
    }
}