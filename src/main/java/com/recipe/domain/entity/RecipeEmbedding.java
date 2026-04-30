package com.recipe.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

// DB를 삭제해야 해서 편의상 Flask에서 사용되는 임베딩을 여기서 만듦

@Entity
@Table(name = "RECIPE_EMBEDDINGS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeEmbedding {

    @Id
    @Column(name = "RCP_SNO")
    private Long rcpSno;

    @Lob
    @Column(name = "VECTOR", nullable = false)
    private String vector;
}