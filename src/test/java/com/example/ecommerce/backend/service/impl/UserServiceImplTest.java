package com.example.ecommerce.backend.service.impl;

import com.example.ecommerce.backend.dto.request.PaginationRequest;
import com.example.ecommerce.backend.dto.request.UpdateUserRequest;
import com.example.ecommerce.backend.dto.request.UserFilterDTO;
import com.example.ecommerce.backend.dto.response.PagedResponse;
import com.example.ecommerce.backend.dto.response.UserDTO;
import com.example.ecommerce.backend.entity.Role;
import com.example.ecommerce.backend.entity.User;
import com.example.ecommerce.backend.exception.ConflictException;
import com.example.ecommerce.backend.exception.ResourceNotFoundException;
import com.example.ecommerce.backend.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    // ── Shared constants ──────────────────────────────────────────────────────
    private static final long EXISTING_ID = 1L;
    private static final long MISSING_ID = 999L;
    private static final String EXISTING_EMAIL = "user@test.com";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    // ── updateUser ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateUser — throws ResourceNotFoundException when user not found")
    void updateUser_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> userService.updateUser(MISSING_ID, UpdateUserRequest.empty().withEmail("new@test.com")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: " + MISSING_ID);
    }

    @Test
    @DisplayName("updateUser — throws ConflictException when email is already in use by another user")
    void updateUser_shouldThrowWhenEmailInUse() {
        User user = createUser(EXISTING_ID, "user", EXISTING_EMAIL);
        UpdateUserRequest request = UpdateUserRequest.empty().withEmail("taken@test.com");

        when(userRepository.findById(EXISTING_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(EXISTING_ID, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email is already in use");
    }

    @Test
    @DisplayName("updateUser — skips existsByEmail DB call when email is unchanged")
    void updateUser_shouldSkipEmailCheckWhenEmailUnchanged() {
        User user = createUser(EXISTING_ID, "user", EXISTING_EMAIL);
        UpdateUserRequest request = UpdateUserRequest.empty()
                .withEmail(EXISTING_EMAIL)
                .withFullName("New Name");

        when(userRepository.findById(EXISTING_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDTO result = userService.updateUser(EXISTING_ID, request);

        assertThat(result).isNotNull();
        assertThat(user.getFullName()).isEqualTo("New Name");
        // Key assertion: no round-trip to the DB for an email that didn't change
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("updateUser — maps 'admin' and 'moderator' labels to correct Role enums")
    void updateUser_shouldUpdateRoles() {
        User user = createUser(EXISTING_ID, "user", EXISTING_EMAIL);
        user.setRoles(Set.of(Role.ROLE_USER));
        UpdateUserRequest request = UpdateUserRequest.empty()
                .withRoles(Set.of("admin", "moderator"));

        when(userRepository.findById(EXISTING_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateUser(EXISTING_ID, request);

        verify(userRepository).save(argThat(u -> u.getRoles().contains(Role.ROLE_ADMIN) &&
                u.getRoles().contains(Role.ROLE_MODERATOR)));
    }

    @Test
    @DisplayName("updateUser — maps alias 'mod' to ROLE_MODERATOR")
    void updateUser_shouldMapModAliasToModerator() {
        User user = createUser(EXISTING_ID, "user", EXISTING_EMAIL);
        UpdateUserRequest request = UpdateUserRequest.empty().withRoles(Set.of("mod"));

        when(userRepository.findById(EXISTING_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateUser(EXISTING_ID, request);

        verify(userRepository).save(argThat(u -> u.getRoles().contains(Role.ROLE_MODERATOR)));
    }

    @Test
    @DisplayName("updateUser — encodes password before persisting")
    void updateUser_shouldEncodePassword() {
        User user = createUser(EXISTING_ID, "user", EXISTING_EMAIL);
        UpdateUserRequest request = UpdateUserRequest.empty().withPassword("newpass123");

        when(userRepository.findById(EXISTING_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass123")).thenReturn("encoded-new");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateUser(EXISTING_ID, request);

        assertThat(user.getPassword()).isEqualTo("encoded-new");
    }

    @Test
    @DisplayName("updateUser — persists enabled=false when explicitly set")
    void updateUser_shouldUpdateEnabled() {
        User user = createUser(EXISTING_ID, "user", EXISTING_EMAIL);
        user.setEnabled(true);
        UpdateUserRequest request = UpdateUserRequest.empty().withEnabled(false);

        when(userRepository.findById(EXISTING_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateUser(EXISTING_ID, request);

        assertThat(user.getEnabled()).isFalse();
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserById — throws ResourceNotFoundException when user not found")
    void getUserById_shouldThrowWhenNotFound() {
        when(userRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(MISSING_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: " + MISSING_ID);
    }

    // ── deleteUser ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteUser — throws ResourceNotFoundException when user not found")
    void deleteUser_shouldThrowWhenNotFound() {
        when(userRepository.softDeleteById(MISSING_ID)).thenReturn(0);

        assertThatThrownBy(() -> userService.deleteUser(MISSING_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: " + MISSING_ID);
    }

    @Test
    @DisplayName("deleteUser — delegates to softDeleteById")
    void deleteUser_shouldSoftDelete() {
        when(userRepository.softDeleteById(EXISTING_ID)).thenReturn(1);

        userService.deleteUser(EXISTING_ID);

        verify(userRepository).softDeleteById(EXISTING_ID);
    }

    // ── restoreUser ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("restoreUser — throws ResourceNotFoundException when user not found")
    void restoreUser_shouldThrowWhenNotFound() {
        when(userRepository.restoreById(MISSING_ID)).thenReturn(0);

        assertThatThrownBy(() -> userService.restoreUser(MISSING_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: " + MISSING_ID);
    }

    @Test
    @DisplayName("restoreUser — delegates to restoreById")
    void restoreUser_shouldRestore() {
        when(userRepository.restoreById(EXISTING_ID)).thenReturn(1);

        userService.restoreUser(EXISTING_ID);

        verify(userRepository).restoreById(EXISTING_ID);
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllUsers — returns paged content with correct size")
    void getAllUsers_shouldReturnPaged() {
        Page<User> page = new PageImpl<>(List.of(createUser(EXISTING_ID, "user", EXISTING_EMAIL)));
        when(userRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        PagedResponse<?> result = userService.getAllUsers(
                UserFilterDTO.empty(),
                new PaginationRequest(0, 10, "id", "asc"));

        assertThat(result.content()).hasSize(1);
    }

    // ── Test helpers ──────────────────────────────────────────────────────────

    private User createUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("hashed-pass");
        return user;
    }
}