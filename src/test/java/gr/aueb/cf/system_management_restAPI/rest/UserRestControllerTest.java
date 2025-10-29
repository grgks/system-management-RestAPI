package gr.aueb.cf.system_management_restAPI.rest.UserRestControllerTests;


import gr.aueb.cf.system_management_restAPI.core.enums.Role;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import gr.aueb.cf.system_management_restAPI.service.UserService;
import gr.aueb.cf.system_management_restAPI.util.TestDBHelper;
import gr.aueb.cf.system_management_restAPI.util.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserRestController
 * Tests business logic ONLY - security is disabled
 * Authentication/Authorization tests are in SecurityIntegrationTest
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)       //mockmvc tool to hit fake http client for testing(false= disable JWT-servlet filters)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)            // same instance for all tests/JUnit new instance per test
@Transactional                                             //Auto rollback after each test
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;                //Inject το MockMvc object

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataSource dataSource;


    @BeforeAll
    void setupClass() throws SQLException {
        TestDBHelper.eraseData(dataSource);
    }

    @BeforeEach
    void setup() {

        createDummyUsers();
    }

//    @AfterEach
//    void tearDown() throws SQLException {
//        TestDBHelper.eraseData(dataSource);
//    }

    /**
     * Test: GET /api/users/me
     * Should return current user info successfully
     * mockMvc.perform = hit http request
     * .contentType(MediaType.APPLICATION_JSON) = add header:
     * `Content-Type: application/json` waiting JSon response
     * .principal = authenticated user(Mock principal (fake authenticated user))
     * jsonPath = queries json e.x.     $.email    → "test@example.com"  (email field)
     */
    @Test
    void getCurrentUser_ShouldReturn200_WhenUserExists() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }


    /**
     * Test: GET /api/users/me
     * Should return 404 when user not found in database
     */
    @Test
    void getCurrentUser_ShouldReturn404_WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "nonexistentuser"))  // ← User don;t exist
                .andExpect(status().isNotFound());
    }

    /**
     * Test: GET /api/users/username/{username}
     * Should return user successfully by username
     */
    @Test
    void getUserByUsername_ShouldReturn200_WhenUserExists() throws Exception {
        String existingUsername = "testuser";

        mockMvc.perform(get("/api/users/username/" + existingUsername)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "admin"))  // ← Logged in aς admin
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(existingUsername))
                .andExpect(jsonPath("$.email").exists());
    }

    /**
     * Test: GET /api/users/username/{username}
     * Should return 404 when user not found
     */
    @Test
    void getUserByUsername_ShouldReturn404_WhenUserNotFound() throws Exception {
        String nonExistentUsername = "nonexistentuser123";

        mockMvc.perform(get("/api/users/username/" + nonExistentUsername)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "admin"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test: Verify response contains all expected fields
     */
    @Test
    void getCurrentUser_ShouldReturnAllFields_WhenCalled() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .principal(() -> "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.role").exists());
    }

    /**
     * Test: Verify JSON content type in response
     */
    @Test
    void getCurrentUser_ShouldReturnJsonContentType() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .principal(() -> "testuser"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Test: Multiple users - verify correct user is returned
     */
    @Test
    void getCurrentUser_ShouldReturnCorrectUser_WhenMultipleUsersExist() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .principal(() -> "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value(
                        userRepository.findByUsername("admin").get().getEmail()
                ));
    }

    // Helper Methods


    private void createDummyUsers() {
        // Clean database first
        userRepository.deleteAll();

        // User 1 - "testuser" (CLIENT role)
        User user1 = TestDataFactory.createDefaultUser();
        user1.setUsername("testuser");
        user1.setEmail(TestDataFactory.generateUniqueEmail());
        user1.setRole(Role.CLIENT);
        userRepository.save(user1);

        // User 2 - "admin" (SUPER_ADMIN role)
        User user2 = TestDataFactory.createDefaultUser();
        user2.setUsername("admin");
        user2.setEmail(TestDataFactory.generateUniqueEmail());
        user2.setRole(Role.SUPER_ADMIN);
        userRepository.save(user2);

        // User 3 - "john" (CLIENT role)
        User user3 = TestDataFactory.createDefaultUser();
        user3.setUsername("john");
        user3.setEmail(TestDataFactory.generateUniqueEmail());
        user3.setRole(Role.CLIENT);
        userRepository.save(user3);

        // Force write to database
        userRepository.flush();
    }
}
