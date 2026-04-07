package com.example.ecommerce.backend.service;

import com.example.ecommerce.backend.dto.request.LoginRequest;
import com.example.ecommerce.backend.dto.request.SignupRequest;
import com.example.ecommerce.backend.dto.request.ChangePasswordRequest;
import com.example.ecommerce.backend.dto.response.JwtResponse;
import com.example.ecommerce.backend.dto.response.MessageResponse;
import com.example.ecommerce.backend.dto.response.UserDTO;

public interface AuthService {

    JwtResponse login(LoginRequest loginRequest);

    UserDTO register(SignupRequest signUpRequest);

    JwtResponse refreshToken(String token);

    MessageResponse logout(String token);

    MessageResponse changePassword(ChangePasswordRequest request);
}
