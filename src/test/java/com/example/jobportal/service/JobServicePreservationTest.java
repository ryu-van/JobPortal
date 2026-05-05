package com.example.jobportal.service;

import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.dto.response.JobDetailResponse;
import com.example.jobportal.model.entity.Company;
import com.example.jobportal.model.entity.Job;
import com.example.jobportal.model.entity.SavedJob;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.repository.ApplicationRepository;
import com.example.jobportal.repository.CompanyRepository;
import com.example.jobportal.repository.JobCategoryRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.SavedJobRepository;
import com.example.jobportal.repository.SkillRepository;
import com.example.jobportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Preservation-checking tests — Task 12 (Backend)
 *
 * These tests verify Property 5: For any input where the bug condition does NOT hold,
 * the fixed code produces the same behavior as the original code.
 *
 * Task 12.1 — getJobDetail with userId = null returns same result before and after fix (unauthenticated preservation)
 * Task 12.4 — Property-based test: for any (jobId, userId) pair where the user has NOT saved the job,
 *             getJobDetail returns isSaved = false and savedJobId = null
 *
 * Validates: Requirements 3.7 (Preservation)
 *
 * **Validates: Requirements 3.7**
 */
@ExtendWith(MockitoExtension.class)
class JobServicePreservationTest {

    @InjectMocks
    private JobServiceImpl jobService;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private JobCategoryRepository jobCategoryRepository;

    @Mock
    private SavedJobRepository savedJobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SkillRepository skillRepository;

    private Company company;
    private Job job;
    private User user;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(1L)
                .name("Test Company")
                .email("company@test.com")
                .build();

        job = Job.builder()
                .id(10L)
                .title("Software Engineer")
                .description("A great job")
                .company(company)
                .build();

