package com.example.demo4.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo4.dto.request.LoginRequest;
import com.example.demo4.dto.request.ChangePasswordRequest;
import com.example.demo4.dto.request.SignupRequest;
import com.example.demo4.dto.response.JwtResponse;
import com.example.demo4.dto.response.MessageResponse;
import com.example.demo4.dto.response.UserDTO;
import com.example.demo4.entity.Role;
import com.example.demo4.entity.User;
import com.example.demo4.exception.BadRequestException;
import com.example.demo4.exception.ConflictException;
import com.example.demo4.exception.TokenRefreshException;
import com.example.demo4.exception.UnauthorizedException;
import com.example.demo4.repository.UserRepository;
import com.example.demo4.security.JwtUtils;
import com.example.demo4.security.UserDetailsServiceImpl;
import com.example.demo4.security.UserDetailsImpl;
import com.example.demo4.service.AuthService;
import com.example.demo4.service.RefreshTokenService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Override
    public JwtResponse login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String accessToken = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication);

        refreshTokenService.save(refreshToken, userDetails.getUsername(), refreshExpirationMs);

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(item -> item.getAuthority())
                .toList();

        return new JwtResponse(
                accessToken,
                refreshToken,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    @Override
    @Transactional
    public UserDTO register(SignupRequest signUpRequest) {

        if (userRepository.countByUsernameIncludeDeleted(signUpRequest.username()) > 0) {
            throw new ConflictException("Username is already taken!");
        }

        if (userRepository.countByEmailIncludeDeleted(signUpRequest.email()) > 0) {
            throw new ConflictException("Email is already in use!");
        }

        User user = new User();
        user.setUsername(signUpRequest.username());
        user.setEmail(signUpRequest.email());
        user.setPassword(passwordEncoder.encode(signUpRequest.password()));
        user.setFullName(signUpRequest.fullName());

        Set<Role> roles = new HashSet<>();

        if (signUpRequest.roles() == null || signUpRequest.roles().isEmpty()) {
            roles.add(Role.ROLE_USER);
        } else {
            signUpRequest.roles().forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin" -> roles.add(Role.ROLE_ADMIN);
                    case "mod" -> roles.add(Role.ROLE_MODERATOR);
                }
            });

            if (roles.isEmpty()) {
                roles.add(Role.ROLE_USER);
            }
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        return UserDTO.fromEntity(savedUser);
    }

    @Override
    public JwtResponse refreshToken(String token) {

        String username = jwtUtils.validateRefreshTokenAndGetUsername(token);

        if (!refreshTokenService.exists(token)) {
            throw new TokenRefreshException(
                    String.format("Refresh token is invalid or has been revoked: %s", token));
        }

        // Rotate refresh token
        refreshTokenService.delete(token);

        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String newAccessToken = jwtUtils.generateJwtToken(authentication);
        String newRefreshToken = jwtUtils.generateRefreshToken(authentication);

        refreshTokenService.save(newRefreshToken, username, refreshExpirationMs);

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(item -> item.getAuthority())
                .toList();

        return new JwtResponse(
                newAccessToken,
                newRefreshToken,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    @Override
    public MessageResponse logout(String token) {

        if (token != null && !token.isBlank()) {
            refreshTokenService.delete(token);
        }

        SecurityContextHolder.clearContext();

        return new MessageResponse("You have been logged out successfully!");
    }

    @Override
    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
            throw new UnauthorizedException("Unauthorized");
        }

        String username = principal.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new BadRequestException("Confirm password does not match");
        }

        if (request.newPassword().equals(request.currentPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        // userRepository.save(user);

        // revoke all refresh tokens for this user
        refreshTokenService.deleteAllByUsername(username);

        return new MessageResponse("Password changed successfully");
    }
}
