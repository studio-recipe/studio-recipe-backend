package com.recipe.repository;

import com.recipe.domain.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findTop10ByOrderByRcmmCntDesc();
    List<Recipe> findByRcpSnoIn(Collection<Long> ids);
}
