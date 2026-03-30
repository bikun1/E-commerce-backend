package com.example.demo4.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// Consolidate all @ConfigurationProperties registrations in one place,
// eliminating the need for a separate AppConfig class.
@Configuration
@EnableConfigurationProperties({
        AppEmailProperties.class,
        CloudinaryProperties.class
})
public class AppConfig {
}