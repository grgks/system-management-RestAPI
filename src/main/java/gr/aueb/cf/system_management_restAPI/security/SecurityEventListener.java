package gr.aueb.cf.system_management_restAPI.security;

import gr.aueb.cf.system_management_restAPI.service.SecurityAuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityEventListener {

    private final SecurityAuditService securityAuditService;

    /**
     * Listens for successful authentication events
     * For: Auto-log when user logs in successfully
     */
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        try {
            String username = event.getAuthentication().getName();
            HttpServletRequest request = getCurrentRequest();

            if (request != null) {
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");

                securityAuditService.logSuccessfulLogin(username, ipAddress, userAgent);

                log.debug("Logged successful authentication for user: {}", username);
            }
        } catch (Exception e) {
            log.error("Failed to log authentication success event", e);
        }
    }

    /**
     * Listens for failed authentication events (bad credentials)
     * For: Auto-log when user enters wrong password
     */
    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        try {
            String username = event.getAuthentication().getName();
            HttpServletRequest request = getCurrentRequest();

            if (request != null) {
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");

                securityAuditService.logFailedLogin(
                        username,
                        ipAddress,
                        userAgent,
                        "Bad credentials"
                );

                log.debug("Logged failed authentication for user: {}", username);
            }
        } catch (Exception e) {
            log.error("Failed to log authentication failure event", e);
        }
    }

    /**
     * Helper: Get current HTTP request
     * For: Need request to get IP and User-Agent
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.warn("Could not get current request", e);
            return null;
        }
    }

    /**
     * Helper: Extract client IP address (handles proxies)
     * For: Real IP might be behind proxy/load balancer
     */

    //To do is duplicate.added to utils
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Handle multiple IPs (proxy chain)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "unknown";
    }
}
