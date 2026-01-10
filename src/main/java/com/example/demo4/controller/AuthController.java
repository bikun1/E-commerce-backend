package com.example.demo4.controller;

import com.example.demo4.dto.request.ChangePasswordRequest;
import com.example.demo4.dto.request.LoginRequest;
import com.example.demo4.dto.request.SignupRequest;
import com.example.demo4.dto.response.*;
import com.example.demo4.exception.TokenRefreshException;
import com.example.demo4.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Authentication", description = "Authentication and account security APIs")
public class AuthController {

        private final AuthService authService;

        @Value("${jwt.refresh-expiration}")
        private long refreshExpirationMs;

        // ------------------------------------------------------------------ login

        @PostMapping("/login")
        @Operation(summary = "Login user", description = "Authenticates user and returns access token; sets refresh token as HttpOnly cookie")
        public ResponseEntity<ApiResponse<JwtResponseBody>> login(
                        @Valid @RequestBody LoginRequest request) {

                JwtResponse jwt = authService.login(request);

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(jwt.refreshToken()).toString())
                                .body(ApiResponse.success("Login successful", jwt.toBody()));
        }

        // --------------------------------------------------------------- register

        @PostMapping("/register")
        @Operation(summary = "Register user", description = "Creates a new user account")
        public ResponseEntity<ApiResponse<UserDTO>> register(
                        @Valid @RequestBody SignupRequest request) {

                UserDTO user = authService.register(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.success("User registered successfully!", user));
        }

        // --------------------------------------------------------------- refresh

        @PostMapping("/refresh")
        @Operation(summary = "Refresh access token", description = "Issues a new access token using the refresh token cookie")
        public ResponseEntity<ApiResponse<JwtResponseBody>> refreshToken(
                        @CookieValue(name = "refreshToken", required = false) String refreshToken) {

                if (refreshToken == null || refreshToken.isBlank()) {
                        throw new TokenRefreshException("Refresh token is required");
                }

                JwtResponse jwt = authService.refreshToken(refreshToken);

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(jwt.refreshToken()).toString())
                                .body(ApiResponse.success("Token refreshed successfully", jwt.toBody()));
        }

        // ----------------------------------------------------------------- logout

        @PostMapping("/logout")
        @Operation(summary = "Logout user", description = "Invalidates the refresh token and clears the refresh token cookie")
        public ResponseEntity<ApiResponse<MessageResponse>> logout(
                        @CookieValue(name = "refreshToken", required = false) String refreshToken) {

                MessageResponse result = authService.logout(refreshToken);

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                                .body(ApiResponse.success(result.message(), result));
        }

        // --------------------------------------------------------- change-password

        @PostMapping("/change-password")
        @Operation(summary = "Change password", description = "Changes the user password and clears the refresh token cookie")
        public ResponseEntity<ApiResponse<MessageResponse>> changePassword(
                        @Valid @RequestBody ChangePasswordRequest request) {

                MessageResponse result = authService.changePassword(request);

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                                .body(ApiResponse.success(result.message(), result));
        }

        // -------------------------------------------------------- cookie helpers

        /** Builds a secure HttpOnly refresh-token cookie with the configured TTL. */
        private ResponseCookie buildRefreshCookie(String token) {
                return ResponseCookie.from("refreshToken", token)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(refreshExpirationMs / 1000)
                                .sameSite("Strict")
                                .build();
        }

        /**
         * Returns an expired cookie that instructs the browser to delete the stored
         * refresh token.
         */
        private ResponseCookie clearRefreshCookie() {
                return ResponseCookie.from("refreshToken", "")
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(0)
                                .sameSite("Strict")
                                .build();
        }
}