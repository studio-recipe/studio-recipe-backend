package com.recipe.controller;

import com.recipe.domain.dto.auth.*;
import com.recipe.domain.dto.user.UserLoginRequestDTO;
import com.recipe.domain.dto.user.UserRegisterRequestDTO;
import com.recipe.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;
    private final MailService mailService;
    private final VerificationCodeService verificationCodeService;
    private final TokenService tokenService;
    private final UserService userService;

    @Operation(summary = "로그인",
            description = "아이디/비밀번호 일치 시 JWT 토큰 발행",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공"),
                    @ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치")
            })
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid UserLoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "회원가입",
            description = "사용자 계정 생성",
            responses = {
                    @ApiResponse(responseCode = "201", description = "회원가입 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "409", description = "아이디/닉네임/이메일 중복")
            })
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid UserRegisterRequestDTO request) {
        authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "닉네임 중복 확인",
            description = "isAvailable=true면 이미 사용 중, false면 사용 가능",
            responses = {
                    @ApiResponse(responseCode = "200", description = "확인 성공")
            })
    @GetMapping("/check-nickname")
    public ResponseEntity<NicknameAvailabilityResponse> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = authService.checkExistsNickname(nickname);
        String message = isAvailable ? "현재 사용중인 닉네임입니다." : "사용 가능한 닉네임입니다.";
        return ResponseEntity.ok(NicknameAvailabilityResponse.builder()
                .isAvailable(isAvailable)
                .message(message)
                .build());
    }

    @Operation(summary = "이메일 인증 번호 발송",
            responses = {
                    @ApiResponse(responseCode = "200", description = "발송 성공"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 이메일")
            })
    @PostMapping("/send-verification")
    public ResponseEntity<String> sendVerificationCode(@RequestBody EmailRequest request) {
        userService.isUserExistsByEmail(request.getEmail());
        String code = verificationCodeService.generateAndSaveCode(request.getEmail());
        mailService.sendVerificationEmail(request.getEmail(), code);
        return ResponseEntity.ok("인증 번호 성공적으로 발송되었습니다.");
    }

    @Operation(summary = "이메일 인증 번호 검증",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 번호 불일치 또는 만료")
            })
    @PostMapping("/verify-code")
    public ResponseEntity<ResetProcessResponse> verifyCode(@RequestBody VerifyCodeRequest request) {
        boolean isVerified = verificationCodeService.verifyCode(
                request.getEmail(), request.getVerificationCode());
        if (isVerified) {
            String resetToken = tokenService.createToken(request.getEmail(), request.getPurpose());
            return ResponseEntity.ok(new ResetProcessResponse("이메일 인증이 성공했습니다.", resetToken));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResetProcessResponse("인증 번호가 유효하지 않거나 만료되었습니다.", null));
    }

    @Operation(summary = "아이디 찾기",
            responses = {
                    @ApiResponse(responseCode = "200", description = "아이디 반환"),
                    @ApiResponse(responseCode = "401", description = "토큰 유효하지 않음")
            })
    @PostMapping("/find-id")
    public ResponseEntity<String> findId(@RequestBody TokenRequest request) {
        Optional<String> emailOptional = tokenService.validateTokenAndGetEmail(
                request.getToken(), TokenPurpose.FIND_ID);
        if (emailOptional.isPresent()) {
            tokenService.invalidateToken(request.getToken());
            return ResponseEntity.ok(userService.findUserIdByEmail(emailOptional.get()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "비밀번호 재설정",
            responses = {
                    @ApiResponse(responseCode = "200", description = "재설정 성공"),
                    @ApiResponse(responseCode = "401", description = "토큰 유효하지 않음")
            })
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        Optional<String> emailOptional = tokenService.validateTokenAndGetEmail(
                request.getToken(), TokenPurpose.RESET_PASSWORD);
        if (emailOptional.isPresent()) {
            tokenService.invalidateToken(request.getToken());
            userService.resetPassword(emailOptional.get(), request.getNewPassword());
            return ResponseEntity.ok("비밀번호가 성공적으로 재설정되었습니다.");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("유효하지 않거나 만료된 토큰입니다.");
    }
}