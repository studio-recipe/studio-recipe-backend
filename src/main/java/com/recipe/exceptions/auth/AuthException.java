package com.recipe.exceptions.auth;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    String message;
    int code;

    public AuthException(String message, int code) {
        super(message);
        this.message = message;
        this.code = code;
    }
}
