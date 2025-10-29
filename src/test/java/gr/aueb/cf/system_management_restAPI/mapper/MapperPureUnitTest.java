package gr.aueb.cf.system_management_restAPI.mapper;

import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import gr.aueb.cf.system_management_restAPI.core.enums.Role;
import gr.aueb.cf.system_management_restAPI.dto.*;
import gr.aueb.cf.system_management_restAPI.model.Appointment;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.PersonalInfo;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.model.static_data.City;
import gr.aueb.cf.system_management_restAPI.core.enums.GenderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Pure unit test:
 * - Tests the Mapper methods **without loading Spring context**.
 * - Dependencies (e.g., CityRepository, PasswordEncoder) are passed as null
 *   if they are not used in the specific method.
 * - Faster execution than @SpringBootTest.
 * - Demonstrates that mapping logic works independently of Spring.
 * - The rest mapping methods are covered in MapperWithSpringContextTest ready for testing.
 */

class MapperPureUnitTest {

    private Mapper mapper;

    @BeforeEach
    void setUp() {
        // Create Mapper manually (no Spring context)
        // pass nulls for dependencies -->
        // (private final PasswordEncoder passwordEncoder;
        // private final CityRepository cityRepository;) from mapper injection
        // not used in this test
        mapper = new Mapper(null, null);
    }

    @Test
    void mapToPersonalInfoEntity() {
        PersonalInfoInsertDTO dto = new PersonalInfoInsertDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        dto.setPhone("6971234567");
        dto.setAddress("Test Street 12");
        dto.setDateOfBirth(LocalDate.of(1990, 5, 15));
        dto.setGender(GenderType.MALE);
        dto.setCityId(null);

        //manually without Spring
        Mapper mapper = new Mapper(null, null);

        //ACT: call the method under test
        PersonalInfo pi = mapper.mapToPersonalInfoEntity(dto);


        // Assert: verify fields are mapped correctly
        assertNotNull(pi);
        assertEquals(dto.getFirstName(), pi.getFirstName());
        assertEquals(dto.getLastName(), pi.getLastName());
        assertEquals(dto.getEmail(), pi.getEmail());
        assertEquals(dto.getPhone(), pi.getPhone());
        assertEquals(dto.getAddress(), pi.getAddress());
        assertEquals(dto.getDateOfBirth(), pi.getDateOfBirth());
        assertEquals(dto.getGender(), pi.getGender());
        assertNull(pi.getCity());
    }

    @Test
    void updatePersonalInfoFromDTO() {

        //create an existing PersonalInfo
        PersonalInfo existingEntity = new PersonalInfo();
        existingEntity.setId(10L);
        existingEntity.setFirstName("OldName");
        existingEntity.setLastName("OldLast");
        existingEntity.setEmail("old@example.com");
        existingEntity.setPhone("6000000000");
        existingEntity.setAddress("Old Address");
        existingEntity.setDateOfBirth(LocalDate.of(1980, 1, 1));
        existingEntity.setGender(GenderType.FEMALE);


        // Create DTO with new values for update
        PersonalInfoUpdateDTO dto = new PersonalInfoUpdateDTO();
        dto.setFirstName("NewName");
        dto.setLastName("NewLast");
        dto.setEmail("new@example.com");
        dto.setPhone("6999999999");
        dto.setAddress("New Address");
        dto.setDateOfBirth(LocalDate.of(1995, 5, 15));
        dto.setGender(GenderType.MALE);
        dto.setCityId(null);

        Mapper mapper = new Mapper(null, null);

        //ACT
        mapper.updatePersonalInfoFromDTO(dto, existingEntity);


        // ASSERT
        assertEquals(dto.getFirstName(), existingEntity.getFirstName());
        assertEquals(dto.getLastName(), existingEntity.getLastName());
        assertEquals(dto.getEmail(), existingEntity.getEmail());
        assertEquals(dto.getPhone(), existingEntity.getPhone());
        assertEquals(dto.getAddress(), existingEntity.getAddress());
        assertEquals(dto.getDateOfBirth(), existingEntity.getDateOfBirth());
        assertEquals(dto.getGender(), existingEntity.getGender());
        assertEquals(10L, existingEntity.getId());
    }

    @Test
    void mapToUserReadOnlyDTO() {

            // given
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");
            user.setEmail("test@aueb.gr");
            user.setUuid(UUID.randomUUID().toString());
            user.setRole(Role.CLIENT);
            user.setIsActive(true);

            // act
            UserReadOnlyDTO dto = mapper.mapToUserReadOnlyDTO(user);

            // assert
            assertNotNull(dto);
            assertEquals(user.getId(), dto.getId());
            assertEquals(user.getUsername(), dto.getUsername());
            assertEquals(user.getEmail(), dto.getEmail());
            assertEquals(user.getRole(), dto.getRole());
            assertEquals(user.getIsActive(), dto.getIsActive());


    }

    @Test
    void mapToUserEntity() {
        UserInsertDTO dto = new UserInsertDTO();
        dto.setUsername("john_doe");
        dto.setPassword("PlainPassword123"); // in pure unit we won't encode it
        dto.setEmail("john@example.com");
        dto.setRole(Role.CLIENT);
        dto.setIsActive(true);

        // Dummy PasswordEncoder για pure unit test
        PasswordEncoder dummyEncoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString(); //  επιστρέφει το ίδιο password
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        };

