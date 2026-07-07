package com.recipe.service;

import com.recipe.domain.entity.Recipe;
import com.recipe.domain.entity.User;
import com.recipe.repository.RecipeRepository;
import com.recipe.repository.UserRepository;
import com.recipe.support.ConcurrentRunner;
import com.recipe.support.ContainerSupport;
import com.recipe.support.RecipeFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 문제 1+3: 서버가 여러 대라서 flush 스케줄러가 동시에 두 번 돌면, 겹치는 레시피 두 건을
 * 서로 다른 순서로 UPDATE하다가 실제 MySQL 데드락이 발생할 수 있다. 이때 패배한 트랜잭션은
 * 롤백되지만, 그 안에서 처리하려던 pending 조회수는 이미 Redis에서 GETDEL로 지워진 뒤라
 * 영구히 사라진다. 멀티서버(동시에 도는 두 개의 flush 인스턴스)가 있어야만 재현된다.
 */
@SpringBootTest
class ConcurrentFlushDeadlockTest extends ContainerSupport {

    // ViewCountService 내부 키 규칙(view:{recipeId})을 그대로 재현한 것.
    private static final String VIEW_KEY_PREFIX = "view:";
    private static final int PENDING_A = 3;
    private static final int PENDING_B = 5;
    private static final int FRESH_VIEW_AMOUNT = 1;
    private static final long AWAIT_TIMEOUT_SECONDS = 10;

    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ViewCountService viewCountService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private Recipe recipeA;
    private Recipe recipeB;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        RecipeFixtures fixtures = new RecipeFixtures(userRepository, recipeRepository);
        User author = fixtures.createUser();
        recipeA = fixtures.createRecipe(author, 0);
        recipeB = fixtures.createRecipe(author, 0);
        transactionTemplate = new TransactionTemplate(transactionManager);

        seedPendingViews(recipeA, PENDING_A);
        seedPendingViews(recipeB, PENDING_B);
    }

    @Test
    void twoOverlappingFlushInstancesInReverseOrder_causeDeadlockAndPermanentLoss() throws InterruptedException {
        CountDownLatch instance1GotPrimaryLock = new CountDownLatch(1);
        CountDownLatch instance2GotPrimaryLock = new CountDownLatch(1);

        List<Callable<Void>> flushInstances = List.of(
                buildFlushInstanceTask(recipeA, recipeB, instance1GotPrimaryLock, instance2GotPrimaryLock),
                buildFlushInstanceTask(recipeB, recipeA, instance2GotPrimaryLock, instance1GotPrimaryLock)
        );

        List<Future<Void>> results = ConcurrentRunner.runAtTheSameMoment(flushInstances);
        int loserIndex = findSingleFailureIndex(results);

        assertThat(loserIndex)
                .as("두 flush 트랜잭션이 반대 순서로 잠그면 정확히 하나는 데드락으로 실패해야 한다")
                .isIn(0, 1);
        assertLoserPendingCountWasPermanentlyLost(loserIndex);
    }

    /**
     * 한 서버 인스턴스의 flush를 흉내낸다: primaryRecipe 키를 먼저 GETDEL해서 DB에 반영하며
     * 그 행의 락을 잡고, 상대 인스턴스도 자신의 primary 락을 잡을 때까지 기다린 뒤,
     * secondaryRecipe(=상대의 primary)를 이어서 처리하려다 반대 순서 잠금으로 충돌한다.
     */
    private Callable<Void> buildFlushInstanceTask(
            Recipe primaryRecipe,
            Recipe secondaryRecipe,
            CountDownLatch primaryLockAcquired,
            CountDownLatch otherInstancePrimaryLockAcquired
    ) {
        return () -> {
            String primaryPending = redisTemplate.opsForValue().getAndDelete(viewKey(primaryRecipe));
            transactionTemplate.executeWithoutResult(status -> {
                recipeRepository.incrementViewCount(primaryRecipe.getRcpSno(), Integer.parseInt(primaryPending));
                primaryLockAcquired.countDown();
                awaitQuietly(otherInstancePrimaryLockAcquired);

                // secondaryRecipe에 방금 새 조회가 발생해 이번 스캔에도 걸린 상황을 재현
                viewCountService.incrementViewCount(secondaryRecipe.getRcpSno());
                String secondaryPending = redisTemplate.opsForValue().getAndDelete(viewKey(secondaryRecipe));
                recipeRepository.incrementViewCount(secondaryRecipe.getRcpSno(), Integer.parseInt(secondaryPending));
            });
            return null;
        };
    }

    private int findSingleFailureIndex(List<Future<Void>> results) throws InterruptedException {
        int failureIndex = -1;
        int failureCount = 0;
        for (int i = 0; i < results.size(); i++) {
            try {
                results.get(i).get();
            } catch (ExecutionException e) {
                failureIndex = i;
                failureCount++;
            }
        }
        assertThat(failureCount)
                .as("데드락 희생자는 정확히 하나여야 한다 (둘 다 성공하거나 둘 다 실패하면 안 됨)")
                .isEqualTo(1);
        return failureIndex;
    }

    /**
     * index 0(=A가 primary인 인스턴스)이 졌다면: A의 원래 대기치(PENDING_A)는 롤백으로 사라지고,
     * A에는 이긴 인스턴스가 나중에 추가한 FRESH_VIEW_AMOUNT만 반영된다. index 1이 졌다면 대칭적으로 B가 그렇다.
     */
    private void assertLoserPendingCountWasPermanentlyLost(int loserIndex) {
        if (loserIndex == 0) {
            assertThat(currentDbCount(recipeA))
                    .as("A의 원래 대기 조회수(%d)는 데드락 롤백으로 영구 유실된다", PENDING_A)
                    .isEqualTo(FRESH_VIEW_AMOUNT);
            assertThat(currentDbCount(recipeB))
                    .as("B를 처리한 인스턴스는 데드락 승자라 정상 반영된다")
                    .isEqualTo(PENDING_B);
        } else {
            assertThat(currentDbCount(recipeB))
                    .as("B의 원래 대기 조회수(%d)는 데드락 롤백으로 영구 유실된다", PENDING_B)
                    .isEqualTo(FRESH_VIEW_AMOUNT);
            assertThat(currentDbCount(recipeA))
                    .as("A를 처리한 인스턴스는 데드락 승자라 정상 반영된다")
                    .isEqualTo(PENDING_A);
        }
    }

    private void seedPendingViews(Recipe recipe, int viewCount) {
        for (int i = 0; i < viewCount; i++) {
            viewCountService.incrementViewCount(recipe.getRcpSno());
        }
    }

    private Integer currentDbCount(Recipe recipe) {
        return recipeRepository.findById(recipe.getRcpSno()).orElseThrow().getInqCnt();
    }

    private String viewKey(Recipe recipe) {
        return VIEW_KEY_PREFIX + recipe.getRcpSno();
    }

    private void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
