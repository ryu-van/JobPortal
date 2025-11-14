package com.example.jobportal.scheduler;

import com.example.jobportal.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvitationCleanupScheduler {

    private final CompanyService companyService;

    @Scheduled(cron = "0 0 * * * *")
    public void deactivateExpiredInvitations() {
        log.info("🔄 Starting scheduled deactivation of expired invitations");

        try {
            int deactivatedCount = companyService.deactivateExpiredInvitations();

            if (deactivatedCount > 0) {
                log.info("✅ Successfully deactivated {} expired invitations", deactivatedCount);
            } else {
                log.debug("No expired invitations to deactivate");
            }
        } catch (Exception e) {
            log.error("❌ Error during scheduled invitation deactivation", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldExpiredInvitations() {
        log.info("🗑️ Starting scheduled cleanup of old expired invitations");

        try {
            int deletedCount = companyService.cleanupExpiredInvitations();

            if (deletedCount > 0) {
                log.info("✅ Successfully deleted {} old expired invitations", deletedCount);
            } else {
                log.debug("No old expired invitations to cleanup");
            }
        } catch (Exception e) {
            log.error("❌ Error during scheduled invitation cleanup", e);
        }
    }
}
