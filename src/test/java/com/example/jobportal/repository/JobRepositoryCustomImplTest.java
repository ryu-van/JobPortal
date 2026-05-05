package com.example.jobportal.repository;

import com.example.jobportal.dto.response.TopJobDto;
import com.example.jobportal.model.entity.*;
import com.example.jobportal.model.enums.ApplicationStatus;
import com.example.jobportal.model.enums.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JobRepositoryCustomImpl
 * Tests custom query methods for dashboard statistics
 */
@DataJpaTest
@ActiveProfiles("test")
class JobRepositoryCustomImplTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private JobRepository jobRepository;
    
    private Company testCompany;
    private User testUser;
    private LocalDateTime testStartDate;
    
    @BeforeEach
    void setUp() {
        // Create test company
        testCompany = new Company();
        testCompany.setName("Test Company");
        testCompany.setEmail("test@company.com");
        testCompany.setIsActive(true);
        testCompany.setIsVerified(true);
        testCompany = entityManager.persist(testCompany);
        
        // Create test user
        testUser = new User();
        testUser.setEmail("test@user.com");
        testUser.setPasswordHash("password");
        testUser.setFullName("Test User");
        testUser.setCode("TEST001");
        testUser.setIsActive(true);
        testUser.setCompany(testCompany);
        testUser = entityManager.persist(testUser);
        
        testStartDate = LocalDateTime.now().minusDays(7);
        
        entityManager.flush();
    }
    
    @Test
    void countPublishedJobsByCompanyId_ShouldReturnCorrectCount() {
        // Given: Create 3 published jobs and 1 draft job
        createJob(testCompany, testUser, JobStatus.PUBLISHED);
        createJob(testCompany, testUser, JobStatus.PUBLISHED);
        createJob(testCompany, testUser, JobStatus.PUBLISHED);
        createJob(testCompany, testUser, JobStatus.DRAFT);
        entityManager.flush();
        
        // When
        long count = jobRepository.countPublishedJobsByCompanyId(testCompany.getId());
        
        // Then
        assertThat(count).isEqualTo(3);
    }
    
    @Test
    void countPublishedJobsByCompanyId_ShouldExcludeInactiveJobs() {
        // Given: Create 2 published jobs
        createJob(testCompany, testUser, JobStatus.PUBLISHED);
        createJob(testCompany, testUser, JobStatus.PUBLISHED);
        entityManager.flush();
        
        // When
        long count = jobRepository.countPublishedJobsByCompanyId(testCompany.getId());
        
        // Then
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    void countPublishedJobsByCompanyIdAndPeriod_ShouldFilterByDate() {
        // Given: Create jobs with different creation dates
        Job oldJob = createJob(testCompany, testUser, JobStatus.PUBLISHED);
        oldJob.setCreatedAt(testStartDate.minusDays(10));
        entityManager.persist(oldJob);
        
        Job recentJob = createJob(testCompany, testUser, JobStatus.PUBLISHED);
        recentJob.setCreatedAt(testStartDate.plusDays(1));
        entityManager.persist(recentJob);
        
        entityManager.flush();
        
        // When
        long count = jobRepository.countPublishedJobsByCompanyIdAndPeriod(
            testCompany.getId(), testStartDate);
        
        // Then
        assertThat(count).isEqualTo(1);
    }
    
    @Test
    void countAllPublishedJobs_ShouldCountAllCompanies() {
        // Given: Create jobs for multiple companies
        Company anotherCompany = new Company();
        anotherCompany.setName("Another Company");
        anotherCompany.setEmail("another@company.com");
        anotherCompany.setIsActive(true);
        anotherCompany = entityManager.persist(anotherCompany);
        
        createJob(testCompany, testUser, JobStatus.PUBLISHED);
        createJob(testCompany, testUser, JobStatus.PUBLISHED);
        
        User anotherUser = new User();
        anotherUser.setEmail("another@user.com");
        anotherUser.setPasswordHash("password");
        anotherUser.setFullName("Another User");
        anotherUser.setCode("TEST002");
        anotherUser.setIsActive(true);
        anotherUser.setCompany(anotherCompany);
        anotherUser = entityManager.persist(anotherUser);
        
        createJob(anotherCompany, anotherUser, JobStatus.PUBLISHED);
        entityManager.flush();
        
        // When
        long count = jobRepository.countAllPublishedJobs();
        
        // Then
        assertThat(count).isEqualTo(3);
    }
    
    @Test
    void countNewJobsInPeriod_ShouldFilterByCreationDate() {
        // Given: Create jobs with different creation dates
        Job oldJob = createJob(testCompany, testUser, JobStatus.DRAFT);
        oldJob.setCreatedAt(testStartDate.minusDays(10));
        entityManager.persist(oldJob);
        
        Job recentJob1 = createJob(testCompany, testUser, JobStatus.PUBLISHED);
        recentJob1.setCreatedAt(testStartDate.plusDays(1));
        entityManager.persist(recentJob1);
        
        Job recentJob2 = createJob(testCompany, testUser, JobStatus.DRAFT);
        recentJob2.setCreatedAt(testStartDate.plusDays(2));
        entityManager.persist(recentJob2);
        
        entityManager.flush();
        
        // When
        long count = jobRepository.countNewJobsInPeriod(testStartDate);
        
        // Then
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    void sumViewsByCompanyId_ShouldReturnTotalViews() {
        // Given: Create jobs with different view counts
        Job job1 = createJob(testCompany, testUser, JobStatus.PUBLISHED);
        job1.setViewsCount(100);
        entityManager.persist(job1);
        
        Job job2 = createJob(testCompany, testUser, JobStatus.PUBLISHED);
        job2.setViewsCount(250);
        entityManager.persist(job2);
        
        Job job3 = createJob(testCompany, testUser, JobStatus.DRAFT);
        job3.setViewsCount(50);
        entityManager.persist(job3);
        
        entityManager.flush();
        
        // When
        long totalViews = jobRepository.sumViewsByCompanyId(testCompany.getId());
        
        // Then
        assertThat(totalViews).isEqualTo(400);
    }
    
    @Test
    void sumViewsByCompanyId_ShouldReturnZeroWhenNoJobs() {
        // Given: No jobs for company
        
        // When
        long totalViews = jobRepository.sumViewsByCompanyId(testCompany.getId());
        
        // Then
        assertThat(totalViews).isEqualTo(0);
    }
    
    @Test
    void findTopJobsByApplicationCount_ShouldReturnTopJobs() {
        // Given: Create jobs with different application counts
        Job job1 = createJob(testCompany, testUser, JobStatus.PUBLISHED);
        job1.setTitle("Job 1");
        job1.setPublishedAt(LocalDateTime.now().minusDays(5));
        job1 = entityManager.persist(job1);
        createApplications(job1, 5);
        
        Job job2 = createJob(testCompany, testUser, JobStatus.PUBLISHED);
        job2.setTitle("Job 2");
        job2.setPublishedAt(LocalDateTime.now().minusDays(3));
        job2 = entityManager.persist(job2);
        createApplications(job2, 10);
        
        Job job3 = createJob(testCompany, testUser, JobStatus.PUBLISHED);
        job3.setTitle("Job 3");
        job3.setPublishedAt(LocalDateTime.now().minusDays(1));
        job3 = entityManager.persist(job3);
        createApplications(job3, 3);
        
        entityManager.flush();
        
        // When
        List<TopJobDto> topJobs = jobRepository.findTopJobsByApplicationCount(
            testCompany.getId(), 2);
        
        // Then
        assertThat(topJobs).hasSize(2);
        assertThat(topJobs.get(0).getJobTitle()).isEqualTo("Job 2");
        assertThat(topJobs.get(0).getApplicationCount()).isEqualTo(10);
        assertThat(topJobs.get(1).getJobTitle()).isEqualTo("Job 1");
        assertThat(topJobs.get(1).getApplicationCount()).isEqualTo(5);
    }
    
    @Test
    void countJobsByStatusForCompany_ShouldReturnStatusBreakdown() {
        // Given: Create jobs with different statuses
        createJob(testCompany, testUser, JobStatus.PUBLISHED);
        createJob(testCompany, testUser, JobStatus.PUBLISHED);
        createJob(testCompany, testUser, JobStatus.DRAFT);
        createJob(testCompany, testUser, JobStatus.CLOSED);
        entityManager.flush();
        
        // When
        Map<JobStatus, Long> statusCounts = jobRepository.countJobsByStatusForCompany(
            testCompany.getId());
        
        // Then
        assertThat(statusCounts).containsEntry(JobStatus.PUBLISHED, 2L);
        assertThat(statusCounts).containsEntry(JobStatus.DRAFT, 1L);
        assertThat(statusCounts).containsEntry(JobStatus.CLOSED, 1L);
        assertThat(statusCounts).containsEntry(JobStatus.ARCHIVED, 0L);
    }
    
    @Test
    void calculateAvgTimeToFirstApplication_ShouldReturnAverageHours() {
        // Given: Create jobs with applications at different times
        Job job1 = createJob(testCompany, testUser, JobStatus.PUBLISHED);
        job1.setPublishedAt(LocalDateTime.now().minusDays(2));
        job1 = entityManager.persist(job1);
        
        Application app1 = new Application();
        app1.setJob(job1);
        app1.setUser(testUser);
        app1.setStatus(ApplicationStatus.PENDING);
        app1.setAppliedAt(job1.getPublishedAt().plusHours(24)); // 24 hours after
        entityManager.persist(app1);
        
        Job job2 = createJob(testCompany, testUser, JobStatus.PUBLISHED);
        job2.setPublishedAt(LocalDateTime.now().minusDays(1));
        job2 = entityManager.persist(job2);
        
        User anotherUser = new User();
        anotherUser.setEmail("another@test.com");
        anotherUser.setPasswordHash("password");
        anotherUser.setFullName("Another User");
        anotherUser.setCode("TEST003");
        anotherUser.setIsActive(true);
        anotherUser = entityManager.persist(anotherUser);
        
        Application app2 = new Application();
        app2.setJob(job2);
        app2.setUser(anotherUser);
        app2.setStatus(ApplicationStatus.PENDING);
        app2.setAppliedAt(job2.getPublishedAt().plusHours(48)); // 48 hours after
        entityManager.persist(app2);
        
        entityManager.flush();
        
        // When
        Double avgHours = jobRepository.calculateAvgTimeToFirstApplication(
            testCompany.getId());
        
        // Then
        assertThat(avgHours).isNotNull();
        assertThat(avgHours).isEqualTo(36.0); // Average of 24 and 48
    }
    
    @Test
    void calculateAvgTimeToFirstApplication_ShouldReturnNullWhenNoApplications() {
        // Given: Create job without applications
        Job job = createJob(testCompany, testUser, JobStatus.PUBLISHED);
        job.setPublishedAt(LocalDateTime.now().minusDays(1));
        entityManager.persist(job);
        entityManager.flush();
        
        // When
        Double avgHours = jobRepository.calculateAvgTimeToFirstApplication(
            testCompany.getId());
        
        // Then
        assertThat(avgHours).isNull();
    }
    
    // Helper methods
    
    private Job createJob(Company company, User user, JobStatus status) {
        Job job = new Job();
        job.setTitle("Test Job");
        job.setDescription("Test Description");
        job.setCompany(company);
        job.setCreatedBy(user);
        job.setStatus(status);
        job.setViewsCount(0);
        job.setApplicationsCount(0);
        job.setIsFeatured(false);
        job.setNumberOfPositions(1);
        job.setIsSalaryNegotiable(false);
        job.setSalaryCurrency("VND");
        return job;
    }
    
    private void createApplications(Job job, int count) {
        for (int i = 0; i < count; i++) {
            User applicant = new User();
            applicant.setEmail("applicant" + i + "@test.com");
            applicant.setPasswordHash("password");
            applicant.setFullName("Applicant " + i);
            applicant.setCode("APP" + String.format("%03d", i));
            applicant.setIsActive(true);
            applicant = entityManager.persist(applicant);
            
            Application application = new Application();
            application.setJob(job);
            application.setUser(applicant);
            application.setStatus(ApplicationStatus.PENDING);
            application.setAppliedAt(LocalDateTime.now());
            entityManager.persist(application);
        }
    }
}
