package com.recipe.service;

import com.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class ViewCountService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RecipeRepository recipeRepository;
    private final PlatformTransactionManager transactionManager;

    private static final String VIEW_KEY_PREFIX = "view:";
    private static final String FLUSH_LOCK_KEY = "lock:flushViewCountsToDB";
    private static final Duration FLUSH_LOCK_TTL = Duration.ofSeconds(50);

    // 락 소유자(값)가 내가 넣은 토큰과 같을 때만 지운다 → 내 TTL이 만료된 뒤
    // 다른 서버가 이미 새로 잡은 락을 실수로 지우는 것을 방지한다.
    private static final RedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del', KEYS[1]) " +
            "else " +
            "  return 0 " +
            "end",
            Long.class
    );

    public void incrementViewCount(Long recipeId) {
        String key = VIEW_KEY_PREFIX + recipeId;
        redisTemplate.opsForValue().increment(key);
    }

    public Long getRedisViewCount(Long recipeId) {
        String key = VIEW_KEY_PREFIX + recipeId;
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? 0L : Long.parseLong(value);
    }

    @Scheduled(fixedDelay = 60000)
    public void flushViewCountsToDB() {
        // 서버가 여러 대여도 한 번에 한 인스턴스만 flush를 돌리도록 분산락으로 보장한다.
        // 락을 못 잡으면 다른 서버가 이미 처리 중이라는 뜻이므로 이번 주기는 조용히 건너뛴다.
        String lockToken = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(FLUSH_LOCK_KEY, lockToken, FLUSH_LOCK_TTL);
        if (!Boolean.TRUE.equals(acquired)) {
            log.debug("[VIEW COUNT] 다른 서버가 flush 실행 중, 이번 주기 스킵");
            return;
        }
        try {
            doFlush();
        } finally {
            redisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(FLUSH_LOCK_KEY), lockToken);
        }
    }

    private void doFlush() {
        Set<String> keys = redisTemplate.keys(VIEW_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return;

        // recipeId 오름차순으로 정렬 후 처리 → 서버가 몇 대든 항상 같은 순서로
        // DB row lock을 잡으므로, 서로 반대 순서로 기다리다 발생하는 데드락을 방지한다.
        List<Long> sortedRecipeIds = keys.stream()
                .map(key -> Long.parseLong(key.replace(VIEW_KEY_PREFIX, "")))
                .sorted(Comparator.naturalOrder())
                .toList();

        int totalUpdated = 0;
        for (Long recipeId : sortedRecipeIds) {
            if (flushOne(recipeId)) {
                totalUpdated++;
            }
        }
        if (totalUpdated > 0) {
            log.info("[VIEW COUNT] DB 반영 완료: {}건", totalUpdated);
        }
    }

    /**
     * 레시피 1건 = 트랜잭션 1건으로 분리해, 한 건의 DB 반영 실패가 다른 레시피에 영향을 주지 않게 한다.
     * Redis 값은 DB 커밋이 확정된 뒤에만 그만큼 차감한다. DB 반영이 실패(데드락 등)하면
     * Redis 값이 그대로 남아 다음 flush 주기에 재시도되므로, GETDEL 직후 DB 트랜잭션이
     * 실패할 때 생기던 조회수 영구 유실이 없어진다.
     */
    private boolean flushOne(Long recipeId) {
        String key = VIEW_KEY_PREFIX + recipeId;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) return false;

        int count = Integer.parseInt(value);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        try {
            transactionTemplate.executeWithoutResult(status ->
                    recipeRepository.incrementViewCount(recipeId, count));
        } catch (Exception e) {
            log.warn("[VIEW COUNT] recipeId={} DB 반영 실패, 다음 주기에 재시도: {}", recipeId, e.getMessage());
            return false;
        }

        // DB 커밋이 확정된 뒤에만 차감 → 그 사이 새로 쌓인 조회수는 지우지 않는다.
        redisTemplate.opsForValue().decrement(key, count);
        return true;
    }
}
