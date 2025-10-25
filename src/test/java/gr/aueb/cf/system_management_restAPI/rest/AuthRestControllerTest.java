package gr.aueb.cf.system_management_restAPI.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.system_management_restAPI.core.enums.Role;
import gr.aueb.cf.system_management_restAPI.dto.AuthenticationRequestDTO;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import gr.aueb.cf.system_management_restAPI.util.TestDBHelper;
import gr.aueb.cf.system_management_restAPI.util.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthRestController
 * Tests authentication with REAL security (JWT enabled)
 */
@SpringBootTest
@AutoConfigureMockMvc  // allows to hit http requests without real server
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "Password123!";
    private static final String WRONG_PASSWORD = "WrongPassword123!";

    @BeforeAll
    void setupClass() throws SQLException {
        TestDBHelper.eraseData(dataSource);
    }

    @BeforeEach
    void setup() {
        createTestUser();
    }


    // AUTHENTICATION TESTS
    /**
     * Test: POST /api/auth/authenticate with valid credentials
     * Should return 200 OK with JWT token
     */
    @Test
    void authenticate_ShouldReturn200WithToken_WhenCredentialsValid() throws Exception {
        // Given: Valid credentials
        AuthenticationRequestDTO request = new AuthenticationRequestDTO(
                TEST_USERNAME,
                TEST_PASSWORD
        );

        // act&assert : POST request → 200 + token
        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andExpect(jsonPath("$.expiresIn").exists());
    }

    /**
     * Test: POST /api/auth/authenticate with invalid password
     * Should return 401 Unauthorized
     */
    @Test
    void authenticate_ShouldReturn401_WhenPasswordInvalid() throws Exception {
        // Given: Wrong password
        AuthenticationRequestDTO request = new AuthenticationRequestDTO(
                TEST_USERNAME,
                WRONG_PASSWORD
        );

        //  act&assert : POST request → 401
        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: POST /api/auth/authenticate with non-existent user
     * Should return 401 Unauthorized
     */
    @Test
    void authenticate_ShouldReturn401_WhenUserNotExists() throws Exception {
        // Given: Non-existent user
        AuthenticationRequestDTO request = new AuthenticationRequestDTO(
                "nonexistentuser",
                TEST_PASSWORD
        );

        // act&assert: POST request → 401
        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: POST /api/auth/authenticate with empty username
     * Should return 400 Bad Request (validation error)
     */
    @Test
    void authenticate_ShouldReturn400_WhenUsernameEmpty() throws Exception {
        // Given: Empty username
        AuthenticationRequestDTO request = new AuthenticationRequestDTO(
                "",  // ← Empty!
                TEST_PASSWORD
        );

        //  act&assert: POST request → 400
        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: POST /api/auth/authenticate with empty password
     * Should return 400 Bad Request (validation error)
     */
    @Test
    void authenticate_ShouldReturn400_WhenPasswordEmpty() throws Exception {
        // Given: Empty password
        AuthenticationRequestDTO request = new AuthenticationRequestDTO(
                TEST_USERNAME,
                ""  // ← Empty!
        );

        // act&assert: POST request → 400
        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: POST /api/auth/authenticate with null request body
     * Should return 400 Bad Request
     */
    @Test
    void authenticate_ShouldReturn400_WhenRequestBodyNull() throws Exception {
        // act&assert: POST with no body → 400
        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: POST /api/auth/authenticate with malformed JSON
     * Should return 400 Bad Request
     */
    @Test
    void authenticate_ShouldReturn400_WhenJsonMalformed() throws Exception {
        // Given: Invalid JSON
        String malformedJson = "{ username: testuser }";  // Missing quotes

        // act&assert: POST → 400
        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Verify token can be used for authenticated requests
     * Should be able to call /api/users/me with the token
     */
    @Test
    void authenticate_TokenShouldWorkForAuthenticatedRequests() throws Exception {
        // Given: Get JWT token
        AuthenticationRequestDTO request = new AuthenticationRequestDTO(
                TEST_USERNAME,
                TEST_PASSWORD
        );

        String responseJson = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract token from response
        String token = objectMapper.readTree(responseJson).get("token").asText();

        // act&assert: Use token to call authenticated endpoint
        mockMvc.perform(get("/api/users/me")  // ← Ή GET αν έχεις
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    // HELPER METHODS
    /**
     * Creates a test user with encoded password
     */
    private void createTestUser() {
        userRepository.deleteAll();

        User user = TestDataFactory.createDefaultUser();
        user.setUsername(TEST_USERNAME);
        user.setEmail(TestDataFactory.generateUniqueEmail());
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));  // ← ENCODED password!
        user.setRole(Role.CLIENT);
        user.setIsActive(true);

        userRepository.save(user);
        userRepository.flush();
    }
}