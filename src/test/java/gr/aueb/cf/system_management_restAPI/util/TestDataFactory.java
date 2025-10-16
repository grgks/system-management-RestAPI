package gr.aueb.cf.system_management_restAPI.util;

import gr.aueb.cf.system_management_restAPI.core.enums.GenderType;
import gr.aueb.cf.system_management_restAPI.core.enums.Role;
import gr.aueb.cf.system_management_restAPI.dto.ClientInsertDTO;
import gr.aueb.cf.system_management_restAPI.dto.PersonalInfoInsertDTO;
import gr.aueb.cf.system_management_restAPI.dto.UserInsertDTO;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.PersonalInfo;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.model.static_data.City;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory class for creating test data objects with default values.
 * Reduces boilerplate code in tests.
 */
public class TestDataFactory {

    //Entities

    /**
     * Creates a default City (Athens)
     */
    public static City createDefaultCity() {
        City city = new City();
        city.setId(1L);
        city.setName("Athens");
        city.setPostalCode("12345");
        return city;
    }

    /**
     * Creates a default PersonalInfo
     */
    public static PersonalInfo createDefaultPersonalInfo() {
        PersonalInfo pi = new PersonalInfo();
        pi.setFirstName("John");
        pi.setLastName("Doe");
        pi.setEmail("john.doe@aueb.gr");
        pi.setPhone("6971234567");
        pi.setDateOfBirth(LocalDate.of(1990, 5, 15));
        pi.setGender(GenderType.MALE);
        pi.setAddress("Test Street 12");
        pi.setCity(createDefaultCity());
        return pi;
    }

    /**
     * Creates a default User with CLIENT role
     */
    public static User createDefaultUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@aueb.gr");
        user.setPassword("Password123!");
        user.setUuid(UUID.randomUUID().toString());
        user.setRole(Role.CLIENT);
        user.setIsActive(true);
        return user;
    }

    /**
     * Creates a default User with SUPER_ADMIN role
     */
    public static User createDefaultUserAdmin() {
        User user = new User();
        user.setUsername("testuserAdmin");
        user.setEmail("testuserAdmin@aueb.gr");
        user.setPassword("Password123!");
        user.setUuid(UUID.randomUUID().toString());
        user.setRole(Role.SUPER_ADMIN);
        user.setIsActive(true);
        return user;
    }

    /**
     * Creates a default Client
     */
    public static Client createDefaultClient() {
        Client client = new Client();
        client.setUuid(UUID.randomUUID().toString());
        client.setUser(createDefaultUser());
        client.setPersonalInfo(createDefaultPersonalInfo());
        client.setVat("123456789");
        client.setNotes("Test client notes");
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        return client;
    }

    //  UTILITY METHODS

    /**
     * Generates a unique email for testing
     */
    public static String generateUniqueEmail() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8) + "@aueb.gr";
    }

    /**
     * Generates a unique phone number for testing
     */
    public static String generateUniquePhone() {
        return "697" + (int)(Math.random() * 10000000);
    }

    /**
     * Generates a unique username for testing
     */
    public static String generateUniqueUsername() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    //  DTO FACTORIES

    /**
     * Creates a valid UserInsertDTO for testing
     */
    public static UserInsertDTO createValidUserInsertDTO() {
        UserInsertDTO dto = new UserInsertDTO();
        dto.setIsActive(true);
        dto.setUsername(generateUniqueUsername());
        dto.setPassword("ValidPassword123!");
        dto.setEmail(generateUniqueEmail());
        dto.setRole(Role.CLIENT);
        return dto;
    }

    /**
     * Creates a valid PersonalInfoInsertDTO for testing
     */
    public static PersonalInfoInsertDTO createValidPersonalInfoInsertDTO() {
        PersonalInfoInsertDTO dto = new PersonalInfoInsertDTO();
        dto.setFirstName("TestFirst");
        dto.setLastName("TestLast");
        dto.setEmail(generateUniqueEmail());
        dto.setPhone(generateUniquePhone());
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto.setGender(GenderType.MALE);
        dto.setAddress("Test Address 123");
        dto.setCityId(1L);
        return dto;
    }

    /**
     * Creates a valid ClientInsertDTO for testing
     */
    public static ClientInsertDTO createValidClientInsertDTO() {
        ClientInsertDTO dto = new ClientInsertDTO();
        dto.setIsActive(true);
        dto.setUser(createValidUserInsertDTO());
        dto.setPersonalInfo(createValidPersonalInfoInsertDTO());
        dto.setVat("1234567890");
        dto.setNotes("Test client for testing");
        return dto;
    }
}

