package gr.aueb.cf.system_management_restAPI.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests για ClientRestController CRUD (Update And Delete) operations
 */
@SpringBootTest
@AutoConfigureMockMvc
class ClientRestControllerUpdateAndDeleteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    // UPDATE TESTS
    @Test
    void testUpdateClient_Success_AsOwner() throws Exception {
        PersonalInfoUpdateDTO personalInfo = new PersonalInfoUpdateDTO();
        personalInfo.setFirstName("UpdatedJohn");
        personalInfo.setLastName("UpdatedDoe");
        personalInfo.setPhone("1234567890");
        personalInfo.setCityId(testCity.getId());

        ClientUpdateDTO updateDTO = new ClientUpdateDTO();
        updateDTO.setPersonalInfo(personalInfo);

        mockMvc.perform(put("/api/clients/{id}", testClient.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalInfo.firstName").value("UpdatedJohn"));
    }

    @Test
    void testUpdateClient_Success_AsSuperAdmin() throws Exception {
        PersonalInfoUpdateDTO personalInfo = new PersonalInfoUpdateDTO();
        personalInfo.setFirstName("AdminUpdated");
        personalInfo.setLastName("Name");
        personalInfo.setPhone("9999999999");
        personalInfo.setCityId(testCity.getId());

        ClientUpdateDTO updateDTO = new ClientUpdateDTO();
        updateDTO.setPersonalInfo(personalInfo);

        mockMvc.perform(put("/api/clients/{id}", testClient.getId())
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalInfo.firstName").value("AdminUpdated"));
    }

    @Test
    void testUpdateClient_Fail_AccessDenied() throws Exception {
        // Create client
        User anotherUser = TestDataFactory.createDefaultUser();
        anotherUser.setUsername("another2");
        anotherUser.setEmail("another2@test.com");
        anotherUser.setPassword(passwordEncoder.encode("Password123!"));
        anotherUser = userRepository.save(anotherUser);

        Client anotherClient = TestDataFactory.createDefaultClient();
        anotherClient.setUser(anotherUser);
        anotherClient.setVat("111222333");
        anotherClient.getPersonalInfo().setFirstName("Another");
        anotherClient.getPersonalInfo().setLastName("Person");
        anotherClient.getPersonalInfo().setPhone("8888888888");
        anotherClient.getPersonalInfo().setEmail(anotherUser.getEmail());
        anotherClient.getPersonalInfo().setCity(testCity);
        anotherClient = clientRepository.save(anotherClient);

        String anotherToken = jwtService.generateToken(anotherUser.getUsername(), anotherUser.getRole().name());

        PersonalInfoUpdateDTO personalInfo = new PersonalInfoUpdateDTO();
        personalInfo.setFirstName("Hacker");
        personalInfo.setLastName("Attempt");
        personalInfo.setPhone("1234567890");
        personalInfo.setCityId(testCity.getId());

        ClientUpdateDTO updateDTO = new ClientUpdateDTO();
        updateDTO.setPersonalInfo(personalInfo);

        // update testClient with another user's token
        mockMvc.perform(put("/api/clients/{id}", testClient.getId())
                        .header("Authorization", "Bearer " + anotherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateClient_Fail_NotFound() throws Exception {
        Long nonExistentId = 99999L;

        PersonalInfoUpdateDTO personalInfo = new PersonalInfoUpdateDTO();
        personalInfo.setFirstName("Ghost");
        personalInfo.setLastName("User");
        personalInfo.setPhone("1234567890");
        personalInfo.setCityId(testCity.getId());

        ClientUpdateDTO updateDTO = new ClientUpdateDTO();
        updateDTO.setPersonalInfo(personalInfo);

        mockMvc.perform(put("/api/clients/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // DELETE TESTS
    @Test
    void testDeleteClient_Success_SelfDelete() throws Exception {
        mockMvc.perform(delete("/api/clients/{id}", testClient.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Client deleted successfully"))
                .andExpect(jsonPath("$.selfDelete").value(true))
                .andExpect(jsonPath("$.clientId").value(testClient.getId()));

        //assert
        assertThat(clientRepository.findById(testClient.getId())).isEmpty();
        assertThat(userRepository.findById(testClientUser.getId())).isEmpty();
    }

    @Test
    void testDeleteClient_Success_AsSuperAdmin() throws Exception {
        mockMvc.perform(delete("/api/clients/{id}", testClient.getId())
                        .header("Authorization", "Bearer " + superAdminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Client deleted successfully"))
                .andExpect(jsonPath("$.selfDelete").value(false));

        // Assert
        assertThat(clientRepository.findById(testClient.getId())).isEmpty();
    }

    @Test
    void testDeleteClient_Fail_AccessDenied() throws Exception {
        // Create client
        User anotherUser = TestDataFactory.createDefaultUser();
        anotherUser.setUsername("another3");
        anotherUser.setEmail("another3@test.com");
        anotherUser.setPassword(passwordEncoder.encode("Password123!"));
        anotherUser = userRepository.save(anotherUser);

        Client anotherClient = TestDataFactory.createDefaultClient();
        anotherClient.setUser(anotherUser);
        anotherClient.setVat("444555666");
        anotherClient.getPersonalInfo().setFirstName("Another");
        anotherClient.getPersonalInfo().setLastName("Person");
        anotherClient.getPersonalInfo().setPhone("6666666666");
        anotherClient.getPersonalInfo().setEmail(anotherUser.getEmail());
        anotherClient.getPersonalInfo().setCity(testCity);
        anotherClient = clientRepository.save(anotherClient);

        String anotherToken = jwtService.generateToken(anotherUser.getUsername(), anotherUser.getRole().name());

        // Try to delete testClient
        mockMvc.perform(delete("/api/clients/{id}", testClient.getId())
                        .header("Authorization", "Bearer " + anotherToken))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Verify NOT deleted
        assertThat(clientRepository.findById(testClient.getId())).isPresent();
    }

    @Test
    void testDeleteClient_Fail_NotFound() throws Exception {
        Long nonExistentId = 99999L;

        mockMvc.perform(delete("/api/clients/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + superAdminToken))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteClient_Fail_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/clients/{id}", testClient.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
