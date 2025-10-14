package gr.aueb.cf.system_management_restAPI.model;

import gr.aueb.cf.system_management_restAPI.core.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    /**
     * Tests the default (no-args) constructor of User
     * and verifies that setters correctly assign values.
     */

    @Test
    void defaultConstructor() {
        //create client
        Client client = new Client();
        client.setId(10L);
        client.setUuid("client-uuid-123");

        User user = new User();
        user.setId(11L);
        user.setUuid("user-uuid-123");
        user.setUsername("User");
        user.setPassword("Password123!");
        user.setEmail("user@aueb.gr");
        user.setRole(Role.CLIENT);
        user.setIsActive(true);
        user.setClient(client);

        assertNotNull(user);
        assertEquals(11L, user.getId());
        assertEquals("user-uuid-123", user.getUuid());
        assertEquals("User", user.getUsername());
        assertEquals("Password123!", user.getPassword());
        assertEquals("user@aueb.gr", user.getEmail());
        assertEquals(Role.CLIENT, user.getRole());
        assertEquals(true, user.getIsActive());
        assertEquals(client, user.getClient());
    }

    /**
     * Tests the all-args constructor of User
     * to ensure all fields are properly initialized.
     */

        @Test
        void allArgsConstructor() {
            //create client
            Client client = new Client();
            client.setId(10L);
            client.setUuid("client-uuid-123");

            // user
            User user = new User(
                    1L,
                    "user-uuid-123",
                    "user",
                    "Password123!",
                    "test@aueb.gr",
                    Role.SUPER_ADMIN,
                    true,
                    client
            );

            // Assert
            assertNotNull(user);
            assertEquals(1L, user.getId());
            assertEquals("user-uuid-123", user.getUuid());
            assertEquals("user", user.getUsername());
            assertEquals("Password123!", user.getPassword());
            assertEquals("test@aueb.gr", user.getEmail());
            assertEquals(Role.SUPER_ADMIN, user.getRole());
            assertTrue(user.getIsActive());
            assertEquals(client, user.getClient());
    }

    // ==================================
    // USER DETAILS INTERFACE TESTS
    // ==================================


    /**
     * Verifies that getAuthorities() correctly returns a collection
     * containing a SimpleGrantedAuthority matching the user's role.
     */

    @Test
    void getAuthorities_ShouldReturnRoleCorrectly() {
        User user = new User();
        user.setRole(Role.SUPER_ADMIN);

        Collection<?> authorities = user.getAuthorities();

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.iterator().next() instanceof SimpleGrantedAuthority);
        assertEquals("SUPER_ADMIN", authorities.iterator().next().toString());
    }

    /**
     * Ensures that isEnabled() returns true when the user is active.
     */

    @Test
    void isEnabled_ShouldReturnTrue_WhenActive() {
        User user = new User();
        user.setIsActive(true);

        assertTrue(user.isEnabled());
    }

    /**
     * Ensures that isEnabled() returns false when the user is inactive.
     */

    @Test
    void isEnabled_ShouldReturnFalse_WhenInactive() {
        User user = new User();
        user.setIsActive(false);

        assertFalse(user.isEnabled());
    }

    /**
     * Ensures that account status methods always return true
     * since they are not dynamically controlled in this implementation.
     */

    @Test
    void accountStatusMethods_ShouldAlwaysReturnTrue() {
        User user = new User();

        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
    }

    /**
     * Ensures that isEnabled() returns false when the active flag is null.
     */

    @Test
    void isEnabled_ShouldReturnFalse_WhenActiveIsNull() {
        User user = new User();
        user.setIsActive(null);

        assertFalse(user.isEnabled());
    }

}





