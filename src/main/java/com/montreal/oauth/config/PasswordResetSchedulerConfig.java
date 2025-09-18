package com.montreal.oauth.config;

import com.montreal.oauth.domain.service.IPasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.password-reset.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class PasswordResetSchedulerConfig {

    private final IPasswordResetService passwordResetService;
    
    @Scheduled(cron = "${app.password-reset.scheduler.cleanup-cron:0 0 2 * * ?}")
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired password reset tokens");

        try {
            passwordResetService.cleanupExpiredTokens();
            log.info("Scheduled cleanup of expired password reset tokens completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled cleanup of expired tokens", e);
        }
    }
}
