package gr.aueb.cf.system_management_restAPI.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    /**
     * Tests the default (no-args) constructor and setter methods of the Client entity.
     * Ensures that fields can be manually set after object creation.
     */
    @Test
    void defaultConstructor(){
        Client c = new Client();
        c.setId(1L);
        c.setUuid("client-uuid-123");
        c.setVat("1345678987");
        c.setNotes("Testing Client");

        // Create PersonalInfo and attach it
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setFirstName("John");
        personalInfo.setLastName("Doe");
        personalInfo.setPhone("6999999999");

        c.setPersonalInfo(personalInfo);


        assertNotNull(c.getPersonalInfo());
        assertEquals(1L,c.getId());
        assertEquals("client-uuid-123",c.getUuid());
        assertEquals("1345678987",c.getVat());
        assertEquals("Testing Client",c.getNotes());


        // Client PersonalInfo
        assertEquals("John" , c.getPersonalInfo().getFirstName());
        assertEquals("Doe" , c.getPersonalInfo().getLastName());
        assertEquals("6999999999" , c.getPersonalInfo().getPhone());

    }

    /**
     * Tests the all-args constructor of the Client entity.
     * Ensures that all fields are correctly initialized through the constructor.
     */
    @Test
    void allArgsConstructor() {

        User user = new User();
        user.setId(20L);


        // Create PersonalInfo and attach it
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setFirstName("John");
        personalInfo.setLastName("Doe");
        personalInfo.setPhone("6999999999");


        Client c = new Client(1L,"client-uuid-123",user,personalInfo,
                "1345678987","Testing Client");

        // Assert
        assertEquals(1L, c.getId());
        assertEquals("client-uuid-123", c.getUuid());
        assertEquals(user, c.getUser());
        assertEquals(personalInfo, c.getPersonalInfo());
        assertEquals("1345678987", c.getVat());
        assertEquals("Testing Client", c.getNotes());
    }

}