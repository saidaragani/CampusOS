package com.campusos.auth_service.security.service;

import com.campusos.auth_service.entity.User;
import com.campusos.auth_service.repository.UserRepository;
import com.campusos.auth_service.security.User.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        log.debug("Loading user by email: {}. Found user with role: {}", user.getEmail(), user.getRole());
        return new UserPrincipal(user);
    }
}
