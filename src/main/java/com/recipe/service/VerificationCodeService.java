package com.recipe.service;

import com.recipe.util.VerificationCodeGenerator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final RedisTemplate<String, String> redisTemplate;

    //인증 코드 만료시간 (예: 5분)
    private static final long VERIFICATION_CODE_EXPIRATION_SECONDS = 5 * 60;
    private static final String REDIS_PREFIX = "email_verification";

    /**
     * 특정 이메일 주소에 대한 인증 코드를 생성, Redis에 저장
     * @param email 인증 코드를 발송할 이메일 주소
     * @return 생성된 인증 코드
     */
    public String generateAndSaveCode(String email) {
        String code = VerificationCodeGenerator.generateNumberCode(6);
        String key = REDIS_PREFIX + email;
        redisTemplate.opsForValue().set(key, code,
                Duration.ofSeconds(VERIFICATION_CODE_EXPIRATION_SECONDS));
        return code;
    }

    /**
     * 이메일과 입력된 코드를 통해 인증/검증
     * @param email 검증할 이메일 주소
     * @param inputCode 사용자가 입력한 인증 코드
     * @return 인증 성공 여부
     */
    public boolean verifyCode(String email, String inputCode) {
        String key = REDIS_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);

        if(storedCode == null) {
            //예외 따로 만들거나 따로 처리 작업 필요
            throw new IllegalStateException("인증 번호 만료되었거나 유효하지 않습니다.");
    }

        if(!storedCode.equals(inputCode)) {
            throw new IllegalStateException("인증 번호가 일치하지 않습니다.");
        }

        redisTemplate.delete(key);
        return true;
    }

    public Optional<Long> getRemainingExpirationTime(String email) {
        String key = REDIS_PREFIX + email;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return Optional.ofNullable(ttl);
    }

    public static Long getExpirationSeconds(){
        return VERIFICATION_CODE_EXPIRATION_SECONDS;
    }
}
