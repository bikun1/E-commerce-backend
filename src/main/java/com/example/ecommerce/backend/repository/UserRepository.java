package com.example.ecommerce.backend.repository;

import com.example.ecommerce.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // -------------------------------------------------------------------------
    // Lookups (honour @SQLRestriction — active users only)
    // -------------------------------------------------------------------------

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // -------------------------------------------------------------------------
    // Soft-delete lifecycle
    // -------------------------------------------------------------------------
    /** Custom soft delete using native SQL (explicitly set is_deleted = true). */
    @Modifying
    @Query(value = "UPDATE users SET is_deleted = true WHERE id = :id", nativeQuery = true)
    Integer softDeleteById(@Param("id") Long id);

    /**
     * Restores a soft-deleted user by clearing the is_deleted flag.
     * Uses a native query to bypass @SQLRestriction, which filters out deleted
     * rows.
     *
     * @return 1 if a row was updated, 0 if no matching deleted user was found
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET is_deleted = false WHERE id = :id", nativeQuery = true)
    Integer restoreById(@Param("id") Long id);

    // -------------------------------------------------------------------------
    // Uniqueness checks that include soft-deleted rows
    // (used before creating a new user to prevent duplicate username/email)
    // -------------------------------------------------------------------------

    /**
     * Returns true if any user — active or deleted — holds the given username.
     * Bypasses @SQLRestriction intentionally to prevent recycling deleted
     * usernames.
     */
    @Query(value = "SELECT COUNT(*) FROM users WHERE username = :username", nativeQuery = true)
    Long countByUsernameIncludeDeleted(@Param("username") String username);

    /**
     * Returns true if any user — active or deleted — holds the given email.
     * Bypasses @SQLRestriction intentionally to prevent recycling deleted emails.
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM users WHERE email = :email", nativeQuery = true)
    Long countByEmailIncludeDeleted(@Param("email") String email);
}