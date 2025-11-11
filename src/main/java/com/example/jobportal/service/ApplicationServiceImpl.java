package com.example.jobportal.service;

import com.example.jobportal.dto.request.ApplicationRequest;
import com.example.jobportal.dto.response.ApplicationResponse;
import com.example.jobportal.dto.response.ApplicationStatusHistoryResponse;
import com.example.jobportal.exception.ApplicationException;
import com.example.jobportal.exception.JobException;
import com.example.jobportal.exception.ResumeException;
import com.example.jobportal.exception.UserException;
import com.example.jobportal.model.entity.*;
import com.example.jobportal.model.enums.ApplicationStatus;
import com.example.jobportal.model.enums.NotificationType;
import com.example.jobportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ApplicationResponse applyForJob(Long userId, ApplicationRequest applicationRequest) {
        if (applicationRepository.existsByUserAndJob(userId, applicationRequest.getJobId())) {
            throw new IllegalStateException("You have already applied for this job.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound("User not found"));
        Job job = jobRepository.findById(applicationRequest.getJobId())
                .orElseThrow(() -> JobException.notFound("Job not found"));
        Resume resume = resumeRepository.findById(applicationRequest.getResumeId())
                .orElseThrow(() -> ResumeException.notFound("Resume not found"));

        Application app = new Application();
        app.setUser(user);
        app.setJob(job);
        app.setResume(resume);
        app.setCoverLetter(applicationRequest.getCoverLetter());
        app.setStatus(ApplicationStatus.PENDING);
        app.setAppliedAt(LocalDateTime.now());

        Application saved = applicationRepository.save(app);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(saved);
        history.setOldStatus(null);
        history.setNewStatus(ApplicationStatus.PENDING.name());
        history.setNotes("Application created");
        history.setChangedBy(user);
        historyRepository.save(history);
        User hr = job.getCreatedBy();
        notificationService.createNotification(
                hr.getId(),
                "Có ứng viên mới ứng tuyển",
                user.getFullName() + " vừa ứng tuyển vào vị trí " + job.getTitle(),
                NotificationType.APPLICATION_SUBMITTED.name(),
                saved.getId(),
                "APPLICATION"
        );
        return ApplicationResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void updateStatus(Long applicationId, String newStatus, String notes, Long reviewerId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> ApplicationException.notFound("Application not found"));

        ApplicationStatus status = ApplicationStatus.fromString(newStatus);
        ApplicationStatus oldStatus = app.getStatus();
        app.setStatus(status);
        app.setReviewedAt(LocalDateTime.now());
        app.setReviewedBy(reviewerId != null
                ? userRepository.findById(reviewerId).orElse(null)
                : null);
        applicationRepository.save(app);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(app);
        history.setOldStatus(oldStatus.name());
        history.setNewStatus(status.name());
        history.setNotes(notes);
        history.setChangedBy(app.getReviewedBy());
        historyRepository.save(history);
        String msg = switch (status) {
            case ACCEPTED -> "Hồ sơ của bạn cho vị trí " + app.getJob().getTitle() + " đã được duyệt!";
            case REJECTED -> "Rất tiếc, hồ sơ của bạn cho vị trí " + app.getJob().getTitle() + " đã bị từ chối.";
            case HIRED -> "Ban chúc mừng! Bạn đã được nhận vào vị trí " + app.getJob().getTitle() + ".";
            case WITHDRAWN -> "Bạn đã rút hồ sơ ứng tuyển cho vị trí " + app.getJob().getTitle() + ".";
            default -> "Trạng thái hồ sơ của bạn đã được cập nhật.";
        };

        notificationService.createNotification(
                app.getUser().getId(),
                "Cập nhật trạng thái hồ sơ",
                msg,
                NotificationType.APPLICATION_STATUS_UPDATE.name(),
                app.getId(),
                "APPLICATION"
        );
    }

    @Override
    public List<ApplicationResponse> getApplicationsByUser(Long userId) {
        return applicationRepository.findByUserId(userId)
                .stream()
                .map(ApplicationResponse::fromEntity)
                .toList();
    }

    @Override
    public List<ApplicationResponse> getApplicationsByJob(Long jobId) {
        return applicationRepository.findByJobId(jobId)
                .stream()
                .map(ApplicationResponse::fromEntity)
                .toList();
    }

    @Override
    public List<ApplicationStatusHistoryResponse> getStatusHistory(Long applicationId) {
        List<ApplicationStatusHistory> historyList =
                historyRepository.findByApplicationIdOrderByChangedAtDesc(applicationId);

        return historyList.stream()
                .map(ApplicationStatusHistoryResponse::fromEntity)
                .toList();
    }
}
