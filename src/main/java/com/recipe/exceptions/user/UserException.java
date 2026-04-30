package com.recipe.exceptions.user;

import lombok.Getter;

@Getter
public class UserException extends RuntimeException {
    String message;
    int code;

    public UserException(String message, int code) {
        super(message);
        this.message = message;
        this.code = code;
    }

    public void changeMessage(String message){
        this.message = message;
    }
}
