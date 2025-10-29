package gr.aueb.cf.system_management_restAPI.service.appointment_service_tests;

import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentInsertDTO;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.model.Appointment;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.AppointmentRepository;
import gr.aueb.cf.system_management_restAPI.repository.ClientRepository;
import gr.aueb.cf.system_management_restAPI.repository.PersonalInfoRepository;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import gr.aueb.cf.system_management_restAPI.service.AppointmentService;
import gr.aueb.cf.system_management_restAPI.util.TestDBHelper;
import gr.aueb.cf.system_management_restAPI.util.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class AppointmentServiceFilteringReadTest {

    @Autowired
    private AppointmentService appointmentService;


    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    PersonalInfoRepository personalInfoRepository;

    @Autowired
    private DataSource dataSource;

    private User testUser;
    private Client testClient;

    @BeforeAll
    void setupClass() throws SQLException {
        TestDBHelper.eraseData(dataSource);
    }

    @BeforeEach
    void setup() {
        createDummyAppointments();
    }

    /**
     * Should get appointments by client.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"CLIENT"})
    void getAppointmentsByClient_ShouldReturnList_WhenClientHasAppointments() throws AppObjectInvalidArgumentException, AppObjectNotFoundException, AppObjectAlreadyExists, AppObjectNotAuthorizedException {
        // given
        AppointmentInsertDTO dto = TestDataFactory.createAppointmentInsertDTO(
                testUser.getId(),
                testClient.getId()
        );
        dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        dto.setEmailReminder(false); // reminderSent = false
        appointmentService.saveAppointment(dto);

        // act
        List<AppointmentReadOnlyDTO> result = appointmentService.getAppointmentsByClient(testClient.getId());
//        result.forEach(a -> {
//            System.out.println(a.getClientName() + " " + a.getClientLastName() + " " + a.getClientPhone());
//        });
//        System.out.println(testClient.getPersonalInfo().getFirstName() + " " + testClient.
//                getPersonalInfo().getLastName() + " " + testClient
//                .getPersonalInfo().getPhone());

        // assert
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isEmpty(), "Result should not be empty");
        String expectedFullName = testClient.getPersonalInfo().getFirstName() + " " +
                testClient.getPersonalInfo().getLastName();                             //mapper returns LastName/ firstName
        // to getClientName

        result.forEach(a -> {
            assertEquals(expectedFullName, a.getClientName(), "Full name mismatch");
            assertEquals(testClient.getPersonalInfo().getPhone(), a.getClientPhone(), "Phone mismatch");
        });
    }

    /**
     * Should get appointments by user.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"SUPER_ADMIN"})
    void getAppointmentsByUser_ShouldReturnList_WhenUserHasAppointments() throws AppObjectNotFoundException {
        // given
        Long userId = testUser.getId();

        // act
        List<AppointmentReadOnlyDTO> result = appointmentService.getAppointmentsByUser(userId);

        // assert
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isEmpty(), "Result should not be empty");
        assertEquals(3, result.size(), "Should return the 3 dummy appointments");

        result.forEach(a -> {
            assertEquals(testUser.getUsername(), a.getUsername(), "Username mismatch");
            assertEquals(testClient.getPersonalInfo().getPhone(), a.getClientPhone(), "Client phone mismatch");
        });
        System.out.println("Appointments for user " + testUser.getUsername() + ": " + result.size());
    }


    /**
     * Should get appointments by status.
     */
    @Test
    @WithMockUser(username = "superadmin", roles = {"SUPER_ADMIN"})
    void getAppointmentsByStatus_ShouldReturnList_WhenStatusMatches() {
        // given
        Appointment app1 = TestDataFactory.createAppointmentWithStatus(AppointmentStatus.PENDING);
        app1.setUser(testUser);
        app1.setClient(testClient);

        Appointment app2 = TestDataFactory.createAppointmentWithStatus(AppointmentStatus.PENDING);
        app2.setUser(testUser);
        app2.setClient(testClient);

        Appointment app3 = TestDataFactory.createAppointmentWithStatus(AppointmentStatus.PENDING);
        app3.setUser(testUser);
        app3.setClient(testClient);

        Appointment app4 = TestDataFactory.createAppointmentWithStatus(AppointmentStatus.PENDING);
        app4.setUser(testUser);
        app4.setClient(testClient);

        appointmentRepository.saveAll(List.of(app1, app2, app3, app4));
        appointmentRepository.flush();

        // act
        List<AppointmentReadOnlyDTO> result = appointmentService.getAppointmentsByStatus(AppointmentStatus.PENDING);


        // assert
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isEmpty(), "Result should not be empty");
        assertTrue(result.stream().allMatch(a -> a.getStatus() == AppointmentStatus.PENDING),
                "All appointments should have status PENDING");
    }

    /**
     * Should get appointments by client phone.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"SUPER_ADMIN"})
    void getAppointmentsByClientPhone_ShouldReturnList_WhenPhoneExists() throws Exception {
        // given
        String clientPhone = testClient.getPersonalInfo().getPhone();
        System.out.println("Using phone: " + clientPhone);

        Appointment extraAppointment = TestDataFactory.createDefaultAppointment();
        extraAppointment.setUser(testUser);
        extraAppointment.setClient(testClient);
        extraAppointment.setAppointmentDateTime(LocalDateTime.now().plusDays(3));

        // save
        personalInfoRepository.saveAndFlush(testClient.getPersonalInfo());
        clientRepository.saveAndFlush(testClient);
        appointmentRepository.saveAndFlush(extraAppointment);

        // act
        List<AppointmentReadOnlyDTO> result = appointmentService.getAppointmentsByClientPhone(clientPhone);

        // assert
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isEmpty(), "Result should not be empty");
        result.forEach(a -> {
            assertEquals(clientPhone, a.getClientPhone(), "Client phone should match");
            assertEquals(
                    testClient.getPersonalInfo().getFirstName() + " " + testClient.getPersonalInfo().getLastName(),
                    a.getClientName(),
                    "Client name should match"
            );
        });
    }

    /**
     * Creates dummy appointments for testing
     */
    private void createDummyAppointments() {
        if (appointmentRepository.count() > 0) return;

        // user
        User user = TestDataFactory.createDefaultUser();
        user.setUsername("testuser");
        user.setEmail(TestDataFactory.generateUniqueEmail());
        testUser = userRepository.saveAndFlush(user); // Αποθηκεύουμε πρώτα τον user

        // client
        Client client = TestDataFactory.createDefaultClient();
        client.setUser(testUser); // Συσχέτιση με τον user
        client.getPersonalInfo().setEmail(TestDataFactory.generateUniqueEmail());
        client.getPersonalInfo().setPhone(TestDataFactory.generateUniquePhone());
        testClient = clientRepository.saveAndFlush(client);

        //  appointments
        Appointment apt1 = TestDataFactory.createDefaultAppointment();
        apt1.setUser(testUser);  // Σιγουρευόμαστε ότι είναι ο persisted user
        apt1.setClient(testClient);
        apt1.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        appointmentRepository.saveAndFlush(apt1);

        Appointment apt2 = TestDataFactory.createAppointmentWithStatus(AppointmentStatus.CONFIRMED);
        apt2.setUser(testUser);
        apt2.setClient(testClient);
        apt2.setAppointmentDateTime(LocalDateTime.now().plusDays(2));
        appointmentRepository.saveAndFlush(apt2);

        Appointment apt3 = TestDataFactory.createPastAppointment();
        apt3.setUser(testUser);
        apt3.setClient(testClient);
        apt3.setAppointmentDateTime(LocalDateTime.now().minusDays(1));
        apt3.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.saveAndFlush(apt3);

//        System.out.println("Saved appointments in DB:");
//        appointmentRepository.findAll().forEach(a -> {
//            System.out.println("Appointment ID: " + a.getId() +
//                    ", User: " + a.getUser().getUsername() +
//                    ", Status: " + a.getStatus() +
//                    ", Client: " + a.getClient().getPersonalInfo().getFirstName());
//        });
    }
}
