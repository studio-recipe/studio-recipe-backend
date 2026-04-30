package com.recipe.util;

import java.security.SecureRandom;
import java.util.Random;

public class VerificationCodeGenerator {
    //보안에 안전한 난수 생성기
    private static final Random RANDOM = new SecureRandom();

    /**
     * 길이 만큼 숫자 인증 코드 생성
     * @param length 코드 길이
     * @return 생성된 숫자 문자열 코드
     */
    public static String generateNumberCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 길이 만큼 영문 대문자+숫자 조합 인증 코드 생성
     * @param length 코드 길이
     * @return 생성된 영문 + 숫자 문자열 코드
     */
    public static String generateAlphaNumberCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(RANDOM.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
