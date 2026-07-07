package com.recipe.service;

import com.recipe.domain.entity.Recipe;
import com.recipe.domain.entity.User;
import com.recipe.repository.RecipeRepository;
import com.recipe.repository.UserReferencesRepository;
import com.recipe.repository.UserRepository;
import com.recipe.support.ConcurrentRunner;
import com.recipe.support.ContainerSupport;
import com.recipe.support.RecipeFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 문제 6: 같은 (user, recipe) 조합을 동시에 조회하면 UserReferencesService.upsertPreference의
 * "조회 후 생성(check-then-act)" 로직과 유니크 제약 부재 때문에 중복 row가 생긴다.
 * 서버 1대, 스레드 여러 개만으로 재현된다 (멀티서버 불필요).
 */
@SpringBootTest
class UserReferencesConcurrencyTest extends ContainerSupport {

    private static final int CONCURRENT_VIEW_REQUESTS = 10;

    @Autowired
    private UserReferencesService userReferencesService;
    @Autowired
    private UserReferencesRepository userReferencesRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RecipeRepository recipeRepository;


    private User viewer;
    private Recipe recipe;

    @BeforeEach
    void setUp() {
        RecipeFixtures fixtures = new RecipeFixtures(userRepository, recipeRepository);
        User author = fixtures.createUser();
        viewer = fixtures.createUser();
        recipe = fixtures.createRecipe(author, 0);
    }

    @Test
    void concurrentViewsOnSameRecipe_shouldNotCreateDuplicatePreferenceRows() throws Exception {
        List<Future<Void>> results = ConcurrentRunner.runAtTheSameMoment(buildConcurrentViewTasks());

        assertAllTasksSucceeded(results);
        assertThat(countPreferenceRows())
                .as("같은 (user, recipe) 조합은 동시에 조회해도 row가 하나여야 한다")
                .isEqualTo(1);
    }

    private List<Callable<Void>> buildConcurrentViewTasks() {
        return IntStream.range(0, CONCURRENT_VIEW_REQUESTS)
                .mapToObj(i -> (Callable<Void>) () -> {
                    userReferencesService.userRecipeView(recipe, viewer.getUserId());
                    return null;
                })
                .collect(Collectors.toList());
    }

    private void assertAllTasksSucceeded(List<Future<Void>> results) throws Exception {
        for (Future<Void> result : results) {
            result.get();
        }
    }

    private int countPreferenceRows() {
        return userReferencesRepository.findAllByUserAndRecipe(viewer, recipe).size();
    }
}
