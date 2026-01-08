package com.example.demo4.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record SignupRequest(
        @NotBlank(message = "Username is required") @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username,

        @NotBlank(message = "Email is required") @Email(message = "Email should be valid") @Size(max = 100, message = "Email must be less than 100 characters") String email,

        @NotBlank(message = "Password is required") @Size(min = 6, max = 100, message = "Password must be at least 6 characters") String password,

        @Size(max = 100, message = "Full name must be less than 100 characters") String fullName,

        Set<String> roles) {
}