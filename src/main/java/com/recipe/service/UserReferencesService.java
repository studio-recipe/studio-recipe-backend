package com.recipe.service;

import com.recipe.domain.entity.Recipe;
import com.recipe.domain.entity.User;
import com.recipe.domain.entity.enums.PreferenceType;
import com.recipe.repository.UserReferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserReferencesService {

    private final UserReferencesRepository referenceRepository;
    private final UserService userService;

    /**
     * 조회 후 삽입(check-then-act)이면 같은 (user, recipe)에 대한 동시 요청이 중복 row를
     * 만들 수 있어, INSERT ... ON DUPLICATE KEY UPDATE 한 문장으로 원자적으로 처리한다
     * (Unique 제약 UQ_USER_RECIPE_PREFERENCE 필요). 전이 규칙은 쿼리의 CASE 안에 있다:
     * VIEW는 기존이 LIKE면 덮어쓰지 않고, LIKE/UNLIKE는 항상 그대로 반영된다.
     */
    @Transactional
    public void upsertPreference(Recipe recipe, Long userId, PreferenceType newType) {
        userService.findByUser(userId); // 존재하지 않는 유저면 여기서 예외
        referenceRepository.upsertPreference(userId, recipe.getRcpSno(), newType.name());
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