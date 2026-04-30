package com.recipe.domain.entity.enums;

import lombok.Getter;

@Getter
public enum Gender {
    F("FEMALE"), M("MALE");

    private final String gender;

    Gender(String gender) {
        this.gender = gender;
    }
}
