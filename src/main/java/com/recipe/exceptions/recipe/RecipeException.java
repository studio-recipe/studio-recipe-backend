package com.recipe.exceptions.recipe;

import lombok.Getter;

@Getter
public class RecipeException extends RuntimeException{
    private String msg;
    private int code;

    public RecipeException(String msg, int code){
        super(msg);
        this.msg = msg;
        this.code = code;
    }
}
