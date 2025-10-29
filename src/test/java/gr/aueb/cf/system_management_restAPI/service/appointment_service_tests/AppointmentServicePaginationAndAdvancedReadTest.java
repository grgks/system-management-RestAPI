package gr.aueb.cf.system_management_restAPI.service.appointment_service_tests;

import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import gr.aueb.cf.system_management_restAPI.core.filters.AppointmentFilters;
import gr.aueb.cf.system_management_restAPI.core.filters.Paginated;
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
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class AppointmentServicePaginationAndAdvancedReadTest {

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
     * Should get paginated appointments.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"CLIENT"})
    void getPaginatedAppointments_ShouldReturnPage_WhenCalled() {
        // given
        int page = 0;
        int size = 10;

        // act
        Page<AppointmentReadOnlyDTO> result = appointmentService.getPaginatedAppointments(page,size);

        // assert
        assertNotNull(result);
        assertTrue(result.hasContent());
        assertEquals(3, result.getTotalElements(), "Total elements must match dummy appointments");
    }

    /**
     * Should get paginated sorted appointments.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"SUPER_ADMIN"})
    void getPaginatedSortedAppointments_ShouldReturnSorted_WhenCalled() {
        // given
        int page = 0;
        int size = 10;
        String sortField = "appointmentDateTime"; // field to Appointment
        String sortDirection = "ASC";

        // act
        Page<AppointmentReadOnlyDTO> result = appointmentService
                .getPaginatedSortedAppointments(page,size,sortField,sortDirection);

        // assert
        List<LocalDateTime> dateTimes = result.getContent()
                .stream()
                .map(AppointmentReadOnlyDTO::getAppointmentDateTime)
                .collect(Collectors.toList());

        List<LocalDateTime> sorted = new ArrayList<>(dateTimes);
        sorted.sort(Comparator.naturalOrder());

        assertEquals(sorted, dateTimes, "Appointments should be sorted ascending by appointmentDateTime");
    }

    /**
     * Should get filtered paginated appointments.
     */
    @Test
    void getAppointmentsFilteredPaginated_ShouldReturnFiltered_WhenFilters() {
        // given
        String existingVat = clientRepository.findAll().get(0).getVat();
        AppointmentFilters filters = AppointmentFilters.builder().clientVat(existingVat).build();

        // act
        Paginated<AppointmentReadOnlyDTO> result = appointmentService.getAppointmentsFilteredPaginated(filters);
        // assert
        assertNotNull(result, "Paginated result should not be null");
        assertFalse(result.getData().isEmpty(), "Results should not be empty");
        String expectedName = testClient.getPersonalInfo().getPhone();
        System.out.println(expectedName);
        assertTrue(result.getData().stream()
                        .allMatch(a -> expectedName.equals(a.getClientPhone())),
                "All results should match the client name filter");
    }

    /**
     * Should get upcoming appointments.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"SUPER_ADMIN"})
    void getUpcomingAppointments_ShouldReturnFuture_WhenCalled() {
        // given
        // Past appointment με PENDING status
        Appointment past = new Appointment();
        past.setUser(testUser);
        past.setClient(testClient);
        past.setAppointmentDateTime(LocalDateTime.now().minusDays(5));
        past.setStatus(AppointmentStatus.PENDING);
        past.setUuid(UUID.randomUUID().toString());
        past.setEmailReminder(false);
        past.setReminderSent(false);
        past.setNotes("Past appointment");
        past.setCreatedAt(LocalDateTime.now());
        past.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.saveAndFlush(past);

        // Future appointment με PENDING status (returns)
        Appointment future = new Appointment();
        future.setUser(testUser);
        future.setClient(testClient);
        future.setAppointmentDateTime(LocalDateTime.now().plusDays(5));
        future.setStatus(AppointmentStatus.PENDING);
        future.setUuid(UUID.randomUUID().toString());
        future.setEmailReminder(false);
        future.setReminderSent(false);
        future.setNotes("Future appointment");
        future.setCreatedAt(LocalDateTime.now());
        future.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.saveAndFlush(future);

        // act
        List<AppointmentReadOnlyDTO> upcoming = appointmentService.getUpcomingAppointments();

        // assert
        assertThat(upcoming).isNotEmpty();
        assertThat(upcoming).hasSize(2);
        assertThat(upcoming.stream()
                .anyMatch(a -> a.getUuid().equals(future.getUuid())))
                .isTrue();
        assertThat(upcoming.stream()
                .noneMatch(a -> a.getUuid().equals(past.getUuid())))
                .isTrue();
        assertThat(upcoming.stream()
                .allMatch(a -> a.getAppointmentDateTime().isAfter(LocalDateTime.now())
                        && a.getStatus() == AppointmentStatus.PENDING))
                .isTrue();
    }

    /**
     * Should get appointments between dates.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"SUPER_ADMIN"})
    void getUpcomingAppointments_ShouldReturnOnlyFuture_WhenCalled() throws Exception {
        // given  από @BeforeEach

        // Past appointment - PENDING status
        Appointment past = new Appointment();
        past.setUser(testUser);
        past.setClient(testClient);
        past.setAppointmentDateTime(LocalDateTime.now().minusDays(5));
        past.setStatus(AppointmentStatus.PENDING);
        past.setUuid(UUID.randomUUID().toString());
        past.setEmailReminder(false);
        past.setReminderSent(false);
        past.setNotes("Past appointment");
        past.setCreatedAt(LocalDateTime.now());
        past.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.saveAndFlush(past);

        // Future appointment - PENDING status
        Appointment future = new Appointment();
        future.setUser(testUser);
        future.setClient(testClient);
        future.setAppointmentDateTime(LocalDateTime.now().plusDays(5));
        future.setStatus(AppointmentStatus.PENDING);
        future.setUuid(UUID.randomUUID().toString());
        future.setEmailReminder(false);
        future.setReminderSent(false);
        future.setNotes("Future appointment");
        future.setCreatedAt(LocalDateTime.now());
        future.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.saveAndFlush(future);

        // act
        List<AppointmentReadOnlyDTO> upcoming = appointmentService.getUpcomingAppointments();

        // assert
        assertThat(upcoming).isNotEmpty();
        assertThat(upcoming).hasSize(2);

        // check future exists
        assertThat(upcoming.stream()
                .anyMatch(a -> a.getUuid().equals(future.getUuid())))
                .isTrue();

        // check pasts not exists
        assertThat(upcoming.stream()
                .noneMatch(a -> a.getUuid().equals(past.getUuid())))
                .isTrue();
        assertThat(upcoming.stream()
                .allMatch(a -> a.getAppointmentDateTime().isAfter(LocalDateTime.now())
                        && a.getStatus() == AppointmentStatus.PENDING))
                .isTrue();
    }
    /**
     * Should get pending email reminders.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"SUPER_ADMIN"})
    void getPendingEmailReminders_ShouldReturnPending_WhenExists() {
        // given -  reminderSent = false

        // Appointment με pending reminder (returns)
        Appointment withPendingReminder = new Appointment();
        withPendingReminder.setUser(testUser);
        withPendingReminder.setClient(testClient);
        withPendingReminder.setAppointmentDateTime(LocalDateTime.now().plusDays(2));
        withPendingReminder.setStatus(AppointmentStatus.PENDING);
        withPendingReminder.setUuid(UUID.randomUUID().toString());
        withPendingReminder.setEmailReminder(true); // emailReminder = true
        withPendingReminder.setReminderSent(false); // reminderSent = false
        withPendingReminder.setReminderDateTime(LocalDateTime.now().minusHours(1)); // reminder time has passed
        withPendingReminder.setNotes("Appointment with pending reminder");
        withPendingReminder.setCreatedAt(LocalDateTime.now());
        withPendingReminder.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.saveAndFlush(withPendingReminder);

        // Appointment με already sent reminder
        Appointment withSentReminder = new Appointment();
        withSentReminder.setUser(testUser);
        withSentReminder.setClient(testClient);
        withSentReminder.setAppointmentDateTime(LocalDateTime.now().plusDays(3));
        withSentReminder.setStatus(AppointmentStatus.PENDING);
        withSentReminder.setUuid(UUID.randomUUID().toString());
        withSentReminder.setEmailReminder(true);
        withSentReminder.setReminderSent(true); // Already sent
        withSentReminder.setReminderDateTime(LocalDateTime.now().minusHours(2));
        withSentReminder.setNotes("Appointment with sent reminder");
        withSentReminder.setCreatedAt(LocalDateTime.now());
        withSentReminder.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.saveAndFlush(withSentReminder);

        // Appointment no reminder
        Appointment withoutReminder = new Appointment();
        withoutReminder.setUser(testUser);
        withoutReminder.setClient(testClient);
        withoutReminder.setAppointmentDateTime(LocalDateTime.now().plusDays(4));
        withoutReminder.setStatus(AppointmentStatus.PENDING);
        withoutReminder.setUuid(UUID.randomUUID().toString());
        withoutReminder.setEmailReminder(false); // No email reminder
        withoutReminder.setReminderSent(false);
        withoutReminder.setNotes("Appointment without reminder");
        withoutReminder.setCreatedAt(LocalDateTime.now());
        withoutReminder.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.saveAndFlush(withoutReminder);

        // act
        List<AppointmentReadOnlyDTO> pendingReminders = appointmentService.getPendingEmailReminders();

        // assert
        assertThat(pendingReminders).isNotEmpty();
        assertThat(pendingReminders.stream()
                .anyMatch(a -> a.getUuid().equals(withPendingReminder.getUuid())))
                .isTrue();
        assertThat(pendingReminders.stream()
                .noneMatch(a -> a.getUuid().equals(withSentReminder.getUuid())))
                .isTrue();
        assertThat(pendingReminders.stream()
                .noneMatch(a -> a.getUuid().equals(withoutReminder.getUuid())))
                .isTrue();
        assertThat(pendingReminders.stream()
                .allMatch(a -> a.getReminderSent() == null || !a.getReminderSent()))
                .isTrue();
    }

    /**
     * Should get client appointments between dates.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"SUPER_ADMIN"})
    void getClientAppointmentsBetweenDates_ShouldReturnFiltered_WhenCalled() {
        // given
        LocalDateTime startDate = LocalDateTime.now().plusDays(5);
        LocalDateTime endDate = LocalDateTime.now().plusDays(15);

        // Appointment (range)
        Appointment insideRange1 = new Appointment();
        insideRange1.setUser(testUser);
        insideRange1.setClient(testClient);
        insideRange1.setAppointmentDateTime(LocalDateTime.now().plusDays(7)); // in range
        insideRange1.setStatus(AppointmentStatus.PENDING);
        insideRange1.setUuid(UUID.randomUUID().toString());
        insideRange1.setEmailReminder(false);
        insideRange1.setReminderSent(false);
        insideRange1.setNotes("Appointment inside range 1");
        insideRange1.setCreatedAt(LocalDateTime.now());
        insideRange1.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.saveAndFlush(insideRange1);

        // Appointment
        Appointment insideRange2 = new Appointment();
        insideRange2.setUser(testUser);
        insideRange2.setClient(testClient);
        insideRange2.setAppointmentDateTime(LocalDateTime.now().plusDays(10)); // in range
        insideRange2.setStatus(AppointmentStatus.CONFIRMED);
        insideRange2.setUuid(UUID.randomUUID().toString());
        insideRange2.setEmailReminder(false);
        insideRange2.setReminderSent(false);
        insideRange2.setNotes("Appointment inside range 2");
        insideRange2.setCreatedAt(LocalDateTime.now());
        insideRange2.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.saveAndFlush(insideRange2);

        // Appointment (out of range)
        Appointment beforeRange = new Appointment();
        beforeRange.setUser(testUser);
        beforeRange.setClient(testClient);
        beforeRange.setAppointmentDateTime(LocalDateTime.now().plusDays(2)); // before startDate
        beforeRange.setStatus(AppointmentStatus.PENDING);
        beforeRange.setUuid(UUID.randomUUID().toString());
        beforeRange.setEmailReminder(false);
        beforeRange.setReminderSent(false);
        beforeRange.setNotes("Appointment before range");
        beforeRange.setCreatedAt(LocalDateTime.now());
        beforeRange.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.saveAndFlush(beforeRange);

        // Appointment (out of range)
        Appointment afterRange = new Appointment();
        afterRange.setUser(testUser);
        afterRange.setClient(testClient);
        afterRange.setAppointmentDateTime(LocalDateTime.now().plusDays(20)); // Μετά το endDate
        afterRange.setStatus(AppointmentStatus.PENDING);
        afterRange.setUuid(UUID.randomUUID().toString());
        afterRange.setEmailReminder(false);
        afterRange.setReminderSent(false);
        afterRange.setNotes("Appointment after range");
        afterRange.setCreatedAt(LocalDateTime.now());
        afterRange.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.saveAndFlush(afterRange);

        // act
        List<AppointmentReadOnlyDTO> appointments = appointmentService
                .getClientAppointmentsBetweenDates(testClient.getId(), startDate, endDate);

        // assert
        assertThat(appointments).isNotEmpty();
        assertThat(appointments).hasSize(2); //  2 in range
        assertThat(appointments.stream()
                .anyMatch(a -> a.getUuid().equals(insideRange1.getUuid())))
                .isTrue();
        assertThat(appointments.stream()
                .anyMatch(a -> a.getUuid().equals(insideRange2.getUuid())))
                .isTrue();

        assertThat(appointments.stream()
                .noneMatch(a -> a.getUuid().equals(beforeRange.getUuid())))
                .isTrue();
        assertThat(appointments.stream()
                .noneMatch(a -> a.getUuid().equals(afterRange.getUuid())))
                .isTrue();

        // to same client
        assertThat(appointments.stream()
                .allMatch(a -> a.getClientName().equals(
                        testClient.getPersonalInfo().getFirstName() + " " +
                                testClient.getPersonalInfo().getLastName())))
                .isTrue();
        assertThat(appointments.stream()
                .allMatch(a -> !a.getAppointmentDateTime().isBefore(startDate)
                        && !a.getAppointmentDateTime().isAfter(endDate)))
                .isTrue();
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

        //appointments
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
