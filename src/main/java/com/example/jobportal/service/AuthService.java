package com.example.jobportal.service;

import com.example.jobportal.dto.request.LoginRequest;
import com.example.jobportal.dto.request.RegisterUserRequest;
import com.example.jobportal.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse register(RegisterUserRequest request, HttpServletResponse response);
    AuthResponse login(LoginRequest request, HttpServletResponse response);
    AuthResponse refreshToken(String refreshToken, HttpServletResponse response);
    void logout(String refreshToken);
    AuthResponse getCurrentUser();
    String verifyEmail(String token);
    String resendVerificationEmail(String email);
}
