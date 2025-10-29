package com.example.jobportal.controller;

import com.example.jobportal.dto.request.LoginRequest;
import com.example.jobportal.dto.request.RegisterUserRequest;
import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.AuthResponse;
import com.example.jobportal.service.AuthService;
import com.example.jobportal.service.AuthServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("${spring.base-url}/auth")
public class AuthController extends BaseController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterUserRequest registerRequest,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.register(registerRequest, response);
        return created("Đăng ký thành công", authResponse);
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(loginRequest, response);
        return ok("Đăng nhập thành công", authResponse);
    }
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = extractRefreshTokenFromCookies(request);

        if (refreshToken == null) {
            return unauthorized("Refresh token không tồn tại");
        }

        AuthResponse authResponse = authService.refreshToken(refreshToken, response);
        return ok("Làm mới token thành công", authResponse);
    }
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = extractRefreshTokenFromCookies(request);

        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        AuthServiceImpl.deleteCookie(response, "access_token");
        AuthServiceImpl.deleteCookie(response, "refresh_token");

        return ok("Đăng xuất thành công");
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser() {
        AuthResponse authResponse = authService.getCurrentUser();
        return ok(authResponse);
    }
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        String message = authService.verifyEmail(token);
        return ok(message);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            return badRequest("Email không được để trống");
        }

        String message = authService.resendVerificationEmail(email);
        return ok(message);
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
       if (request.getCookies() == null) {
           return null;
       }
       return Arrays.stream(request.getCookies())
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

}
