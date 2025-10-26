package gr.aueb.cf.system_management_restAPI.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.system_management_restAPI.core.enums.Role;
import gr.aueb.cf.system_management_restAPI.dto.*;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.model.static_data.City;
import gr.aueb.cf.system_management_restAPI.repository.ClientRepository;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import gr.aueb.cf.system_management_restAPI.repository.CityRepository;
import gr.aueb.cf.system_management_restAPI.security.JwtService;
import gr.aueb.cf.system_management_restAPI.util.TestDataFactory;
import gr.aueb.cf.system_management_restAPI.util.TestDBHelper;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests για ClientRestController CRUD (Save And Read) operations
 */
@SpringBootTest
@AutoConfigureMockMvc
class ClientRestControllerSaveAndReadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;    //from jackson serialize/deserialize objects from/to JSon

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DataSource dataSource;

    // Test data
    private String superAdminToken;
    private String clientToken;
    private User superAdminUser;
    private User testClientUser;
    private Client testClient;
    private City testCity;

    @BeforeEach
    void setUp() throws SQLException {
        // Clean up
        TestDBHelper.eraseData(dataSource);

        // Create test city
        testCity = cityRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("City not found"));

        // Create SUPER_ADMIN user
        superAdminUser = TestDataFactory.createDefaultUserAdmin();
        superAdminUser.setUsername("superadmin");
        superAdminUser.setEmail("admin@test.com");
        superAdminUser.setPassword(passwordEncoder.encode("Password123!"));
        superAdminUser = userRepository.save(superAdminUser);
        superAdminToken = jwtService.generateToken(superAdminUser.getUsername(), superAdminUser.getRole().name());

        // Create regular CLIENT user
        testClientUser = TestDataFactory.createDefaultUser();
        testClientUser.setUsername("testclient");
        testClientUser.setEmail("client@test.com");
        testClientUser.setPassword(passwordEncoder.encode("Password123!"));
        testClientUser = userRepository.save(testClientUser);

        // Create client with personal info
        testClient = TestDataFactory.createDefaultClient();
        testClient.setUser(testClientUser);
        testClient.getPersonalInfo().setFirstName("John");
        testClient.getPersonalInfo().setLastName("Doe");
        testClient.getPersonalInfo().setPhone("1234567890");
        testClient.getPersonalInfo().setEmail(testClientUser.getEmail());
        testClient.getPersonalInfo().setCity(testCity);
        testClient.setCreatedAt(LocalDateTime.now());
        testClient.setUpdatedAt(LocalDateTime.now());
        testClient = clientRepository.save(testClient);

        clientToken = jwtService.generateToken(testClientUser.getUsername(), testClientUser.getRole().name());
    }

    @AfterEach
    void tearDown() throws SQLException {
        TestDBHelper.eraseData(dataSource);
    }

    // CREATE TESTS
    @Test
    void testSaveClient_Success_Unauthenticated() throws Exception {
        // Arrange
        ClientInsertDTO clientDTO = TestDataFactory.createValidClientInsertDTO();
        clientDTO.getUser().setUsername("newuser");
        clientDTO.getUser().setEmail("new@test.com");
        clientDTO.getPersonalInfo().setFirstName("Jane");
        clientDTO.getPersonalInfo().setLastName("Smith");
        clientDTO.getPersonalInfo().setPhone("9876543210");
        clientDTO.getPersonalInfo().setEmail("new@test.com");

        // Act & Assert
        mockMvc.perform(post("/api/clients/save")          //perform(...) ->  sends request
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientDTO)))
                .andDo(print())                                       //prints response to console(useful for debugging)
                .andExpect(status().isOk())                           //andExpect(...) -> assertion over response
                //jsonPath("$.personalInfo.firstname") -> do path-based check to Json body
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.personalInfo.firstName").value("Jane"))
                .andExpect(jsonPath("$.personalInfo.lastName").value("Smith"));

        // assert
        assertThat(clientRepository.findAll()).hasSize(2);
    }

    @Test
    void testSaveClient_Success_AsSuperAdmin() throws Exception {
        // Arrange
        ClientInsertDTO clientDTO = TestDataFactory.createValidClientInsertDTO();
        clientDTO.getUser().setUsername("adminuser");
        clientDTO.getUser().setEmail("admin2@test.com");
        clientDTO.getPersonalInfo().setFirstName("Admin");
        clientDTO.getPersonalInfo().setLastName("User");
        clientDTO.getPersonalInfo().setPhone("5555555555");
        clientDTO.getPersonalInfo().setEmail("admin2@test.com");

        // Act & Assert
        mockMvc.perform(post("/api/clients/save")
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalInfo.firstName").value("Admin"));
    }

    @Test
    void testSaveClient_Fail_AuthenticatedClient() throws Exception {
        // Arrange
        ClientInsertDTO clientDTO = TestDataFactory.createValidClientInsertDTO();
        clientDTO.getUser().setUsername("blocked");
        clientDTO.getUser().setEmail("blocked@test.com");
        clientDTO.getPersonalInfo().setFirstName("Blocked");
        clientDTO.getPersonalInfo().setLastName("User");
        clientDTO.getPersonalInfo().setPhone("1111111111");
        clientDTO.getPersonalInfo().setEmail("blocked@test.com");

        // Act & Assert
        mockMvc.perform(post("/api/clients/save")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientDTO)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Only SUPER_ADMIN")));
    }

    @Test
    void testSaveClient_Fail_ValidationError() throws Exception {
        // Arrange - Invalid DTO (empty username)
        UserInsertDTO invalidUser = new UserInsertDTO();
        invalidUser.setIsActive(true);
        invalidUser.setUsername(""); // Empty - should fail
        invalidUser.setPassword("ValidPass123!");
        invalidUser.setEmail("test@test.com");
        invalidUser.setRole(Role.CLIENT);

        PersonalInfoInsertDTO validPersonalInfo = new PersonalInfoInsertDTO();
        validPersonalInfo.setFirstName("Test");
        validPersonalInfo.setLastName("User");
        validPersonalInfo.setPhone("1234567890");

        ClientInsertDTO invalidDTO = new ClientInsertDTO();
        invalidDTO.setIsActive(true);
        invalidDTO.setUser(invalidUser);
        invalidDTO.setPersonalInfo(validPersonalInfo);

        // Act & Assert
        mockMvc.perform(post("/api/clients/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSaveClient_Fail_DuplicateUsername() throws Exception {
        // Arrange
        ClientInsertDTO clientDTO = TestDataFactory.createValidClientInsertDTO();
        clientDTO.getUser().setUsername(testClientUser.getUsername()); // Duplicate username
        clientDTO.getUser().setEmail("different@test.com");
        clientDTO.getPersonalInfo().setFirstName("Another");
        clientDTO.getPersonalInfo().setLastName("Person");
        clientDTO.getPersonalInfo().setPhone("9999999999");
        clientDTO.getPersonalInfo().setEmail("different@test.com");

        // Act & Assert
        mockMvc.perform(post("/api/clients/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientDTO)))
                .andDo(print())
                .andExpect(status().isConflict());
    }


    // READ TESTS
    @Test
    void testGetClientById_Success_AsSuperAdmin() throws Exception {
        mockMvc.perform(get("/api/clients/{id}", testClient.getId())
                        .header("Authorization", "Bearer " + superAdminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClient.getId()))
                .andExpect(jsonPath("$.personalInfo.firstName").value("John"))
                .andExpect(jsonPath("$.personalInfo.lastName").value("Doe"));
    }

    @Test
    void testGetClientById_Success_AsOwner() throws Exception {
        mockMvc.perform(get("/api/clients/{id}", testClient.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClient.getId()));
    }

    @Test
    void testGetClientById_Fail_AccessDenied() throws Exception {
        // Create another client using TestDataFactory
        User anotherUser = TestDataFactory.createDefaultUser();
        anotherUser.setUsername("another");
        anotherUser.setEmail("another@test.com");
        anotherUser.setPassword(passwordEncoder.encode("Password123!"));
        anotherUser = userRepository.save(anotherUser);

        Client anotherClient = TestDataFactory.createDefaultClient();
        anotherClient.setUser(anotherUser);
        anotherClient.setVat("987654321");
        anotherClient.getPersonalInfo().setFirstName("Another");
        anotherClient.getPersonalInfo().setLastName("Person");
        anotherClient.getPersonalInfo().setPhone("7777777777");
        anotherClient.getPersonalInfo().setEmail(anotherUser.getEmail());
        anotherClient.getPersonalInfo().setCity(testCity);
        anotherClient = clientRepository.save(anotherClient);

        String anotherToken = jwtService.generateToken(anotherUser.getUsername(), anotherUser.getRole().name());

        // Try access testClient with another user's token
        mockMvc.perform(get("/api/clients/{id}", testClient.getId())
                        .header("Authorization", "Bearer " + anotherToken))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetClientById_Fail_NotFound() throws Exception {
        Long nonExistentId = 99999L;

        mockMvc.perform(get("/api/clients/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + superAdminToken))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetClientById_Fail_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/clients/{id}", testClient.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}