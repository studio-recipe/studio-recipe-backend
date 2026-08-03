package com.recipe.service;

import com.recipe.domain.dto.recipe.RecipeResponseDTO;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * flush가 Redis 값을 GETDEL로 이미 비웠지만 DB 커밋은 아직 하지 않은 순간에
 * 다른 스레드가 상세 페이지를 읽으면, 실제 합계보다 적은 조회수를 보게 된다.
 * 웹 요청 스레드와 스케줄러 스레드가 같은 JVM 안에서만 있어도 재현되므로 멀티서버가 필요 없다.
 */
@SpringBootTest
class ViewCountUndercountWindowTest extends ContainerSupport {

    // ViewCountService 내부 키 규칙(view:{recipeId})을 그대로 재현한 것.
    // flush의 "Redis 삭제 → DB 커밋" 사이 창을 의도적으로 재현하려면 원시 Redis 접근이 필요하다.
    private static final String VIEW_KEY_PREFIX = "view:";
    private static final int DB_COMMITTED_COUNT = 100;
    private static final int PENDING_REDIS_VIEWS = 15;
    private static final long AWAIT_TIMEOUT_SECONDS = 5;

    @Autowired
    private RecipeService recipeService;
    @Autowired
    private ViewCountService viewCountService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private Recipe recipe;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        RecipeFixtures fixtures = new RecipeFixtures(userRepository, recipeRepository);
        User author = fixtures.createUser();
        recipe = fixtures.createRecipe(author, DB_COMMITTED_COUNT);
        transactionTemplate = new TransactionTemplate(transactionManager);
        seedPendingRedisViews(PENDING_REDIS_VIEWS);
    }

    @Test
    void readingDuringFlushWindow_seesFewerViewsThanActuallyExist() throws InterruptedException {
        CountDownLatch redisAlreadyCleared = new CountDownLatch(1);
        CountDownLatch readerFinished = new CountDownLatch(1);
        AtomicInteger observedCountDuringWindow = new AtomicInteger();
        AtomicReference<Throwable> flushFailure = new AtomicReference<>();
        AtomicReference<Throwable> readerFailure = new AtomicReference<>();

        Thread flushThread = new Thread(() -> simulateFlushHalfway(redisAlreadyCleared, readerFinished));
        Thread readerThread = new Thread(() ->
                simulateConcurrentReader(redisAlreadyCleared, readerFinished, observedCountDuringWindow));
        flushThread.setUncaughtExceptionHandler((t, e) -> flushFailure.set(e));
        readerThread.setUncaughtExceptionHandler((t, e) -> readerFailure.set(e));

        flushThread.start();
        readerThread.start();
        flushThread.join(TimeUnit.SECONDS.toMillis(AWAIT_TIMEOUT_SECONDS));
        readerThread.join(TimeUnit.SECONDS.toMillis(AWAIT_TIMEOUT_SECONDS));

        assertThat(flushFailure.get()).as("flush 스레드가 예외 없이 끝나야 한다").isNull();
        assertThat(readerFailure.get()).as("reader 스레드가 예외 없이 끝나야 한다").isNull();

        assertThat(observedCountDuringWindow.get())
                .as("flush 중간(Redis는 비었지만 DB 커밋 전)에 읽으면 실제 합계(%d)보다 적게 보인다",
                        DB_COMMITTED_COUNT + PENDING_REDIS_VIEWS)
                .isEqualTo(DB_COMMITTED_COUNT);

        assertThat(currentDbCount())
                .as("flush가 끝나면 DB에는 정상적으로 반영된다")
                .isEqualTo(DB_COMMITTED_COUNT + PENDING_REDIS_VIEWS);
    }

    private void seedPendingRedisViews(int viewCount) {
        for (int i = 0; i < viewCount; i++) {
            viewCountService.incrementViewCount(recipe.getRcpSno());
        }
    }

    private void simulateFlushHalfway(CountDownLatch redisAlreadyCleared, CountDownLatch readerFinished) {
        String pendingValue = redisTemplate.opsForValue().getAndDelete(viewKey());
        redisAlreadyCleared.countDown();
        awaitQuietly(readerFinished);
        transactionTemplate.executeWithoutResult(status ->
                recipeRepository.incrementViewCount(recipe.getRcpSno(), Integer.parseInt(pendingValue)));
    }

    private void simulateConcurrentReader(
            CountDownLatch redisAlreadyCleared,
            CountDownLatch readerFinished,
            AtomicInteger observedCountDuringWindow
    ) {
        awaitQuietly(redisAlreadyCleared);
        RecipeResponseDTO response = recipeService.findOneRecipe(recipe.getRcpSno(), null);
        observedCountDuringWindow.set(response.getInqCnt());
        readerFinished.countDown();
    }

    private Integer currentDbCount() {
        return recipeRepository.findById(recipe.getRcpSno()).orElseThrow().getInqCnt();
    }

    private String viewKey() {
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
