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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Fix-checking tests — run on FIXED code to verify Properties 1–4.
 *
 * These tests assert the CORRECT (expected) behavior and MUST PASS on the fixed code.
 *
 * Task 11.1 — addJobToListSavedJob returns savedJobId != null and isSaved = true (Property 1)
 * Task 11.2 — getSavedJobs returns all items with savedJobId != null (Property 2)
 * Task 11.3 — getJobDetail with saved job returns isSaved = true and savedJobId != null (Property 3)
 * Task 11.4 — getJobDetail with unsaved job returns isSaved = false and savedJobId = null (Property 3)
 * Task 11.5 — getJobDetail with userId = null returns isSaved = false and savedJobId = null (Property 3 — unauthenticated)
 *
 * Validates: Requirements 2.1, 2.2, 2.4, 2.5
 */
@ExtendWith(MockitoExtension.class)
class JobServiceFixCheckTest {

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
    private SavedJob savedJob;

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

        savedJob = SavedJob.builder()
                .id(42L)
                .job(job)
                .user(user)
                .build();
    }

    // =========================================================================
    // Task 11.1 — Property 1: addJobToListSavedJob returns savedJobId != null and isSaved = true
    // =========================================================================

    /**
     * 11.1 — addJobToListSavedJob should return savedJobId = 42 and isSaved = true.
     *
     * Validates: Requirements 2.1, 2.2
     * Property 1: Save stores savedJobId and unsave uses it.
     *
     * This test PASSES on fixed code.
     */
    @Test
    @DisplayName("11.1 addJobToListSavedJob should return savedJobId=42 and isSaved=true [MUST PASS on fixed code]")
    void addJobToListSavedJob_shouldReturnSavedJobIdAndIsSavedTrue() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(savedJobRepository.save(any(SavedJob.class))).thenReturn(savedJob);

        JobBaseResponse response = jobService.addJobToListSavedJob(10L, 5L);

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getSavedJobId(),
                "savedJobId must not be null — fixed code must return the SavedJob record id");
        assertEquals(42L, response.getSavedJobId(),
                "savedJobId must equal the persisted SavedJob id (42)");
        assertTrue(Boolean.TRUE.equals(response.getIsSaved()),
                "isSaved must be true after saving a job");
    }

    // =========================================================================
    // Task 11.2 — Property 2: getSavedJobs returns all items with savedJobId != null
    // =========================================================================

    /**
     * 11.2 — getSavedJobs should return all items with savedJobId != null and isSaved = true.
     *
     * Validates: Requirements 2.4
     * Property 2: Saved Jobs List Includes savedJobId.
     *
     * This test PASSES on fixed code.
     */
    @Test
    @DisplayName("11.2 getSavedJobs should return items with savedJobId != null and isSaved=true [MUST PASS on fixed code]")
    void getSavedJobs_shouldReturnItemsWithNonNullSavedJobIdAndIsSavedTrue() {
        when(savedJobRepository.findByUserId(5L)).thenReturn(List.of(savedJob));

        List<JobBaseResponse> items = jobService.getSavedJobs(5L);

        assertNotNull(items, "Result list should not be null");
        assertFalse(items.isEmpty(), "Result list should not be empty");

        for (JobBaseResponse item : items) {
            assertNotNull(item.getSavedJobId(),
                    "Every saved job item must have a non-null savedJobId");
            assertTrue(Boolean.TRUE.equals(item.getIsSaved()),
                    "Every saved job item must have isSaved=true");
        }

        assertEquals(42L, items.get(0).getSavedJobId(),
                "savedJobId must equal the SavedJob record id (42)");
    }

    // =========================================================================
    // Task 11.3 — Property 3: getJobDetail with saved job returns isSaved=true and savedJobId!=null
    // =========================================================================

    /**
     * 11.3 — getJobDetail with saved job should return isSaved=true and savedJobId=42.
     *
     * Validates: Requirements 2.5
     * Property 3: Job Detail Includes isSaved and savedJobId for Authenticated Users.
     *
     * This test PASSES on fixed code.
     */
    @Test
    @DisplayName("11.3 getJobDetail with saved job should return isSaved=true and savedJobId=42 [MUST PASS on fixed code]")
    void getJobDetail_withSavedJob_shouldReturnIsSavedTrueAndNonNullSavedJobId() {
        when(jobRepository.findWithDetailsById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByUserIdAndJobId(5L, 10L)).thenReturn(Optional.empty());
        when(savedJobRepository.findByUser_IdAndJob_Id(5L, 10L)).thenReturn(Optional.of(savedJob));

        JobDetailResponse response = jobService.getJobDetail(10L, 5L);

        assertNotNull(response, "Response should not be null");
        assertTrue(Boolean.TRUE.equals(response.getIsSaved()),
                "isSaved must be true when the user has saved the job");
        assertNotNull(response.getSavedJobId(),
                "savedJobId must not be null when the user has saved the job");
        assertEquals(42L, response.getSavedJobId(),
                "savedJobId must equal the SavedJob record id (42)");
    }

    // =========================================================================
    // Task 11.4 — Property 3: getJobDetail with unsaved job returns isSaved=false and savedJobId=null
    // =========================================================================

    /**
     * 11.4 — getJobDetail with unsaved job should return isSaved=false and savedJobId=null.
     *
     * Validates: Requirements 2.5
     * Property 3: Job Detail Includes isSaved and savedJobId for Authenticated Users.
     *
     * This test PASSES on fixed code.
     */
    @Test
    @DisplayName("11.4 getJobDetail with unsaved job should return isSaved=false and savedJobId=null [MUST PASS on fixed code]")
    void getJobDetail_withUnsavedJob_shouldReturnIsSavedFalseAndNullSavedJobId() {
        when(jobRepository.findWithDetailsById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByUserIdAndJobId(5L, 10L)).thenReturn(Optional.empty());
        when(savedJobRepository.findByUser_IdAndJob_Id(5L, 10L)).thenReturn(Optional.empty());

        JobDetailResponse response = jobService.getJobDetail(10L, 5L);

        assertNotNull(response, "Response should not be null");
        assertFalse(Boolean.TRUE.equals(response.getIsSaved()),
                "isSaved must be false when the user has not saved the job");
        assertNull(response.getSavedJobId(),
                "savedJobId must be null when the user has not saved the job");
    }

    // =========================================================================
    // Task 11.5 — Property 3 (unauthenticated): getJobDetail with userId=null returns isSaved=false and savedJobId=null
    // =========================================================================

    /**
     * 11.5 — getJobDetail with userId=null should return isSaved=false and savedJobId=null.
     *
     * Validates: Requirements 3.7
     * Property 3 (unauthenticated): Unauthenticated requests must not expose saved state.
     *
     * This test PASSES on fixed code.
     */
    @Test
    @DisplayName("11.5 getJobDetail with userId=null should return isSaved=false and savedJobId=null [MUST PASS on fixed code]")
    void getJobDetail_withNullUserId_shouldReturnIsSavedFalseAndNullSavedJobId() {
        when(jobRepository.findWithDetailsById(10L)).thenReturn(Optional.of(job));

        JobDetailResponse response = jobService.getJobDetail(10L, null);

        assertNotNull(response, "Response should not be null");
        assertFalse(Boolean.TRUE.equals(response.getIsSaved()),
                "isSaved must be false for unauthenticated users");
        assertNull(response.getSavedJobId(),
                "savedJobId must be null for unauthenticated users");
    }
}
