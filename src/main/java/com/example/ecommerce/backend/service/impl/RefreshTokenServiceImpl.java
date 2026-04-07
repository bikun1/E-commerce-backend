package com.example.ecommerce.backend.service.impl;

import com.example.ecommerce.backend.entity.RefreshToken;
import com.example.ecommerce.backend.entity.User;
import com.example.ecommerce.backend.exception.ResourceNotFoundException;
import com.example.ecommerce.backend.repository.RefreshTokenRepository;
import com.example.ecommerce.backend.repository.UserRepository;
import com.example.ecommerce.backend.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void save(String token, String username, long expirationMs) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        RefreshToken entity = new RefreshToken();
        entity.setToken(token);
        entity.setUser(user);
        entity.setExpiresAt(Instant.now().plusMillis(expirationMs));
        // revoked defaults to false in the entity — no need to set explicitly

        refreshTokenRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(String token) {
        Instant now = Instant.now();
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isRevoked())
                .filter(rt -> rt.getExpiresAt().isAfter(now))
                .isPresent();
    }

    @Override
    @Transactional
    public void delete(String token) {
        refreshTokenRepository.revokeByToken(token);
    }

    @Override
    @Transactional
    public void deleteAllByUsername(String username) {
        if (userRepository.findByUsername(username).isEmpty()) {
            throw new ResourceNotFoundException("User", "username", username);
        }
        // Query by username directly — avoids loading the User entity just to pass it
        // in
        refreshTokenRepository.revokeAllByUsername(username);
    }
}