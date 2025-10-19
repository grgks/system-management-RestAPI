package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotAuthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityServiceTest {

    @Autowired
    private SecurityService securityService;

    /**
     * Should return true when user is super admin.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void isCurrentUserSuperAdmin_ShouldReturnTrue_WhenSuperAdmin() {
        // given - authentication context set by @WithMockUser

        // act & assert
        assertTrue(securityService.isCurrentUserSuperAdmin());
    }

    /**
     * Should return false when user is not super admin.
     */
    @Test
    @WithMockUser(username = "user", roles = {"CLIENT"})
    void isCurrentUserSuperAdmin_ShouldReturnFalse_WhenRegularUser() {
        // given - authentication context set by @WithMockUser

        // act & assert
        assertFalse(securityService.isCurrentUserSuperAdmin());
    }

    /**
     * Should return false when user is anonymous.
     */
    @Test
    @WithAnonymousUser  // Spring Security annotation when not authenticated user
    void isCurrentUserSuperAdmin_ShouldReturnFalse_WhenAnonymous() {
        // given - anonymous authentication set by @WithAnonymousUser

        // act & assert
        assertFalse(securityService.isCurrentUserSuperAdmin());
    }

    /**
     * Should return username when authenticated.
     */
    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUsername_ShouldReturnUsername_WhenAuthenticated() {
        // given - authentication context set by @WithMockUser

        // act
        String username = securityService.getCurrentUsername();

        // assert
        assertEquals("testuser", username);
    }

    /**
     * Should return null when not authenticated.
     */
    @Test
    void getCurrentUsername_ShouldReturnNull_WhenNotAuthenticated() {
        // given - no authentication context

       // act & assert
        assertNull(securityService.getCurrentUsername());
    }

    /**
     * Should allow access when super admin.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void validateUserAccess_ShouldAllow_WhenSuperAdmin() {     //checks that super admin can access any entity
        // given
        String entityOwnerUsername = "otheruser";
        String entityType = "Client";
        String entityId = "123";

        // act & assert
        assertDoesNotThrow(() ->
                securityService.validateUserAccess(entityOwnerUsername, entityType, entityId)
        );
    }

    /**
     * Should allow access when user is owner.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void validateUserAccess_ShouldAllow_WhenUserIsOwner() {   //checks that users can not access any entity
        // given
        String entityOwnerUsername = "testuser"; // Same as authenticated user
        String entityType = "Client";
        String entityId = "123";

        // act & assert
        assertDoesNotThrow(() ->
                securityService.validateUserAccess(entityOwnerUsername, entityType, entityId)
        );
    }

    /**
     * Should deny access when user is not owner.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void validateUserAccess_ShouldDeny_WhenUserIsNotOwner() {
        // given
        String entityOwnerUsername = "otheruser"; // Different from authenticated user
        String entityType = "Client";
        String entityId = "123";

        // act & assert
        AppObjectNotAuthorizedException exception = assertThrows(
                AppObjectNotAuthorizedException.class,
                () -> securityService.validateUserAccess(entityOwnerUsername, entityType, entityId)
        );

        assertTrue(exception.getMessage().contains("You don't have permission")); //message from security service/validateUserAccess
        assertTrue(exception.getMessage().contains(entityId));

//        // act
//        AppObjectNotAuthorizedException exception = null;
//        try {
//            securityService.validateUserAccess(entityOwnerUsername, entityType, entityId);
//            fail("Should have thrown AppObjectNotAuthorizedException");
//        } catch (AppObjectNotAuthorizedException e) {
//            exception = e;
//        }

    }

    /**
     * Should deny access when not authenticated.
     */
    @Test
    void validateUserAccess_ShouldDeny_WhenNotAuthenticated() {
        // given
        String entityOwnerUsername = "someuser";
        String entityType = "Client";
        String entityId = "123";

        // act & assert
        assertThrows(
                AppObjectNotAuthorizedException.class,
                () -> securityService.validateUserAccess(entityOwnerUsername, entityType, entityId)
        );
//        AppObjectNotAuthorizedException exception = null;
//        try {
//            securityService.validateUserAccess(entityOwnerUsername, entityType, entityId);
//            fail("Should have thrown AppObjectNotAuthorizedException");
//        } catch (AppObjectNotAuthorizedException e) {
//            exception = e;
//        }
    }
}