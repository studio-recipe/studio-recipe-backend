package com.recipe.service;

import com.recipe.domain.entity.User;
import com.recipe.exceptions.user.UserExceptions;
import com.recipe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //회원 단건 조회
    public User findByUser(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(UserExceptions.NOT_FOUND::getUserException);
    }

    //이메일로 아이디 찾기
    public String findUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                UserExceptions.NOT_FOUND::getUserException);
        return user.getId();
    }

    //
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserExceptions.NOT_FOUND::getUserException);

        String encodePassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodePassword);
    }

    public void isUserExistsByEmail(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(UserExceptions.NOT_FOUND::getUserException);
    }
}
