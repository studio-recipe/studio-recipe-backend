package com.recipe.exceptions.recipe;

import lombok.Getter;

@Getter
public enum RecipeExceptions {
    NOT_FOUND("해당 레시피를 찾을 수 없습니다.", 404),
    BAD_REQUEST("요청 값이 잘못되었습니다.", 400),
    FORBIDDEN("본인의 레시피만 수정/삭제할 수 있습니다.", 403);

    private RecipeException recipeException;

    RecipeExceptions(String massage, int code) {
        recipeException = new RecipeException(massage, code);
    }

    public RecipeException getRecipeException() {
        return recipeException;
    }
}
