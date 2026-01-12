package com.example.demo4.service.impl;

import com.example.demo4.dto.request.ChangePasswordRequest;
import com.example.demo4.dto.request.LoginRequest;
import com.example.demo4.dto.request.SignupRequest;
import com.example.demo4.entity.Role;
import com.example.demo4.entity.User;
import com.example.demo4.exception.BadRequestException;
import com.example.demo4.exception.ConflictException;
import com.example.demo4.exception.TokenRefreshException;
import com.example.demo4.exception.UnauthorizedException;
import com.example.demo4.repository.UserRepository;
import com.example.demo4.security.JwtUtils;
import com.example.demo4.security.UserDetailsImpl;
import com.example.demo4.security.UserDetailsServiceImpl;
import com.example.demo4.service.RefreshTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

        // ── constants ────────────────────────────────────────────────────────────
        private static final long REFRESH_EXPIRATION_MS = 86_400_000L;
        private static final String USERNAME = "user";
        private static final String EMAIL = "user@test.com";
        private static final String ENCODED_PASSWORD = "encoded-old";
        private static final String RAW_PASSWORD = "old";
        private static final String NEW_PASSWORD = "newpass";
        private static final String ENCODED_NEW_PASSWORD = "encoded-new";
        private static final String ROLE_USER = "ROLE_USER";

        // ── mocks ────────────────────────────────────────────────────────────────
        @Mock
        private AuthenticationManager authenticationManager;
        @Mock
        private UserRepository userRepository;
        @Mock
        private PasswordEncoder passwordEncoder;
        @Mock
        private JwtUtils jwtUtils;
        @Mock
        private UserDetailsServiceImpl userDetailsService;
        @Mock
        private RefreshTokenService refreshTokenService;

        @InjectMocks
        private AuthServiceImpl authService;

        @BeforeEach
        void injectRefreshExpiration() {
                ReflectionTestUtils.setField(authService, "refreshExpirationMs", REFRESH_EXPIRATION_MS);
        }

        @AfterEach
        void clearSecurityContext() {
                SecurityContextHolder.clearContext();
        }

        // ── fixture builders ─────────────────────────────────────────────────────

        private static UserDetailsImpl userDetails() {
                return new UserDetailsImpl(
                                1L, USERNAME, EMAIL, ENCODED_PASSWORD,
                                List.of(new SimpleGrantedAuthority(ROLE_USER)));
        }

        // private static UserDetailsImpl userDetails(String username, String email) {
        // return new UserDetailsImpl(
        // 1L, username, email, ENCODED_PASSWORD,
        // List.of(new SimpleGrantedAuthority(ROLE_USER)));
        // }

        private static Authentication authenticationFor(UserDetailsImpl details) {
                return new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        }

        /** Sets an authenticated principal in the SecurityContext. */
        private static void authenticateAs(UserDetailsImpl details) {
                SecurityContextHolder.getContext().setAuthentication(authenticationFor(details));
        }

        private static User userEntity() {
                User user = new User();
                user.setUsername(USERNAME);
                user.setPassword(ENCODED_PASSWORD);
                return user;
        }

        private static User savedUser(String username, String email, Set<Role> roles) {
                User user = new User();
                user.setId(1L);
                user.setUsername(username);
                user.setEmail(email);
                user.setRoles(roles);
                user.setEnabled(true);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                return user;
        }

        // ════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("login()")
        class Login {

                @Test
                @DisplayName("returns JwtResponse with tokens and user info on success")
                void returnsJwtResponseOnSuccess() {
                        LoginRequest request = new LoginRequest(EMAIL, "password123");
                        UserDetailsImpl details = userDetails();
                        Authentication auth = authenticationFor(details);

                        when(authenticationManager.authenticate(any())).thenReturn(auth);
                        when(jwtUtils.generateJwtToken(auth)).thenReturn("access-token");
                        when(jwtUtils.generateRefreshToken(auth)).thenReturn("refresh-token");

                        var result = authService.login(request);

                        assertThat(result.accessToken()).isEqualTo("access-token");
                        assertThat(result.refreshToken()).isEqualTo("refresh-token");
                        assertThat(result.username()).isEqualTo(USERNAME);
                        assertThat(result.email()).isEqualTo(EMAIL);
                        assertThat(result.roles()).containsExactly(ROLE_USER);
                        verify(refreshTokenService).save("refresh-token", USERNAME, REFRESH_EXPIRATION_MS);
                }
        }

        // ════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("register()")
        class Register {

                @Test
                @DisplayName("creates user with ROLE_USER when no roles are specified")
                void createsUserWithDefaultRole() {
                        SignupRequest request = new SignupRequest("newuser", "new@test.com", "password123", "New User",
                                        null);

                        when(userRepository.countByUsernameIncludeDeleted("newuser")).thenReturn(0L);
                        when(userRepository.countByEmailIncludeDeleted("new@test.com")).thenReturn(0L);
                        when(passwordEncoder.encode("password123")).thenReturn("encoded-pass");
                        when(userRepository.save(any(User.class)))
                                        .thenReturn(savedUser("newuser", "new@test.com", Set.of(Role.ROLE_USER)));

                        var result = authService.register(request);

                        assertThat(result.username()).isEqualTo("newuser");
                        assertThat(result.email()).isEqualTo("new@test.com");
                        verify(userRepository).save(argThat(u -> u.getUsername().equals("newuser") &&
                                        u.getRoles().contains(Role.ROLE_USER)));
                }

                @Test
                @DisplayName("assigns ROLE_ADMIN when roles contain 'admin'")
                void assignsAdminRole() {
                        SignupRequest request = new SignupRequest("adminuser", "admin@test.com", "pass", "Admin",
                                        Set.of("admin"));

                        when(userRepository.countByUsernameIncludeDeleted("adminuser")).thenReturn(0L);
                        when(userRepository.countByEmailIncludeDeleted("admin@test.com")).thenReturn(0L);
                        when(passwordEncoder.encode("pass")).thenReturn("encoded");
                        when(userRepository.save(any(User.class)))
                                        .thenReturn(savedUser("adminuser", "admin@test.com", Set.of(Role.ROLE_ADMIN)));

                        authService.register(request);

                        verify(userRepository).save(argThat(u -> u.getRoles().contains(Role.ROLE_ADMIN)));
                }

                @Test
                @DisplayName("throws ConflictException when username is already taken")
                void throwsWhenUsernameExists() {
                        SignupRequest request = new SignupRequest("existing", "e@test.com", "pass", "Name", null);
                        when(userRepository.countByUsernameIncludeDeleted("existing")).thenReturn(1L);

                        assertThatThrownBy(() -> authService.register(request))
                                        .isInstanceOf(ConflictException.class)
                                        .hasMessageContaining("Username is already taken");

                        verify(userRepository, never()).save(any());
                }

                @Test
                @DisplayName("throws ConflictException when email is already in use")
                void throwsWhenEmailExists() {
                        SignupRequest request = new SignupRequest("newuser", "existing@test.com", "pass", "Name", null);
                        when(userRepository.countByUsernameIncludeDeleted("newuser")).thenReturn(0L);
                        when(userRepository.countByEmailIncludeDeleted("existing@test.com")).thenReturn(1L);

                        assertThatThrownBy(() -> authService.register(request))
                                        .isInstanceOf(ConflictException.class)
                                        .hasMessageContaining("Email is already in use");

                        verify(userRepository, never()).save(any());
                }
        }

        // ════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("refreshToken()")
        class RefreshToken {

                @Test
                @DisplayName("returns new token pair and rotates the old token")
                void rotatesAndReturnsNewTokens() {
                        UserDetailsImpl details = userDetails();

                        when(jwtUtils.validateRefreshTokenAndGetUsername("valid-token")).thenReturn(USERNAME);
                        when(refreshTokenService.exists("valid-token")).thenReturn(true);
                        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(details);
                        when(jwtUtils.generateJwtToken(any())).thenReturn("new-access");
                        when(jwtUtils.generateRefreshToken(any())).thenReturn("new-refresh");

                        var result = authService.refreshToken("valid-token");

                        assertThat(result.accessToken()).isEqualTo("new-access");
                        assertThat(result.refreshToken()).isEqualTo("new-refresh");
                        verify(refreshTokenService).delete("valid-token");
                        verify(refreshTokenService).save("new-refresh", USERNAME, REFRESH_EXPIRATION_MS);
                }

                @Test
                @DisplayName("throws TokenRefreshException when token is not in the store")
                void throwsWhenTokenRevoked() {
                        when(jwtUtils.validateRefreshTokenAndGetUsername("invalid-token")).thenReturn(USERNAME);
                        when(refreshTokenService.exists("invalid-token")).thenReturn(false);

                        assertThatThrownBy(() -> authService.refreshToken("invalid-token"))
                                        .isInstanceOf(TokenRefreshException.class)
                                        .hasMessageContaining("Refresh token is invalid or has been revoked");
                }
        }

        // ════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("logout()")
        class Logout {

                @Test
                @DisplayName("deletes the token and clears the security context")
                void deletesTokenAndClearsContext() {
                        var result = authService.logout("token123");

                        assertThat(result.message()).isEqualTo("You have been logged out successfully!");
                        verify(refreshTokenService).delete("token123");
                }

                @Test
                @DisplayName("skips token deletion when token is null")
                void skipsDeleteWhenTokenIsNull() {
                        var result = authService.logout(null);

                        assertThat(result.message()).isEqualTo("You have been logged out successfully!");
                        verifyNoInteractions(refreshTokenService);
                }
        }

        // ════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("changePassword()")
        class ChangePassword {

                @Test
                @DisplayName("throws UnauthorizedException when no authentication is present")
                void throwsWhenNotAuthenticated() {
                        SecurityContextHolder.clearContext();

                        assertThatThrownBy(() -> authService.changePassword(
                                        new ChangePasswordRequest(RAW_PASSWORD, NEW_PASSWORD, NEW_PASSWORD)))
                                        .isInstanceOf(UnauthorizedException.class)
                                        .hasMessageContaining("Unauthorized");
                }

                @Test
                @DisplayName("throws BadRequestException when current password is wrong")
                void throwsWhenCurrentPasswordIncorrect() {
                        authenticateAs(userDetails());
                        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity()));
                        when(passwordEncoder.matches("wrong", ENCODED_PASSWORD)).thenReturn(false);

                        assertThatThrownBy(() -> authService.changePassword(
                                        new ChangePasswordRequest("wrong", NEW_PASSWORD, NEW_PASSWORD)))
                                        .isInstanceOf(BadRequestException.class)
                                        .hasMessageContaining("Current password is incorrect");
                }

                @Test
                @DisplayName("throws BadRequestException when confirm password does not match")
                void throwsWhenConfirmPasswordMismatch() {
                        authenticateAs(userDetails());
                        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity()));
                        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

                        assertThatThrownBy(() -> authService.changePassword(
                                        new ChangePasswordRequest(RAW_PASSWORD, NEW_PASSWORD, "different")))
                                        .isInstanceOf(BadRequestException.class)
                                        .hasMessageContaining("Confirm password does not match");
                }

                @Test
                @DisplayName("throws BadRequestException when new password is the same as current")
                void throwsWhenNewPasswordSameAsCurrent() {
                        authenticateAs(userDetails());
                        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity()));
                        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

                        assertThatThrownBy(() -> authService.changePassword(
                                        new ChangePasswordRequest(RAW_PASSWORD, RAW_PASSWORD, RAW_PASSWORD)))
                                        .isInstanceOf(BadRequestException.class)
                                        .hasMessageContaining("New password must be different");
                }

                @Test
                @DisplayName("updates password and revokes all refresh tokens on success")
                void updatesPasswordAndRevokesTokens() {
                        authenticateAs(userDetails());
                        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity()));
                        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
                        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);

                        var result = authService.changePassword(
                                        new ChangePasswordRequest(RAW_PASSWORD, NEW_PASSWORD, NEW_PASSWORD));

                        assertThat(result.message()).isEqualTo("Password changed successfully");
                        verify(refreshTokenService).deleteAllByUsername(USERNAME);
                }
        }
}