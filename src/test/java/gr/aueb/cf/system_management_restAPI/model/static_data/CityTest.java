package gr.aueb.cf.system_management_restAPI.model.static_data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CityTest {

    /**
     * Tests if fields can be correctly set using the default (no-args) constructor and setters.
     */

    @Test
    void defaultConstructor() {
        City city = new City();
        city.setId(1L);
        city.setName("Athens");
        city.setPostalCode("13321");

        assertEquals(1L,city.getId());
        assertEquals("Athens",city.getName());
        assertEquals("13321",city.getPostalCode());
    }

    /**
     * Tests if fields are correctly initialized using the all-args constructor.
     */
    
    @Test
    void allArgsConstructor() {

        City city2 = new City(2L, "Thessaloniki", "14565");

        assertEquals(2L, city2.getId());
        assertEquals("Thessaloniki", city2.getName());
        assertEquals("14565", city2.getPostalCode());
    }

}