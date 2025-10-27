package com.example.jobportal.service;

import com.example.jobportal.dto.request.JobRequest;
import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.dto.response.JobBaseResponseV2;
import com.example.jobportal.dto.response.JobDetailResponse;
import com.example.jobportal.exception.UserException;
import com.example.jobportal.model.entity.*;
import com.example.jobportal.model.enums.JobStatus;
import com.example.jobportal.exception.JobException;
import com.example.jobportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;

    @Override
    public Page<JobBaseResponse> getBaseJobs(String keyword, String category, String location, Pageable pageable) {
        return jobRepository.getBaseJobs(keyword, category, location, pageable);
    }

    @Override
    public Page<JobBaseResponseV2> getJobs(String keyword, String category, String location, Pageable pageable) {
        Page<Object[]> results = jobRepository.getJobsNative(keyword, category, location, pageable);

        // Chuyển Object[] sang DTO
        return results.map(row -> new JobBaseResponseV2(
                ((Number) row[0]).longValue(),                   // j.id
                (String) row[1],                                 // j.title
                (String) row[2],                                 // company_name
                (String) row[3],
                (String) row[4],
                (String) row[5],
                (String) row[6],
                (String) row[7],                                 // company_logo
                row[8] != null && (Boolean) row[8],              // is_salary_negotiable
                (BigDecimal) row[9],                             // salary_min
                (BigDecimal) row[10],                             // salary_max
                (String) row[11],                                 // salary_currency
                (String) row[12],                                 // work_type
                (String) row[13],                                // employment_type
                (String) row[14],                                // experience_level
                row[15] != null ? ((Number) row[15]).intValue() : null,  // number_of_positions
                row[16] != null ? ((Timestamp) row[16]).toLocalDateTime() : null, // application_deadline
                (String) row[17]                                 // category_names (STRING_AGG)
        ));
    }


    @Override
    @Transactional
    public Job createJob(JobRequest jobRequest) {
        validateJobRequest(jobRequest);

        Company existingCompany = companyRepository.findById(jobRequest.getCompanyId())
                .orElseThrow(() -> JobException.notFound("Company not found"));

        Set<JobCategory> existingCategories = jobCategoryRepository.findByIdIn(jobRequest.getCategoryIds());
        if (existingCategories.isEmpty()) {
            throw JobException.badRequest("No valid categories found");
        }
        Job job = new Job();
        mapJobRequestToEntity(jobRequest,job,existingCompany,existingCategories);
        if (job.getStatus() == JobStatus.PUBLISHED) {
            job.setPublishedAt(LocalDateTime.now());
        }

        return jobRepository.save(job);
    }

    @Override
    @Transactional
    public Job updateJob(Long jobId, JobRequest jobRequest) {
        validateJobRequest(jobRequest);

        Company existingCompany = companyRepository.findById(jobRequest.getCompanyId())
                .orElseThrow(() -> JobException.notFound("Company not found"));

        Set<JobCategory> existingCategories = jobCategoryRepository.findByIdIn(jobRequest.getCategoryIds());
        if (existingCategories.isEmpty()) {
            throw JobException.badRequest("No valid categories found");
        }

        Job existingJob = jobRepository.findById(jobId)
                .orElseThrow(() -> JobException.notFound("Job not found"));
        mapJobRequestToEntity(jobRequest,existingJob,existingCompany,existingCategories);
        if (existingJob.getStatus() == JobStatus.PUBLISHED) {
            existingJob.setPublishedAt(LocalDateTime.now());
        }

        return jobRepository.save(existingJob);
    }

    @Override
    public void changeStatusJob(Long jobId, String status) {
        Job existingJob = jobRepository.findById(jobId)
                .orElseThrow(() -> JobException.notFound("Job not found"));
        existingJob.setStatus(JobStatus.valueOf(status));
        jobRepository.save(existingJob);
    }

    @Override
    public JobDetailResponse getJobDetail(Long jobId) {
        Job existingJob = jobRepository.findById(jobId)
                .orElseThrow(() -> JobException.notFound("Job not found"));
        return JobDetailResponse.fromEntity(existingJob);
    }


    @Override
    public Page<JobBaseResponse> getJobsByHr(String keyword, String category, String location,String status,Long hrId, Pageable pageable) {
        return jobRepository.getJobsByHrId(keyword, category, location, status ,hrId, pageable);
    }

    @Override
    public Page<JobBaseResponse> getJobsByCompany(String keyword, String category, String location,String status,Long companyId, Pageable pageable) {
        return jobRepository.getJobsByCompanyId(keyword, category, location, companyId, status, pageable);
    }

    @Override
    @Transactional
    public JobBaseResponse addJobToListSavedJob(Long jobId, Long userId) {
        Job existingJob = jobRepository.findById(jobId)
                .orElseThrow(() -> JobException.notFound("Job not found"));
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound("User not found"));
        SavedJob savedJob = SavedJob.builder()
            .job(existingJob)
            .user(existingUser)
            .build();
        savedJobRepository.save(savedJob);
        return JobBaseResponse.fromEntity(existingJob);
    }

    @Override
    @Transactional
    public void removeJobFromListSavedJob(Long savedJobId) {
        SavedJob existingSavedJob = savedJobRepository.findById(savedJobId)
                .orElseThrow(() -> JobException.notFound("Saved job not found"));
        savedJobRepository.delete(existingSavedJob);
    }


    private void validateJobRequest(JobRequest jobRequest) {
        if (!companyRepository.existsById(jobRequest.getCompanyId())) {
            throw new JobException("Company does not exist", HttpStatus.BAD_REQUEST);
        }

        if (jobRequest.getCategoryIds() == null || jobRequest.getCategoryIds().isEmpty()) {
            throw new JobException("At least one category is required", HttpStatus.BAD_REQUEST);
        }

        long count = jobCategoryRepository.countByIdIn(jobRequest.getCategoryIds());
        if (count != jobRequest.getCategoryIds().size()) {
            throw new JobException("One or more categories are invalid", HttpStatus.BAD_REQUEST);
        }

        if (Boolean.FALSE.equals(jobRequest.getIsSalaryNegotiable())) {
            if (jobRequest.getSalaryMin() == null || jobRequest.getSalaryMax() == null) {
                throw new JobException("Salary range is required when not negotiable", HttpStatus.BAD_REQUEST);
            }
            if (jobRequest.getSalaryMin().compareTo(jobRequest.getSalaryMax()) > 0) {
                throw new JobException("Minimum salary cannot be greater than maximum salary", HttpStatus.BAD_REQUEST);
            }
        }

        if (jobRequest.getApplicationDeadline() != null &&
                jobRequest.getApplicationDeadline().isBefore(LocalDateTime.now())) {
            throw new JobException("Application deadline must be in the future", HttpStatus.BAD_REQUEST);
        }

        if (jobRequest.getStatus() != null && jobRequest.getStatus().equalsIgnoreCase("PUBLISHED")) {
            if (jobRequest.getApplicationDeadline() == null) {
                throw new JobException("Application deadline is required for published jobs", HttpStatus.BAD_REQUEST);
            }
        }

        if (jobRequest.getNumberOfPositions() != null && jobRequest.getNumberOfPositions() <= 0) {
            throw new JobException("Number of positions must be greater than 0", HttpStatus.BAD_REQUEST);
        }
    }
    private void mapJobRequestToEntity(JobRequest req, Job job, Company company, Set<JobCategory> categories) {
        BaseAddress baseAddress = BaseAddress.builder()
                .street(req.getStreet())
                .ward(req.getWard())
                .district(req.getDistrict())
                .city(req.getCity())
                .country(req.getCountry())
                .build();

        job.setTitle(req.getTitle());
        job.setDescription(req.getDescription());
        job.setRequirements(req.getRequirements());
        job.setResponsibilities(req.getResponsibilities());
        job.setBenefits(req.getBenefits());
        job.setAddress(baseAddress);
        job.setWorkType(req.getWorkType());
        job.setEmploymentType(req.getEmploymentType());
        job.setExperienceLevel(req.getExperienceLevel());
        job.setIsSalaryNegotiable(req.getIsSalaryNegotiable());
        job.setSalaryMin(req.getSalaryMin());
        job.setSalaryMax(req.getSalaryMax());
        job.setSalaryCurrency(req.getSalaryCurrency());
        job.setSkills(req.getSkills());
        job.setNumberOfPositions(req.getNumberOfPositions());
        job.setStatus(JobStatus.fromValue(req.getStatus()));
        job.setApplicationDeadline(req.getApplicationDeadline());
        job.setCompany(company);
        job.setCategories(categories);
    }

}
