package com.example.ecommerce.backend.repository;

import com.example.ecommerce.backend.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /**
     * Soft-revokes a single token by marking it as deleted.
     * Named "revoke" (not "delete") to reflect that the row is not physically
     * removed.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.token = :token")
    void revokeByToken(@Param("token") String token);

    /**
     * Soft-revokes all tokens belonging to a user (e.g. on logout or password
     * change).
     * Queries by username directly — avoids a redundant User lookup before calling
     * this.
     *
     * @return number of tokens revoked
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.username = :username")
    Integer revokeAllByUsername(@Param("username") String username);
}