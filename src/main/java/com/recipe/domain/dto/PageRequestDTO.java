package com.recipe.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Builder
@AllArgsConstructor
@Getter
//@Setter
@ToString
public class PageRequestDTO {
    @Builder.Default
    @Min(0)
    private int page =0;

    @Builder.Default
    @Min(10)
    @Max(100)
    private int size = 10;

    @Builder.Default
    private SortBy sortBy = SortBy.CREATED_AT;

    @Builder.Default
    private String direction = "asc";


    public Pageable getPageable() {
        int pageNumber = page < 0 ? 0 : page;
        int sizeNumber = size <= 10 ? 10 : size;

        Sort sort;
        if ("desc".equalsIgnoreCase(this.direction)) {
            sort = Sort.by(Sort.Order.desc(this.sortBy.getFieldName()));
        }else{
            sort = Sort.by(Sort.Order.asc(this.sortBy.getFieldName()));
        }
        return PageRequest.of(pageNumber, sizeNumber, sort);
    }
}
