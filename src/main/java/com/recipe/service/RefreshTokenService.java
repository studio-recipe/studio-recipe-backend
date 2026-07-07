package com.recipe.service;

import com.recipe.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * Refresh Token을 Redis에 저장/검증/회전한다.
 * 여러 서버 인스턴스가 동일한 Redis를 공유하므로 어느 서버가 재발급 요청을 받아도 동일하게 검증할 수 있다.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String KEY_PREFIX = "refresh_token:";
    private static final String GRACE_KEY_PREFIX = "refresh_token:grace:";

    /**
     * 회전 직후 폐기된 옛 Refresh Token을 잠시 유효한 것으로 인정해주는 유예 기간.
     * 프론트에서 동시에 여러 요청이 401을 맞아 짧은 시간차로 재발급을 호출하는 경우를
     * 탈취로 오탐하지 않기 위함이다.
     */
    private static final Duration GRACE_PERIOD = Duration.ofSeconds(5);

    /**
     * "현재 값 조회 -> 제시된 토큰과 비교 -> 회전/유예 판정 -> 기록"을 하나의 Lua 스크립트로 묶어
     * 원자적으로 처리한다. 이 판단과 쓰기를 애플리케이션 레벨에서 분리하면(GET 후 별도 SET 호출)
     * 그 사이에 동시 요청이 끼어들어 서로의 회전 결과를 덮어쓰는 TOCTOU 경합이 생긴다.
     *
     * 반환값: {status, refreshToken}
     *  - ROTATED        : 제시된 토큰이 현재 값과 일치 -> 회전 완료, refreshToken = 새로 발급한 토큰
     *  - GRACE           : 제시된 토큰이 유예 기간 내 옛 값과 일치 -> refreshToken = 이미 회전된 현재 토큰
     *  - REUSE_DETECTED  : 어느 쪽과도 불일치 -> 재사용(탈취 의심)으로 판단, 세션 즉시 폐기
     *  - NO_SESSION      : 현재 저장된 토큰이 없음(로그아웃 상태 등)
     */
    @SuppressWarnings("rawtypes")
    private static final DefaultRedisScript<List> REISSUE_SCRIPT = new DefaultRedisScript<>(
            "local grace = KEYS[1] " +
            "local current = KEYS[2] " +
            "local presented = ARGV[1] " +
            "local newToken = ARGV[2] " +
            "local graceTtl = ARGV[3] " +
            "local currentTtl = ARGV[4] " +
            "local currentVal = redis.call('GET', current) " +
            "if not currentVal then return {'NO_SESSION', ''} end " +
            "if currentVal == presented then " +
            "  redis.call('SET', grace, presented, 'EX', graceTtl) " +
            "  redis.call('SET', current, newToken, 'EX', currentTtl) " +
            "  return {'ROTATED', newToken} " +
            "end " +
            "local graceVal = redis.call('GET', grace) " +
            "if graceVal == presented then return {'GRACE', currentVal} end " +
            "redis.call('DEL', current) " +
            "redis.call('DEL', grace) " +
            "return {'REUSE_DETECTED', ''}",
            List.class
    );

    /**
     * 로그아웃/탈취 탐지 시 현재 토큰과 유예 토큰을 함께 지운다.
     * 하나만 지워지면(크래시 등) grace 키가 남아 탈취 탐지를 짧게 우회할 수 있어 원자적으로 묶는다.
     */
    private static final DefaultRedisScript<Long> DELETE_SCRIPT = new DefaultRedisScript<>(
            "redis.call('DEL', KEYS[1]) " +
            "redis.call('DEL', KEYS[2]) " +
            "return 1",
            Long.class
    );

    public void save(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + userId,
                refreshToken,
                Duration.ofSeconds(jwtTokenProvider.getRefreshTokenValiditySeconds())
        );
    }

    /**
     * 제시된 Refresh Token을 검증하고, 필요하면 회전까지 한 번에 원자적으로 수행한다.
     * newRefreshToken은 ROTATED로 판정될 때만 실제로 저장되며, 그 외의 경우 버려진다.
     */
    @SuppressWarnings("unchecked")
    public ReissueResult processReissue(Long userId, String presentedRefreshToken, String newRefreshToken) {
        List<Object> result = redisTemplate.execute(
                REISSUE_SCRIPT,
                List.of(GRACE_KEY_PREFIX + userId, KEY_PREFIX + userId),
                presentedRefreshToken,
                newRefreshToken,
                String.valueOf(GRACE_PERIOD.getSeconds()),
                String.valueOf(jwtTokenProvider.getRefreshTokenValiditySeconds())
        );

        ReissueResult.Status status = ReissueResult.Status.valueOf((String) result.get(0));
        String refreshToken = (String) result.get(1);
        return new ReissueResult(status, refreshToken);
    }

    public void delete(Long userId) {
        redisTemplate.execute(
                DELETE_SCRIPT,
                List.of(KEY_PREFIX + userId, GRACE_KEY_PREFIX + userId)
        );
    }

    public record ReissueResult(Status status, String refreshToken) {
        public enum Status { ROTATED, GRACE, REUSE_DETECTED, NO_SESSION }
    }
}
