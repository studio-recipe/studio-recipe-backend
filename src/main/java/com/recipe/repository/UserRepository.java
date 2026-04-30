package com.recipe.repository;

import com.recipe.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    public boolean existsByNickname(String nickname);
    public boolean existsByEmail(String email);
    public boolean existsById(String id);

    public Optional<User> findById(String id);
    public Optional<User> findByEmail(String email);
}
