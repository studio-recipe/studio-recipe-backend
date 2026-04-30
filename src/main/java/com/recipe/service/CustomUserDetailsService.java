package com.recipe.service;

import com.recipe.domain.dto.auth.CustomerDetails;
import com.recipe.domain.entity.User;
import com.recipe.exceptions.user.UserExceptions;
import com.recipe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findById(username)
                .orElseThrow(UserExceptions.NOT_FOUND :: getUserException);

        return new CustomerDetails(
                user.getUserId(),
                user.getId(),
                user.getPwd(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE" + user.getRole().name()))
        );
    }
}