        user = User.builder()
                .id(5L)
                .fullName("Test User")
                .email("user@test.com")
                .code("USR001")
                .passwordHash("hash")
                .build();
    }

    // =========================================================================
    // Task 12.1 — Unauthenticated preservation: getJobDetail with userId = null
    // =========================================================================

    /**
     * 12.1 — getJobDetail with userId = null returns same result before and after fix.
     *
     * Preservation: Unauthenticated requests must return applied = false, isSaved = false, savedJobId = null.
     * This behavior must be identical before and after the fix.
     *
     * Validates: Requirements 3.7
     *
     * This test PASSES on fixed code.
     */
    @Test
    @DisplayName("12.1 getJobDetail with userId=null returns isSaved=false and savedJobId=null [MUST PASS on fixed code]")
    void getJobDetail_unauthenticated_preservesOriginalBehavior() {
        when(jobRepository.findWithDetailsById(10L)).thenReturn(Optional.of(job));

        JobDetailResponse response = jobService.getJobDetail(10L, null);

        assertNotNull(response, "Response should not be null");
        assertFalse(Boolean.TRUE.equals(response.getApplied()),
                "applied must be false for unauthenticated users");
        assertFalse(Boolean.TRUE.equals(response.getIsSaved()),
                "isSaved must be false for unauthenticated users");
        assertNull(response.getSavedJobId(),
                "savedJobId must be null for unauthenticated users");
    }

    /**
     * 12.1b — getJobDetail with userId = null for multiple jobs returns consistent results.
     *
     * Preservation: All unauthenticated requests must return the same behavior.
     *
     * Validates: Requirements 3.7
     *
     * This test PASSES on fixed code.
     */
    @Test
    @DisplayName("12.1b getJobDetail with userId=null for multiple jobs returns consistent results [MUST PASS on fixed code]")
    void getJobDetail_unauthenticated_multipleJobs_preservesOriginalBehavior() {
        Job job2 = Job.builder()
                .id(20L)
                .title("Data Scientist")
                .description("Another great job")
                .company(company)
                .build();

        when(jobRepository.findWithDetailsById(10L)).thenReturn(Optional.of(job));
        when(jobRepository.findWithDetailsById(20L)).thenReturn(Optional.of(job2));

        JobDetailResponse response1 = jobService.getJobDetail(10L, null);
        JobDetailResponse response2 = jobService.getJobDetail(20L, null);

        // Both responses must have the same behavior for unauthenticated users
        assertFalse(Boolean.TRUE.equals(response1.getIsSaved()));
        assertNull(response1.getSavedJobId());
        assertFalse(Boolean.TRUE.equals(response2.getIsSaved()));
        assertNull(response2.getSavedJobId());
    }

    // =========================================================================
    // Task 12.4 — Property-based test: unsaved jobs always return isSaved=false and savedJobId=null
    // =========================================================================

    /**
     * 12.4 — Property-based test: for any (jobId, userId) pair where the user has NOT saved the job,
     * getJobDetail returns isSaved = false and savedJobId = null.
     *
     * Property 5: Preservation — non-buggy inputs unchanged.
     * For unsaved jobs, the behavior must be identical before and after the fix.
     *
     * Validates: Requirements 3.7
     *
     * This test PASSES on fixed code.
     */
    @ParameterizedTest
    @MethodSource("unsavedJobPairs")
    @DisplayName("12.4 getJobDetail for unsaved job always returns isSaved=false and savedJobId=null [MUST PASS on fixed code]")
    void getJobDetail_forUnsavedJob_alwaysReturnsIsSavedFalse(Long jobId, Long userId) {
        Job testJob = Job.builder()
                .id(jobId)
                .title("Test Job " + jobId)
                .description("Description")
                .company(company)
                .build();

        when(jobRepository.findWithDetailsById(jobId)).thenReturn(Optional.of(testJob));
        when(applicationRepository.findByUserIdAndJobId(userId, jobId)).thenReturn(Optional.empty());
        when(savedJobRepository.findByUser_IdAndJob_Id(userId, jobId)).thenReturn(Optional.empty());

        JobDetailResponse response = jobService.getJobDetail(jobId, userId);

        assertNotNull(response, "Response should not be null");
        assertFalse(Boolean.TRUE.equals(response.getIsSaved()),
                "isSaved must be false when the user has not saved the job");
        assertNull(response.getSavedJobId(),
                "savedJobId must be null when the user has not saved the job");
    }

    /**
     * Test data for 12.4 — multiple (jobId, userId) pairs where the user has NOT saved the job.
     */
    static Stream<Arguments> unsavedJobPairs() {
        return Stream.of(
                Arguments.of(1L, 1L),
                Arguments.of(2L, 3L),
                Arguments.of(100L, 50L),
                Arguments.of(5L, 10L),
                Arguments.of(999L, 1L),
                Arguments.of(42L, 42L),
                Arguments.of(7L, 8L),
                Arguments.of(15L, 20L)
        );
    }

    // =========================================================================
    // Additional preservation checks
    // =========================================================================

    /**
     * 12.4b — getJobDetail for unsaved job with applied=true preserves applied state.
     *
     * Preservation: The fix must not affect the applied field for unsaved jobs.
     *
     * Validates: Requirements 3.7
     *
     * This test PASSES on fixed code.
     */
    @Test
    @DisplayName("12.4b getJobDetail for unsaved job with applied=true preserves applied state [MUST PASS on fixed code]")
    void getJobDetail_forUnsavedJobWithApplication_preservesAppliedState() {
        when(jobRepository.findWithDetailsById(10L)).thenReturn(Optional.of(job));
        com.example.jobportal.model.entity.Application application =
                new com.example.jobportal.model.entity.Application();
        when(applicationRepository.findByUserIdAndJobId(5L, 10L))
                .thenReturn(Optional.of(application));
        when(savedJobRepository.findByUser_IdAndJob_Id(5L, 10L)).thenReturn(Optional.empty());

        JobDetailResponse response = jobService.getJobDetail(10L, 5L);

        assertNotNull(response, "Response should not be null");
        assertTrue(Boolean.TRUE.equals(response.getApplied()),
                "applied must be true when the user has applied");
        assertFalse(Boolean.TRUE.equals(response.getIsSaved()),
                "isSaved must be false when the user has not saved the job");
        assertNull(response.getSavedJobId(),
                "savedJobId must be null when the user has not saved the job");
    }

    /**
     * 12.4c — getJobDetail for unsaved job returns job details correctly.
     *
     * Preservation: The fix must not affect the job detail fields.
     *
     * Validates: Requirements 3.7
     *
     * This test PASSES on fixed code.
     */
    @Test
    @DisplayName("12.4c getJobDetail for unsaved job returns job details correctly [MUST PASS on fixed code]")
    void getJobDetail_forUnsavedJob_returnsJobDetailsCorrectly() {
        when(jobRepository.findWithDetailsById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByUserIdAndJobId(5L, 10L)).thenReturn(Optional.empty());
        when(savedJobRepository.findByUser_IdAndJob_Id(5L, 10L)).thenReturn(Optional.empty());

        JobDetailResponse response = jobService.getJobDetail(10L, 5L);

        assertNotNull(response, "Response should not be null");
        assertEquals(10L, response.getId(), "Job ID must match");
        assertEquals("Software Engineer", response.getTitle(), "Job title must match");
        assertEquals("A great job", response.getDescription(), "Job description must match");
    }
}
