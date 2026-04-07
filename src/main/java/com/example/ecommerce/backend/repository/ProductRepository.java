package com.example.ecommerce.backend.repository;

import com.example.ecommerce.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository
                extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

        /**
         * Soft-deletes a product by setting is_deleted = true.
         * Native SQL bypasses @SQLDelete to allow explicit control.
         *
         * @return true if a row was affected (id existed)
         */
        @Modifying
        @Query(value = "UPDATE products SET is_deleted = true WHERE id = :id", nativeQuery = true)
        Integer softDeleteById(@Param("id") Long id);

        /**
         * Restores a soft-deleted product.
         * Native SQL bypasses @SQLRestriction which filters out is_deleted = true rows.
         *
         * @return true if a row was affected (id existed and was deleted)
         */
        @Modifying
        @Query(value = "UPDATE products SET is_deleted = false WHERE id = :id", nativeQuery = true)
        Integer restoreById(@Param("id") Long id);
}