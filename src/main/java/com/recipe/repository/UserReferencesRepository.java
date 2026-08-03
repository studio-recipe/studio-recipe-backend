package com.recipe.repository;

import com.recipe.domain.entity.Recipe;
import com.recipe.domain.entity.User;
import com.recipe.domain.entity.UserReferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserReferencesRepository extends JpaRepository<UserReferences, Long> {
    public Optional<UserReferences> findByUserAndRecipe(User user, Recipe recipe);

    public List<UserReferences> findAllByUserAndRecipe(User user, Recipe recipe);

    /**
     * INSERT ... ON DUPLICATE KEY UPDATE로 조회/삽입/갱신을 한 문장에 묶어 원자적으로 처리한다.
     * Unique 제약(UQ_USER_RECIPE_PREFERENCE)이 있어야 동작하며, 같은 (user, recipe)에 대한
     * 동시 요청을 애플리케이션 레벨의 조회-후-삽입(check-then-act) 없이 DB 락만으로 직렬화한다.
     * 전이 규칙: VIEW는 LIKE를 덮어쓰지 않고, LIKE/UNLIKE는 항상 그대로 반영된다.
     */
    @Modifying
    @Query(value = "INSERT INTO user_references (user_id, rcp_sno, preference_type, created_at, modified_at) " +
            "VALUES (:userId, :recipeId, :newType, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "preference_type = CASE " +
            "  WHEN :newType <> 'VIEW' THEN :newType " +
            "  WHEN preference_type = 'LIKE' THEN 'LIKE' " +
            "  ELSE 'VIEW' " +
            "END, " +
            "modified_at = NOW()",
            nativeQuery = true)
    public void upsertPreference(@Param("userId") Long userId, @Param("recipeId") Long recipeId, @Param("newType") String newType);
}
 