package com.recipe.domain.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "USER_EMBEDDINGS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEmbedding {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Lob
    @Column(name = "VECTOR", nullable = false)
    private String vector;
}