package com.recipe.service;

import com.recipe.config.CustomerDetails;
import com.recipe.config.JwtTokenProvider;
import com.recipe.domain.dto.auth.TokenResponseDTO;
import com.recipe.domain.dto.auth.UserLoginRequestDTO;
import com.recipe.domain.dto.auth.UserRegisterRequestDTO;
import com.recipe.domain.entity.User;
import com.recipe.domain.entity.enums.Role;
import com.recipe.exceptions.auth.AuthExceptions;
import com.recipe.exceptions.user.UserExceptions;
import com.recipe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Log4j2
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void registerUser(UserRegisterRequestDTO request) {

        //닉네임 중복
        if(checkExistsNickname(request.getNickname()))
            throw UserExceptions.CONFLICT.getUserException("닉네임이 중복됩니다.");

        //아이디 중복
        if(checkExistsId(request.getId()))
            throw UserExceptions.CONFLICT.getUserException("아이디가 중복됩니다.");

        if(checkExistsEmail(request.getEmail()))
            throw UserExceptions.CONFLICT.getUserException("이메일이 중복됩니다.");

        String password = encoder.encode(request.getPassword());

        User user = User.builder()
                .id(request.getId())
                .pwd(password)
                .name(request.getName())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .birth(request.getBirth())
                .gender(request.getGender())
                .role(Role.GUEST)
                .build();

        userRepository.save(user);
        log.info("회원 가입 완료: {}", user.getId());
    }

    public TokenResponseDTO login(UserLoginRequestDTO request) {
        //아이디, 비밀번호 기반 인증 토큰
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getId(), request.getPassword());

        //인증 수행, CustomerDetailService가 사용자 정보 로드, (비밀번호 비교)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        //인증 정보 저장 (요청 처리 동안 사용 가능)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //권한 추출
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_GUEST");

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        CustomerDetails principal = (CustomerDetails) authentication.getPrincipal();
        refreshTokenService.save(principal.getUserId(), refreshToken);

        return buildTokenResponse(accessToken, refreshToken, role);
    }

    /**
     * Refresh Token Rotation(RTR) + Grace Period.
     * 조회, 비교, 회전/유예 판정, Redis 기록까지를 RefreshTokenService.processReissue()의 Lua 스크립트로
     * 원자적으로 처리해, 동시 요청이 서로의 회전 결과를 덮어쓰는 경합(TOCTOU)이 생기지 않도록 한다.
     */
    @Transactional
    public TokenResponseDTO reissue(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw AuthExceptions.INVALID_REFRESH_TOKEN.getAuthException();
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        CustomerDetails principal = (CustomerDetails) authentication.getPrincipal();
        Long userId = principal.getUserId();

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_GUEST");

        //회전이 필요 없을 수도 있지만, 판정을 Lua 스크립트 안에서 원자적으로 하기 위해 미리 발급해 둔다.
        //ROTATED가 아니면 이 값은 저장되지 않고 버려진다.
        String candidateRefreshToken = jwtTokenProvider.createRefreshToken(authentication);
        RefreshTokenService.ReissueResult result =
                refreshTokenService.processReissue(userId, refreshToken, candidateRefreshToken);

        return switch (result.status()) {
            case ROTATED, GRACE -> {
                String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
                yield buildTokenResponse(newAccessToken, result.refreshToken(), role);
            }
            case REUSE_DETECTED -> throw AuthExceptions.TOKEN_REUSE_DETECTED.getAuthException();
            case NO_SESSION -> throw AuthExceptions.INVALID_REFRESH_TOKEN.getAuthException();
        };
    }

    private TokenResponseDTO buildTokenResponse(String accessToken, String refreshToken, String role) {
        return TokenResponseDTO.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn(jwtTokenProvider.getAccessTokenValiditySeconds())
                .refreshToken(refreshToken)
                .refreshTokenExpiresIn(jwtTokenProvider.getRefreshTokenValiditySeconds())
                .role(role)
                .build();
    }

    public void logout(String refreshToken) {
        if (jwtTokenProvider.validateToken(refreshToken) && jwtTokenProvider.isRefreshToken(refreshToken)) {
            refreshTokenService.delete(jwtTokenProvider.getUserId(refreshToken));
        }
    }

    public boolean checkExistsNickname(String nickname){
        return userRepository.existsByNickname(nickname);
    }

    public boolean checkExistsId(String id){
        return userRepository.existsById(id);
    }

    public boolean checkExistsEmail(String email){
        return userRepository.existsByEmail(email);
    }

}
