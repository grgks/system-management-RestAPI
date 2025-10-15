package gr.aueb.cf.system_management_restAPI.repository;

import gr.aueb.cf.system_management_restAPI.core.enums.Role;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.util.TestDBHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest                                                                   // load only JPA components
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)   // use  MySQL (not h2)
@TestPropertySource(locations = "classpath:application-test.properties")       // read test properties
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataSource dataSource;     // Inject DataSource from Spring

    @BeforeEach
    void setUp() throws SQLException {
        // clean DB
        TestDBHelper.eraseData(dataSource);
    }

    /**
     * Test basic save operation
     */
    @Test
    void testSaveUser() {
        // given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@aueb.gr");
        user.setPassword("password123");
        user.setUuid(UUID.randomUUID().toString());
        user.setRole(Role.CLIENT);
        user.setIsActive(true);

        // act
        User savedUser = userRepository.save(user);

        // assert
        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@aueb.gr", savedUser.getEmail());
    }

    /**
     * Test findByUsername - success case
     */
    @Test
    void testFindByUsername_whenExists() {
        // given
        User user = new User();
        user.setUsername("johndoe");
        user.setEmail("john@aueb.gr");
        user.setPassword("pass123");
        user.setUuid(UUID.randomUUID().toString());
        user.setRole(Role.CLIENT);
        user.setIsActive(true);
        userRepository.save(user);

        // act
        Optional<User> found = userRepository.findByUsername("johndoe");

        // assert
        assertTrue(found.isPresent());
        assertEquals("johndoe", found.get().getUsername());
        assertEquals("john@aueb.gr", found.get().getEmail());
    }

    /**
     * Test findByUsername - not found case
     */
    @Test
    void testFindByUsername_whenNotExists() {
        // act
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // assert
        assertFalse(found.isPresent());
    }

    /**
     * Test findByEmail - success case
     */
    @Test
    void testFindByEmail_whenExists() {
        // given
        User user = new User();
        user.setUsername("maria");
        user.setEmail("maria@aueb.gr");
        user.setPassword("pass456");
        user.setUuid(UUID.randomUUID().toString());
        user.setRole(Role.CLIENT);
        user.setIsActive(true);
        userRepository.save(user);

        // act
        Optional<User> found = userRepository.findByEmail("maria@aueb.gr");

        // assert
        assertTrue(found.isPresent());
        assertEquals("maria", found.get().getUsername());
        assertEquals(Role.CLIENT, found.get().getRole());
    }

    /**
     * Test findByEmail - not found case
     */
    @Test
    void testFindByEmail_whenNotExists() {
        // act
        Optional<User> found = userRepository.findByEmail("notfound@aueb.gr");

        // assert
        assertFalse(found.isPresent());
    }

    /**
     * Test findByUuid - success case
     */
    @Test
    void testFindByUuid_whenExists() {
        // given
        String testUuid = UUID.randomUUID().toString();
        User user = new User();
        user.setUsername("uuidtest");
        user.setEmail("uuid@aueb.gr");
        user.setPassword("pass789");
        user.setUuid(testUuid);
        user.setRole(Role.CLIENT);
        user.setIsActive(true);
        userRepository.save(user);

        // act
        Optional<User> found = userRepository.findByUuid(testUuid);

        // assert
        assertTrue(found.isPresent());
        assertEquals(testUuid, found.get().getUuid());
        assertEquals("uuidtest", found.get().getUsername());
    }

    /**
     * Test findByUuid - not found case
     */
    @Test
    void testFindByUuid_whenNotExists() {
        // act
        Optional<User> found = userRepository.findByUuid(UUID.randomUUID().toString());

        // assert
        assertFalse(found.isPresent());
    }

    /**
     * Test findByRole - multiple users with same role
     */
    @Test
    void testFindByRole() {
        // given - 3 users: 2 ADMINs and 1 USER
        User admin1 = new User();
        admin1.setUsername("admin1");
        admin1.setEmail("admin1@aueb.gr");
        admin1.setPassword("pass1");
        admin1.setUuid(UUID.randomUUID().toString());
        admin1.setRole(Role.SUPER_ADMIN);
        admin1.setIsActive(true);

        User admin2 = new User();
        admin2.setUsername("admin2");
        admin2.setEmail("admin2@aueb.gr");
        admin2.setPassword("pass2");
        admin2.setUuid(UUID.randomUUID().toString());
        admin2.setRole(Role.SUPER_ADMIN);
        admin2.setIsActive(true);

        User user = new User();
        user.setUsername("regularuser");
        user.setEmail("user@aueb.gr");
        user.setPassword("pass3");
        user.setUuid(UUID.randomUUID().toString());
        user.setRole(Role.CLIENT);
        user.setIsActive(true);

        userRepository.save(admin1);
        userRepository.save(admin2);
        userRepository.save(user);

        // act
        List<User> admins = userRepository.findByRole(Role.SUPER_ADMIN);
        List<User> users = userRepository.findByRole(Role.CLIENT);

        // assert
        assertEquals(2, admins.size());
        assertEquals(1, users.size());
        assertTrue(admins.stream().allMatch(u -> u.getRole() == Role.SUPER_ADMIN));
    }

    /**
     * Test findByRole - no users with that role
     */
    @Test
    void testFindByRole_whenNoUsersWithRole() {
        // given - only USER role users
        User user = new User();
        user.setUsername("user1");
        user.setEmail("user1@aueb.gr");
        user.setPassword("pass");
        user.setUuid(UUID.randomUUID().toString());
        user.setRole(Role.CLIENT);
        user.setIsActive(true);
        userRepository.save(user);

        // act
        List<User> admins = userRepository.findByRole(Role.SUPER_ADMIN);

        //assert
        assertTrue(admins.isEmpty());
    }

    /**
     * Test findByIsActiveTrue - only active users
     */
    @Test
    void testFindByIsActiveTrue() {
        // given - 2 active, 1 inactive user
        User active1 = new User();
        active1.setUsername("active1");
        active1.setEmail("active1@aueb.gr");
        active1.setPassword("pass1");
        active1.setUuid(UUID.randomUUID().toString());
        active1.setRole(Role.CLIENT);
        active1.setIsActive(true);

        User active2 = new User();
        active2.setUsername("active2");
        active2.setEmail("active2@aueb.gr");
        active2.setPassword("pass2");
        active2.setUuid(UUID.randomUUID().toString());
        active2.setRole(Role.CLIENT);
        active2.setIsActive(true);

        User inactive = new User();
        inactive.setUsername("inactive");
        inactive.setEmail("inactive@aueb.gr");
        inactive.setPassword("pass3");
        inactive.setUuid(UUID.randomUUID().toString());
        inactive.setRole(Role.CLIENT);
        inactive.setIsActive(false);

        userRepository.save(active1);
        userRepository.save(active2);
        userRepository.save(inactive);

        // act
        List<User> activeUsers = userRepository.findByIsActiveTrue();

        // assert
        assertEquals(2, activeUsers.size());
        assertTrue(activeUsers.stream().allMatch(User::getIsActive));
    }

    /**
     * Test existsByUsername - true case
     */
    @Test
    void testExistsByUsername_whenExists() {
        // given
        User user = new User();
        user.setUsername("existinguser");
        user.setEmail("existing@aueb.gr");
        user.setPassword("pass");
        user.setUuid(UUID.randomUUID().toString());
        user.setRole(Role.CLIENT);
        user.setIsActive(true);
        userRepository.save(user);

        // act
        boolean exists = userRepository.existsByUsername("existinguser");

        // assert
        assertTrue(exists);
    }

    /**
     * Test existsByUsername - false case
     */
    @Test
    void testExistsByUsername_whenNotExists() {
        // act
        boolean exists = userRepository.existsByUsername("nonexistentuser");

        // assert
        assertFalse(exists);
    }

    /**
     * Test existsByEmail - true case
     */
    @Test
    void testExistsByEmail_whenExists() {
        // given
        User user = new User();
        user.setUsername("emailtest");
        user.setEmail("exists@aueb.gr");
        user.setPassword("pass");
        user.setUuid(UUID.randomUUID().toString());
        user.setRole(Role.CLIENT);
        user.setIsActive(true);
        userRepository.save(user);

        // act
        boolean exists = userRepository.existsByEmail("exists@aueb.gr");

        // assert
        assertTrue(exists);
    }

    /**
     * Test existsByEmail - false case
     */
    @Test
    void testExistsByEmail_whenNotExists() {
        // act
        boolean exists = userRepository.existsByEmail("notexists@aueb.gr");

        // assert
        assertFalse(exists);
    }

    /**
     * Test update operation
     */
    @Test
    void testUpdateUser() {
        // given
        User user = new User();
        user.setUsername("updatetest");
        user.setEmail("update@aueb.gr");
        user.setPassword("oldpass");
        user.setUuid(UUID.randomUUID().toString());
        user.setRole(Role.CLIENT);
        user.setIsActive(true);
        User saved = userRepository.save(user);

        // act
        saved.setEmail("newemail@aueb.gr");
        saved.setRole(Role.CLIENT);
        User updated = userRepository.save(saved);

        // assert
        assertEquals(saved.getId(), updated.getId());
        assertEquals("newemail@aueb.gr", updated.getEmail());
        assertEquals(Role.CLIENT, updated.getRole());
    }

    /**
     * Test delete operation
     */
    @Test
    void testDeleteUser() {
        // given
        User user = new User();
        user.setUsername("deletetest");
        user.setEmail("delete@aueb.gr");
        user.setPassword("pass");
        user.setUuid(UUID.randomUUID().toString());
        user.setRole(Role.CLIENT);
        user.setIsActive(true);
        User saved = userRepository.save(user);

        // act
        userRepository.deleteById(saved.getId());

        //assert
        Optional<User> deleted = userRepository.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }
}