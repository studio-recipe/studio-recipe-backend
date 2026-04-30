package com.recipe.controller;

import com.recipe.controller.inter.AuthController;
import com.recipe.domain.dto.auth.*;
import com.recipe.domain.dto.user.UserLoginRequestDTO;
import com.recipe.domain.dto.user.UserRegisterRequestDTO;
import com.recipe.service.*;
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
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;
    private final MailService mailService;
    private final VerificationCodeService verificationCodeService;
    private final TokenService tokenService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid UserLoginRequestDTO request) {
        TokenResponseDTO tokenResponse = authService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid UserRegisterRequestDTO request) {
        log.info("================ register ================");
        log.info("request = {}", request);

        authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping("/check-nickname")
    public ResponseEntity<NicknameAvailabilityResponse> checkNickname(
            @RequestParam String nickname){

        boolean isAvailable = authService.checkExistsNickname(nickname);
        String message = isAvailable ?  "현재 사용중인 닉네임입니다." : "사용 가능한 닉네임입니다.";
        NicknameAvailabilityResponse response = NicknameAvailabilityResponse.builder()
                .isAvailable(isAvailable)
                .message(message)
                .build();
        return  ResponseEntity.ok(response);
    }

    //이메일 인증 번호 발송 요청
    //Swagger 작성 나중에
    @PostMapping("/send-verification")
    public ResponseEntity<String> sendVerificationCode(@RequestBody EmailRequest request) {
        //User 테이블에 이메일이 존재하는지 검증해야 함
        userService.isUserExistsByEmail(request.getEmail());

        String code = verificationCodeService.generateAndSaveCode(request.getEmail());
        mailService.sendVerificationEmail(request.getEmail(), code);
        return ResponseEntity.ok("인증 번호 성공적으로 발송되었습니다.");
    }

    //이메일 인증 번호 검증
    //Swagger 작성 나중에
    @PostMapping("/verify-code")
    public ResponseEntity<ResetProcessResponse> verifyCode(@RequestBody VerifyCodeRequest request) {
        boolean isVerified = verificationCodeService
                .verifyCode(request.getEmail(), request.getVerificationCode());

        if(isVerified) {
            String resetToken = tokenService.createToken(request.getEmail(),
                    request.getPurpose());
            return ResponseEntity.ok(new ResetProcessResponse("이메일 인증이 성공했습니다.", resetToken));
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResetProcessResponse("인증 번호가 유효하지 않거나 만료되었습니다.", null));
        }
    }

    //아이디 찾기
    //Swagger 작성 필요
    @PostMapping("/find-id")
    public ResponseEntity<String> findId(@RequestBody TokenRequest request) {
        Optional<String> emailOptional =
                tokenService.validateTokenAndGetEmail(request.getToken(),  TokenPurpose.FIND_ID);

        if(emailOptional.isPresent()) {
            String email = emailOptional.get();
            tokenService.invalidateToken(request.getToken());

            String userId = userService.findUserIdByEmail(email);
            return ResponseEntity.ok(userId);
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    //비밀번호 재설정
    //Swagger 필요
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        Optional<String> emailOptional =
                tokenService.validateTokenAndGetEmail(request.getToken(),
                        TokenPurpose.RESET_PASSWORD);

        if(emailOptional.isPresent()) {
            String email = emailOptional.get();
            tokenService.invalidateToken(request.getToken());

            userService.resetPassword(email, request.getNewPassword());
            return ResponseEntity.ok("비밀번호가 성공적으로 재설정되었습니다.");
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않거나 만료된 토큰입니다.");
        }
    }
}
