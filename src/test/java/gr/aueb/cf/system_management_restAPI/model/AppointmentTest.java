package gr.aueb.cf.system_management_restAPI.model;

import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentTest {

    /**
     * Tests the default constructor and setters of Appointment
     */

    @Test
    void defaultConstructor() {

        //create user
        User user = new User();
        user.setId(20L);
        user.setUsername("testuser");

        //create client
        Client client = new Client();
        client.setId(10L);
        client.setUuid("client-uuid-123");

        //create appointment me default constructor/setters
        Appointment ap = new Appointment();
        ap.setId(11L);
        ap.setUuid("app-uuid-123");
        ap.setUser(user);
        ap.setClient(client);
        ap.setAppointmentDateTime(LocalDateTime.of(2026,10, 15,14,0));
        ap.setStatus(AppointmentStatus.PENDING);
        ap.setEmailReminder(true);
        ap.setReminderDateTime(LocalDateTime.of(2026,10, 13,14,0));
        ap.setReminderSent(false);
        ap.setNotes("Testing appointment default const");


        //assert
        assertNotNull(ap);
        assertEquals(11L,ap.getId());
        assertEquals("app-uuid-123",ap.getUuid());
        assertEquals(user,ap.getUser());
        assertEquals(client,ap.getClient());
        assertEquals(LocalDateTime.of(2026,10, 15,14,0),ap.getAppointmentDateTime());
        assertEquals(AppointmentStatus.PENDING,ap.getStatus());
        assertEquals(true,ap.getEmailReminder());
        assertEquals(LocalDateTime.of(2026,10, 13,14,0),ap.getReminderDateTime());
        assertEquals(false,ap.getReminderSent());
        assertEquals("Testing appointment default const",ap.getNotes());
    }

    /**
     * Tests the all-args constructor of Appointment
     */

    @Test
    void allArgsConstructor() {
        //create user
        User user = new User();
        user.setId(20L);
        user.setUsername("testuser");

        //create client
        Client client = new Client();
        client.setId(10L);
        client.setUuid("client-uuid-123");

        Appointment ap = new Appointment(
                11L,
                "app-uuid-123",
                user, client,
                LocalDateTime.of(2026, 10, 15, 14, 0),
                AppointmentStatus.PENDING,
                true,
                LocalDateTime.of(2026, 10, 13, 14, 0),
                false, "Testing appointment default const"
        );

       //Assert
        assertNotNull(ap);
        assertEquals(11L,ap.getId());
        assertEquals("app-uuid-123",ap.getUuid());
        assertEquals(user,ap.getUser());
        assertEquals(client,ap.getClient());
        assertEquals(LocalDateTime.of(2026, 10, 15, 14, 0),ap.getAppointmentDateTime());
        assertEquals(AppointmentStatus.PENDING,ap.getStatus());
        assertEquals(true,ap.getEmailReminder());
        assertEquals(LocalDateTime.of(2026, 10, 13, 14, 0),ap.getReminderDateTime());
        assertEquals(false,ap.getReminderSent());
        assertEquals("Testing appointment default const",ap.getNotes());
    }
}