        // Mapper χωρίς Spring context, περνάμε dummy encoder
        Mapper mapper = new Mapper(dummyEncoder, null);

        // Act:
        User entity = mapper.mapToUserEntity(dto);


        // Assert
        assertNotNull(entity);
        assertEquals(dto.getUsername(),entity.getUsername());
        assertEquals(dto.getPassword(),entity.getPassword());
        assertEquals(dto.getEmail(),entity.getEmail());
        assertEquals(dto.getRole(),entity.getRole());
        assertEquals(dto.getIsActive(), entity.getIsActive());
    }

    @Test
    void mapToAppointmentReadOnlyDTO() {
        // Arrange
        User user = new User();
        user.setUsername("john_doe");

        Client client = new Client();
        client.setId(10L);
        client.setUuid("client-uuid-123");

        // Create PersonalInfo and attach it
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setFirstName("John");
        personalInfo.setLastName("Doe");
        personalInfo.setPhone("6999999999");

        client.setPersonalInfo(personalInfo);

        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setUuid("abc-uuid-123");
        appointment.setUser(user);
        appointment.setClient(client);
        appointment.setAppointmentDateTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setEmailReminder(true);
        appointment.setReminderDateTime(LocalDateTime.of(2024, 12, 31, 12, 0));
        appointment.setReminderSent(false);
        appointment.setNotes("Test Notes");
        appointment.setCreatedAt(LocalDateTime.of(2024, 11, 1, 8, 0));
        appointment.setUpdatedAt(LocalDateTime.of(2024, 11, 15, 8, 0));

        Mapper mapper = new Mapper(null, null);

        // Act
        AppointmentReadOnlyDTO dto = mapper.mapToAppointmentReadOnlyDTO(appointment);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("abc-uuid-123", dto.getUuid());
        assertEquals("john_doe", dto.getUsername());
        assertEquals("John Doe", dto.getClientName());
        assertEquals("Doe", dto.getClientLastName());
        assertEquals("6999999999", dto.getClientPhone());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), dto.getAppointmentDateTime());
        assertEquals(AppointmentStatus.CONFIRMED, dto.getStatus());
        assertTrue(dto.getEmailReminder());
        assertEquals(LocalDateTime.of(2024, 12, 31, 12, 0), dto.getReminderDateTime());
        assertFalse(dto.getReminderSent());
        assertEquals("Test Notes", dto.getNotes());
        assertEquals(LocalDateTime.of(2024, 11, 1, 8, 0), dto.getCreatedAt());
        assertEquals(LocalDateTime.of(2024, 11, 15, 8, 0), dto.getUpdatedAt());
    }


    @Test
    void mapToAppointmentEntity() {
        AppointmentInsertDTO dto = new AppointmentInsertDTO();
        dto.setClientId(10L);
        dto.setUserId(20L);
        dto.setEmailReminder(true);
        dto.setAppointmentDateTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        dto.setNotes("Test insert");


        Client client = new Client();
        client.setId(10L);

        User user = new User();
        user.setId(20L);

        Mapper mapper = new Mapper(null, null);

        Appointment entity = mapper.mapToAppointmentEntity(dto);

        // Manually attach client beacause mapper don't
        entity.setClient(client);
        entity.setUser(user);

        //ASSERT
        assertNotNull(entity);
        assertEquals(dto.getEmailReminder(),entity.getEmailReminder());
        assertEquals(dto.getAppointmentDateTime(),entity.getAppointmentDateTime());
        assertEquals(dto.getNotes(),entity.getNotes());

        // Επειδή δεν κάνουμε resolve από repo, απλά ελέγχουμε ότι δημιουργούνται αντικείμενα με id
        assertNotNull(entity.getClient());
        assertEquals(10L, entity.getClient().getId());

        assertNotNull(entity.getUser());
        assertEquals(20L, entity.getUser().getId());
    }

    @Test
    void updateAppointmentFromDTO() {

        Appointment existingAppointment = new Appointment();
        existingAppointment.setId(10L);
        existingAppointment.setAppointmentDateTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        existingAppointment.setEmailReminder(true);
        existingAppointment.setReminderDateTime(LocalDateTime.of(2024, 12, 31, 10, 0));
        existingAppointment.setNotes("Old Notes Test insert");



        AppointmentUpdateDTO dto = new AppointmentUpdateDTO();
        dto.setAppointmentDateTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        dto.setEmailReminder(true);
        dto.setReminderDateTime(LocalDateTime.of(2024, 12, 31, 10, 0));
        dto.setNotes("New Test insert");


        Mapper mapper = new Mapper(null, null);

        //ACT
        mapper.updateAppointmentFromDTO(dto, existingAppointment);

        // ASSERT
        assertEquals(dto.getAppointmentDateTime(),existingAppointment.getAppointmentDateTime());
        assertEquals(dto.getEmailReminder(),existingAppointment.getEmailReminder());
        assertEquals(dto.getReminderDateTime(),existingAppointment.getReminderDateTime());
        assertEquals(dto.getNotes(),existingAppointment.getNotes());
    }
}
