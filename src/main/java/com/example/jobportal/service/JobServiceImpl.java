package com.example.jobportal.service;

import com.example.jobportal.dto.request.JobRequest;
import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.entity.BaseAddress;
import com.example.jobportal.entity.Company;
import com.example.jobportal.entity.Job;
import com.example.jobportal.entity.JobCategory;
import com.example.jobportal.enums.JobStatus;
import com.example.jobportal.exception.JobException;
import com.example.jobportal.repository.CompanyRepository;
import com.example.jobportal.repository.JobCategoryRepository;
import com.example.jobportal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final JobCategoryRepository jobCategoryRepository;

    @Override
    public Page<JobBaseResponse> getBaseJobs(String keyword, String category, String location, Pageable pageable) {
        return jobRepository.getBaseJobs(keyword, category, location, pageable);
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
        BaseAddress baseAddress = BaseAddress.builder()
                .address(jobRequest.getAddress())
                .city(jobRequest.getCity())
                .country(jobRequest.getCountry())
                .build();

        Job job = Job.builder()
                .title(jobRequest.getTitle())
                .description(jobRequest.getDescription())
                .requirements(jobRequest.getRequirements())
                .responsibilities(jobRequest.getResponsibilities())
                .benefits(jobRequest.getBenefits())
                .address(baseAddress)      // nếu Job có field này
                .workType(jobRequest.getWorkType())
                .employmentType(jobRequest.getEmploymentType())
                .experienceLevel(jobRequest.getExperienceLevel())
                .isSalaryNegotiable(jobRequest.getIsSalaryNegotiable())
                .salaryMin(jobRequest.getSalaryMin())
                .salaryMax(jobRequest.getSalaryMax())
                .salaryCurrency(jobRequest.getSalaryCurrency())
                .skills(jobRequest.getSkills())
                .numberOfPositions(jobRequest.getNumberOfPositions())
                .status(JobStatus.fromValue(jobRequest.getStatus()))
                .company(existingCompany)
                .categories(existingCategories)
                .applicationDeadline(jobRequest.getApplicationDeadline())
                .build();

        if (job.getStatus() == JobStatus.PUBLISHED) {
            job.setPublishedAt(LocalDateTime.now());
        }

        return jobRepository.save(job);
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
}
