package com.recipe.service;

import com.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class ViewCountService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RecipeRepository recipeRepository;

    private static final String VIEW_KEY_PREFIX = "view:";

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
    @Transactional
    public void flushViewCountsToDB() {
        Set<String> keys = redisTemplate.keys(VIEW_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return;

        int totalUpdated = 0;
        for (String key : keys) {
            String value = redisTemplate.opsForValue().getAndDelete(key);
            if (value == null) continue;

            Long recipeId = Long.parseLong(key.replace(VIEW_KEY_PREFIX, ""));
            int count = Integer.parseInt(value);

            // 한 번의 UPDATE로 누적 반영
            int updated = recipeRepository.incrementViewCount(recipeId, count);
            totalUpdated += updated;
        }
        if (totalUpdated > 0) {
            log.info("[VIEW COUNT] DB 반영 완료: {}건", totalUpdated);
        }
    }
}
