package com.recipe.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Log4j2
public class MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine  templateEngine; //Thymeleaf 템플릿 엔진

    @Value("${MAIL_USERNAME}")
    private String fromEmail;

    /**
     * HTML 형식 인증 번호 이메일
     * @param toEmail 수신자 이메일 주소
     * @param verificationCode 발송한 인증 코드
     */
    public void sendVerificationEmail(String toEmail, String verificationCode) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[원룸 레시피] 이메일 인증 번호");

            //타임리프 컨텍스트 설정
            Context context = new Context();
            context.setVariable("verificationCode", verificationCode); //템플릿에서 사용할 변수
            context.setVariable("expirationMinutes",
                    VerificationCodeService.getExpirationSeconds()/ 60);

            //템플릿 엔진으로 HTML 내용 생성
            String htmlContent = templateEngine.process("email_verification-template", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email Send to {} with code {}", toEmail, verificationCode);
        } catch (MessagingException ex) {
            log.error("Failed to send verification email to {} ", toEmail);
            throw new RuntimeException("이메일 발송 실패 예외 변경 필요", ex);
        }
    }
}
