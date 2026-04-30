package com.recipe.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SortBy {
    @Schema(name = "날짜순")
    CREATED_AT("firstRegDt"),
    @Schema(name = "조회수")
    INQUIRY_COUNT("inqCnt"),
    @Schema(name = "좋아요")
    RECOMMENDED_COUNT("rcmmCnt");

    private final String fieldName;

    public static SortBy formString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return CREATED_AT; // default fallback
        }
        
        for (SortBy b : SortBy.values()) {
            if (b.name().equalsIgnoreCase(text.trim()) || b.fieldName.equalsIgnoreCase(text.trim())) {
                return b;
            }
        }
        
        // ✅ Return default instead of null to prevent NPE
        return CREATED_AT;
    }

}
