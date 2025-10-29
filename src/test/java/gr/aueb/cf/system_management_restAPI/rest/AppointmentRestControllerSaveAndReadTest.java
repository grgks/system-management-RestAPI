package gr.aueb.cf.system_management_restAPI.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentInsertDTO;
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
 * Integration Tests for AppointmentRestController CRUD (Save And Read) operations
 */
@SpringBootTest
@AutoConfigureMockMvc
class AppointmentRestControllerSaveAndReadTest {

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
        superAdminUser.setEmail("admin@aueb.com");
        superAdminUser.setPassword(passwordEncoder.encode("Password123!"));
        superAdminUser = userRepository.save(superAdminUser);
        superAdminToken = jwtService.generateToken(superAdminUser.getUsername(), superAdminUser.getRole().name());

        // Create regular CLIENT user
        testClientUser = TestDataFactory.createDefaultUser();
        testClientUser.setUsername("testclient");
        testClientUser.setEmail("client@aueb.com");
        testClientUser.setPassword(passwordEncoder.encode("Password123!"));
        testClientUser = userRepository.save(testClientUser);

        // Create client
        testClient = TestDataFactory.createDefaultClient();
        testClient.setUser(testClientUser);
        testClient.getPersonalInfo().setFirstName("Chuck");
        testClient.getPersonalInfo().setLastName("Norris");
        testClient.getPersonalInfo().setPhone("1234567890");
        testClient.getPersonalInfo().setEmail(testClientUser.getEmail());
        testClient.getPersonalInfo().setCity(testCity);
        testClient.setCreatedAt(LocalDateTime.now());
        testClient.setUpdatedAt(LocalDateTime.now());
        testClient = clientRepository.save(testClient);

        clientToken = jwtService.generateToken(testClientUser.getUsername(), testClientUser.getRole().name());

        // Create test appointment
        testAppointment = TestDataFactory.createDefaultAppointment();
        testAppointment.setUser(superAdminUser);
        testAppointment.setClient(testClient);
        testAppointment = appointmentRepository.save(testAppointment);
    }

    @AfterEach
    void tearDown() throws SQLException {
        TestDBHelper.eraseData(dataSource);
    }


