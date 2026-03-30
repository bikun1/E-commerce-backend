package com.example.demo4.service.impl;

import com.example.demo4.entity.RefreshToken;
import com.example.demo4.entity.User;
import com.example.demo4.exception.ResourceNotFoundException;
import com.example.demo4.repository.RefreshTokenRepository;
import com.example.demo4.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    // ── Constants ────────────────────────────────────────────────────────────
    private static final String KNOWN_USERNAME = "user";
    private static final String UNKNOWN_USERNAME = "unknown";
    private static final String TOKEN_VALUE = "refresh-token";
    private static final long EXPIRATION_MS = 86_400_000L;
    private static final long ONE_HOUR_SEC = 3_600L;

    // ── Mocks & SUT ──────────────────────────────────────────────────────────
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    // ── Shared fixtures ──────────────────────────────────────────────────────
    private User knownUser;

    @BeforeEach
    void setUpFixtures() {
        knownUser = buildUser(1L, KNOWN_USERNAME);
    }

    // ════════════════════════════════════════════════════════════════════════
    // save()
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("throws IllegalArgumentException when user does not exist")
        void throwsWhenUserNotFound() {
            stubUserNotFound();

            assertThatThrownBy(() -> refreshTokenService.save(TOKEN_VALUE, UNKNOWN_USERNAME, EXPIRATION_MS))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found with username: " + UNKNOWN_USERNAME);

            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("persists token with correct user, value and future expiry")
        void persistsTokenForKnownUser() {
            stubUserFound();
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            refreshTokenService.save(TOKEN_VALUE, KNOWN_USERNAME, EXPIRATION_MS);

            RefreshToken saved = captureLastSavedToken();
            assertThat(saved.getToken()).isEqualTo(TOKEN_VALUE);
            assertThat(saved.getUser()).isSameAs(knownUser);
            assertThat(saved.isRevoked()).isFalse();
            assertThat(saved.getExpiresAt()).isAfter(Instant.now());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // exists()
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("exists()")
    class Exists {

        @Test
        @DisplayName("returns false when token not found")
        void returnsFalseWhenTokenMissing() {
            when(refreshTokenRepository.findByToken(TOKEN_VALUE)).thenReturn(Optional.empty());

            assertThat(refreshTokenService.exists(TOKEN_VALUE)).isFalse();
        }

        @Test
        @DisplayName("returns false when token is revoked")
        void returnsFalseWhenRevoked() {
            stubFoundToken(revokedToken());

            assertThat(refreshTokenService.exists(TOKEN_VALUE)).isFalse();
        }

        @Test
        @DisplayName("returns false when token is expired")
        void returnsFalseWhenExpired() {
            stubFoundToken(expiredToken());

            assertThat(refreshTokenService.exists(TOKEN_VALUE)).isFalse();
        }

        @Test
        @DisplayName("returns true when token is valid (not revoked, not expired)")
        void returnsTrueWhenValid() {
            stubFoundToken(validToken());

            assertThat(refreshTokenService.exists(TOKEN_VALUE)).isTrue();
        }

        // ── Stubs ────────────────────────────────────────────────────────────
        private void stubFoundToken(RefreshToken token) {
            when(refreshTokenRepository.findByToken(TOKEN_VALUE)).thenReturn(Optional.of(token));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // delete()
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("delegates deletion to repository by token value")
        void delegatesToRepository() {
            refreshTokenService.delete(TOKEN_VALUE);

            verify(refreshTokenRepository).revokeByToken(TOKEN_VALUE);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // deleteAllByUsername()
    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("deleteAllByUsername()")
    class DeleteAllByUsername {

        @Test
        @DisplayName("throws ResourceNotFoundException when user does not exist")
        void throwsWhenUserNotFound() {
            stubUserNotFound();

            assertThatThrownBy(() -> refreshTokenService.deleteAllByUsername(UNKNOWN_USERNAME))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found with username: " + UNKNOWN_USERNAME);

            verify(refreshTokenRepository, never()).revokeAllByUsername(any());
        }

        @Test
        @DisplayName("deletes all tokens belonging to the resolved user entity")
        void deletesAllTokensForUser() {
            stubUserFound();

            refreshTokenService.deleteAllByUsername(KNOWN_USERNAME);

            verify(refreshTokenRepository).revokeAllByUsername(KNOWN_USERNAME);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Shared stubs
    // ════════════════════════════════════════════════════════════════════════
    private void stubUserFound() {
        when(userRepository.findByUsername(KNOWN_USERNAME)).thenReturn(Optional.of(knownUser));
    }

    private void stubUserNotFound() {
        when(userRepository.findByUsername(UNKNOWN_USERNAME)).thenReturn(Optional.empty());
    }

    // ════════════════════════════════════════════════════════════════════════
    // Token factories — each communicates its own intent
    // ════════════════════════════════════════════════════════════════════════
    private RefreshToken validToken() {
        RefreshToken rt = new RefreshToken();
        rt.setToken(TOKEN_VALUE);
        rt.setRevoked(false);
        rt.setExpiresAt(Instant.now().plusSeconds(ONE_HOUR_SEC));
        return rt;
    }

    private RefreshToken revokedToken() {
        RefreshToken rt = validToken();
        rt.setRevoked(true);
        return rt;
    }

    private RefreshToken expiredToken() {
        RefreshToken rt = validToken();
        rt.setExpiresAt(Instant.now().minusSeconds(ONE_HOUR_SEC));
        return rt;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Builders & capture helpers
    // ════════════════════════════════════════════════════════════════════════
    private User buildUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(username + "@test.com");
        return u;
    }

    private RefreshToken captureLastSavedToken() {
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        return captor.getValue();
    }
}