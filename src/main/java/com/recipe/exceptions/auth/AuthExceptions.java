package com.recipe.exceptions.auth;

import lombok.Getter;

@Getter
public enum AuthExceptions {
    INVALID_REFRESH_TOKEN("유효하지 않은 Refresh Token입니다.", 401),
    TOKEN_REUSE_DETECTED("Refresh Token 재사용이 감지되어 세션이 종료되었습니다. 다시 로그인해주세요.", 401);

    private String message;
    private int code;

    AuthExceptions(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public AuthException getAuthException() {
        return new AuthException(message, code);
    }

    public AuthException getAuthException(String changeMessage) {
        return new AuthException(changeMessage, code);
    }
}