//     Tests for POST /api/appointments/save endpoint
//     Validates appointment creation with proper authentication and data validation

     @Test
    void testSaveAppointment_Success_AsSuperAdmin() throws Exception {
        //Arrange - Create a valid AppointmentInsertDTO using TestDataFactory
         AppointmentInsertDTO appointmentDTO = TestDataFactory.createAppointmentInsertDTO(
                 superAdminUser.getId(),
                 testClient.getId()
         );

         appointmentDTO.setAppointmentDateTime(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0));
         appointmentDTO.setNotes("Test appointment by super admin");
         appointmentDTO.setStatus(AppointmentStatus.PENDING);

        //Act & Assert - POST to /api/appointments/save with superAdminToken
         mockMvc.perform(post("/api/appointments/save")
                         .header("Authorization", "Bearer " + superAdminToken)
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(objectMapper.writeValueAsString(appointmentDTO)))
                 .andDo(print())
                 .andExpect(status().isOk())    //  ----> expect status 200 ok!
                 .andExpect(jsonPath("$.id").exists())
                 .andExpect(jsonPath("$.uuid").exists())
                 .andExpect(jsonPath("$.username").value(superAdminUser.getUsername()))
                 .andExpect(jsonPath("$.clientName").value("Chuck Norris")) //Full name from dto
                 .andExpect(jsonPath("$.clientPhone").value("1234567890"))
                 .andExpect(jsonPath("$.notes").value("Test appointment by super admin"))
                 .andExpect(jsonPath("$.status").value("PENDING"))
                 .andExpect(jsonPath("$.emailReminder").value(true))
                 .andExpect(jsonPath("$.reminderSent").value(false));

         // Assert - Verify appointmentRepository count increased   1 from @BeforeEach setUp + 1 new
         assertThat(appointmentRepository.findAll()).hasSize(2);
     }

    @Test
    void testSaveAppointment_Fail_ValidationError() throws Exception {
        //Arrange Create valid AppointmentInsertDTO
        AppointmentInsertDTO appointmentDTO = TestDataFactory.createAppointmentInsertDTO(
                superAdminUser.getId(),
                testClient.getId()
        );
        appointmentDTO.setClientId(null);   //--> set null for bad request status 400

        //Act & Assert: Expect status 400 (Bad Request)
        mockMvc.perform(post("/api/appointments/save")
                .header("Authorization", "Bearer " + superAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSaveAppointment_Fail_Unauthorized() throws Exception {
        //Arrange - Create valid AppointmentInsertDTO
        AppointmentInsertDTO appointmentDTO = TestDataFactory.createAppointmentInsertDTO(
                superAdminUser.getId(),
                testClient.getId()
        );
        appointmentDTO.setAppointmentDateTime(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0));
        appointmentDTO.setNotes("Test appointment by super admin");
        appointmentDTO.setStatus(AppointmentStatus.PENDING);

        //Act & Assert - POST without token , Expect status 401 (Unauthorized)
        mockMvc.perform(post("/api/appointments/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointmentDTO)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // Tests for GET /api/appointments/{id} endpoint
    // Validates read operations with different authorization levels
    @Test
    void testGetAppointmentById_Success_AsSuperAdmin() throws Exception {
        //Act & Assert - GET /api/appointments/{id} with superAdminToken
        mockMvc.perform(get("/api/appointments/{id}", testAppointment.getId())
                        .header("Authorization", "Bearer " + superAdminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAppointment.getId()))
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(jsonPath("$.username").value("superadmin"))
                .andExpect(jsonPath("$.clientName").value("Chuck Norris"))
                .andExpect(jsonPath("$.clientPhone").value("1234567890"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.emailReminder").value(true))
                .andExpect(jsonPath("$.reminderSent").value(false))
                .andExpect(jsonPath("$.notes").value("Test appointment"));
    }

    @Test
    void testGetAppointmentById_Fail_AccessDenied() throws Exception {
        // Arrange - Create another client and their token
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

        // Act & Assert - Try to access testAppointment with wrong token
        mockMvc.perform(get("/api/appointments/{id}", testAppointment.getId())
                        .header("Authorization", "Bearer " + anotherToken))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAppointmentById_Fail_NotFound() throws Exception {
        //Arrange
        Long nonExistentId = 99999L;

        // Act & Assert
        mockMvc.perform(get("/api/appointments/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAppointmentById_Fail_Unauthorized() throws Exception {
        //Act & Assert - GET /api/appointments/{id} without token
        mockMvc.perform(get("/api/appointments/{id}",testAppointment.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    //GET ALL PAGINATED TESTS
    // Tests for GET /api/appointments endpoint with pagination
    // Validates paginated retrieval of appointments

    @Test
    void testGetPaginatedAppointments_Success() throws Exception {
        //Arrange - Create additional appointments
        Appointment appointment2 = TestDataFactory.createDefaultAppointment();
        appointment2.setUser(superAdminUser);
        appointment2.setClient(testClient);
        appointment2.setAppointmentDateTime(LocalDateTime.now().plusDays(2).withHour(11).withMinute(0).withSecond(0).withNano(0));
        appointment2 = appointmentRepository.save(appointment2);

        Appointment appointment3 = TestDataFactory.createDefaultAppointment();
        appointment3.setUser(superAdminUser);
        appointment3.setClient(testClient);
        appointment3.setAppointmentDateTime(LocalDateTime.now().plusDays(3).withHour(12).withMinute(0).withSecond(0).withNano(0));
        appointment3 = appointmentRepository.save(appointment3);

        // Act & Assert - GET /api/appointments?page=0&size=10 with superAdminToken
        mockMvc.perform(get("/api/appointments")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + superAdminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3))) // 3 total appointments
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0)); // Current page is 0
    }

    //GET SORTED PAGINATED TESTS
    // Tests for GET /api/appointments/sorted endpoint
    // Validates sorting functionality by date

    @Test
    void testGetPaginatedSortedAppointments_SortByDateTime_Desc() throws Exception {
        //Arrange - Create appointments with different dates now+1day from default
        Appointment appointment2 = TestDataFactory.createDefaultAppointment();
        appointment2.setUser(superAdminUser);
        appointment2.setClient(testClient);
        appointment2.setAppointmentDateTime(LocalDateTime.now().plusDays(3).withHour(11).withMinute(0).withSecond(0).withNano(0));
        appointment2 = appointmentRepository.save(appointment2);

        Appointment appointment3 = TestDataFactory.createDefaultAppointment();
        appointment3.setUser(superAdminUser);
        appointment3.setClient(testClient);
        appointment3.setAppointmentDateTime(LocalDateTime.now().plusDays(5).withHour(12).withMinute(0).withSecond(0).withNano(0));
        appointment3 = appointmentRepository.save(appointment3);

        // Act & Assert - GET /api/appointments/sorted?sortBy=appointmentDateTime&sortDirection=desc
        mockMvc.perform(get("/api/appointments/sorted")
                        .param("sortBy", "appointmentDateTime")
                        .param("sortDirection", "desc")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + superAdminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].id").value(appointment3.getId())) // +5 days (latest first)
                .andExpect(jsonPath("$.content[1].id").value(appointment2.getId())) // +3 days
                .andExpect(jsonPath("$.content[2].id").value(testAppointment.getId())); // +1 day (earliest last)
    }

    //GET BY CLIENT TESTS
    // Tests for GET /api/appointments/client/{clientId} endpoint
    // Validates retrieval of appointments for a specific client

    @Test
    void testGetAppointmentsByClient_Success() throws Exception {
        // Arrange - Already 1 from setUp, create 2 more
        Appointment appointment2 = TestDataFactory.createDefaultAppointment();
        appointment2.setUser(superAdminUser);
        appointment2.setClient(testClient);
        appointment2.setAppointmentDateTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0));
        appointment2 = appointmentRepository.save(appointment2);

        Appointment appointment3 = TestDataFactory.createDefaultAppointment();
        appointment3.setUser(superAdminUser);
        appointment3.setClient(testClient);
        appointment3.setAppointmentDateTime(LocalDateTime.now().plusDays(4).withHour(15).withMinute(0).withSecond(0).withNano(0));
        appointment3 = appointmentRepository.save(appointment3);

        // Act & Assert - GET /api/appointments/client/{clientId}
        mockMvc.perform(get("/api/appointments/client/{clientId}", testClient.getId())
                        .header("Authorization", "Bearer " + superAdminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3))) // 3 appointments for testClient
                .andExpect(jsonPath("$[*].clientName", everyItem(is("Chuck Norris")))) // All belong to Chuck Norris
                .andExpect(jsonPath("$[*].clientPhone", everyItem(is("1234567890")))); // All have same phone
    }

    //GET BY STATUS TESTS
    // Tests for GET /api/appointments/status/{status} endpoint
    // Validates filtering appointments by status

    @Test
    void testGetAppointmentsByStatus_Pending() throws Exception {
        //Arrange - Create appointments with different statuses
        Appointment appointment2 = TestDataFactory.createDefaultAppointment();
        appointment2.setUser(superAdminUser);
        appointment2.setClient(testClient);
        appointment2.setAppointmentDateTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0));
        appointment2.setStatus(AppointmentStatus.PENDING);
        appointment2 = appointmentRepository.save(appointment2);

        Appointment appointment3 = TestDataFactory.createDefaultAppointment();
        appointment3.setUser(superAdminUser);
        appointment3.setClient(testClient);
        appointment3.setAppointmentDateTime(LocalDateTime.now().plusDays(4).withHour(15).withMinute(0).withSecond(0).withNano(0));
        appointment3.setStatus(AppointmentStatus.CANCELLED);
        appointment3 = appointmentRepository.save(appointment3);

        Appointment appointment4 = TestDataFactory.createDefaultAppointment();
        appointment4.setUser(superAdminUser);
        appointment4.setClient(testClient);
        appointment4.setAppointmentDateTime(LocalDateTime.now().plusDays(4).withHour(15).withMinute(0).withSecond(0).withNano(0));
        appointment4.setStatus(AppointmentStatus.CONFIRMED);
        appointment4 = appointmentRepository.save(appointment4);


        //Act & Assert - GET /api/appointments/status/PENDING Verify only PENDING appointments are returned
        mockMvc.perform(get("/api/appointments/status/PENDING")
                        .header("Authorization", "Bearer " + superAdminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2))) // Only 2 PENDING (testAppointment + appointment2)
                .andExpect(jsonPath("$[*].status", everyItem(is("PENDING")))) // All have PENDING status
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(
                        testAppointment.getId().intValue(),
                        appointment2.getId().intValue()
                ))); // Verify correct IDs

    }

    //GET UPCOMING TESTS
    // Tests for GET /api/appointments/upcoming endpoint
    // Validates retrieval of future appointments only

    @Test
    void testGetUpcomingAppointments_Success() throws Exception {
        // Arrange - Create appointments: some in the future, some in the past
        // testAppointment from setUp is already in future (now+1day)

        //future appointment
        Appointment futureAppointment = TestDataFactory.createDefaultAppointment();
        futureAppointment.setUser(superAdminUser);
        futureAppointment.setClient(testClient);
        futureAppointment.setAppointmentDateTime(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0));
        futureAppointment = appointmentRepository.save(futureAppointment);

        //past appointments
        Appointment pastAppointment1 = TestDataFactory.createPastAppointment();
        pastAppointment1.setUser(superAdminUser);
        pastAppointment1.setClient(testClient);
        pastAppointment1.setAppointmentDateTime(LocalDateTime.now().minusDays(5).withHour(10).withMinute(0).withSecond(0).withNano(0));
        pastAppointment1 = appointmentRepository.save(pastAppointment1);

        Appointment pastAppointment2 = TestDataFactory.createPastAppointment();
        pastAppointment2.setUser(superAdminUser);
        pastAppointment2.setClient(testClient);
        pastAppointment2.setAppointmentDateTime(LocalDateTime.now().minusDays(2).withHour(11).withMinute(0).withSecond(0).withNano(0));
        pastAppointment2 = appointmentRepository.save(pastAppointment2);

        // Act & Assert - GET /api/appointments/upcoming
        mockMvc.perform(get("/api/appointments/upcoming")
                        .header("Authorization", "Bearer " + superAdminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2))) // Only 2 future appointments
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(
                        testAppointment.getId().intValue(),
                        futureAppointment.getId().intValue()
                ))) // Verify correct IDs (only future ones)
                .andExpect(jsonPath("$[*].id", not(hasItem(pastAppointment1.getId().intValue())))) // Past appointments NOT included
                .andExpect(jsonPath("$[*].id", not(hasItem(pastAppointment2.getId().intValue())))); // Past appointments NOT included
    }


    // GET PENDING EMAIL REMINDERS TESTS
    // Tests for GET /api/appointments/reminders/pending endpoint
    // Validates retrieval of appointments with unsent email reminders

//    @Test
//    void testGetPendingEmailReminders_Success() throws Exception {
//        //  Arrange - Create appointments with different reminder states
//        //  Create 2 appointments with emailReminder=true and reminderSent=false
//        //  Create 1 appointment with emailReminder=false
//        //  Create 1 appointment with reminderSent=true
//
//        //  Act & Assert - GET /api/appointments/reminders/pending
//        // Verify only appointments with pending reminders are returned
//        // Verify count matches appointments with emailReminder=true and reminderSent=false
//    }
}