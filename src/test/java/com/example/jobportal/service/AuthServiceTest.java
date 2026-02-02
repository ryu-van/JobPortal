package com.example.jobportal.service;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.jobportal.dto.request.RegisterUserRequest;
import com.example.jobportal.exception.RoleException;
import com.example.jobportal.exception.UserException;
import com.example.jobportal.model.entity.Company;
import com.example.jobportal.model.entity.CompanyInvitation;
import com.example.jobportal.model.entity.Role;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.repository.RefreshTokenRepository;
import com.example.jobportal.repository.RoleRepository;
import com.example.jobportal.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private CompanyService companyService;

    @Mock
    private HttpServletResponse response;

    // ========= HELPER =========
    private void setJwtExpiration() {
        try {
            Field accessField = AuthServiceImpl.class.getDeclaredField("accessTokenExpiration");
            accessField.setAccessible(true);
            accessField.set(authServiceImpl, 86400000L);

            Field refreshField = AuthServiceImpl.class.getDeclaredField("refreshTokenExpiration");
            refreshField.setAccessible(true);
            refreshField.set(authServiceImpl, 604800000L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void injectCompanyService() {
        try {
            Field f = AuthServiceImpl.class.getDeclaredField("companyService");
            f.setAccessible(true);
            f.set(authServiceImpl, companyService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ========= TESTS =========

    @Test
    void register_shouldThrowException_whenUserAlreadyExists() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("test@gmail.com");

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(true);

        UserException ex = assertThrows(
                UserException.class,
                () -> authServiceImpl.register(request, response)
        );

        assertTrue(ex.getMessage().contains("Email đã tồn tại"));

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
    }

    @Test
    void register_shouldProceed_whenUserDoesNotExist() {
        setJwtExpiration();

        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");
        request.setFullName("Test User");
        request.setRoleId(1L);

        Role role = new Role();
        role.setId(1L);
        role.setName("CANDIDATE");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@gmail.com");

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        authServiceImpl.register(request, response);

        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any());
        verify(emailService).sendVerificationEmail(eq("test@gmail.com"), anyString(), anyString());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void register_withoutRoleId_shouldThrowException() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("test@gmail.com");

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);

        RoleException ex = assertThrows(
                RoleException.class,
                () -> authServiceImpl.register(request, response)
        );

        assertTrue(ex.getMessage().contains("Role ID không hợp lệ"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withInvalidRoleId_shouldThrowException() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("test@gmail.com");
        request.setRoleId(999L);

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        RoleException ex = assertThrows(
                RoleException.class,
                () -> authServiceImpl.register(request, response)
        );

        assertTrue(ex.getMessage().contains("Invalid role ID"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withValidCodeInvitation_shouldProceed() {
        setJwtExpiration();
        injectCompanyService();

        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("invited@example.com");
        request.setPassword("password123");
        request.setFullName("Invited User");
        request.setCodeInvitation("VALID-CODE");

        Company company = new Company();
        company.setId(1L);

        CompanyInvitation invitation = new CompanyInvitation();
        invitation.setCode("VALID-CODE");
        invitation.setEmail("invited@example.com");
        invitation.setRole("COMPANY_ADMIN");
        invitation.setCompany(company);

        Role role = new Role();
        role.setId(4L);
        role.setName("COMPANY_ADMIN");

        when(userRepository.existsByEmail("invited@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(companyService.findValidInvitation("VALID-CODE")).thenReturn(Optional.of(invitation));
        when(roleRepository.findByName("COMPANY_ADMIN")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(new User());

        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        authServiceImpl.register(request, response);

        verify(companyService).useInvitation("VALID-CODE");
        verify(userRepository).save(any());
        verify(emailService).sendVerificationEmail(eq("invited@example.com"), anyString(), anyString());
    }

    @Test
    void register_withInvalidCodeInvitation_shouldThrowException() {
        injectCompanyService();
        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("test@gmail.com");
        request.setCodeInvitation("INVALID");

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);
        when(companyService.findValidInvitation("INVALID")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authServiceImpl.register(request, response)
        );

        assertTrue(ex.getMessage().contains("Mã mời không hợp lệ"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withEmailNotMatchingInvitation_shouldThrowException() {
        injectCompanyService();
        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("wrong@example.com");
        request.setCodeInvitation("VALID");

        CompanyInvitation invitation = new CompanyInvitation();
        invitation.setEmail("correct@example.com");

        when(userRepository.existsByEmail("wrong@example.com")).thenReturn(false);
        when(companyService.findValidInvitation("VALID")).thenReturn(Optional.of(invitation));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authServiceImpl.register(request, response)
        );

        assertTrue(ex.getMessage().contains("Email không khớp"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_successButEmailFail_shouldStillRegister() {
        setJwtExpiration();

        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");
        request.setRoleId(1L);

        Role role = new Role();
        role.setId(1L);

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.save(any())).thenReturn(new User());
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh");

        doThrow(new RuntimeException("Email error"))
                .when(emailService)
                .sendVerificationEmail(anyString(), anyString(), anyString());

        authServiceImpl.register(request, response);

        verify(userRepository).save(any());
        verify(refreshTokenRepository).save(any());
    }
}
