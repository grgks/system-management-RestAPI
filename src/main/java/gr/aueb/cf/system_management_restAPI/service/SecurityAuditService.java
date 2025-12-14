package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.model.SecurityAuditLog;
import gr.aueb.cf.system_management_restAPI.repository.SecurityAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditService {

    private final SecurityAuditLogRepository auditLogRepository;

    /**
     * Log a security event asynchronously
     *  @Async: We do not want to  slow down το authentication request!
     */
    @Async
    @Transactional
    public void logSecurityEvent(
            String eventType,
            String username,
            String ipAddress,
            String userAgent,
            boolean success,
            Map<String, Object> details
    ) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .eventType(eventType)
                    .username(username)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .success(success)
                    .details(details != null ? details : new HashMap<>())
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

            log.debug("Security event logged: {} - {} - {}",
                    eventType, username, success ? "SUCCESS" : "FAILED");

        } catch (Exception e) {
            // CRITICAL: Logging failure should NEVER break the app!
            log.error("Failed to log security event: {}", eventType, e);
        }
    }

    /**
     * Convenience method - Failed login
     */
    @Async
    public void logFailedLogin(String username, String ipAddress, String userAgent, String reason) {
        Map<String, Object> details = new HashMap<>();
        details.put("reason", reason);

        logSecurityEvent("LOGIN_FAILED", username, ipAddress, userAgent, false, details);
    }

    /**
     * Convenience method - Successful login
     */
    @Async
    public void logSuccessfulLogin(String username, String ipAddress, String userAgent) {
        logSecurityEvent("LOGIN_SUCCESS", username, ipAddress, userAgent, true, null);
    }

    /**
     * Convenience method - Token expired
     */
    @Async
    public void logTokenExpired(String username, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("reason", "JWT token expired");

        logSecurityEvent("TOKEN_EXPIRED", username, ipAddress, null, false, details);
    }

    /**
     * Convenience method - Invalid token
     */
    @Async
    public void logInvalidToken(String ipAddress, String reason) {
        Map<String, Object> details = new HashMap<>();
        details.put("reason", reason);

        logSecurityEvent("TOKEN_INVALID", "unknown", ipAddress, null, false, details);
    }

    /**
     * Convenience method - Authorization failure (403)
     */
    @Async
    public void logAuthorizationFailure(String username, String ipAddress, String endpoint, String requiredRole) {
        Map<String, Object> details = new HashMap<>();
        details.put("endpoint", endpoint);
        details.put("requiredRole", requiredRole);

        logSecurityEvent("AUTHORIZATION_FAILED", username, ipAddress, null, false, details);
    }

    //  Query Methods (for Dashboard)

    /**
     * Get recent security events
     */
    public List<SecurityAuditLog> getRecentEvents(int limit) {
        return auditLogRepository.findRecentEvents(limit);
    }

    /**
     * Get failed login attempts in last N hours
     */
    public long getFailedLoginCount(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepository.countFailedAttemptsSince("LOGIN_FAILED", since);
    }

    /**
     * Get all failed events for specific username
     */
    public List<SecurityAuditLog> getFailedAttemptsByUsername(String username) {
        return auditLogRepository.findByUsername(username)
                .stream()
                .filter(auditLog -> !auditLog.getSuccess())
                .toList();
    }

    /**
     * Get all failed events from specific IP
     */
    public List<SecurityAuditLog> getFailedAttemptsByIp(String ipAddress) {
        return auditLogRepository.findFailedAttemptsByIp(ipAddress);
    }

    /**
     * Check if IP has suspicious activity (brute force detection)
     * For: Detect attacks!
     */
    public boolean isSuspiciousActivity(String ipAddress, int threshold, int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);

        List<SecurityAuditLog> recentFailures = auditLogRepository
                .findByTimestampAfter(since)
                .stream()
                .filter(auditLog -> ipAddress.equals(auditLog.getIpAddress()) && !auditLog.getSuccess())
                .toList();

        return recentFailures.size() >= threshold;
    }
}