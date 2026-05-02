package com.recipe.domain.entity;

import jakarta.persistence.*;
import lombok.*;


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