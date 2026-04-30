/*

package com.recipe.repository;

import com.recipe.domain.entity.UserRecipeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRecipeLogRepository extends JpaRepository<UserRecipeLogEntity, Long> {
    // UserRecipeLogEntity 안의 user는 User 타입이므로 user.userId로 접근해야 함
    List<UserRecipeLogEntity> findByUser_UserId(Long userId);
}
*/