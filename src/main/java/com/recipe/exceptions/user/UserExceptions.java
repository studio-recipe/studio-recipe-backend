package com.recipe.exceptions.user;

import lombok.Getter;

@Getter
public enum UserExceptions {
    NOT_FOUND("NOT_FOUND", 404),
    CONFLICT("CONFLICT", 409);

    private String message;
    private int code;

    UserExceptions(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public UserException getUserException() {
        return new UserException(message, code);
    }

    public UserException getUserException(String changeMessage) {
        return new UserException(changeMessage, code);
    }
}
