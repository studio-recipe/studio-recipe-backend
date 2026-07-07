package com.recipe.support;

import com.recipe.domain.entity.Recipe;
import com.recipe.domain.entity.User;
import com.recipe.domain.entity.enums.Gender;
import com.recipe.domain.entity.enums.Role;
import com.recipe.repository.RecipeRepository;
import com.recipe.repository.UserRepository;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 테스트에 필요한 최소한의 User/Recipe 엔티티를 만들고 저장하는 역할만 한다.
 */
public class RecipeFixtures {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    public RecipeFixtures(UserRepository userRepository, RecipeRepository recipeRepository) {
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
    }

    public User createUser() {
        long seq = SEQUENCE.incrementAndGet();
        User user = User.builder()
                .id("test-user-" + seq)
                .pwd("encoded-password")
                .name("테스트유저" + seq)
                .nickname("tester" + seq)
                .email("tester" + seq + "@example.com")
                .birth(LocalDate.of(2000, 1, 1))
                .gender(Gender.F)
                .role(Role.GUEST)
                .build();
        return userRepository.save(user);
    }

    public Recipe createRecipe(User author, int initialInqCnt) {
        Recipe recipe = Recipe.builder()
                .rcpTtl("테스트 레시피 " + SEQUENCE.incrementAndGet())
                .ckgNm("테스트 요리")
                .inqCnt(initialInqCnt)
                .rcmmCnt(0)
                .author(author)
                .build();
        return recipeRepository.save(recipe);
    }
}
