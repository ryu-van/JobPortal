package com.example.jobportal.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jobportal.dto.request.LoginRequest;
import com.example.jobportal.dto.request.RegisterUserRequest;
import com.example.jobportal.dto.response.AuthResponse;
import com.example.jobportal.dto.response.UserBaseResponse;
import com.example.jobportal.exception.RoleException;
import com.example.jobportal.exception.UserException;
import com.example.jobportal.model.entity.Company;
import com.example.jobportal.model.entity.CompanyInvitation;
import com.example.jobportal.model.entity.RefreshToken;
import com.example.jobportal.model.entity.Role;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.repository.RefreshTokenRepository;
import com.example.jobportal.repository.RoleRepository;
import com.example.jobportal.repository.UserRepository;
import com.example.jobportal.security.CustomUserDetails;
import com.example.jobportal.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private CompanyService companyService;

    @Value("${spring.jwt.access-expiration}")
    private Long accessTokenExpiration;

    @Value("${spring.jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    @Override
    @Transactional
    public AuthResponse register(RegisterUserRequest request, HttpServletResponse response) {
        log.info("📝 Starting registration for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw UserException.illegal("Email đã tồn tại, vui lòng đăng nhập hoặc xác thực tài khoản.");
        }

        Company company = null;
        Role role;

        if(request.getRoleId() == null && (request.getCodeInvitation() == null || request.getCodeInvitation().isBlank())) {
            throw RoleException.badRequest("Role ID không hợp lệ");
        }

        if (request.getCodeInvitation() != null && !request.getCodeInvitation().isBlank()) {
            log.info("🎫 Processing invitation code: {}", request.getCodeInvitation());

            CompanyInvitation invitation = companyService.findValidInvitation(request.getCodeInvitation())
                    .orElseThrow(() -> new IllegalArgumentException("Mã mời không hợp lệ, đã hết hạn hoặc đã được sử dụng hết"));

            if (invitation.getEmail() != null && !invitation.getEmail().equalsIgnoreCase(request.getEmail())) {
                throw new IllegalArgumentException("Email không khớp với mã mời. Mã mời này dành cho: " + invitation.getEmail());
            }

            company = invitation.getCompany();
            role = (Role) roleRepository.findByName(invitation.getRole())
                    .orElseThrow(() -> RoleException.notFound("Vai trò trong mã mời không hợp lệ: " + invitation.getRole()));

            log.info("✅ Valid invitation found for company: {} (role: {})", company.getName(), invitation.getRole());
        } else {
            role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> RoleException.notFound("Invalid role ID: " + request.getRoleId()));

            log.info("✅ Standard registration with role: {}", role.getName());
        }

        String userCode = generateUserCode();
        String otpToken = generateOTP();

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .code(userCode)
                .role(role)
                .isActive(true)
                .isEmailVerified(false)
                .verificationToken(otpToken)
                .build();

        user.setVerificationToken(otpToken);

        userRepository.save(user);
        log.info("✅ User created successfully with ID: {} for company: {}",
                user.getId(), company != null ? company.getName() : "N/A");

        if (request.getCodeInvitation() != null && !request.getCodeInvitation().isBlank()) {
            try {

                CompanyInvitation usedInvitation = companyService.useInvitation(request.getCodeInvitation());
                log.info("🔁 Invitation {} used successfully ({}/{})",
                        usedInvitation.getCode(),
                        usedInvitation.getUsedCount(),
                        usedInvitation.getMaxUses());
            } catch (Exception e) {
                log.error("❌ Failed to mark invitation as used: {}", e.getMessage());
            }
        }

        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), otpToken);
            log.info("📨 Verification email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send verification email: {}", e.getMessage());
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        saveRefreshToken(user, refreshToken);

        addTokenCookie(response, "access_token", accessToken, Duration.ofMillis(accessTokenExpiration));
        addTokenCookie(response, "refresh_token", refreshToken, Duration.ofMillis(refreshTokenExpiration));

        log.info("🎉 Registration completed successfully for {} (Company: {})",
                user.getEmail(),
                company != null ? company.getName() : "N/A");

        return createAuthResponse(accessToken, refreshToken, user);
    }


    private String generateUserCode() {
        String code;
        do {
            code = String.format("USER-%s", UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } while (userRepository.existsByCode(code));
        return code;
    }

  
    private String generateOTP() {
        return String.format("%06d", new SecureRandom().nextInt(999999));
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
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

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
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw  UserException.forbidden("Chưa đăng nhập");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        return createAuthResponse(null, null, user);
    }

    @Override
    public String verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> UserException.notFound("Token không hợp lệ hoặc đã hết hạn."));

        if (user.getIsEmailVerified()) {
            throw UserException.illegal("Email đã được xác nhận trước đó!");
        }

        if (user.isTokenExpired()) {
            throw UserException.badRequest("Token đã hết hạn! Vui lòng yêu cầu gửi lại email xác nhận.");
        }

        if (!token.equals(user.getVerificationToken())) {
            throw UserException.badRequest("Token không khớp!");
        }

        user.setIsEmailVerified(true);
        userRepository.save(user);

        return "Email đã được xác nhận thành công! Bạn có thể đăng nhập ngay.";
    }

    @Override
    public String resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> UserException.notFound("Email không tồn tại!"));

        if (Boolean.TRUE.equals(user.getIsEmailVerified())) {
            throw UserException.illegal("Email đã được xác nhận, không cần xác minh lại!");
        }

        if (user.getVerificationToken() != null && !user.isTokenExpired()) {
            throw UserException.badRequest("Mã xác minh cũ vẫn còn hiệu lực. Vui lòng kiểm tra hộp thư!");
        }

        String newToken = String.format("%06d", new java.util.Random().nextInt(999999));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        user.setVerificationToken(newToken);
        user.setTokenExpiryDate(expiry);
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), newToken);

        return "Mã xác nhận mới đã được gửi đến email của bạn!";
    }




    private void saveRefreshToken(User user, String token) {
        if (refreshTokenExpiration==null){
            throw new IllegalStateException("refreshTokenExpiration is null!");
        }
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
        String displayName = null;
        Long rid = user.getRole().getId();
        try {
            displayName = com.example.jobportal.model.enums.Role.fromId(rid.intValue()).getDisplayName();
        } catch (IllegalArgumentException e) {
            displayName = user.getRole().getName();
        }
        userResponse.setRoleName(displayName != null ? displayName : user.getRole().getName());
        userResponse.setIsEmailVerified(user.getIsEmailVerified());
        userResponse.setPhoneNumber(user.getPhoneNumber());
        userResponse.setRoleId(user.getRole().getId());
        userResponse.setGender(user.getGender());
        userResponse.setCode(user.getCode());
        userResponse.setIsActive(user.getIsActive());
        userResponse.setTokenExpiryDate(user.getTokenExpiryDate());
        userResponse.setAvatarUrl(user.getAvatarUrl());
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setTokenType("Bearer");
        authResponse.setUser(userResponse);

        return authResponse;
    }

}
