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
 * Exploratory tests — run on UNFIXED code to confirm root causes.
 *
 * These tests assert the CORRECT (expected) behavior.
 * They are EXPECTED TO FAIL on the current unfixed code, confirming the bugs exist.
 *
 * Task 1.1 — addJobToListSavedJob returns non-null savedJobId
 * Task 1.2 — getSavedJobs returns items with non-null savedJobId
 * Task 1.3 — getJobDetail with authenticated user populates isSaved and savedJobId
 */
@ExtendWith(MockitoExtension.class)
class JobServiceExploratoryTest {

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
    // Task 1.1 — addJobToListSavedJob returns non-null savedJobId
    // =========================================================================

    /**
     * BUG: addJobToListSavedJob calls JobBaseResponse.fromEntity(existingJob) which
     * never sets savedJobId. The returned response always has savedJobId == null.
     *
     * EXPECTED (correct): savedJobId should equal the saved SavedJob record's id (42).
     *
     * This test FAILS on unfixed code, confirming Bug Condition B (root cause 3).
     */
    @Test
    @DisplayName("1.1 addJobToListSavedJob should return non-null savedJobId [EXPECTED TO FAIL on unfixed code]")
    void addJobToListSavedJob_shouldReturnNonNullSavedJobId() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(savedJobRepository.save(any(SavedJob.class))).thenReturn(savedJob);

        JobBaseResponse response = jobService.addJobToListSavedJob(10L, 5L);

