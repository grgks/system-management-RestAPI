package gr.aueb.cf.system_management_restAPI.model;

import gr.aueb.cf.system_management_restAPI.core.enums.GenderType;
import gr.aueb.cf.system_management_restAPI.model.static_data.City;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PersonalInfoTest{

    /**
     * Tests the default (no-args) constructor of PersonalInfo.
     * We create an object using the default constructor, set each field using setters,
     * and verify that the values are correctly assigned.
     */

    @Test
    void  defaultConstructor(){
        City city = new City(1L,"Athens","12345");

        //create PersonalInfo
        PersonalInfo pi = new PersonalInfo();
        pi.setId(1L);
        pi.setFirstName("Test");
        pi.setLastName("Tester");
        pi.setEmail("test@aueb.gr");
        pi.setPhone("6978909876");
        pi.setDateOfBirth(LocalDate.of(1987,12,23));
        pi.setGender(GenderType.FEMALE);
        pi.setAddress("Karias 12");
        pi.setCity(city);

        //assert
        assertNotNull(pi);
        assertEquals(1L,pi.getId());
        assertEquals("Test",pi.getFirstName());
        assertEquals("Tester",pi.getLastName());
        assertEquals("test@aueb.gr",pi.getEmail());
        assertEquals("6978909876",pi.getPhone());
        assertEquals(LocalDate.of(1987,12,23),pi.getDateOfBirth());
        assertEquals((GenderType.FEMALE),pi.getGender());
        assertEquals("Karias 12",pi.getAddress());
    }

    /**
     * Tests the all-args constructor of PersonalInfo.
     * This ensures that all fields are correctly initialized when using the constructor
     * with parameters, including the related City object.
     */

    @Test
    void allArgsConstructor() {
        //create object city
        City city = new City(1L,"Athens","12345");

        //create all args
        PersonalInfo pi = new PersonalInfo(1L,"Test","Tester","test@aueb.gr","6978909876",
                LocalDate.of(1987,12,23),GenderType.FEMALE,"Karias 12",city);


        //assert
        assertNotNull(pi);
        assertEquals(1L,pi.getId());
        assertEquals("Test",pi.getFirstName());
        assertEquals("Tester",pi.getLastName());
        assertEquals("test@aueb.gr",pi.getEmail());
        assertEquals("6978909876",pi.getPhone());
        assertEquals(LocalDate.of(1987,12,23),pi.getDateOfBirth());
        assertEquals((GenderType.FEMALE),pi.getGender());
        assertEquals("Karias 12",pi.getAddress());

    }




}