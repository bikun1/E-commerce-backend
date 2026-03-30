package com.example.demo4.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Binds 'app.email.*' from application properties.
 * Immutable record ensures config is read-only after startup.
 */
@ConfigurationProperties(prefix = "app.email")
public record AppEmailProperties(
        @DefaultValue("false") boolean enabled,
        String from,
        String subjectPrefix) {
}