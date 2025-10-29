package gr.aueb.cf.system_management_restAPI.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentUpdateDTO;
import gr.aueb.cf.system_management_restAPI.model.Appointment;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.model.static_data.City;
import gr.aueb.cf.system_management_restAPI.repository.AppointmentRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
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
 * Integration Tests for AppointmentRestController CRUD (Update And Delete) operations
 */
@SpringBootTest
@AutoConfigureMockMvc
class AppointmentRestControllerUpdateAndDeleteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppointmentRepository appointmentRepository;

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
    private Appointment testAppointment;

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

        // Create  CLIENT
        testClientUser = TestDataFactory.createDefaultUser();
        testClientUser.setUsername("testclient");
        testClientUser.setEmail("client@test.com");
        testClientUser.setPassword(passwordEncoder.encode("Password123!"));
        testClientUser = userRepository.save(testClientUser);

        // Create client
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

        // Create  appointment
        testAppointment = TestDataFactory.createDefaultAppointment();
        testAppointment.setUser(superAdminUser);
        testAppointment.setClient(testClient);
        testAppointment = appointmentRepository.save(testAppointment);
    }

    @AfterEach
    void tearDown() throws SQLException {
        TestDBHelper.eraseData(dataSource);
    }

    //UPDATE TESTS

    @Test
    void testUpdateAppointment_Success_AsSuperAdmin() throws Exception {
        //Arrange - Create AppointmentUpdateDTO
        AppointmentUpdateDTO dto = TestDataFactory.createValidAppointmentUpdateDTO();
        dto.setAppointmentDateTime(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0));
        dto.setStatus(AppointmentStatus.CONFIRMED);
        dto.setNotes("Updated notes - SuperAdmin test");
        dto.setEmailReminder(true);

        // Act & Assert - PUT to /api/appointments/update/{id} with superAdminToken
        mockMvc.perform(put("/api/appointments/update/{id}", testAppointment.getId())
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAppointment.getId()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.notes").value("Updated notes - SuperAdmin test"))
                .andExpect(jsonPath("$.emailReminder").value(true));

        //database  updated values
        Appointment updatedAppointment = appointmentRepository.findById(testAppointment.getId()).orElseThrow();
        assertThat(updatedAppointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(updatedAppointment.getNotes()).isEqualTo("Updated notes - SuperAdmin test");
        assertThat(updatedAppointment.getEmailReminder()).isTrue();
    }

    @Test
    void testUpdateAppointment_Fail_ValidationError() throws Exception {
        //Arrange -  INVALID(null date time) AppointmentUpdateDTO
        AppointmentUpdateDTO dto = TestDataFactory.createValidAppointmentUpdateDTO();
        dto.setAppointmentDateTime(null);
        dto.setStatus(AppointmentStatus.CONFIRMED);
        dto.setNotes("Updated notes - Null");
        dto.setEmailReminder(true);

        //Act & Assert - PUT to /api/appointments/update/{id}
        mockMvc.perform(put("/api/appointments/update/{id}", testAppointment.getId())
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateAppointment_Fail_NotFound() throws Exception {
        //Arrange - Create valid AppointmentUpdateDTO
        AppointmentUpdateDTO dto = TestDataFactory.createValidAppointmentUpdateDTO();
        dto.setAppointmentDateTime(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0));
        dto.setStatus(AppointmentStatus.CONFIRMED);
        dto.setNotes("Updated notes - Not Found test");
        dto.setEmailReminder(true);

        Long nonExistentId = 99999L;

        //Act & Assert - PUT to /api/appointments/update/{id} with non-existent ID
        mockMvc.perform(put("/api/appointments/update/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateAppointment_Fail_Unauthorized() throws Exception {
        //Arrange - Create valid AppointmentUpdateDTO
        AppointmentUpdateDTO dto = TestDataFactory.createValidAppointmentUpdateDTO();
        dto.setAppointmentDateTime(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0));
        dto.setStatus(AppointmentStatus.CONFIRMED);
        dto.setNotes("Updated notes - SuperAdmin test");
        dto.setEmailReminder(true);

        //Act & Assert - PUT to /api/appointments/update/{id} WITHOUT token
        mockMvc.perform(put("/api/appointments/update/{id}", testAppointment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateAppointment_Fail_AccessDenied() throws Exception {
        //Arrange - Create another user and their token.Try to update testAppointment
        // (that belong to superAdminUser) with the other user token
        User anotherUser = TestDataFactory.createDefaultUser();
        anotherUser.setUsername("anotherclient");
        anotherUser.setEmail("another@aueb.com");
        anotherUser.setPassword(passwordEncoder.encode("Password123!"));
        anotherUser = userRepository.save(anotherUser);

        Client anotherClient = TestDataFactory.createDefaultClient();
        anotherClient.setUser(anotherUser);
        anotherClient.setVat("987654321");
        anotherClient.getPersonalInfo().setFirstName("Bruce");
        anotherClient.getPersonalInfo().setLastName("Lee");
        anotherClient.getPersonalInfo().setPhone("9876543210");
        anotherClient.getPersonalInfo().setEmail(anotherUser.getEmail());
        anotherClient.getPersonalInfo().setCity(testCity);
        anotherClient = clientRepository.save(anotherClient);

        String anotherToken = jwtService.generateToken(anotherUser.getUsername(), anotherUser.getRole().name());
        AppointmentUpdateDTO dto = TestDataFactory.createValidAppointmentUpdateDTO();
        dto.setAppointmentDateTime(LocalDateTime.now().plusDays(5).withHour(15).withMinute(0).withSecond(0).withNano(0));
        dto.setStatus(AppointmentStatus.CONFIRMED);
        dto.setNotes("Trying to update someone else's appointment");

        //Act & Assert - PUT with wrong user token
        mockMvc.perform(put("/api/appointments/update/{id}", testAppointment.getId())
                        .header("Authorization", "Bearer " + anotherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    //DELETE TESTS
    @Test
    void testDeleteAppointment_Success_AsSuperAdmin() throws Exception {
        //Arrange - Get  ID of testAppointment
        Long appointmentId = testAppointment.getId();
        assertThat(appointmentRepository.findById(appointmentId)).isPresent();

        //Act & Assert - DELETE /api/appointments/{id} with superAdminToken Expect status 200
        mockMvc.perform(delete("/api/appointments/{id}", appointmentId)
                        .header("Authorization", "Bearer " + superAdminToken))
                .andDo(print())
                .andExpect(status().isOk());

        //appointment no longer exists
        assertThat(appointmentRepository.findById(appointmentId)).isEmpty();
    }

    @Test
    void testDeleteAppointment_Fail_NotFound() throws Exception {
        //Arrange - Use non-existent ID
        Long nonExistentId = 99999L;

        //Act & Assert - DELETE /api/appointments/{id} with non-existent ID
        mockMvc.perform(delete("/api/appointments/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + superAdminToken))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteAppointment_Fail_Unauthorized() throws Exception {
        //Act & Assert - DELETE /api/appointments/{id} WITHOUT token
        mockMvc.perform(delete("/api/appointments/{id}",testAppointment.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "anotherclient", roles = {"CLIENT"})
    void testDeleteAppointment_Fail_AccessDenied() throws Exception {
        //Arrange - Create another user and their token
        //  Try to delete testAppointment with the other user's token
        User anotherUser = TestDataFactory.createDefaultUser();
        anotherUser.setUsername("anotherclient");
        anotherUser.setEmail("another@aueb.com");
        anotherUser.setPassword(passwordEncoder.encode("Password123!"));
        anotherUser = userRepository.save(anotherUser);

        Client anotherClient = TestDataFactory.createDefaultClient();
        anotherClient.setUser(anotherUser);
        anotherClient.setVat("987654321");
        anotherClient.getPersonalInfo().setFirstName("Bruce");
        anotherClient.getPersonalInfo().setLastName("Lee");
        anotherClient.getPersonalInfo().setPhone("9876543210");
        anotherClient.getPersonalInfo().setEmail(anotherUser.getEmail());
        anotherClient.getPersonalInfo().setCity(testCity);
        anotherClient = clientRepository.save(anotherClient);

        String anotherToken = jwtService.generateToken(anotherUser.getUsername(), anotherUser.getRole().name());

        // Act & Assert - DELETE with wrong user token (Bruce Lee trying to delete testAppointment)
        mockMvc.perform(delete("/api/appointments/{id}", testAppointment.getId())
                        .header("Authorization", "Bearer " + anotherToken))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    //    //MARK REMINDER AS SENT TESTS
//
//    @Test
//    void testMarkReminderAsSent_Success() throws Exception {
//        //Arrange - Ensure testAppointment has emailReminder=true and reminderSent=false
//
//        //Act & Assert - PUT to /api/appointments/{id}/reminder/sent with superAdminToken
//        // Expect status 200
//        // Verify response contains success message
//        // Verify reminderSent is now true in database
//    }
//
//    @Test
//    void testMarkReminderAsSent_Fail_NotFound() throws Exception {
//        // Arrange - Use non-existent ID
//
//        // Act & Assert - PUT to /api/appointments/{id}/reminder/sent
//        // Expect status 404 (Not Found)
//    }
//
//    @Test
//    void testMarkReminderAsSent_Fail_Unauthorized() throws Exception {
//        //Act & Assert - PUT to /api/appointments/{id}/reminder/sent WITHOUT token
//        //Expect status 401 (Unauthorized)
//    }

    @Test
    void testUpdateMultipleFields_Success() throws Exception {
            // Arrange - Create AppointmentUpdateDTO
            AppointmentUpdateDTO dto = TestDataFactory.createValidAppointmentUpdateDTO();

            // Change MULTIPLE fields at once
            LocalDateTime newDateTime = LocalDateTime.now().plusDays(7).withHour(16).withMinute(30).withSecond(0).withNano(0);
            dto.setAppointmentDateTime(newDateTime);
            dto.setStatus(AppointmentStatus.COMPLETED);
            dto.setNotes("Updated multiple fields - all at once");
            dto.setEmailReminder(false);

            LocalDateTime reminderDateTime = newDateTime.minusHours(24);
            dto.setReminderDateTime(reminderDateTime);

            // Act & Assert - PUT to /api/appointments/update/{id}
            mockMvc.perform(put("/api/appointments/update/{id}", testAppointment.getId())
                            .header("Authorization", "Bearer " + superAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testAppointment.getId()))
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.notes").value("Updated multiple fields - all at once"))
                    .andExpect(jsonPath("$.emailReminder").value(false));

            //ALL fields were updated correctly in database
            Appointment updatedAppointment = appointmentRepository.findById(testAppointment.getId()).orElseThrow();
            assertThat(updatedAppointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
            assertThat(updatedAppointment.getNotes()).isEqualTo("Updated multiple fields - all at once");
            assertThat(updatedAppointment.getEmailReminder()).isFalse();
            assertThat(updatedAppointment.getAppointmentDateTime()).isEqualToIgnoringNanos(newDateTime);
            assertThat(updatedAppointment.getReminderDateTime()).isEqualToIgnoringNanos(reminderDateTime);
        }

    @Test
    void testDeleteAndVerifyNotAccessible() throws Exception {
        //Arrange -appointment ID
        Long appointmentId = testAppointment.getId();

        assertThat(appointmentRepository.findById(appointmentId)).isPresent();
        //DELETE appointment
        mockMvc.perform(delete("/api/appointments/{id}", appointmentId)
                        .header("Authorization", "Bearer " + superAdminToken))
                .andDo(print())
                .andExpect(status().isOk());
        //Assert - GET  deleted appointment
        mockMvc.perform(get("/api/appointments/{id}", appointmentId)
                        .header("Authorization", "Bearer " + superAdminToken))
                .andDo(print())
                .andExpect(status().isNotFound());
        assertThat(appointmentRepository.findById(appointmentId)).isEmpty();
    }
}
