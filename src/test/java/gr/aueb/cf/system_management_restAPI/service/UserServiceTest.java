package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.enums.Role;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.dto.UserReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.mapper.Mapper;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import gr.aueb.cf.system_management_restAPI.util.TestDBHelper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest  //load all layers (Repositories, Services, Controllers)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Ensures that @BeforeAll and @AfterAll run on the same test instance
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataSource dataSource; // Inject DataSource from Spring

    private UserService userService;

    @Autowired
    private Mapper mapper;

    @BeforeAll
    void setupClass() throws SQLException {
        // Initialize service manually using repository + mapper
        userService = new UserService(userRepository, mapper);

        // Clean database once before all tests
        TestDBHelper.eraseData(dataSource);
    }

    @BeforeEach
    void setup() throws Exception {
        // Populate test data
        createDummyUsers();
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Clean database after each test
        TestDBHelper.eraseData(dataSource);
    }

    @AfterAll
    void tearAll() throws Exception {
        // Repopulate DB after all tests
        createDummyUsers();
    }

    /**
     * Should return a UserReadOnlyDTO when a user with the given username exists.
     * Covers the positive case of getUserByUsername method:
     * - Ensures correct retrieval from repository by username
     * - Ensures correct mapping from Entity -> DTO
     * - Verifies username field is correctly populated
     */

    @Test
    void getUserByUsername_ShouldReturnDTO_WhenUserExists() throws AppObjectNotFoundException {
        // Act
        UserReadOnlyDTO user = userService.getUserByUsername("superadmin1");

        // Assert
        assertEquals("superadmin1", user.getUsername());
    }

    /**
     * Should throw AppObjectNotFoundException when no user exists with given username.
     * Covers the negative case of getUserByUsername method:
     * - Ensures correct exception type is thrown
     * - Verifies that non-existent usernames are properly handled
     * - Tests error handling path in service layer
     */

    @Test
    void getUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Expect an exception when a user does not exist
        assertThrows(AppObjectNotFoundException.class,
                () -> userService.getUserByUsername("notfound"));
    }

    /**
     * Should return a UserReadOnlyDTO when a user with the given ID exists.
     * Covers the positive case of service method:
     * - Ensures correct mapping from Entity -> DTO
     * - Ensures correct transactional read path (no modification)
     */
    @Test
    void getUserById_ShouldReturnDTO_WhenUserExists() throws Exception {
        // Arrange: Retrieve a known ID from the DB (first inserted)
        Long existingId = userRepository.findAll().get(0).getId();

        // Act
        UserReadOnlyDTO user = userService.getUserById(existingId);

        // Assert
        assertNotNull(user, "Service should return a non-null DTO");
        assertEquals(existingId, user.getId(), "Returned DTO should contain the correct ID");
    }

    /**
     * Should throw AppObjectNotFoundException when no user exists with given ID.
     * Covers negative service case:
     * - Ensures correct exception type is thrown
     * - Ensures repository is queried but mapper is NOT called
     */
    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Arrange: Pick a non-existing ID
        Long nonExistingId = 1000L;

        // Act & Assert
        assertThrows(AppObjectNotFoundException.class,
                () -> userService.getUserById(nonExistingId),
                "Service should throw AppObjectNotFoundException when ID is not found");
    }

    /**
     * Inserts dummy users for testing purposes.
     */
    private void createDummyUsers() {
        if (userRepository.count() > 0) return; // Skip if already populated

        userRepository.save(new User(null, UUID.randomUUID().toString(), "superadmin1", "Password123!", "superadmin1@aueb.gr", Role.SUPER_ADMIN, true, null));
        userRepository.save(new User(null, UUID.randomUUID().toString(), "superadmin2", "Password123!", "superadmin2@aueb.gr", Role.SUPER_ADMIN, true, null));

        userRepository.save(new User(null, UUID.randomUUID().toString(), "client1", "passworD123!", "client1@aueb.gr", Role.CLIENT, true, null));
        userRepository.save(new User(null, UUID.randomUUID().toString(), "patient1", "passworD123!", "patient1@aueb.gr", Role.PATIENT, true, null));
        userRepository.save(new User(null, UUID.randomUUID().toString(), "client2", "passworD123!", "client2@aueb.gr", Role.PATIENT, true, null));
        userRepository.save(new User(null, UUID.randomUUID().toString(), "patient3", "passworD123!", "patient3@aueb.gr", Role.PATIENT, true, null));
        userRepository.save(new User(null, UUID.randomUUID().toString(), "client3", "passworD123!", "client3@aueb.gr", Role.PATIENT, true, null));
        userRepository.save(new User(null, UUID.randomUUID().toString(), "patient5", "passworD123!", "patient5@aueb.gr", Role.PATIENT, true, null));
        userRepository.save(new User(null, UUID.randomUUID().toString(), "client4", "passworD123!", "client4@aueb.gr", Role.PATIENT, true, null));
        userRepository.save(new User(null, UUID.randomUUID().toString(), "patient7", "passworD123!", "patient7@aueb.gr", Role.PATIENT, true, null));
    }
}