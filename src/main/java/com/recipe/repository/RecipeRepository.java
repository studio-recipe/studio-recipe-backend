package com.recipe.repository;

import com.recipe.domain.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByRcpSnoIn(Collection<Long> ids);

    @Modifying
    @Query("UPDATE Recipe r SET r.inqCnt = r.inqCnt + :count WHERE r.rcpSno = :recipeId")
    int incrementViewCount(@Param("recipeId") Long recipeId, @Param("count") int count);
}
