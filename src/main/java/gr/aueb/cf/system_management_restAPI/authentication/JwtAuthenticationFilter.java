package gr.aueb.cf.system_management_restAPI.authentication;

import gr.aueb.cf.system_management_restAPI.security.JwtService;
import gr.aueb.cf.system_management_restAPI.service.SecurityAuditService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final SecurityAuditService securityAuditService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
//        LOGGER.info("JWT FILTER HIT - path: {}", request.getRequestURI());
//        LOGGER.info("Processing request path: {}", path);

        // Skip Swagger-related paths
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.equals("/swagger-ui.html")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String jwt;
        String username;
        String userRole = null;

        //process token if present, but don't require it
        if (path.equals("/api/clients/save")) {
//            LOGGER.info("Authorization header = {}", request.getHeader("Authorization"));
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                try {
                    username = jwtService.extractSubject(jwt);
                    userRole = jwtService.getStringClaim(jwt, "role");
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        if (jwtService.isTokenValid(jwt, userDetails)) {
                            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                            if (userRole != null) {
                                authorities.add(new SimpleGrantedAuthority(userRole));
                            }
                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    authorities
                            );
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
//                            LOGGER.info("AUTH SET: {}", SecurityContextHolder.getContext().getAuthentication());
//                            LOGGER.info("Authentication set for client/save with role: {}", userDetails.getAuthorities());
                          }
                    }
                } catch (Exception e) {
                    LOGGER.warn("Invalid token for /api/clients/save, proceeding without auth: {}", e.getMessage());
                }
            }
            filterChain.doFilter(request, response);
            return;
        }
// token extraction & validation
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.info("Authorization header missing or invalid");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"code\": \"userNotAuthenticated\", \"description\": \"User must authenticate in order to access this endpoint\"}");
          //  filterChain.doFilter(request, response);// ← Never reached(prevent unauthorize access,performance)
            return; // <- Stop here
        }
        jwt = authHeader.substring(7);

        try {
            username = jwtService.extractSubject(jwt);
            userRole = jwtService.getStringClaim(jwt, "role");


//            LOGGER.info("DEBUG: JWT subject = {}", username);
//            LOGGER.info("DEBUG: JWT role claim = {}", userRole);


            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (userRole != null) {
                        String authority = userRole.startsWith("ROLE_") ? userRole : "ROLE_" + userRole;
                        authorities.add(new SimpleGrantedAuthority(authority));
                    }


                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    LOGGER.warn("Token is not valid for request: {}", request.getRequestURI());
                }
            }
        } catch (ExpiredJwtException e) {
            LOGGER.warn("WARN: Expired token", e);
            try {
                String expiredTokenUsername = e.getClaims().getSubject();  // Get username from expired token
                String ipAddress = getClientIpAddress(request);
                securityAuditService.logTokenExpired(expiredTokenUsername, ipAddress);
            } catch (Exception logEx) {
                LOGGER.error("Failed to log expired token event", logEx);
            }

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");

            //to do change from json to object serialization <-----

            String jsonBody = "{\"code\": \"expired_token\", \"message\": \"" + e.getMessage() + "\"}";
            response.getWriter().write(jsonBody);
            return;
        } catch (Exception e) {
            LOGGER.warn("WARN: Something went wrong while parsing JWT", e);

            try {
                String ipAddress = getClientIpAddress(request);
                String reason = e.getClass().getSimpleName() + ": " + e.getMessage();
                // ↑ Examples:
//   "MalformedJwtException: JWT strings must contain exactly 2 period characters"
//   "SignatureException: JWT signature does not match"
//   "IllegalArgumentException: JWT String argument cannot be null or empty"

// Helps  understand WHAT kind of attack:
// - Tampered token? → SignatureException
// - Garbage data?   → MalformedJwtException
// - Missing token?  → IllegalArgumentException

                securityAuditService.logInvalidToken(ipAddress, reason);
            } catch (Exception logEx) {
                LOGGER.error("Failed to log invalid token event", logEx);
            }

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");

            //to do change from json to object serialization <-----

            String jsonBody = "{\"code\": \"invalid_token\", \"description\": \"" + e.getMessage() + "\"}";
            response.getWriter().write(jsonBody);
            return;
        }
        filterChain.doFilter(request, response);
    }

    //to do move the getClientIpAddress method to Utils

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