package gr.aueb.cf.system_management_restAPI.rest;

import gr.aueb.cf.system_management_restAPI.dto.SecurityEventDTO;
import gr.aueb.cf.system_management_restAPI.dto.SecurityMetricsDTO;
import gr.aueb.cf.system_management_restAPI.mapper.Mapper;
import gr.aueb.cf.system_management_restAPI.model.SecurityAuditLog;
import gr.aueb.cf.system_management_restAPI.service.SecurityAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/security")
@RequiredArgsConstructor
@Tag(name = "Security Dashboard", description = "Security monitoring and audit APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class SecurityDashboardController {

    private final SecurityAuditService securityAuditService;
    private final Mapper mapper;

    @Operation(
            summary = "Get all events",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Events Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SecurityEventDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access Denied",
                            content = @Content
                    )
            }
    )
    @GetMapping("/events")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')") //(Method-Level Security)Lvl 2 - Defense in Depth.
    // hasAuthority(Role_Super_Admin) at SecurityConfig = Lvl 1 layer

    public ResponseEntity<List<SecurityEventDTO>> getRecentEvents(
            @RequestParam(defaultValue = "50") int limit) {

        List<SecurityAuditLog> events = securityAuditService.getRecentEvents(limit);
        List<SecurityEventDTO> dtos = events.stream()
                .map(mapper::mapToSecurityEventDTO)
                .collect(Collectors.toList());

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @Operation(
            summary = "Get metrics",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Metrics Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SecurityMetricsDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access Denied",
                            content = @Content
                    )
            }
    )
    @GetMapping("/metrics")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<SecurityMetricsDTO> getMetrics() {
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        List<SecurityAuditLog> allEvents = securityAuditService.getRecentEvents(1000);

        long failedLogins = securityAuditService.getFailedLoginCount(24);
        long successfulLogins = countByType(allEvents, "LOGIN_SUCCESS", true, last24h);
        long tokenErrors = countByType(allEvents, "TOKEN_EXPIRED", false, last24h) +
                countByType(allEvents, "TOKEN_INVALID", false, last24h);
        long authFailures = countByType(allEvents, "AUTHORIZATION_FAILED", false, last24h);

        double totalLogins = failedLogins + successfulLogins;
        double successRate = totalLogins > 0 ? (successfulLogins / totalLogins) * 100 : 100.0;

        SecurityMetricsDTO metrics = SecurityMetricsDTO.builder()
                .totalEvents((long) allEvents.size())
                .failedLoginsLast24h(failedLogins)
                .successfulLoginsLast24h(successfulLogins)
                .tokenErrorsLast24h(tokenErrors)
                .authorizationFailuresLast24h(authFailures)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .suspiciousIPs(findSuspiciousIPs(allEvents))
                .recentFailures(getRecentFailures(allEvents))
                .build();

        return new ResponseEntity<>(metrics, HttpStatus.OK);
    }

    private long countByType(List<SecurityAuditLog> events, String type, boolean success, LocalDateTime since) {
        return events.stream()
                .filter(log -> log.getEventType().equals(type))
                .filter(log -> log.getSuccess() == success)
                .filter(log -> log.getTimestamp().isAfter(since))
                .count();
    }

    private List<String> findSuspiciousIPs(List<SecurityAuditLog> events) {
        return events.stream()
                .filter(log -> !log.getSuccess())
                .collect(Collectors.groupingBy(SecurityAuditLog::getIpAddress, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() >= 5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getRecentFailures(List<SecurityAuditLog> events) {
        return events.stream()
                .filter(log -> !log.getSuccess())
                .limit(10)
                .map(log -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("username", log.getUsername() != null ? log.getUsername() : "unknown");
                    map.put("ipAddress", log.getIpAddress() != null ? log.getIpAddress() : "unknown");
                    map.put("eventType", log.getEventType());
                    map.put("timestamp", log.getTimestamp().toString());
                    return map;
                })
                .collect(Collectors.toList());
    }
}