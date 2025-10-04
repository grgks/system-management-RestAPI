package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.enums.Role;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotAuthorizedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    public boolean isCurrentUserSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(Role.SUPER_ADMIN.getAuthority()));
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    public void validateUserAccess(String entityOwnerUsername, String entityType, String entityId)
            throws AppObjectNotAuthorizedException {
        if (!isCurrentUserSuperAdmin()) {
            String currentUsername = getCurrentUsername();
            if (currentUsername == null || !entityOwnerUsername.equals(currentUsername)) {
                throw new AppObjectNotAuthorizedException(entityType,
                        "You don't have permission to access " + entityType + " with id: " + entityId);
            }
        }
    }
}