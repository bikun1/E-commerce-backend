package com.example.ecommerce.backend.controller;

import com.example.ecommerce.backend.dto.response.ApiResponse;
import com.example.ecommerce.backend.dto.response.ImageUploadResponse;
import com.example.ecommerce.backend.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Uploads", description = "File upload APIs")
public class ImageUploadController {

        private final ImageUploadService imageUploadService;

        @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Upload image", description = "Uploads an image file and returns its public URL")
        public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
                        @RequestPart("file") MultipartFile file) {

                String url = imageUploadService.uploadImage(file);
                return ResponseEntity.ok(
                                ApiResponse.success("Image uploaded successfully", new ImageUploadResponse(url)));
        }
}