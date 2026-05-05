package com.example.jobportal.repository;

import com.example.jobportal.dto.response.TopHrDto;
import com.example.jobportal.model.entity.*;
import com.example.jobportal.model.enums.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserRepositoryCustomImpl
 * Tests custom query methods for dashboard statistics
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryCustomImplTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    private Company testCompany;
    private Role hrRole;
    private Role candidateRole;
    private LocalDateTime testStartDate;
    
    @BeforeEach
    void setUp() {
        // Create test roles
        hrRole = new Role();
        hrRole.setName("HR");
        hrRole.setDescription("HR Role");
        hrRole = entityManager.persist(hrRole);
        
        candidateRole = new Role();
        candidateRole.setName("CANDIDATE");
        candidateRole.setDescription("Candidate Role");
        candidateRole = entityManager.persist(candidateRole);
        
        // Create test company
        testCompany = new Company();
        testCompany.setName("Test Company");
        testCompany.setEmail("test@company.com");
        testCompany.setIsActive(true);
        testCompany.setIsVerified(true);
        testCompany = entityManager.persist(testCompany);
        
        testStartDate = LocalDateTime.now().minusDays(7);
        
        entityManager.flush();
    }
    
    @Test
    void countAllActiveUsers_ShouldReturnCorrectCount() {
        // Given: Create 3 active users and 1 inactive user
        createUser("user1@test.com", "USER001", hrRole, testCompany, true);
        createUser("user2@test.com", "USER002", hrRole, testCompany, true);
        createUser("user3@test.com", "USER003", candidateRole, null, true);
        createUser("user4@test.com", "USER004", candidateRole, null, false);
        entityManager.flush();
        
        // When
        long count = userRepository.countAllActiveUsers();
        
        // Then
        assertThat(count).isEqualTo(3);
    }
    
    @Test
    void countNewUsersInPeriod_ShouldFilterByCreationDate() {
        // Given: Create users with different creation dates
        User oldUser = createUser("old@test.com", "OLD001", candidateRole, null, true);
        oldUser.setCreatedAt(testStartDate.minusDays(10));
        entityManager.persist(oldUser);
        
        User recentUser1 = createUser("recent1@test.com", "REC001", hrRole, testCompany, true);
        recentUser1.setCreatedAt(testStartDate.plusDays(1));
        entityManager.persist(recentUser1);
        
        User recentUser2 = createUser("recent2@test.com", "REC002", candidateRole, null, true);
        recentUser2.setCreatedAt(testStartDate.plusDays(2));
        entityManager.persist(recentUser2);
        
        entityManager.flush();
        
        // When
        long count = userRepository.countNewUsersInPeriod(testStartDate);
        
        // Then
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    void findTopHrByJobCount_ShouldReturnTopHrUsers() {
        // Given: Create HR users with different job counts
        User hr1 = createUser("hr1@test.com", "HR001", hrRole, testCompany, true);
        User hr2 = createUser("hr2@test.com", "HR002", hrRole, testCompany, true);
        User hr3 = createUser("hr3@test.com", "HR003", hrRole, testCompany, true);
        
        // Create jobs for each HR user
        createJobs(hr1, testCompany, 5);
        createJobs(hr2, testCompany, 10);
        createJobs(hr3, testCompany, 3);
        
        entityManager.flush();
        
        // When
        List<TopHrDto> topHrUsers = userRepository.findTopHrByJobCount(testCompany.getId(), 2);
        
        // Then
        assertThat(topHrUsers).hasSize(2);
        assertThat(topHrUsers.get(0).getFullName()).isEqualTo("HR User hr2@test.com");
        assertThat(topHrUsers.get(0).getJobsCreated()).isEqualTo(10);
        assertThat(topHrUsers.get(1).getFullName()).isEqualTo("HR User hr1@test.com");
        assertThat(topHrUsers.get(1).getJobsCreated()).isEqualTo(5);
    }
    
    @Test
    void findTopHrByJobCount_ShouldOnlyIncludeActiveHrUsers() {
        // Given: Create active and inactive HR users
        User activeHr = createUser("active@test.com", "ACT001", hrRole, testCompany, true);
        User inactiveHr = createUser("inactive@test.com", "INA001", hrRole, testCompany, false);
        
        createJobs(activeHr, testCompany, 5);
        createJobs(inactiveHr, testCompany, 10);
        
        entityManager.flush();
        
        // When
        List<TopHrDto> topHrUsers = userRepository.findTopHrByJobCount(testCompany.getId(), 5);
        
        // Then
        assertThat(topHrUsers).hasSize(1);
        assertThat(topHrUsers.get(0).getFullName()).isEqualTo("HR User active@test.com");
        assertThat(topHrUsers.get(0).getJobsCreated()).isEqualTo(5);
    }
    
    @Test
    void findTopHrByJobCount_ShouldOnlyIncludeHrRole() {
        // Given: Create HR and non-HR users
        User hrUser = createUser("hr@test.com", "HR001", hrRole, testCompany, true);
        User candidateUser = createUser("candidate@test.com", "CAN001", candidateRole, testCompany, true);
        
        createJobs(hrUser, testCompany, 5);
        createJobs(candidateUser, testCompany, 10);
        
        entityManager.flush();
        
        // When
        List<TopHrDto> topHrUsers = userRepository.findTopHrByJobCount(testCompany.getId(), 5);
        
        // Then
        assertThat(topHrUsers).hasSize(1);
        assertThat(topHrUsers.get(0).getFullName()).isEqualTo("HR User hr@test.com");
    }
    
    @Test
    void findTopHrByJobCount_ShouldIncludeHrWithZeroJobs() {
        // Given: Create HR users, some with jobs and some without
        User hr1 = createUser("hr1@test.com", "HR001", hrRole, testCompany, true);
        User hr2 = createUser("hr2@test.com", "HR002", hrRole, testCompany, true);
        
        createJobs(hr1, testCompany, 5);
        // hr2 has no jobs
        
        entityManager.flush();
        
        // When
        List<TopHrDto> topHrUsers = userRepository.findTopHrByJobCount(testCompany.getId(), 5);
        
        // Then
        assertThat(topHrUsers).hasSize(2);
        assertThat(topHrUsers.get(0).getJobsCreated()).isEqualTo(5);
        assertThat(topHrUsers.get(1).getJobsCreated()).isEqualTo(0);
    }
    
    @Test
    void findTopHrByJobCount_ShouldFilterByCompany() {
        // Given: Create HR users in different companies
        Company anotherCompany = new Company();
        anotherCompany.setName("Another Company");
        anotherCompany.setEmail("another@company.com");
        anotherCompany.setIsActive(true);
        anotherCompany = entityManager.persist(anotherCompany);
        
        User hr1 = createUser("hr1@test.com", "HR001", hrRole, testCompany, true);
        User hr2 = createUser("hr2@test.com", "HR002", hrRole, anotherCompany, true);
        
        createJobs(hr1, testCompany, 5);
        createJobs(hr2, anotherCompany, 10);
        
        entityManager.flush();
        
        // When
        List<TopHrDto> topHrUsers = userRepository.findTopHrByJobCount(testCompany.getId(), 5);
        
        // Then
        assertThat(topHrUsers).hasSize(1);
        assertThat(topHrUsers.get(0).getFullName()).isEqualTo("HR User hr1@test.com");
        assertThat(topHrUsers.get(0).getJobsCreated()).isEqualTo(5);
    }
    
    // Helper methods
    
    private User createUser(String email, String code, Role role, Company company, boolean isActive) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("password");
        user.setFullName("HR User " + email);
        user.setCode(code);
        user.setRole(role);
        user.setCompany(company);
        user.setIsActive(isActive);
        user.setIsEmailVerified(true);
        return entityManager.persist(user);
    }
    
    private void createJobs(User creator, Company company, int count) {
        for (int i = 0; i < count; i++) {
            Job job = new Job();
            job.setTitle("Job " + i + " by " + creator.getEmail());
            job.setDescription("Test Description");
            job.setCompany(company);
            job.setCreatedBy(creator);
            job.setStatus(JobStatus.PUBLISHED);
            job.setViewsCount(0);
            job.setApplicationsCount(0);
            job.setIsFeatured(false);
            job.setNumberOfPositions(1);
            job.setIsSalaryNegotiable(false);
            job.setSalaryCurrency("VND");
            entityManager.persist(job);
        }
    }
}
