package com.example.ecommerce.backend.repository;

import com.example.ecommerce.backend.entity.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  /**
   * Aggregates top-selling products across paid/shipped/completed orders.
   * LIMIT 10 hardcoded in SQL — caller applies .limit(n) in service layer for
   * flexibility.
   * Native SQL: bypasses @SQLRestriction via explicit is_deleted check.
   */
  @Query(value = """
      SELECT p.id          AS productId,
             p.name        AS productName,
             SUM(oi.quantity) AS totalQuantity
      FROM order_items oi
      JOIN orders  o ON oi.order_id  = o.id
      JOIN products p ON oi.product_id = p.id
      WHERE o.is_deleted = false
        AND o.status IN ('PAID', 'SHIPPED', 'COMPLETED')
      GROUP BY p.id, p.name
      ORDER BY totalQuantity DESC
      LIMIT :limit
      """, nativeQuery = true)
  List<Object[]> findTopSellingProducts(@Param("limit") int limit);
}