package com.example.jobportal.service;

import com.example.jobportal.config.JwtAuthenticationFilter;
import com.example.jobportal.dto.request.LoginRequest;
import com.example.jobportal.dto.request.RegisterRequest;
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

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Override
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        User existingUser = (User) userRepository.findByEmail(request.getEmail());
        if (existingUser != null) {
            throw new RuntimeException("Email đã tồn tại");
        }

        User user = User.builder().email(request.getEmail()).
                passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .gender(request.getGender())
                .build();
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role ID"));
        user.setRole(role);
        userRepository.save(user);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        saveRefreshToken(user, refreshToken);
        addTokenCookie(response, "access_token", accessToken, Duration.ofMillis(accessTokenExpiration));
        addTokenCookie(response, "refresh_token", refreshToken, Duration.ofMillis(refreshTokenExpiration));
        return createAuthResponse(accessToken, refreshToken, user);
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = (User) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenRepository.deleteByUserId(user.getId());
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
                .maxAge(0)                // Set Max-Age = 0 để xóa
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
    private AuthResponse createAuthResponse(String accessToken, String refreshToken, User user) {
        UserBaseResponse userResponse = new UserBaseResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFullName(user.getFullName());
        userResponse.setRole(user.getRole());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setTokenType("Bearer");
        authResponse.setUser(userResponse);

        return authResponse;
    }

}
