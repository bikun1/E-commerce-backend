package com.example.demo4.service;

import com.example.demo4.dto.request.LoginRequest;
import com.example.demo4.dto.request.SignupRequest;
import com.example.demo4.dto.request.ChangePasswordRequest;
import com.example.demo4.dto.response.JwtResponse;
import com.example.demo4.dto.response.MessageResponse;
import com.example.demo4.dto.response.UserDTO;

public interface AuthService {

    JwtResponse login(LoginRequest loginRequest);

    UserDTO register(SignupRequest signUpRequest);

    JwtResponse refreshToken(String token);

    MessageResponse logout(String token);

    MessageResponse changePassword(ChangePasswordRequest request);
}
