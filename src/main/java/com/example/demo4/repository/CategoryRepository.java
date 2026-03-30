package com.example.demo4.repository;

import com.example.demo4.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

        /** Custom soft delete using native SQL (explicitly set is_deleted = true). */
        @Modifying
        @Query(value = "UPDATE categories SET is_deleted = true WHERE id = :id", nativeQuery = true)
        Integer softDeleteById(@Param("id") Long id);

        /**
         * Native SQL to bypass @SQLRestriction so we can restore rows with is_deleted =
         * true.
         */
        @Modifying
        @Query(value = "UPDATE categories SET is_deleted = false WHERE id = :id", nativeQuery = true)
        Integer restoreById(@Param("id") Long id);

        /**
         * Check unique including soft-deleted (bypass @SQLRestriction). Use before
         * create.
         */
        @Query(value = "SELECT COUNT(*) FROM categories WHERE name = :name", nativeQuery = true)
        Integer countByNameIncludeDeleted(@Param("name") String name);
}
