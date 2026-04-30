package com.recipe.repository;

import com.recipe.domain.entity.Like;
import com.recipe.domain.entity.Recipe;
import com.recipe.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    public Optional<Like> findByUserAndRecipe(User user, Recipe recipe);

    @EntityGraph(attributePaths = "recipe")
    Page<Like> findByUser(User user, Pageable pageable);
}
