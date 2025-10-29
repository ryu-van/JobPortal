package com.example.jobportal.service;

import com.example.jobportal.security.JwtAuthenticationFilter;
import com.example.jobportal.dto.request.LoginRequest;
import com.example.jobportal.dto.request.RegisterUserRequest;
import com.example.jobportal.dto.response.AuthResponse;
import com.example.jobportal.dto.response.UserBaseResponse;
import com.example.jobportal.model.entity.RefreshToken;
import com.example.jobportal.model.entity.Role;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.repository.RefreshTokenRepository;
import com.example.jobportal.repository.RoleRepository;
import com.example.jobportal.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService{
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    @Value("${spring.jwt.access-expiration}")
    private Long accessTokenExpiration;

    @Value("${spring.jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    @Override
    @Transactional
    public AuthResponse register(RegisterUserRequest request, HttpServletResponse response) {
        log.info("📝 Starting registration for email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
        }
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role ID"));
        String token = UUID.randomUUID().toString();
        String code = String.format("USER-%s", UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        User user = User.builder().email(request.getEmail()).
                passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .code(code)
                .gender(request.getGender())
                .role(role)
                .isEmailVerified(false)
                .verificationToken(token)
                .build();
        userRepository.save(user);
        log.info("User created successfully with ID: {}", user.getId());
        emailService.sendVerificationEmail(user, user.getVerificationToken());
        log.info("Verification email triggered (async) for: {}", user.getEmail());
        log.info("Registration completed in ~200-300ms");
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        saveRefreshToken(user, refreshToken);
        addTokenCookie(response, "access_token", accessToken, Duration.ofMillis(accessTokenExpiration));
        addTokenCookie(response, "refresh_token", refreshToken, Duration.ofMillis(refreshTokenExpiration));
        return createAuthResponse(accessToken, refreshToken, user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = (User) authentication.getPrincipal();
        user.setLastLoginAt(LocalDateTime.now());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenRepository.deleteByUserId(user.getId());
        userRepository.save(user);
        saveRefreshToken(user, refreshToken);
        addTokenCookie(response, "access_token", accessToken, Duration.ofMillis(accessTokenExpiration));
        addTokenCookie(response, "refresh_token", refreshToken, Duration.ofMillis(refreshTokenExpiration));
        return createAuthResponse(accessToken, refreshToken, user);

    }

    @Override
    public AuthResponse refreshToken(String refreshToken, HttpServletResponse response) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken).orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new IllegalArgumentException("Refresh token expired");
        }
        User user = storedToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        refreshTokenRepository.delete(storedToken);
        saveRefreshToken(user, newRefreshToken);
        addTokenCookie(response, "access_token", newAccessToken, Duration.ofMillis(accessTokenExpiration));
        addTokenCookie(response, "refresh_token", newRefreshToken, Duration.ofMillis(refreshTokenExpiration));
        return createAuthResponse(newAccessToken, newRefreshToken, user);

    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);

    }

    @Override
    public AuthResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return createAuthResponse(null, null, user);
    }

    @Override
    public String verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ!"));

        if (user.getIsEmailVerified()) {
            throw new RuntimeException("Email đã được xác nhận trước đó!");
        }

        if (user.isTokenExpired()) {
            throw new RuntimeException("Token đã hết hạn! Vui lòng yêu cầu gửi lại email xác nhận.");
        }

        user.setIsEmailVerified(true);
        userRepository.save(user);

        return "Email đã được xác nhận thành công! Bạn có thể đăng nhập ngay.";
    }

    @Override
    public String resendVerificationEmail(String email) {
        User user = (User) userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("Email không tồn tại!");
        }

        if (user.getIsEmailVerified()) {
            throw new RuntimeException("Email đã được xác nhận!");
        }

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        userRepository.save(user);

        emailService.sendVerificationEmail(user, token);

        return "Email xác nhận đã được gửi lại!";
    }


    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000));
        refreshTokenRepository.save(refreshToken);
    }
    private void addTokenCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
        JwtAuthenticationFilter.addTokenCookie(response, name, value, maxAge);
    }
    public static void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
    private AuthResponse createAuthResponse(String accessToken, String refreshToken, User user) {
        UserBaseResponse userResponse = new UserBaseResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFullName(user.getFullName());
        userResponse.setRoleName(user.getRole().getName());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setTokenType("Bearer");
        authResponse.setUser(userResponse);

        return authResponse;
    }

}
