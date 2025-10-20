package gr.aueb.cf.system_management_restAPI.service.appointment_service_tests;

import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentInsertDTO;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentUpdateDTO;
import gr.aueb.cf.system_management_restAPI.model.Appointment;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.AppointmentRepository;
import gr.aueb.cf.system_management_restAPI.repository.ClientRepository;
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
class AppointmentServiceCrudTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

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
     * Should save appointment successfully.
     */
    @Test
    @WithMockUser(username = "testuser")
    void saveAppointment_ShouldSave_WhenValidData() throws Exception {
        // given
        AppointmentInsertDTO dto = TestDataFactory.createAppointmentInsertDTO(
                testUser.getId(),
                testClient.getId()
        );
        dto.setAppointmentDateTime(LocalDateTime.now().plusDays(5).withHour(14).withMinute(0)
                .withSecond(0).withNano(0));

        // act
        AppointmentReadOnlyDTO result = appointmentService.saveAppointment(dto);

        // assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getUuid());
        assertTrue(appointmentRepository.existsById(result.getId()));
        assertEquals(dto.getAppointmentDateTime(), result.getAppointmentDateTime());
        assertEquals(dto.getStatus(), result.getStatus());
    }

    /**
     * Should throw exception when user not found.
     */
    @Test
    @WithMockUser(username = "testuser")
    void saveAppointment_ShouldThrowException_WhenClientNotFound() {
        // given
        Long nonExistingClientId  = 9999L;
        AppointmentInsertDTO appointmentInsertDTO = TestDataFactory
                .createAppointmentInsertDTO(testUser.getId(), nonExistingClientId);
        //Change time to avoid conflict!
        appointmentInsertDTO.setAppointmentDateTime(
                LocalDateTime.now().plusDays(10).withHour(16).withMinute(0)
                        .withSecond(0).withNano(0)
        );
        // act & assert
        assertThrows(AppObjectNotFoundException.class,
                () -> appointmentService.saveAppointment(appointmentInsertDTO));
    }

    /**
     * Should throw exception when appointment time conflict.
     */
    @Test
    @WithMockUser(username = "testuser")
    void saveAppointment_ShouldThrowException_WhenTimeConflict() {
        // given
        AppointmentInsertDTO dto = TestDataFactory.createAppointmentInsertDTO(
                testUser.getId(),
                testClient.getId()
        );
        // act & assert
        assertThrows(
                AppObjectAlreadyExists.class,
                () -> appointmentService.saveAppointment(dto)
        );
    }

    /**
     * Should update appointment successfully.
     */
    @Test
    @WithMockUser(username = "testuser")
    void updateAppointment_ShouldUpdate_WhenValidData() throws Exception {
        // given
        Appointment appointment = appointmentRepository.findAll().get(0);

        if (appointment.getAppointmentDateTime() == null) {
            appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            appointmentRepository.save(appointment);
        }

        AppointmentUpdateDTO dto = new AppointmentUpdateDTO();
        dto.setStatus(AppointmentStatus.CANCELLED);
        dto.setNotes("Updated notes for testing");

        // act
        AppointmentReadOnlyDTO result = appointmentService.updateAppointment(appointment.getId(), dto);

        // assert
        assertNotNull(result);
        assertEquals(appointment.getId(), result.getId());
        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());
        assertEquals("Updated notes for testing", result.getNotes());
    }


    /**
     * Should throw exception when updating non-existing appointment.
     */
    @Test
    void updateAppointment_ShouldThrowException_WhenNotFound() {
        // given
        Long nonExistingClientId  = 9999L;
        AppointmentInsertDTO appointmentInsertDTO = TestDataFactory
                .createAppointmentInsertDTO(testUser.getId(), nonExistingClientId);

        // act & assert
        assertThrows(AppObjectNotFoundException.class,
                () -> appointmentService.saveAppointment(appointmentInsertDTO));
    }

    /**
     * Should delete appointment successfully.
     */
    @Test
    void deleteAppointment_ShouldDelete_WhenExists() throws Exception {
        // given
        createDummyAppointments();
        Appointment aptToDelete = appointmentRepository.findAll().get(0);

        // act
        appointmentService.deleteAppointment(aptToDelete.getId());

        // assert
        assertFalse(appointmentRepository.existsById(aptToDelete.getId()));
    }

    /**
     * Should throw exception when deleting non-existing appointment.
     */
    @Test
    void deleteAppointment_ShouldThrowException_WhenNotFound() {
        // given
        Long nonExistingId = 9999L;

        // act & assert
        assertThrows(AppObjectNotFoundException.class,
                () -> appointmentService.deleteAppointment(nonExistingId));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"SUPER_ADMIN"})
    void markReminderAsSent_ShouldUpdate_WhenExists() throws Exception {
        // given
        AppointmentInsertDTO dto = TestDataFactory.createAppointmentInsertDTO(
                testUser.getId(),
                testClient.getId()
        );
        dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        dto.setEmailReminder(false); // reminderSent = false

        // save the appointment
        AppointmentReadOnlyDTO savedAppointment = appointmentService.saveAppointment(dto);

        // act
        appointmentService.markReminderAsSent(savedAppointment.getId());

        // assert
        Appointment updatedAppointment = appointmentRepository.findById(savedAppointment.getId())
                .orElseThrow(() -> new AssertionError("Appointment not found"));
        assertTrue(updatedAppointment.getReminderSent(), "Reminder should be marked as sent");
    }


    /**
     * Creates dummy appointments for testing
     */
    private void createDummyAppointments() {
        if (appointmentRepository.count() > 0) return;

        // Create and save user
        User user = TestDataFactory.createDefaultUser();
        user.setUsername("testuser");
        user.setEmail(TestDataFactory.generateUniqueEmail()); // ← UNIQUE!
        testUser = userRepository.save(user);

        // Create and save client (same pattern as ClientService tests)
        Client client = TestDataFactory.createDefaultClient();
        client.setUser(testUser);  // Use the saved user
        client.getUser().setEmail(testUser.getEmail()); // ← Ensure same email!
        client.getPersonalInfo().setEmail(TestDataFactory.generateUniqueEmail());
        client.getPersonalInfo().setPhone(TestDataFactory.generateUniquePhone());
        testClient = clientRepository.save(client);


        // Appointment 1 - PENDING, tomorrow
        Appointment apt1 = TestDataFactory.createDefaultAppointment();
        apt1.setUser(testUser);
        apt1.setClient(testClient);
        appointmentRepository.save(apt1);

        // Appointment 2 - CONFIRMED, in 2 days
        Appointment apt2 = TestDataFactory.createAppointmentWithStatus(AppointmentStatus.CONFIRMED);
        apt2.setAppointmentDateTime(LocalDateTime.now().plusDays(2));
        apt2.setUser(testUser);
        apt2.setClient(testClient);
        appointmentRepository.save(apt2);

        // Appointment 3 - COMPLETED, past
        Appointment apt3 = TestDataFactory.createPastAppointment();
        apt3.setStatus(AppointmentStatus.COMPLETED);
        apt3.setUser(testUser);
        apt3.setClient(testClient);
        appointmentRepository.save(apt3);
    }
}