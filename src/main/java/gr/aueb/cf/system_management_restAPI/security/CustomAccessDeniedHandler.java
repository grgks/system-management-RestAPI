package gr.aueb.cf.system_management_restAPI.security;


import gr.aueb.cf.system_management_restAPI.service.SecurityAuditService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityAuditService securityAuditService;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "anonymous";
            String ipAddress = getClientIpAddress(request);
            String endpoint = request.getRequestURI();
            String requiredRole = "unknown";  // no easy access to required role

            securityAuditService.logAuthorizationFailure(username, ipAddress, endpoint, requiredRole);
        } catch (Exception e) {
            // Don't let logging errors affect the response
        }

        // Set the response status and content type
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        //  custom JSON response with the collected information
        String json = "{\"code\": \"userNotAuthorized\", " +
                "\"description\": \"User is not allowed to access this endpoint.\"}";
        response.getWriter().write(json);
    }

    /**
     * Helper: Extract client IP address (handles proxies)
     */
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
