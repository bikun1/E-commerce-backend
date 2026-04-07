package com.example.ecommerce.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.backend.entity.User;
import com.example.ecommerce.backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        @Transactional
        public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
                User user = usernameOrEmail.contains("@")
                                ? userRepository.findByEmail(usernameOrEmail)
                                                .orElseThrow(
                                                                () -> new UsernameNotFoundException(
                                                                                "User Not Found with email: "
                                                                                                + usernameOrEmail))
                                : userRepository.findByUsername(usernameOrEmail)
                                                .orElseThrow(() -> new UsernameNotFoundException(
                                                                "User Not Found with username: " + usernameOrEmail));
                return UserDetailsImpl.build(user);
        }
}