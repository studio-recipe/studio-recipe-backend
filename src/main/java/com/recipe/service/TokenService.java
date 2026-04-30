package com.recipe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.domain.dto.auth.TokenData;
import com.recipe.domain.dto.auth.TokenPurpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class TokenService {

    //RedisTemplate <String, String>
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final long TOKEN_EXPIRATION_SECONDS = 10 * 60;
    private static final String REDIS_PREFIX = "generic_token:"; // 접두사

    /**
     * 토큰을 생성하고 Redis에 저장
     * @param email 토큰 연결과 사용자 이메일
     * @param purpose 토큰의 목적 TokenPurpose(FIND_ID, RESET_PASSWORD)
     */
    public String createToken(String email, TokenPurpose purpose) {
        String token = UUID.randomUUID().toString();
        String key = REDIS_PREFIX + token;

        TokenData tokenData = TokenData.builder()
                .email(email)
                .tokenPurpose(purpose)
                .build();

        try{
            //tokenData 객체를 JSON 문자열로 직렬화
            String tokenDataJson = objectMapper.writeValueAsString(tokenData);
            log.info("tokenDataJson: {} ", tokenDataJson);
            redisTemplate.opsForValue().set(key, tokenDataJson,
                    Duration.ofSeconds(TOKEN_EXPIRATION_SECONDS));
            return token;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("토큰 데이터를 JSON으로 변환하는 데 실패했습니다.", e);
        }
    }

    /**
     * 토큰의 유효성을 검증하고 목적에 부합한지 확인 후 연결된 사용자 이메일 반환
     * @param token 사용자가 제출한 토큰
     * @param expectedPurpose 토큰이 사용될 목적
     * @return 유효하다면 Optional<String>, else Optional.empty()
     */
    public Optional<String> validateTokenAndGetEmail(String token, TokenPurpose expectedPurpose) {
        log.info("validateTokenAndGetEmail: token={}, expectedPurpose={}", token, expectedPurpose);
        String key = REDIS_PREFIX + token;
        String tokenDataJson = redisTemplate.opsForValue().get(key);
        log.info("validateTokenAndGetEmail: tokenDataJson={}", tokenDataJson);

        if(tokenDataJson == null){
            return Optional.empty(); //토큰이 없거나 만료
        }

        try{
            //JSON 문자열을 TokenData로 역질렬화
            TokenData tokenData = objectMapper.readValue(tokenDataJson, TokenData.class);
            if(tokenData.getTokenPurpose() == expectedPurpose){
                return Optional.of(tokenData.getEmail());
            }else{
                //목적 일치하지 않음, 보안을 위해 삭제
                redisTemplate.delete(key);
                return Optional.empty();
            }
        }catch (JsonProcessingException e){
            log.info("Error parsing token data: {}", e.getMessage());
            redisTemplate.delete(key);
            return Optional.empty();
        }
    }

    public void invalidateToken(String token) {
        String key = REDIS_PREFIX + token;
        redisTemplate.delete(key);
    }
}
