package com.example.jobportal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.example.jobportal.dto.response.JobResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jobportal.constant.AppConstants;
import com.example.jobportal.dto.request.JobRequest;
import com.example.jobportal.dto.request.SkillRequest;
import com.example.jobportal.dto.response.AddressResponse;
import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.dto.response.JobDetailResponse;
import com.example.jobportal.exception.JobException;
import com.example.jobportal.exception.UserException;
import com.example.jobportal.model.entity.Address;
import com.example.jobportal.model.entity.Application;
import com.example.jobportal.model.entity.Company;
import com.example.jobportal.model.entity.Job;
import com.example.jobportal.model.entity.JobCategory;
import com.example.jobportal.model.entity.SavedJob;
import com.example.jobportal.model.entity.Skill;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.model.enums.JobStatus;
import com.example.jobportal.model.enums.NotificationType;
import com.example.jobportal.repository.ApplicationRepository;
import com.example.jobportal.repository.CompanyRepository;
import com.example.jobportal.repository.JobCategoryRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.SavedJobRepository;
import com.example.jobportal.repository.SkillRepository;
import com.example.jobportal.repository.UserRepository;
import com.example.jobportal.utils.SecurityUtils;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;
    private final SkillRepository skillRepository;

    @Override
    @Cacheable(cacheNames = "jobs:base", key = "T(java.util.Objects).toString(#keyword)+'|'+T(java.util.Objects).toString(#category)+'|'+T(java.util.Objects).toString(#location)+'|'+#pageable.pageNumber+'|'+#pageable.pageSize+'|'+#pageable.sort")
    public Page<JobBaseResponse> getBaseJobs(String keyword, String category, String location, Pageable pageable) {
        Specification<Job> spec = buildSpecification(keyword, category, location, null, null, null, true);
        return jobRepository.findAll(spec, pageable).map(JobBaseResponse::fromEntity);
    }


    @Override
    @Cacheable(cacheNames = "jobs:list", key = "T(java.util.Objects).toString(#keyword)+'|'+T(java.util.Objects).toString(#category)+'|'+T(java.util.Objects).toString(#location)+'|'+#pageable.pageNumber+'|'+#pageable.pageSize+'|'+#pageable.sort")
    public Page<JobResponse> getJobs(String keyword, String category, String location, Pageable pageable) {
        Specification<Job> spec = buildSpecification(keyword, category, location, null, null, null, true);
        return jobRepository.findAll(spec, pageable).map(this::toJobResponse);
    }


    @Override
    @CacheEvict(cacheNames = {"jobs:base","jobs:list","jobs:hr","jobs:company","jobs:detail"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public JobBaseResponse createJob(JobRequest jobRequest) {
        validateJobRequest(jobRequest);

        Company existingCompany = companyRepository.findById(jobRequest.getCompanyId())
                .orElseThrow(() -> JobException.notFound("Company not found"));

        Set<JobCategory> existingCategories = jobCategoryRepository.findByIdIn(jobRequest.getCategoryIds());
        if (existingCategories.isEmpty()) {
            throw JobException.badRequest("No valid categories found");
        }

        Set<Skill> existingSkills = Set.of();
        if (jobRequest.getSkillIds() != null && !jobRequest.getSkillIds().isEmpty()) {
            existingSkills = skillRepository.findByIdIn(jobRequest.getSkillIds());
        }

        Job job = new Job();
        mapJobRequestToEntity(jobRequest, job, existingCompany, existingCategories, existingSkills);
        Long currentUserId = SecurityUtils.currentUserId();
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> JobException.notFound("Current user not found"));
        job.setCreatedBy(creator);
        applyStatusTimestamps(job);

        Job savedJob = jobRepository.save(job);
        log.info("Created job {}", savedJob.getId());

        if (savedJob.getStatus() == JobStatus.PUBLISHED) {
            notificationService.createNotificationForRole(
                    AppConstants.ROLE_ADMIN,
                    "Tin tuyển dụng mới được đăng",
                    "Công ty '" + existingCompany.getName() + "' vừa đăng việc: " + savedJob.getTitle(),
                    NotificationType.JOB_CREATED,
                    savedJob.getId(),
                    AppConstants.ENTITY_JOB
            );
        }

        return JobBaseResponse.fromEntity(savedJob);
    }

    @Override
    @CacheEvict(cacheNames = {"jobs:base","jobs:list","jobs:hr","jobs:company","jobs:detail"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public JobBaseResponse updateJob(Long jobId, JobRequest jobRequest) {
        validateJobRequest(jobRequest);

        Company existingCompany = companyRepository.findById(jobRequest.getCompanyId())
                .orElseThrow(() -> JobException.notFound("Company not found"));

        Set<JobCategory> existingCategories = jobCategoryRepository.findByIdIn(jobRequest.getCategoryIds());
        if (existingCategories.isEmpty()) {
            throw JobException.badRequest("No valid categories found");
        }

        Set<Skill> existingSkills = Set.of();
        if (jobRequest.getSkillIds() != null && !jobRequest.getSkillIds().isEmpty()) {
            existingSkills = skillRepository.findByIdIn(jobRequest.getSkillIds());
        }

        Job existingJob = jobRepository.findById(jobId)
                .orElseThrow(() -> JobException.notFound("Job not found"));
        JobStatus previousStatus = existingJob.getStatus();
        mapJobRequestToEntity(jobRequest, existingJob, existingCompany, existingCategories, existingSkills);
        applyStatusTimestamps(existingJob, previousStatus);

        Job updatedJob = jobRepository.save(existingJob);
        log.info("Updated job {}", updatedJob.getId());

        if (updatedJob.getStatus() == JobStatus.PUBLISHED) {
            notificationService.createNotificationForRole(
                    AppConstants.ROLE_ADMIN,
                    "Tin tuyển dụng được cập nhật",
                    "Công ty '" + existingCompany.getName() + "' vừa cập nhật tin: " + updatedJob.getTitle(),
                    NotificationType.JOB_UPDATED,
                    updatedJob.getId(),
                    AppConstants.ENTITY_JOB
            );
        }

        return JobBaseResponse.fromEntity(updatedJob);
    }

    @Override
    @CacheEvict(cacheNames = {"jobs:base","jobs:list","jobs:hr","jobs:company","jobs:detail"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void changeStatusJob(Long jobId, String status) {
        Job existingJob = jobRepository.findById(jobId)
                .orElseThrow(() -> JobException.notFound("Job not found"));

        existingJob.setStatus(JobStatus.fromValue(status));
        jobRepository.save(existingJob);
        log.info("Changed job {} status to {}", jobId, status);

        User hr = existingJob.getCreatedBy();
        if (hr != null) {
            notificationService.createNotification(
                hr.getId(),
                "Cập nhật trạng thái tin tuyển dụng",
                "Tin '" + existingJob.getTitle() + "' đã được chuyển sang trạng thái: " + status,
                NotificationType.JOB_STATUS_CHANGED,
                existingJob.getId(),
                AppConstants.ENTITY_JOB
        );
        }
    }

    @Override
    @Cacheable(cacheNames = "jobs:detail", key = "#jobId")
    public JobDetailResponse getJobDetail(Long jobId, Long userId) {
        Job job = jobRepository.findWithDetailsById(jobId)
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
    @Cacheable(cacheNames = "jobs:hr", key = "T(java.util.Objects).toString(#keyword)+'|'+T(java.util.Objects).toString(#category)+'|'+T(java.util.Objects).toString(#location)+'|'+T(java.util.Objects).toString(#status)+'|'+#hrId+'|'+#pageable.pageNumber+'|'+#pageable.pageSize+'|'+#pageable.sort")
    public Page<JobBaseResponse> getJobsByHr(String keyword, String category, String location,
                                             String status, Long hrId, Pageable pageable) {
        Specification<Job> spec = buildSpecification(keyword, category, location, status, hrId, null, false);
        return jobRepository.findAll(spec, pageable).map(JobBaseResponse::fromEntity);
    }

    @Override
    @Cacheable(cacheNames = "jobs:company", key = "T(java.util.Objects).toString(#keyword)+'|'+T(java.util.Objects).toString(#category)+'|'+T(java.util.Objects).toString(#location)+'|'+T(java.util.Objects).toString(#status)+'|'+#companyId+'|'+#pageable.pageNumber+'|'+#pageable.pageSize+'|'+#pageable.sort")
    public Page<JobBaseResponse> getJobsByCompany(String keyword, String category, String location,
                                                  String status, Long companyId, Pageable pageable) {
        Specification<Job> spec = buildSpecification(keyword, category, location, status, null, companyId, false);
        return jobRepository.findAll(spec, pageable).map(JobBaseResponse::fromEntity);
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
                NotificationType.JOB_SAVED,
                existingJob.getId(),
                AppConstants.ENTITY_JOB
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

    @Override
    @Transactional(readOnly = true)
    public List<JobBaseResponse> getSavedJobs(Long userId) {
        return savedJobRepository.findByUserId(userId).stream()
                .map(saved -> JobBaseResponse.fromEntity(saved.getJob()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Skill createSkill(SkillRequest skillRequest) {
        Skill newSkill = Skill.builder().name(skillRequest.getName()).build();
         skillRepository.save(newSkill);
         return newSkill;
    }

    @Override
    public Skill updateSkill(SkillRequest skillRequest, Long id) {
        Skill existingSkill = skillRepository.findById(id)
                .orElseThrow(() -> JobException.notFound("Skill not found with id: " + id));
        if (skillRequest != null && skillRequest.getName() != null && !skillRequest.getName().isBlank()) {
            existingSkill.setName(skillRequest.getName());
        }
        return skillRepository.save(existingSkill);
    }

    @Override
    public void deleteSkill(Long skillId) {
        Skill existingSkill = skillRepository.findById(skillId)
                .orElseThrow(() -> JobException.notFound("Skill not found with id: " + skillId));
        skillRepository.delete(existingSkill);
    }

    @Override
    public List<Skill> getSkills() {
        return skillRepository.findAll();
    }

    @Override
    @Cacheable(cacheNames = "jobs:list", key = "T(java.util.Objects).toString(#keyword)+'|'+T(java.util.Objects).toString(#category)+'|'+T(java.util.Objects).toString(#location)+'|'+T(java.util.Objects).toString(#status)+'|'+#pageable.pageNumber+'|'+#pageable.pageSize+'|'+#pageable.sort")
    public Page<JobResponse> getJobs(String keyword, String category, String location, String status, Pageable pageable) {
        Specification<Job> spec = buildSpecification(keyword, category, location, status, null, null, false);
        return jobRepository.findAll(spec, pageable).map(this::toJobResponse);
    }

    private Specification<Job> buildSpecification(String keyword,
                                                  String category,
                                                  String location,
                                                  String status,
                                                  Long hrId,
                                                  Long companyId,
                                                  boolean onlyPublished) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
            }
            if (category != null && !category.isBlank()) {
                Join<Job, JobCategory> catJoin = root.join("categories", JoinType.LEFT);
                predicates.add(cb.equal(cb.lower(catJoin.get("name")), category.toLowerCase()));
                query.distinct(true);
            }
            if (location != null && !location.isBlank()) {
                Join<Job, Address> addressJoin = root.join("address", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(addressJoin.get("provinceName")), "%" + location.toLowerCase() + "%"));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), JobStatus.fromValue(status)));
            }
            if (onlyPublished) {
                predicates.add(cb.equal(root.get("status"), JobStatus.PUBLISHED));
            }
            if (hrId != null) {
                predicates.add(cb.equal(root.get("createdBy").get("id"), hrId));
            }
            if (companyId != null) {
                predicates.add(cb.equal(root.get("company").get("id"), companyId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private JobResponse toJobResponse(Job job) {
        if (job == null) {
            return null;
        }
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany() != null ? job.getCompany().getName() : null)
                .addressResponse(AddressResponse.fromEntity(job.getAddress()))
                .employmentType(job.getEmploymentType() != null ? job.getEmploymentType().getValue() : null)
                .numberOfPositions(job.getNumberOfPositions())
                .status(job.getStatus() != null ? job.getStatus().getValue() : null)
                .publishedAt(job.getPublishedAt())
                .build();
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

    private void mapJobRequestToEntity(JobRequest req, Job job, Company company, Set<JobCategory> categories, Set<Skill> skills) {
        Address address = job.getAddress();
        if (req.getAddressRequest() != null) {
            if (address == null) {
                address = new Address();
            }
            var ar = req.getAddressRequest();
            address.setAddressType(ar.getAddressType() != null ? ar.getAddressType() : "job");
            address.setProvinceCode(ar.getProvinceCode());
            address.setProvinceName(ar.getProvinceName());
            address.setCommuneCode(ar.getCommuneCode());
            address.setCommuneName(ar.getCommuneName());
            address.setDetailAddress(ar.getDetailAddress());
            address.setIsActive(true);
            address.setIsPrimary(Boolean.TRUE.equals(ar.getIsPrimary()));
            // Explicitly ensure this address is NOT linked to any company or verification request.
            // Job addresses are private to the job — they must never be shared.
            address.setCompany(null);
            address.setVerificationRequest(null);
        }

        job.setTitle(req.getTitle());
        job.setDescription(req.getDescription());
        job.setRequirements(req.getRequirements());
        job.setResponsibilities(req.getResponsibilities());
        job.setBenefits(req.getBenefits());
        if (address != null) {
            job.setAddress(address);
        }
        job.setWorkType(req.getWorkType());
        job.setEmploymentType(req.getEmploymentType());
        job.setExperienceLevel(req.getExperienceLevel());
        job.setIsSalaryNegotiable(Boolean.TRUE.equals(req.getIsSalaryNegotiable()));
        job.setSalaryMin(req.getSalaryMin());
        job.setSalaryMax(req.getSalaryMax());
        job.setSalaryCurrency(req.getSalaryCurrency() != null ? req.getSalaryCurrency() : "VND");
        job.setSkills(skills);
        job.setNumberOfPositions(req.getNumberOfPositions() != null ? req.getNumberOfPositions() : 1);
        String statusValue = req.getStatus() != null ? req.getStatus() : JobStatus.DRAFT.getValue();
        job.setStatus(JobStatus.fromValue(statusValue));
        job.setIsFeatured(Boolean.TRUE.equals(req.getIsFeatured()));
        job.setApplicationDeadline(req.getApplicationDeadline());
        job.setCompany(company);
        job.setCategories(categories);
    }

    private void applyStatusTimestamps(Job job) {
        applyStatusTimestamps(job, null);
    }

    private void applyStatusTimestamps(Job job, JobStatus previousStatus) {
        if (job.getStatus() == JobStatus.PUBLISHED) {
            if (job.getPublishedAt() == null || previousStatus != JobStatus.PUBLISHED) {
                job.setPublishedAt(LocalDateTime.now());
            }
            job.setClosedAt(null);
            return;
        }
        if (job.getStatus() == JobStatus.CLOSED) {
            if (job.getClosedAt() == null || previousStatus != JobStatus.CLOSED) {
                job.setClosedAt(LocalDateTime.now());
            }
            return;
        }
        if (job.getStatus() == JobStatus.DRAFT || job.getStatus() == JobStatus.ARCHIVED) {
            job.setClosedAt(null);
        }
    }

}
