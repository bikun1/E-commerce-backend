package com.example.demo4.dto.response;

import java.util.List;

/** Response body sent to client — refreshToken is intentionally excluded. */
public record JwtResponseBody(
        String accessToken,
        Long id,
        String username,
        String email,
        List<String> roles) {
}