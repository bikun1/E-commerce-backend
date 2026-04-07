package com.example.ecommerce.backend.repository;

import com.example.ecommerce.backend.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}