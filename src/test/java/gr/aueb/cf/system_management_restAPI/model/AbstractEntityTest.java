package gr.aueb.cf.system_management_restAPI.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Concrete subclass used for testing AbstractEntity
 */

class AbstractEntityTest extends AbstractEntity {

    // dummy subclass only for testing!
    // to create object because main class is abstract
    public AbstractEntityTest() { super(); }



    @Test
    void defaultConstructor(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy HH:mm");

        AbstractEntityTest entity = new AbstractEntityTest();
        entity.setCreatedAt(LocalDateTime.parse("12/2/2025 14:48",formatter));
        entity.setUpdatedAt(LocalDateTime.parse("12/2/2025 14:50",formatter));

        assertEquals(LocalDateTime.parse("12/2/2025 14:48", formatter), entity.getCreatedAt());
        assertEquals(LocalDateTime.parse("12/2/2025 14:50", formatter), entity.getUpdatedAt());

    }


    /**
     * Tests the default constructor and setters of AbstractEntity
     */

    @Test
    void allArgsConstructor(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy HH:mm");

        AbstractEntityTest entity = new AbstractEntityTest();
        entity.setCreatedAt(LocalDateTime.parse("12/2/2025 14:48", formatter));
        entity.setUpdatedAt(LocalDateTime.parse("12/2/2025 14:50", formatter));


        assertEquals(LocalDateTime.parse("12/2/2025 14:48", formatter), entity.getCreatedAt());
        assertEquals(LocalDateTime.parse("12/2/2025 14:50", formatter), entity.getUpdatedAt());
    }
}