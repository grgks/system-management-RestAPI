package gr.aueb.cf.system_management_restAPI.mapper;

import gr.aueb.cf.system_management_restAPI.dto.*;
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
        // ClientInsertDTO
        ClientInsertDTO insertDTO = new ClientInsertDTO();
        insertDTO.setVat("123456789");
        insertDTO.setNotes("Test notes");

        //  PersonalInfoInsertDTO
        PersonalInfoInsertDTO piInsertDTO = new PersonalInfoInsertDTO();
        piInsertDTO.setFirstName("John");
        piInsertDTO.setLastName("Doe");
        piInsertDTO.setEmail("john.doe@example.com");
        piInsertDTO.setPhone("6971234567");
        piInsertDTO.setAddress("Test Street");
        piInsertDTO.setDateOfBirth(LocalDate.of(1990, 5, 15));
        piInsertDTO.setGender(GenderType.MALE);
        piInsertDTO.setCityId(null); // για απλότητα εδώ δεν χρειάζεται city repository

        insertDTO.setPersonalInfo(piInsertDTO);

        // WHEN
        Client entity = mapper.mapToClientEntity(insertDTO);

        // THEN - ΕΛΕΓΧΟΙ
        assertNotNull(entity);
        assertEquals(insertDTO.getVat(), entity.getVat());
        assertEquals(insertDTO.getNotes(), entity.getNotes());

        // PersonalInfo mapping
        assertNotNull(entity.getPersonalInfo());
        assertEquals(piInsertDTO.getFirstName(), entity.getPersonalInfo().getFirstName());
        assertEquals(piInsertDTO.getLastName(), entity.getPersonalInfo().getLastName());
        assertEquals(piInsertDTO.getEmail(), entity.getPersonalInfo().getEmail());
        assertEquals(piInsertDTO.getPhone(), entity.getPersonalInfo().getPhone());
        assertEquals(piInsertDTO.getAddress(), entity.getPersonalInfo().getAddress());
        assertEquals(piInsertDTO.getDateOfBirth(), entity.getPersonalInfo().getDateOfBirth());
        assertEquals(piInsertDTO.getGender(), entity.getPersonalInfo().getGender());
    }



        @Test
        void updateClientFromDTO() {

            Client existingClient = new Client();
            existingClient.setId(10L);
            existingClient.setUuid(UUID.randomUUID().toString());
            existingClient.setVat("123456789");
            existingClient.setNotes("Old notes");


            ClientUpdateDTO dto = new ClientUpdateDTO();
            dto.setVat("987654321");
            dto.setNotes("New updated notes");


            mapper.updateClientFromDTO(dto, existingClient);


            assertEquals("987654321", existingClient.getVat());
            assertEquals("New updated notes", existingClient.getNotes());


            assertEquals(10L, existingClient.getId());
            assertNotNull(existingClient.getUuid());
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