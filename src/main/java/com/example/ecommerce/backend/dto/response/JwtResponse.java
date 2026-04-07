package com.example.ecommerce.backend.dto.response;

import java.util.List;

/** Internal representation — includes refreshToken for cookie extraction. */
public record JwtResponse(
        String accessToken,
        String refreshToken,
        Long id,
        String username,
        String email,
        List<String> roles) {
    /** Public-safe projection — omits refreshToken (sent via HttpOnly cookie). */
    public JwtResponseBody toBody() {
        return new JwtResponseBody(accessToken, id, username, email, roles);
    }
}