package com.example.demo4.service.impl;

import com.example.demo4.dto.request.PaginationRequest;
import com.example.demo4.dto.request.UpdateUserRequest;
import com.example.demo4.dto.request.UserFilterDTO;
import com.example.demo4.dto.response.PagedResponse;
import com.example.demo4.dto.response.PaginationResponse;
import com.example.demo4.dto.response.UserDTO;
import com.example.demo4.dto.response.UserResponse;
import com.example.demo4.entity.Role;
import com.example.demo4.entity.User;
import com.example.demo4.exception.ConflictException;
import com.example.demo4.exception.ResourceNotFoundException;
import com.example.demo4.repository.UserRepository;
import com.example.demo4.service.UserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(UserFilterDTO filter, PaginationRequest pagination) {
        var pageable = PageRequest.of(
                pagination.page(),
                pagination.size(),
                buildSort(pagination.sortDir(), pagination.sortBy()));

        Page<UserResponse> userPage = userRepository
                .findAll(buildSpecification(filter), pageable)
                .map(UserResponse::fromEntity);

        return new PagedResponse<>(
                userPage.getContent(),
                new PaginationResponse(
                        userPage.getNumber(),
                        userPage.getSize(),
                        userPage.getTotalPages(),
                        userPage.getTotalElements()));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    // -------------------------------------------------------------------------
    // Mutations
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        applyEmailUpdate(user, request.email());
        applyPasswordUpdate(user, request.password());
        applyFullNameUpdate(user, request.fullName());
        applyRolesUpdate(user, request.roles());
        applyEnabledUpdate(user, request.enabled());

        return UserDTO.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (userRepository.softDeleteById(id) == 0) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
    }

    @Override
    @Transactional
    public void restoreUser(Long id) {
        if (userRepository.restoreById(id) == 0) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
    }

    // -------------------------------------------------------------------------
    // Update helpers — each owns one field, satisfying SRP
    // -------------------------------------------------------------------------

    private void applyEmailUpdate(User user, String newEmail) {
        if (newEmail == null || newEmail.equalsIgnoreCase(user.getEmail())) {
            return;
        }
        if (userRepository.existsByEmail(newEmail)) {
            throw new ConflictException("Email is already in use");
        }
        user.setEmail(newEmail);
    }

    private void applyPasswordUpdate(User user, String newPassword) {
        if (StringUtils.hasText(newPassword)) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
    }

    private void applyFullNameUpdate(User user, String fullName) {
        if (fullName != null) {
            user.setFullName(fullName);
        }
    }

    private void applyRolesUpdate(User user, Set<String> roleLabels) {
        if (roleLabels == null)
            return;
        // Role.fromString() handles validation and throws a descriptive error on
        // unknown values
        Set<Role> resolved = roleLabels.stream()
                .map(Role::fromString)
                .collect(Collectors.toSet());
        user.setRoles(resolved);
    }

    private void applyEnabledUpdate(User user, Boolean enabled) {
        if (enabled != null) {
            user.setEnabled(enabled);
        }
    }

    // -------------------------------------------------------------------------
    // Specification & sorting — private, not part of the public contract
    // -------------------------------------------------------------------------

    private Specification<User> buildSpecification(UserFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            addLikePredicate(predicates, cb, root.get("username"), filter.username());
            addLikePredicate(predicates, cb, root.get("email"), filter.email());
            addLikePredicate(predicates, cb, root.get("fullName"), filter.fullName());

            if (filter.enabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), filter.enabled()));
            }

            if (StringUtils.hasText(filter.role())) {
                Role roleEnum = Role.fromFilterString(filter.role());
                predicates.add(cb.equal(root.join("roles"), roleEnum));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /** Adds a case-insensitive LIKE predicate only when the value is non-blank. */
    private void addLikePredicate(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Path<String> path,
            String value) {

        if (StringUtils.hasText(value)) {
            predicates.add(cb.like(cb.lower(path), "%" + value.toLowerCase() + "%"));
        }
    }

    private Sort buildSort(String sortDir, String sortBy) {
        return "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
    }
}