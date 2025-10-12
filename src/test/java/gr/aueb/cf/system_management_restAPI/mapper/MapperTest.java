package gr.aueb.cf.system_management_restAPI.mapper;

import gr.aueb.cf.system_management_restAPI.dto.ClientReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.dto.PersonalInfoReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.PersonalInfo;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.model.static_data.City;
import gr.aueb.cf.system_management_restAPI.core.enums.GenderType;
import org.junit.jupiter.api.Test;
import gr.aueb.cf.system_management_restAPI.core.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MapperTest {

    @Autowired
    private Mapper mapper;

    @Test
    void mapToClientReadOnlyDTO() {
        // Δημιουργία User
        User user = new User();
        user.setId(10L);
        user.setUsername("johndoe");
        user.setPassword("dummyPass123");
        user.setEmail("johndoe@example.com");
        user.setRole(Role.CLIENT); // Ταιριάζει με τον enum σου
        user.setIsActive(true);

        // Δημιουργία PersonalInfo
        PersonalInfo pi = new PersonalInfo();
        pi.setId(20L);
        pi.setFirstName("John");
        pi.setLastName("Doe");
        pi.setEmail("john.doe@example.com");
        pi.setPhone("6971234567");
        pi.setDateOfBirth(LocalDate.of(1990, 5, 15));
        pi.setGender(GenderType.MALE);
        pi.setAddress("Dummy Street 12");

        // Dummy City
        City city = new City();
        city.setId(1L);
        city.setName("Athens");
        pi.setCity(city);

        // Δημιουργία Client
        Client client = new Client();
        client.setId(1L);
        client.setUuid(UUID.randomUUID().toString());
        client.setUser(user);
        client.setPersonalInfo(pi);
        client.setVat("123456789");
        client.setNotes("Test notes");
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());

        // Map

        ClientReadOnlyDTO dto = mapper.mapToClientReadOnlyDTO(client);

        // Assert Client fields
        assertEquals(client.getId(), dto.getId());
        assertEquals(client.getUuid(), dto.getUuid());
        assertEquals(client.getVat(), dto.getVat());
        assertEquals(client.getNotes(), dto.getNotes());
        assertEquals(client.getCreatedAt(), dto.getCreatedAt());
        assertEquals(client.getUpdatedAt(), dto.getUpdatedAt());

        // Assert PersonalInfo fields
        PersonalInfoReadOnlyDTO dtoPi = dto.getPersonalInfo();
        assertNotNull(dtoPi);
        assertEquals(pi.getId(), dtoPi.getId());
        assertEquals(pi.getFirstName(), dtoPi.getFirstName());
        assertEquals(pi.getLastName(), dtoPi.getLastName());
        assertEquals(pi.getEmail(), dtoPi.getEmail());
        assertEquals(pi.getPhone(), dtoPi.getPhone());
        assertEquals(pi.getDateOfBirth(), dtoPi.getDateOfBirth());
        assertEquals(pi.getGender(), dtoPi.getGender());
        assertEquals(pi.getAddress(), dtoPi.getAddress());
        assertEquals(pi.getCity().getName(), dtoPi.getCityName());
        assertEquals(pi.getCreatedAt(), dtoPi.getCreatedAt());
        assertEquals(pi.getUpdatedAt(), dtoPi.getUpdatedAt());
    }


    @Test
    void mapToClientEntity() {
    }

    @Test
    void updateClientFromDTO() {
    }

    @Test
    void mapToPersonalInfoReadOnlyDTO() {
    }

    @Test
    void mapToPersonalInfoEntity() {
    }

    @Test
    void updatePersonalInfoFromDTO() {
    }

    @Test
    void mapToUserReadOnlyDTO() {
    }

    @Test
    void mapToUserEntity() {
    }

    @Test
    void mapToAppointmentReadOnlyDTO() {
    }

    @Test
    void mapToAppointmentEntity() {
    }

    @Test
    void updateAppointmentFromDTO() {
    }


}