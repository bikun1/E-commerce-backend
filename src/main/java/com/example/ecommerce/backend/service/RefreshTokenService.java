package com.example.ecommerce.backend.service;

public interface RefreshTokenService {

    void save(String refreshToken, String username, long expirationMs);

    boolean exists(String refreshToken);

    void delete(String refreshToken);

    void deleteAllByUsername(String username);
}
