package com.recipe.service;

import com.recipe.domain.dto.recipe.RecipeRequestDTO;
import com.recipe.domain.entity.Recipe;
import com.recipe.domain.entity.User;
import com.recipe.repository.RecipeRepository;
import com.recipe.repository.UserRepository;
import com.recipe.support.ContainerSupport;
import com.recipe.support.RecipeFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * flush가 특정 레시피 행의 row lock을 잡고 있는 동안, 같은 레시피를
 * 수정하려는 유저 요청 스레드는 flush 트랜잭션이 끝날 때까지 대기(블로킹)한다.
 * 웹 요청 스레드와 스케줄러 스레드가 같은 JVM 안에서만 있어도 재현되므로 멀티서버가 필요 없다.
 */
@SpringBootTest
class RecipeLockContentionTest extends ContainerSupport {

    private static final long LOCK_HOLD_MILLIS = 2000;
    private static final long AWAIT_TIMEOUT_SECONDS = 10;

    @Autowired
    private RecipeService recipeService;
    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private User author;
    private Recipe recipe;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        RecipeFixtures fixtures = new RecipeFixtures(userRepository, recipeRepository);
        author = fixtures.createUser();
        recipe = fixtures.createRecipe(author, 0);
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    void updatingRecipeWhileFlushHoldsItsLock_blocksUntilFlushCommits() throws InterruptedException {
        CountDownLatch flushLockAcquired = new CountDownLatch(1);
        AtomicLong updateElapsedMillis = new AtomicLong();

        Thread flushHoldingLock = new Thread(() -> simulateFlushHoldingRowLock(flushLockAcquired));
        Thread userUpdate = new Thread(() -> measureUpdateWaitTime(flushLockAcquired, updateElapsedMillis));

        flushHoldingLock.start();
        userUpdate.start();
        flushHoldingLock.join(TimeUnit.SECONDS.toMillis(AWAIT_TIMEOUT_SECONDS));
        userUpdate.join(TimeUnit.SECONDS.toMillis(AWAIT_TIMEOUT_SECONDS));

        assertThat(updateElapsedMillis.get())
                .as("flush가 잡은 락이 풀릴 때까지 update 요청이 대기했어야 한다")
                .isGreaterThanOrEqualTo((long) (LOCK_HOLD_MILLIS * 0.8));
    }

    private void simulateFlushHoldingRowLock(CountDownLatch flushLockAcquired) {
        transactionTemplate.executeWithoutResult(status -> {
            recipeRepository.incrementViewCount(recipe.getRcpSno(), 1);
            flushLockAcquired.countDown();
            sleepQuietly(LOCK_HOLD_MILLIS);
        });
    }

    private void measureUpdateWaitTime(CountDownLatch flushLockAcquired, AtomicLong updateElapsedMillis) {
        awaitQuietly(flushLockAcquired);
        long start = System.currentTimeMillis();
        recipeService.updateRecipe(recipe.getRcpSno(), buildUpdateRequest(), author.getUserId());
        updateElapsedMillis.set(System.currentTimeMillis() - start);
    }

    private RecipeRequestDTO buildUpdateRequest() {
        return RecipeRequestDTO.builder()
                .rcpTtl("수정된 제목")
                .ckgNm("수정된 요리명")
                .build();
    }

    private void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
