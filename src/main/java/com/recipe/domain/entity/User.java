package com.recipe.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.recipe.domain.entity.enums.Gender;
import com.recipe.domain.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Table(name = "USERS")
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@Getter
public class User extends BaseEntityTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "ID",unique = true, nullable = false, length = 50)
    private String id;
    @Column(name="PWD", nullable = false, length = 255)
    private String pwd;
    @Column(name="NAME", nullable = false, length = 100)
    private String name;
    @Column(name="NICKNAME" ,unique = true, nullable = false, length = 50)
    private String nickname;
    @Column(name="EMAIL",unique = true, nullable = false, length = 50)
    private String email;

    @Column(name="BIRTH", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(name="GENDER", nullable = false,
    columnDefinition = "ENUM('F', 'M')")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", columnDefinition = "ENUM('ADMIN', 'GUEST') DEFAULT 'GUEST'")
    private Role role;

    public void changePassword(String newPassword) {
        this.pwd = newPassword;
    }

}
