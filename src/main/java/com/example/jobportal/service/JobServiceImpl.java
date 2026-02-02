package com.example.jobportal.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jobportal.dto.request.JobRequest;
import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.dto.response.JobBaseResponseV2;
import com.example.jobportal.dto.response.JobDetailResponse;
import com.example.jobportal.exception.JobException;
import com.example.jobportal.exception.UserException;
import com.example.jobportal.model.entity.Address;
import com.example.jobportal.model.entity.Application;
import com.example.jobportal.model.entity.Company;
import com.example.jobportal.model.entity.Job;
import com.example.jobportal.model.entity.JobCategory;
import com.example.jobportal.model.entity.SavedJob;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.model.enums.JobStatus;
import com.example.jobportal.model.enums.NotificationType;
import com.example.jobportal.repository.ApplicationRepository;
import com.example.jobportal.repository.CompanyRepository;
import com.example.jobportal.repository.JobCategoryRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.SavedJobRepository;
import com.example.jobportal.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;

    @Override
    public Page<JobBaseResponse> getBaseJobs(String keyword, String category, String location, Pageable pageable) {
        return jobRepository.getBaseJobs(keyword, category, location, pageable)
                .map(JobBaseResponse::fromEntity);
    }


    @Override
    public Page<JobBaseResponseV2> getJobs(String keyword, String category, String location, Pageable pageable) {
        Page<Object[]> results = jobRepository.getJobsNative(keyword, category, pageable);

        return results.map(row -> JobBaseResponseV2.builder()
                .id(((Number) row[0]).longValue())
                .title((String) row[1])
                .companyName((String) row[2])
                .companyLogo((String) row[3])
                .isSalaryNegotiable((Boolean) row[4])
                .salaryMin((BigDecimal) row[5])
                .salaryMax((BigDecimal) row[6])
                .salaryCurrency((String) row[7])
                .workType((String) row[8])
                .employmentType((String) row[9])
                .experienceLevel((String) row[10])
                .numberOfPositions(row[11] != null ? ((Number) row[11]).intValue() : null)
                .applicationDeadline(row[12] != null
                        ? ((Timestamp) row[12]).toLocalDateTime()
                        : null)
                .categoryNames(
                        row[13] != null
                                ? List.of(((String) row[13]).split(",\\s*"))
                                : List.of()
                )
                .build());
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
        mapJobRequestToEntity(jobRequest, job, existingCompany, existingCategories);
        if (job.getStatus() == JobStatus.PUBLISHED) {
            job.setPublishedAt(LocalDateTime.now());
        }

        Job savedJob = jobRepository.save(job);

        notificationService.createNotificationForRole(
                com.example.jobportal.constant.AppConstants.ROLE_ADMIN,
                "Tin tuyển dụng mới được đăng",
                "Công ty '" + existingCompany.getName() + "' vừa đăng việc: " + savedJob.getTitle(),
                NotificationType.JOB_CREATED.name(),
                savedJob.getId(),
                "JOB"
        );

        return savedJob;
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
        mapJobRequestToEntity(jobRequest, existingJob, existingCompany, existingCategories);

        if (existingJob.getStatus() == JobStatus.PUBLISHED) {
            existingJob.setPublishedAt(LocalDateTime.now());
        }

        Job updatedJob = jobRepository.save(existingJob);

        notificationService.createNotificationForRole(
                com.example.jobportal.constant.AppConstants.ROLE_ADMIN,
                "Tin tuyển dụng được cập nhật",
                "Công ty '" + existingCompany.getName() + "' vừa cập nhật tin: " + updatedJob.getTitle(),
                NotificationType.JOB_UPDATED.name(),
                updatedJob.getId(),
                "JOB"
        );

        return updatedJob;
    }

    @Override
    @Transactional
    public void changeStatusJob(Long jobId, String status) {
        Job existingJob = jobRepository.findById(jobId)
                .orElseThrow(() -> JobException.notFound("Job not found"));

        existingJob.setStatus(JobStatus.valueOf(status));
        jobRepository.save(existingJob);

        User hr = existingJob.getCreatedBy();
        if (hr != null) {
            notificationService.createNotification(
                    hr.getId(),
                    "Cập nhật trạng thái tin tuyển dụng",
                    "Tin '" + existingJob.getTitle() + "' đã được chuyển sang trạng thái: " + status,
                    NotificationType.JOB_STATUS_CHANGED.name(),
                    existingJob.getId(),
                    "JOB"
            );
        }
    }

    @Override
    public JobDetailResponse getJobDetail(Long jobId, Long userId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> JobException.notFound("Job not found"));

        JobDetailResponse response = JobDetailResponse.fromEntity(job);

        if (userId != null) {
            Optional<Application> applicationOpt = applicationRepository.findByUserIdAndJobId(userId, jobId);
            if (applicationOpt.isPresent()) {
                response.setApplied(true);
                response.setAppliedAt(applicationOpt.get().getAppliedAt());
            }
        } else {
            response.setApplied(false);
        }

        return response;
    }


    @Override
    public Page<JobBaseResponse> getJobsByHr(String keyword, String category, String location,
                                             String status, Long hrId, Pageable pageable) {
        return jobRepository.getJobsByHrId(keyword, category, location, status, hrId, pageable)
                .map(JobBaseResponse::fromEntity);
    }

    @Override
    public Page<JobBaseResponse> getJobsByCompany(String keyword, String category, String location,
                                                  String status, Long companyId, Pageable pageable) {
        return jobRepository.getJobsByCompanyId(keyword, category, location, status, companyId, pageable)
                .map(JobBaseResponse::fromEntity);
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
        notificationService.createNotification(
                existingUser.getId(),
                "Đã lưu tin tuyển dụng",
                "Bạn đã lưu công việc: " + existingJob.getTitle(),
                NotificationType.JOB_SAVED.name(),
                existingJob.getId(),
                "JOB"
        );
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
        Address address = job.getAddress();
        if (address == null) {
            address = new Address();
        }
        address.setDetailAddress(req.getStreet());
        address.setCommuneName(req.getWard());
        address.setAddressType("job");
        address.setProvinceName(req.getCity());
        address.setIsActive(true);
        address.setIsPrimary(true);

        job.setTitle(req.getTitle());
        job.setDescription(req.getDescription());
        job.setRequirements(req.getRequirements());
        job.setResponsibilities(req.getResponsibilities());
        job.setBenefits(req.getBenefits());
        job.setAddress(address);
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
