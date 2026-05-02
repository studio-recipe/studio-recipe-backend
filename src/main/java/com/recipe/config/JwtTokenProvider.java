package com.recipe.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Log4j2
@Getter
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValiditySeconds;
    private final long refreshTokenValiditySeconds;

    public JwtTokenProvider(
            @Value("${JWT_SECRET_KEY}") String secretKey,
            @Value("${JWT_ACCESS_TOKEN_VALIDITY_IN_SECONDS}") long accessTokenValiditySeconds,
            @Value("${JWT_REFRESH_TOKEN_VALIDITY_IN_SECONDS}") long refreshTokenValiditySeconds
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValiditySeconds = accessTokenValiditySeconds * 1000;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds * 1000;
    }

    //Access Token, Refresh Token 모두 사용 이유 -> 둘의 구분은 시간이다.
    public String createToken(Authentication authentication, long validity) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        CustomerDetails customerDetails = (CustomerDetails) authentication.getPrincipal();
        String userIdStr = customerDetails.getUserId().toString();
        String username = authentication.getName();

        long now = (new Date()).getTime();
        Date validityDate = new Date(now + validity);

        return Jwts.builder()
                .setSubject(userIdStr) //토큰의 주체를 DB PK 값으로 했음
                .claim("auth", authorities) //권한 정보
                .claim("username", username)
                .setIssuedAt(new Date(now)) //토큰 발행 시간
                .setExpiration(validityDate) // 토큰 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) //서명
                .compact();
    }

    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, accessTokenValiditySeconds);
    }

    public String createRefreshToken(Authentication authentication) {
        return createToken(authentication, refreshTokenValiditySeconds);
    }

    //토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        //클레임에서 권한 정보 추출
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        log.debug("User Principal: {}, Authorities: {}", claims.getSubject(), authorities);

        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username").toString();
        String loginId = (String) claims.get("username");

        CustomerDetails principal = new CustomerDetails(userId, loginId, "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);

//        UserDetails principal = new User(claims.getSubject(), "", authorities);
//        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    //토큰 유효성 검증
    public boolean validateToken(String token) {
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    //Request Header 토큰 정보 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
