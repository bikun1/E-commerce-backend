package com.example.demo4.repository;

import com.example.demo4.entity.Order;
import com.example.demo4.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

  /**
   * Caller controls sort via Pageable — pass Sort.by(DESC, "createdAt") at call
   * site.
   */
  Page<Order> findByUserId(Long userId, Pageable pageable);

  Optional<Order> findByIdAndUserId(Long id, Long userId);

  /**
   * Caller controls sort via Pageable — pass Sort.by(DESC, "createdAt") at call
   * site.
   */
  Page<Order> findAll(Pageable pageable);

  Page<Order> findByStatus(OrderStatus status, Pageable pageable);

  /**
   * Aggregates monthly revenue for paid/shipped/completed orders.
   * Native SQL: bypasses @SQLRestriction intentionally via explicit is_deleted
   * check.
   * MySQL-specific YEAR()/MONTH() functions used intentionally.
   */
  @Query(value = """
      SELECT YEAR(o.created_at)  AS year,
             MONTH(o.created_at) AS month,
             SUM(o.total_price)  AS revenue
      FROM orders o
      WHERE o.is_deleted = false
        AND o.status IN ('PAID', 'SHIPPED', 'COMPLETED')
      GROUP BY YEAR(o.created_at), MONTH(o.created_at)
      ORDER BY year DESC, month DESC
      """, nativeQuery = true)
  List<Object[]> findRevenueByMonth();
}