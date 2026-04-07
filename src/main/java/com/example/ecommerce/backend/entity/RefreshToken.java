package com.example.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens", indexes = {
                @Index(name = "idx_refresh_tokens_token", columnList = "token", unique = true),
                @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id")
})
@SQLDelete(sql = "UPDATE refresh_tokens SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken extends SoftDeletableEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, unique = true, length = 600)
        private String token;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Column(name = "expires_at", nullable = false)
        private Instant expiresAt;

        @Column(name = "revoked", nullable = false)
        private boolean revoked = false;

        @CreationTimestamp
        @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt;
}
