package gr.aueb.cf.system_management_restAPI.service.appointment_service_tests;

import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class AppointmentServiceBasicReadTest {

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
     * Should get appointment by ID.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"CLIENT"})
    void getAppointmentById_ShouldReturn_WhenExists() throws Exception {
        // given
        Appointment existing = appointmentRepository.findAll().get(0);

        // act
        AppointmentReadOnlyDTO result = appointmentService.getAppointmentById(existing.getId());

        // assert
        assertNotNull(result, "DTO must not be null");
        assertEquals(existing.getId(), result.getId(), "ID no match");
        assertEquals(existing.getUser().getUsername(), result.getUsername(), "username no match");
        assertEquals(
                existing.getClient().getPersonalInfo().getFirstName() + " " + existing.getClient().getPersonalInfo().getLastName(),
                result.getClientName()
        );
        assertEquals(existing.getStatus(), result.getStatus(), "status no match");
    }

    /**
     * Should throw exception when ID not found.
     */
    @Test
    void getAppointmentById_ShouldThrowException_WhenNotFound() {
        // given
        Long nonExistingId = 9999L;

        // act & assert
        assertThrows(AppObjectNotFoundException.class,
                () -> appointmentService.getAppointmentById(nonExistingId));
    }

    /**
     * Should get appointment by UUID.
     */
    @Test
    void getAppointmentByUuid_ShouldReturn_WhenExists() throws Exception {
        // given
        Appointment appointment = appointmentRepository.findAll().get(0);

        // act
        AppointmentReadOnlyDTO result = appointmentService.getAppointmentByUuid(appointment.getUuid());

        // assert
        assertNotNull(result);
        assertEquals(appointment.getUuid(),result.getUuid());
    }

    /**
     * Creates dummy appointments for testing
     */
    private void createDummyAppointments() {
        if (appointmentRepository.count() > 0) return;

        // userη
        User user = TestDataFactory.createDefaultUser();
        user.setUsername("testuser");
        user.setEmail(TestDataFactory.generateUniqueEmail());
        testUser = userRepository.saveAndFlush(user); //  user

        //  client
        Client client = TestDataFactory.createDefaultClient();
        client.setUser(testUser); // link
        client.getPersonalInfo().setEmail(TestDataFactory.generateUniqueEmail());
        client.getPersonalInfo().setPhone(TestDataFactory.generateUniquePhone());
        testClient = clientRepository.saveAndFlush(client);

        // appointments
        Appointment apt1 = TestDataFactory.createDefaultAppointment();
        apt1.setUser(testUser);  // persisted user
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