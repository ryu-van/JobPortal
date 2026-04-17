package com.example.jobportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.jobportal.dto.response.UploadResultResponse;
import com.example.jobportal.model.entity.*;
import com.example.jobportal.model.enums.UploadType;
import com.example.jobportal.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import com.example.jobportal.dto.request.EducationRequest;
import com.example.jobportal.dto.request.ExperienceRequest;
import com.example.jobportal.dto.request.ResumeRequest;
import com.example.jobportal.dto.response.ResumeBaseResponse;
import com.example.jobportal.dto.response.ResumeDetailResponse;
import com.example.jobportal.exception.ResumeException;
import com.example.jobportal.exception.UserException;
import com.example.jobportal.repository.ResumeEducationRepository;
import com.example.jobportal.repository.ResumeExperienceRepository;
import com.example.jobportal.repository.ResumeRepository;
import com.example.jobportal.repository.ResumeSkillRepository;
import com.example.jobportal.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class ResumeServiceImpl implements ResumeService{
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ResumeEducationRepository resumeEducationRepository;
    private final ResumeExperienceRepository resumeExperienceRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final FileUploadService  fileUploadService;

    @Override
    public ResumeBaseResponse createResume(ResumeRequest resumeRequest) {
        var user = userRepository.findById(resumeRequest.getUserId())
                .orElseThrow(() -> UserException.notFound("User not found"));

        Resume resume = Resume.builder()
                .title(resumeRequest.getTitle())
                .fileUrl(resumeRequest.getFileUrl())
                .fileName(resumeRequest.getFileName())
                .fileType(resumeRequest.getFileType())
                .summary(resumeRequest.getSummary())
                .isPrimary(resumeRequest.getIsPrimary())
                .isPublic(resumeRequest.getIsPublic())
                .user(user)
                .build();

        if (resume.getEducations() == null) resume.setEducations(new ArrayList<>());
        if (resume.getExperiences() == null) resume.setExperiences(new ArrayList<>());
        if (resume.getSkills() == null) resume.setSkills(new ArrayList<>());

        if (resumeRequest.getEducations() != null) {
            for (var eduReq : resumeRequest.getEducations()) {
                ResumeEducation edu = ResumeEducation.builder()
                        .institution(eduReq.getInstitution())
                        .degree(eduReq.getDegree())
                        .fieldOfStudy(eduReq.getFieldOfStudy())
                        .startDate(eduReq.getStartDate())
                        .endDate(eduReq.getEndDate())
                        .gpa(eduReq.getGpa())
                        .description(eduReq.getDescription())
                        .displayOrder(eduReq.getDisplayOrder())
                        .resume(resume)
                        .build();
                resume.getEducations().add(edu);
            }
        }

        if (resumeRequest.getExperiences() != null) {
            for (var expReq : resumeRequest.getExperiences()) {
                ResumeExperience exp = ResumeExperience.builder()
                        .companyName(expReq.getCompanyName())
                        .position(expReq.getPosition())
                        .description(expReq.getDescription())
                        .startDate(expReq.getStartDate())
                        .endDate(expReq.getEndDate())
                        .isCurrent(expReq.getIsCurrent())
                        .displayOrder(expReq.getDisplayOrder())
                        .resume(resume)
                        .build();
                resume.getExperiences().add(exp);
            }
        }

        if (resumeRequest.getSkills() != null) {
            for (var skillReq : resumeRequest.getSkills()) {
                ResumeSkill skill = ResumeSkill.builder()
                        .skillName(skillReq.getName())
                        .proficiencyLevel(skillReq.getProficiencyLevel())
                        .yearsOfExperience(skillReq.getYearsOfExperience())
                        .resume(resume)
                        .build();
                resume.getSkills().add(skill);
            }
        }
        Resume saved = resumeRepository.save(resume);
        return ResumeBaseResponse.fromEntity(saved);
    }
    @Override
    public ResumeBaseResponse updateResume(ResumeRequest request, Long idResume) {
        Resume resume = resumeRepository.findById(idResume)
                .orElseThrow(() ->ResumeException.notFound("Resume not found"));

        resume.setTitle(request.getTitle());
        resume.setSummary(request.getSummary());
        resume.setFileUrl(request.getFileUrl());
        resume.setFileName(request.getFileName());
        resume.setFileType(request.getFileType());
        resume.setIsPrimary(request.getIsPrimary());
        resume.setIsPublic(request.getIsPublic());

        if (resume.getEducations() == null) resume.setEducations(new ArrayList<>()); else resume.getEducations().clear();
        if (resume.getExperiences() == null) resume.setExperiences(new ArrayList<>()); else resume.getExperiences().clear();
        if (resume.getSkills() == null) resume.setSkills(new ArrayList<>()); else resume.getSkills().clear();

        if (request.getEducations() != null) {
            boolean hasIds = request.getEducations().stream().anyMatch(e -> e.getId() != null);

            if (!hasIds) {
                resume.getEducations().clear();
                request.getEducations().forEach(e ->
                        resume.getEducations().add(ResumeEducation.builder()
                                .institution(e.getInstitution())
                                .degree(e.getDegree())
                                .fieldOfStudy(e.getFieldOfStudy())
                                .startDate(e.getStartDate())
                                .endDate(e.getEndDate())
                                .gpa(e.getGpa())
                                .description(e.getDescription())
                                .displayOrder(e.getDisplayOrder())
                                .resume(resume)
                                .build()));
            } else {
                List<Long> incomingIds = request.getEducations().stream()
                        .map(EducationRequest::getId)
                        .filter(Objects::nonNull)
                        .toList();

                resume.getEducations().removeIf(e -> !incomingIds.contains(e.getId()));

                for (EducationRequest e : request.getEducations()) {
                    if (e.getId() != null) {
                        ResumeEducation old = resume.getEducations().stream()
                                .filter(ed -> ed.getId().equals(e.getId()))
                                .findFirst().orElse(null);
                        if (old != null) {
                            old.setInstitution(e.getInstitution());
                            old.setDegree(e.getDegree());
                            old.setFieldOfStudy(e.getFieldOfStudy());
                            old.setStartDate(e.getStartDate());
                            old.setEndDate(e.getEndDate());
                            old.setGpa(e.getGpa());
                            old.setDescription(e.getDescription());
                            old.setDisplayOrder(e.getDisplayOrder());
                        }
                    } else {
                        resume.getEducations().add(ResumeEducation.builder()
                                .institution(e.getInstitution())
                                .degree(e.getDegree())
                                .fieldOfStudy(e.getFieldOfStudy())
                                .startDate(e.getStartDate())
                                .endDate(e.getEndDate())
                                .gpa(e.getGpa())
                                .description(e.getDescription())
                                .displayOrder(e.getDisplayOrder())
                                .resume(resume)
                                .build());
                    }
                }
            }
        }


        if (request.getExperiences() != null) {
            boolean hasIds = request.getExperiences().stream().anyMatch(e -> e.getId() != null);

            if (!hasIds) {
                resume.getExperiences().clear();
                request.getExperiences().forEach(e ->
                        resume.getExperiences().add(ResumeExperience.builder()
                                .companyName(e.getCompanyName())
                                .position(e.getPosition())
                                .description(e.getDescription())
                                .startDate(e.getStartDate())
                                .endDate(e.getEndDate())
                                .isCurrent(e.getIsCurrent())
                                .displayOrder(e.getDisplayOrder())
                                .resume(resume)
                                .build()));
            } else {
                List<Long> incomingIds = request.getExperiences().stream()
                        .map(ExperienceRequest::getId)
                        .filter(Objects::nonNull)
                        .toList();

                resume.getExperiences().removeIf(e -> !incomingIds.contains(e.getId()));

                for (ExperienceRequest e : request.getExperiences()) {
                    if (e.getId() != null) {
                        ResumeExperience old = resume.getExperiences().stream()
                                .filter(ex -> ex.getId().equals(e.getId()))
                                .findFirst().orElse(null);
                        if (old != null) {
                            old.setCompanyName(e.getCompanyName());
                            old.setPosition(e.getPosition());
                            old.setDescription(e.getDescription());
                            old.setStartDate(e.getStartDate());
                            old.setEndDate(e.getEndDate());
                            old.setIsCurrent(e.getIsCurrent());
                            old.setDisplayOrder(e.getDisplayOrder());
                        }
                    } else {
                        resume.getExperiences().add(ResumeExperience.builder()
                                .companyName(e.getCompanyName())
                                .position(e.getPosition())
                                .description(e.getDescription())
                                .startDate(e.getStartDate())
                                .endDate(e.getEndDate())
                                .isCurrent(e.getIsCurrent())
                                .displayOrder(e.getDisplayOrder())
                                .resume(resume)
                                .build());
                    }
                }
            }
        }

        

        if (request.getSkills() != null) {
            resume.getSkills().clear();
            for (var skillReq : request.getSkills()) {
                ResumeSkill skill = ResumeSkill.builder()
                        .skillName(skillReq.getName())
                        .proficiencyLevel(skillReq.getProficiencyLevel())
                        .yearsOfExperience(skillReq.getYearsOfExperience())
                        .resume(resume)
                        .build();
                resume.getSkills().add(skill);
            }
        }

        Resume updated = resumeRepository.save(resume);

        return ResumeBaseResponse.fromEntity(updated);
    }

    @Override
    public void deleteResume(Long idResume) {
        Resume resume = resumeRepository.findById(idResume)
                .orElseThrow(() -> ResumeException.notFound("Resume not found"));
        resumeRepository.delete(resume);
    }

    @Override
    public ResumeDetailResponse getResumeDetail(Long idResume) {
        Resume resume = resumeRepository.findById(idResume)
                .orElseThrow(() -> ResumeException.notFound("Resume not found"));
        return ResumeDetailResponse.fromEntity(resume);
    }

    @Override
    public void changePrimaryResume(Long idResume, Boolean isPrimary) {
        Resume resume = resumeRepository.findById(idResume)
                .orElseThrow(() -> ResumeException.notFound("Resume not found"));
        resume.setIsPrimary(isPrimary);
        resumeRepository.save(resume);
    }

    @Override
    public void changePublicResume(Long idResume, Boolean isPublic) {
        Resume resume = resumeRepository.findById(idResume)
                .orElseThrow(() -> ResumeException.notFound("Resume not found"));
        resume.setIsPublic(isPublic);
        resumeRepository.save(resume);
    }

    @Override
    public List<ResumeBaseResponse> getAllResumes(Boolean isPublic) {
        Long userId = SecurityUtils.currentUserId();
        return resumeRepository.getAllResumes(isPublic, userId);
    }

    @Override
    public UploadResultResponse uploadResume(MultipartFile resumeFile) {
        UploadResultResponse uploadResultResponse =
                fileUploadService.uploadSingle(resumeFile, UploadType.DOCUMENTS);

        Long senderId = SecurityUtils.currentUserId();
        User user = userRepository.findById(senderId).orElseThrow();

        String originalFileName = uploadResultResponse.getOriginalFilename();
        String title = originalFileName != null
                ? originalFileName.replaceFirst("[.][^.]+$", "")
                : "My Resume";

        Resume resume = Resume.builder()
                .fileName(originalFileName)
                .filePublicId(uploadResultResponse.getPublicId())
                .fileUrl(uploadResultResponse.getUrl())
                .title(title)
                .user(user)
                .isPrimary(false)
                .isPublic(false)
                .build();

        resumeRepository.save(resume);
        return uploadResultResponse;
    }

    @Override
    public ResumeBaseResponse uploadResumeAndCreate(MultipartFile resumeFile) {
        UploadResultResponse uploadResultResponse =
                fileUploadService.uploadSingle(resumeFile, UploadType.DOCUMENTS);

        Long senderId = SecurityUtils.currentUserId();
        User user = userRepository.findById(senderId).orElseThrow();

        String originalFileName = uploadResultResponse.getOriginalFilename();
        String title = originalFileName != null
                ? originalFileName.replaceFirst("[.][^.]+$", "")
                : "My Resume";

        Resume resume = Resume.builder()
                .fileName(originalFileName)
                .filePublicId(uploadResultResponse.getPublicId())
                .fileUrl(uploadResultResponse.getUrl())
                .title(title)
                .user(user)
                .isPrimary(false)
                .isPublic(false)
                .build();

        Resume saved = resumeRepository.save(resume);
        return ResumeBaseResponse.fromEntity(saved);
    }


}
