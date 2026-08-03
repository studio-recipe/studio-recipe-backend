package com.recipe.domain.entity;

import com.recipe.domain.entity.enums.PreferenceType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "USER_REFERENCES",
        uniqueConstraints = {
        @UniqueConstraint(name = "UQ_USER_RECIPE_PREFERENCE", columnNames = {"USER_ID", "RCP_SNO"})
        })
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Getter
@ToString
public class UserReferences extends BaseEntityTime{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PREFERENCE_ID")
    private Long preferenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RCP_SNO")
    private Recipe recipe;


// VIEW, SAVE, SHARE, SEARCH 고려
@Enumerated(EnumType.STRING)
@Column(name = "PREFERENCE_TYPE", nullable = false,
        columnDefinition = "ENUM ('VIEW', 'LIKE', 'UNLIKE')")
private PreferenceType preference;

//RATING

public void changePreference(PreferenceType preference) {
    this.preference = preference;
}
}