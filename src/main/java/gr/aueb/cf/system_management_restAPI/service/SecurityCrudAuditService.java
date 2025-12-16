package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.model.SecurityAuditLog;
import gr.aueb.cf.system_management_restAPI.repository.SecurityAuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service για CRUD operations auditing
 * Separation of Concerns: Authentication events → SecurityAuditService
 *                        CRUD events → SecurityCrudAuditService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityCrudAuditService {

    private final SecurityAuditLogRepository auditLogRepository;


    private void logCrudEvent(String eventType,
                              String username,
                              String ipAddress,
                              String userAgent,   //user agent gia front
                              Map<String, Object> details
    ) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .eventType(eventType)
                    .username(username)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .success(true)   // CRUD events successful (otherwise exception)
                    .details(details != null ? details : new HashMap<>())
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

            log.debug("CRUD event logged: {} by {} - {}", eventType, username, details);

        } catch (Exception e) {
            log.error("Failed to log CRUD event: {}", eventType, e);
        }
    }


// USER CRUD OPERATIONS

    public void logUserCreated(String adminUsername, String ipAddress,
                               HttpServletRequest request, Long userId, String newUsername) {
        Map<String, Object> details = new HashMap<>();
        details.put("userId", userId);
        details.put("newUsername", newUsername);
        details.put("action", "User created by admin");

        String userAgent = extractUserAgent(request);
        logCrudEvent("USER_CREATED", adminUsername, ipAddress, userAgent, details);
    }


    public void logUserUpdated(String adminUsername, String ipAddress, HttpServletRequest request,
                               Long userId, String targetUsername, String whatChanged) {
        Map<String, Object> details = new HashMap<>();
        details.put("userId", userId);
        details.put("targetUsername", targetUsername);
        details.put("changes", whatChanged);

        String userAgent = extractUserAgent(request);
        logCrudEvent("USER_UPDATED", adminUsername, ipAddress, userAgent, details);
    }


    public void logUserDeleted(String adminUsername, String ipAddress, HttpServletRequest request,
                               Long userId, String deletedUsername) {
        Map<String, Object> details = new HashMap<>();
        details.put("userId", userId);
        details.put("deletedUsername", deletedUsername);
        details.put("action", "User deleted by admin");

        String userAgent = extractUserAgent(request);
        logCrudEvent("USER_DELETED", adminUsername, ipAddress, userAgent, details);
    }


// CLIENT CRUD OPERATIONS

    public void logClientCreated(String username, String ipAddress, HttpServletRequest request,
                                 Long clientId, String clientName) {
        Map<String, Object> details = new HashMap<>();
        details.put("clientId", clientId);
        details.put("clientName", clientName);
        details.put("action", "New client registered");

        String userAgent = extractUserAgent(request);
        logCrudEvent("CLIENT_CREATED", username, ipAddress, userAgent, details);
    }


    public void logClientUpdated(String username, String ipAddress, HttpServletRequest request,
                                 Long clientId, String clientName) {
        Map<String, Object> details = new HashMap<>();
        details.put("clientId", clientId);
        details.put("clientName", clientName);
        details.put("action", "Client information updated");

        String userAgent = extractUserAgent(request);
        logCrudEvent("CLIENT_UPDATED", username, ipAddress, userAgent, details);
    }


    public void logClientDeleted(String username, String ipAddress, HttpServletRequest request,
                                 Long clientId, String clientName) {
        Map<String, Object> details = new HashMap<>();
        details.put("clientId", clientId);
        details.put("clientName", clientName);
        details.put("action", "Client deleted");

        String userAgent = extractUserAgent(request);
        logCrudEvent("CLIENT_DELETED", username, ipAddress, userAgent, details);
    }


// APPOINTMENT CRUD OPERATIONS

    public void logAppointmentCreated(String username, String ipAddress, HttpServletRequest request,
                                      Long appointmentId, String clientName) {
        Map<String, Object> details = new HashMap<>();
        details.put("appointmentId", appointmentId);
        details.put("clientName", clientName);
        details.put("action", "New appointment created");

        String userAgent = extractUserAgent(request);
        logCrudEvent("APPOINTMENT_CREATED", username, ipAddress, userAgent, details);
    }


    public void logAppointmentUpdated(String username, String ipAddress, HttpServletRequest request,
                                      Long appointmentId, String clientName, String whatChanged) {
        Map<String, Object> details = new HashMap<>();
        details.put("appointmentId", appointmentId);
        details.put("clientName", clientName);
        details.put("changes", whatChanged);

        String userAgent = extractUserAgent(request);
        logCrudEvent("APPOINTMENT_UPDATED", username, ipAddress, userAgent, details);
    }


    public void logAppointmentDeleted(String username, String ipAddress, HttpServletRequest request,
                                      Long appointmentId, String clientName, String reason) {
        Map<String, Object> details = new HashMap<>();
        details.put("appointmentId", appointmentId);
        details.put("clientName", clientName);
        details.put("reason", reason != null ? reason : "No reason provided");

        String userAgent = extractUserAgent(request);
        logCrudEvent("APPOINTMENT_DELETED", username, ipAddress, userAgent, details);
    }

    /**
     * Extract User Agent from HttpServletRequest
     */
    private String extractUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader("User-Agent");
    }
}