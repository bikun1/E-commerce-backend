package com.example.ecommerce.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class SoftDeletableEntity {

    /**
     * Managed exclusively by Hibernate's @SQLDelete — do NOT call setDeleted()
     * from application code. The setter is package-private to prevent misuse.
     * Column name kept as 'is_deleted' to match the existing DB schema.
     */
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // Package-private: only Hibernate and repository @Modifying queries should
    // touch this.
    void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}