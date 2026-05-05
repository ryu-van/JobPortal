package com.example.jobportal.service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.jobportal.config.CookieProperties;
import com.example.jobportal.dto.request.RegisterUserRequest;
import com.example.jobportal.model.entity.Company;
import com.example.jobportal.model.entity.CompanyInvitation;
import com.example.jobportal.model.entity.Role;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.repository.RefreshTokenRepository;
import com.example.jobportal.repository.RoleRepository;
import com.example.jobportal.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

/**
 * AuthServiceImplTest
 *
 * Contains two groups of tests:
 *
 * Task 1 — Bug Condition Exploration Tests (Property 1):
 *   These MUST FAIL on unfixed code — failure confirms the bugs exist.
 *   They encode the EXPECTED (correct) behavior and will pass once the fix is applied.
 *   Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5
 *
 * Task 2 — Preservation Tests (Property 3):
 *   These MUST PASS on unfixed code — they confirm baseline behavior to preserve.
 *   They encode the ¬C(X) (non-bug-condition) paths that must remain unchanged after the fix.
 *   Validates: Requirements 3.3, 3.4, 3.5, 3.6
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

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
    private CookieProperties cookieProperties;

    @Mock
    private HttpServletResponse response;

    // ========= HELPERS =========

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

    // ========= TASK 1: BUG CONDITION EXPLORATION TEST =========

    /**
     * Bug Condition Exploration Test — Backend Bug 1
     *
     * isBugCondition_Backend: codeInvitation non-blank AND invitation valid AND user.company IS NULL after save
     *
     * This test asserts the EXPECTED (correct) behavior:
     *   - When register() is called with a valid invitation code linked to a company,
     *     the User passed to userRepository.save() must have a non-null company
     *     whose ID equals the invitation's company ID.
     *
     * EXPECTED OUTCOME ON UNFIXED CODE: FAIL
     *   Counterexample: user.getCompany() is null after save — the bug is confirmed.
     *
     * Validates: Requirements 1.1, 1.5
     */
    @Test
    void isBugCondition_Backend_invitationRegistration_userShouldHaveCompanyAfterSave() {
        setJwtExpiration();

        // Arrange: a valid invitation linked to a company
        Company company = new Company();
        company.setId(42L);
        company.setName("Acme Corp");
        company.setEmail("acme@example.com");

        CompanyInvitation invitation = new CompanyInvitation();
        invitation.setCode("INV-ABC123");
        invitation.setEmail("hr@example.com");
        invitation.setRole("HR");
        invitation.setCompany(company);

        Role hrRole = new Role();
        hrRole.setId(3L);
        hrRole.setName("HR");

        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("hr@example.com")
                .password("password123")
                .fullName("HR User")
                .codeInvitation("INV-ABC123")
                .build();

        when(userRepository.existsByEmail("hr@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(companyService.findValidInvitation("INV-ABC123")).thenReturn(Optional.of(invitation));
        when(roleRepository.findByName("HR")).thenReturn(Optional.of(hrRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        // Act
        authServiceImpl.register(request, response);

        // Assert: the User saved to the repository must have the company set
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        // EXPECTED BEHAVIOR (will FAIL on unfixed code — confirms Bug 1 exists):
        // user.getCompany() should be non-null and equal to the invitation's company
        assertNotNull(savedUser.getCompany(),
                "BUG CONFIRMED: user.getCompany() is null after invitation-based registration. " +
                "user.setCompany(company) is missing in AuthServiceImpl.register before userRepository.save(user).");

        assertEquals(42L, savedUser.getCompany().getId(),
                "BUG CONFIRMED: user.getCompany().getId() does not match the invitation's company ID.");
    }

    // ========= TASK 2: PRESERVATION TESTS =========
    // Property 3: Preservation — Non-Invitation Registration and Invitation Validation Unchanged
    //
    // These tests MUST PASS on unfixed code — they confirm baseline behavior to preserve.
    // They encode the ¬C(X) (non-bug-condition) paths that must remain unchanged after the fix.
    //
    // Validates: Requirements 3.3, 3.4, 3.5, 3.6

    /**
     * Preservation Test 1 — Non-Invitation Registration Saves company = null
     *
     * ¬C(X) for Bug 1: codeInvitation is null → user.getCompany() must be null after save.
     *
     * Observation: on unfixed code, register() with codeInvitation = null saves user with company = null.
     * This is CORRECT behavior that must be preserved after the fix.
     *
     * EXPECTED OUTCOME: PASS (confirms baseline behavior)
     *
     * Validates: Requirements 3.3
     */
    @Test
    void preservation_nonInvitationRegistration_userCompanyShouldBeNull() {
        setJwtExpiration();

        Role candidateRole = new Role();
        candidateRole.setId(4L);
        candidateRole.setName("CANDIDATE");

        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("candidate@example.com")
                .password("password123")
                .fullName("Candidate User")
                .roleId(4L)
                .codeInvitation(null)
                .build();

        when(userRepository.existsByEmail("candidate@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findById(4L)).thenReturn(Optional.of(candidateRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        authServiceImpl.register(request, response);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        // PRESERVATION: non-invitation registration must always save company = null
        assertNull(savedUser.getCompany(),
                "PRESERVATION VIOLATED: user.getCompany() should be null for non-invitation registration.");
    }

    /**
     * Preservation Test 2 — Blank codeInvitation Saves company = null
     *
     * ¬C(X) for Bug 1: codeInvitation is blank → user.getCompany() must be null after save.
     *
     * Validates: Requirements 3.3
     */
    @Test
    void preservation_blankCodeInvitation_userCompanyShouldBeNull() {
        setJwtExpiration();

        Role hrRole = new Role();
        hrRole.setId(3L);
        hrRole.setName("HR");

        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("hr@example.com")
                .password("password123")
                .fullName("HR User")
                .roleId(3L)
                .codeInvitation("   ")
                .build();

        when(userRepository.existsByEmail("hr@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findById(3L)).thenReturn(Optional.of(hrRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        authServiceImpl.register(request, response);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        // PRESERVATION: blank codeInvitation must always save company = null
        assertNull(savedUser.getCompany(),
                "PRESERVATION VIOLATED: user.getCompany() should be null when codeInvitation is blank.");
    }

    /**
     * Preservation Property Test — For all non-invitation requests, company is always null
     *
     * Property-based test: for all RegisterUserRequest where codeInvitation is null or blank,
     * user.getCompany() is always null after save (¬C(X) for Bug 1).
     *
     * Uses parameterized testing to cover multiple role IDs and null/blank invitation codes.
     *
     * Validates: Requirements 3.3, 3.4
     */
    @ParameterizedTest(name = "roleId={0}, codeInvitation=null → company must be null")
    @ValueSource(longs = {1L, 2L, 4L})
    void preservation_property_allNonInvitationRoles_companyAlwaysNull(long roleId) {
        setJwtExpiration();

        Role role = new Role();
        role.setId(roleId);
        role.setName("ROLE_" + roleId);

        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("user" + roleId + "@example.com")
                .password("password123")
                .fullName("User " + roleId)
                .roleId(roleId)
                .codeInvitation(null)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(roleId);
            return u;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        authServiceImpl.register(request, response);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        // PRESERVATION PROPERTY: ¬C(X) → company is always null
        assertNull(savedUser.getCompany(),
                "PRESERVATION VIOLATED for roleId=" + roleId +
                ": user.getCompany() should be null when codeInvitation is null.");
    }

    /**
     * Preservation Test 3 — Expired Invitation Rejects Registration
     *
     * ¬C(X) for Bug 1 (validation path): expired invitation → registration rejected.
     * This behavior must continue to work after the fix.
     *
     * Validates: Requirements 3.4
     */
    @Test
    void preservation_expiredInvitation_registrationRejected() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("hr@example.com")
                .password("password123")
                .fullName("HR User")
                .codeInvitation("EXPIRED-CODE")
                .build();

        when(userRepository.existsByEmail("hr@example.com")).thenReturn(false);
        // findValidInvitation returns empty for expired/used/inactive invitations
        when(companyService.findValidInvitation("EXPIRED-CODE")).thenReturn(Optional.empty());

        // PRESERVATION: expired invitation must still reject registration
        assertThrows(IllegalArgumentException.class,
                () -> authServiceImpl.register(request, response),
                "PRESERVATION VIOLATED: expired invitation should still reject registration.");
    }

    /**
     * Preservation Test 4 — Email Mismatch Invitation Rejects Registration
     *
     * ¬C(X) for Bug 1 (validation path): invitation email does not match registering user's email
     * → registration rejected. This behavior must continue to work after the fix.
     *
     * Validates: Requirements 3.5
     */
    @Test
    void preservation_emailMismatchInvitation_registrationRejected() {
        Company company = new Company();
        company.setId(10L);
        company.setName("Test Corp");
        company.setEmail("test@corp.com");

        CompanyInvitation invitation = new CompanyInvitation();
        invitation.setCode("MISMATCH-CODE");
        invitation.setEmail("other@example.com"); // different email
        invitation.setRole("HR");
        invitation.setCompany(company);
        invitation.setIsActive(true);
        invitation.setMaxUses(5);
        invitation.setUsedCount(0);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));

        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("hr@example.com") // does not match invitation.email
                .password("password123")
                .fullName("HR User")
                .codeInvitation("MISMATCH-CODE")
                .build();

        when(userRepository.existsByEmail("hr@example.com")).thenReturn(false);
        when(companyService.findValidInvitation("MISMATCH-CODE")).thenReturn(Optional.of(invitation));

        // PRESERVATION: email mismatch must still reject registration
        assertThrows(IllegalArgumentException.class,
                () -> authServiceImpl.register(request, response),
                "PRESERVATION VIOLATED: email mismatch invitation should still reject registration.");
    }

    /**
     * Preservation Property Test — Invitation Validation Errors Always Reject
     *
     * Property-based test: invitation validation errors (expired, fully used, inactive, email mismatch)
     * continue to reject registration.
     *
     * Uses parameterized testing to cover multiple invalid invitation scenarios.
     *
     * Validates: Requirements 3.4, 3.5, 3.6
     */
    @ParameterizedTest(name = "invalid invitation scenario: {0}")
    @MethodSource("invalidInvitationScenarios")
    void preservation_property_invalidInvitationAlwaysRejectsRegistration(String scenario) {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("hr@example.com")
                .password("password123")
                .fullName("HR User")
                .codeInvitation("INVALID-CODE-" + scenario)
                .build();

        when(userRepository.existsByEmail("hr@example.com")).thenReturn(false);
        // All invalid invitation scenarios return empty from findValidInvitation
        when(companyService.findValidInvitation("INVALID-CODE-" + scenario)).thenReturn(Optional.empty());

        // PRESERVATION PROPERTY: all invalid invitation scenarios must reject registration
        assertThrows(IllegalArgumentException.class,
                () -> authServiceImpl.register(request, response),
                "PRESERVATION VIOLATED for scenario '" + scenario +
                "': invalid invitation should always reject registration.");
    }

    static Stream<String> invalidInvitationScenarios() {
        return Stream.of(
                "expired",
                "fully-used",
                "inactive",
                "nonexistent"
        );
    }
}
