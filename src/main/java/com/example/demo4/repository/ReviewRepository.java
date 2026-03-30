package com.example.demo4.repository;

import com.example.demo4.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    List<Review> findByProductId(Long productId);

    @Query("select avg(r.rating) from Review r where r.product.id = :productId")
    Double getAverageRatingForProduct(@Param("productId") Long productId);
}

