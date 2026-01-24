package com.example.demo4.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds 'cloudinary.*' from application properties.
 */
@ConfigurationProperties(prefix = "cloudinary")
public record CloudinaryProperties(
        String cloudName,
        String apiKey,
        String apiSecret) {
}