package com.example.ecommerce.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {

    /**
     * Upload an image to Cloudinary and return its secure URL.
     */
    String uploadImage(MultipartFile file);
}
