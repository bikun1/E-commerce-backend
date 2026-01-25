package com.example.demo4.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo4.entity.Image;
import com.example.demo4.exception.BadRequestException;
import com.example.demo4.repository.ImageRepository;
import com.example.demo4.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageUploadServiceImpl implements ImageUploadService {

    private final Cloudinary cloudinary;
    private final ImageRepository imageRepository;

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.emptyMap());

            String url = (String) uploadResult.get("secure_url");
            if (url == null) {
                throw new IllegalStateException("Upload succeeded but secure_url is missing");
            }

            Image image = new Image();
            image.setUrl(url);
            imageRepository.save(image);

            return url;
        } catch (IOException e) {
            throw new BadRequestException("Failed to read file for upload: " + e.getMessage());
        }
    }
}