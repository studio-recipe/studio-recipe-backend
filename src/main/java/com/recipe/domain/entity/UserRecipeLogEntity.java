/*

package com.recipe.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "USER_RECIPE_LOGS",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_USER_RECIPE_LOG", columnNames = {"USER_ID", "RCP_SNO"})
        })
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"user", "recipe"})
public class UserRecipeLogEntity extends BaseEntityTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOG_ID")
    private Long id;

    // 사용자 정보 (N:1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    // 레시피 정보 (N:1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RCP_SNO", nullable = false)
    private Recipe recipe;

    // 사용자의 행동 기록
    @Column(name = "LIKED", nullable = false)
    private boolean liked;

    @Column(name = "BOOKMARKED", nullable = false)
    private boolean bookmarked;

    @Column(name = "VIEW_COUNT", nullable = false)
    private int viewCount;

    // === 비즈니스 로직 ===
    public void increaseViewCount() {
        this.viewCount++;
    }

    public void toggleLike() {
        this.liked = !this.liked;
    }

    public void toggleBookmark() {
        this.bookmarked = !this.bookmarked;
    }
}
*/