        assertNotNull(response, "Response should not be null");
        // BUG: This assertion FAILS on unfixed code because fromEntity() never sets savedJobId
        assertNotNull(response.getSavedJobId(),
                "savedJobId must not be null — backend must return the SavedJob record id");
        assertEquals(42L, response.getSavedJobId(),
                "savedJobId must equal the persisted SavedJob id");
    }

    /**
     * Additional check: isSaved should be true after saving.
     * This test FAILS on unfixed code.
     */
    @Test
    @DisplayName("1.1b addJobToListSavedJob should return isSaved=true [EXPECTED TO FAIL on unfixed code]")
    void addJobToListSavedJob_shouldReturnIsSavedTrue() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(savedJobRepository.save(any(SavedJob.class))).thenReturn(savedJob);

        JobBaseResponse response = jobService.addJobToListSavedJob(10L, 5L);

        assertNotNull(response, "Response should not be null");
        // BUG: This assertion FAILS on unfixed code because fromEntity() never sets isSaved
        assertTrue(Boolean.TRUE.equals(response.getIsSaved()),
                "isSaved must be true after saving a job");
    }

    // =========================================================================
    // Task 1.2 — getSavedJobs returns items with non-null savedJobId
    // =========================================================================

    /**
     * BUG: getSavedJobs maps via JobBaseResponse.fromEntity(saved.getJob()) which
     * never sets savedJobId. Every item in the list has savedJobId == null.
     *
     * EXPECTED (correct): every item should have savedJobId equal to the SavedJob record id.
     *
     * This test FAILS on unfixed code, confirming Bug Condition B (root cause 4).
     */
    @Test
    @DisplayName("1.2 getSavedJobs should return items with non-null savedJobId [EXPECTED TO FAIL on unfixed code]")
    void getSavedJobs_shouldReturnItemsWithNonNullSavedJobId() {
        when(savedJobRepository.findByUserId(5L)).thenReturn(List.of(savedJob));

        List<JobBaseResponse> items = jobService.getSavedJobs(5L);

        assertNotNull(items, "Result list should not be null");
        assertFalse(items.isEmpty(), "Result list should not be empty");

        for (JobBaseResponse item : items) {
            // BUG: This assertion FAILS on unfixed code because fromEntity() never sets savedJobId
            assertNotNull(item.getSavedJobId(),
                    "Every saved job item must have a non-null savedJobId");
        }
    }

    /**
     * Verify the savedJobId value matches the SavedJob record id.
     * This test FAILS on unfixed code.
     */
    @Test
    @DisplayName("1.2b getSavedJobs savedJobId should equal SavedJob record id [EXPECTED TO FAIL on unfixed code]")
    void getSavedJobs_savedJobIdShouldMatchRecordId() {
        when(savedJobRepository.findByUserId(5L)).thenReturn(List.of(savedJob));

        List<JobBaseResponse> items = jobService.getSavedJobs(5L);

        assertFalse(items.isEmpty());
        // BUG: This assertion FAILS on unfixed code
        assertEquals(42L, items.get(0).getSavedJobId(),
                "savedJobId must equal the SavedJob record id (42)");
    }

    // =========================================================================
    // Task 1.3 — getJobDetail with authenticated user populates isSaved and savedJobId
    // =========================================================================

    /**
     * BUG: getJobDetail never queries SavedJobRepository, so isSaved and savedJobId
     * are always null/false for authenticated users who have saved the job.
     *
     * EXPECTED (correct): when userId is non-null, isSaved should be non-null
     * (either true or false) and the service should have queried the saved job state.
     * Specifically, for an authenticated user, isSaved must not be null — it must be
     * explicitly set to true or false.
     *
     * This test FAILS on unfixed code because getJobDetail never sets isSaved for
     * authenticated users (it only sets applied/appliedAt), leaving isSaved as null.
     *
     * NOTE: findByUserIdAndJobId does not exist in SavedJobRepository yet (Task 2.1
     * will add it). This test confirms the bug by checking the current output is wrong.
     */
    @Test
    @DisplayName("1.3 getJobDetail with authenticated user should set isSaved (not null) [EXPECTED TO FAIL on unfixed code]")
    void getJobDetail_withAuthenticatedUser_shouldSetIsSavedField() {
        when(jobRepository.findWithDetailsById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByUserIdAndJobId(5L, 10L)).thenReturn(Optional.empty());
        // NOTE: savedJobRepository.findByUserIdAndJobId does not exist yet on unfixed code.
        // The service never calls it, so no mock is needed here.
        // The bug is that isSaved is never set for authenticated users.

        JobDetailResponse response = jobService.getJobDetail(10L, 5L);

        assertNotNull(response, "Response should not be null");
        // BUG: This assertion FAILS on unfixed code because getJobDetail never sets isSaved
        // for authenticated users — it remains null (the default from fromEntity() which
        // does not set isSaved at all).
        assertNotNull(response.getIsSaved(),
                "isSaved must not be null for authenticated users — it must be explicitly set to true or false");
    }

    /**
     * Additional check: for an authenticated user, savedJobId should be explicitly set.
     * On unfixed code, savedJobId is always null because the service never queries SavedJobRepository.
     * This test FAILS on unfixed code.
     */
    @Test
    @DisplayName("1.3b getJobDetail with authenticated user should have isSaved=false when job not saved [EXPECTED TO FAIL on unfixed code]")
    void getJobDetail_withAuthenticatedUserWhoHasNotSavedJob_shouldReturnIsSavedFalse() {
        when(jobRepository.findWithDetailsById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByUserIdAndJobId(5L, 10L)).thenReturn(Optional.empty());

        JobDetailResponse response = jobService.getJobDetail(10L, 5L);

        assertNotNull(response, "Response should not be null");
        // BUG: On unfixed code, isSaved is null (not false) for authenticated users.
        // The fix must explicitly set isSaved=false when the user has not saved the job.
        assertFalse(Boolean.TRUE.equals(response.getIsSaved()),
                "isSaved must be false (not null) when the authenticated user has not saved the job");
        // The real assertion that confirms the bug: isSaved should be explicitly false, not null
        assertNotNull(response.getIsSaved(),
                "isSaved must be explicitly set to false (not left as null) for authenticated users");
    }

    /**
     * Verify that getJobDetail with userId=null returns isSaved=false and savedJobId=null.
     * This is the PRESERVATION case — it should pass on both unfixed and fixed code.
     */
    @Test
    @DisplayName("1.3c getJobDetail with unauthenticated user should return isSaved=false and savedJobId=null [should PASS on both unfixed and fixed code]")
    void getJobDetail_withUnauthenticatedUser_shouldReturnIsSavedFalseAndNullSavedJobId() {
        when(jobRepository.findWithDetailsById(10L)).thenReturn(Optional.of(job));

        JobDetailResponse response = jobService.getJobDetail(10L, null);

        assertNotNull(response, "Response should not be null");
        assertFalse(Boolean.TRUE.equals(response.getIsSaved()),
                "isSaved must not be true for unauthenticated users");
        assertNull(response.getSavedJobId(),
                "savedJobId must be null for unauthenticated users");
    }
}
