package gr.aueb.cf.system_management_restAPI.repository;

import gr.aueb.cf.system_management_restAPI.core.enums.GenderType;
import gr.aueb.cf.system_management_restAPI.model.PersonalInfo;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.util.TestDBHelper;
import gr.aueb.cf.system_management_restAPI.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CRUD operation tests for PersonalInfoRepository
 * Tests: save, update, delete operations
 */
@DataJpaTest  // creates only components(repositories, EntityManager, DataSource).
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)//uses real db(notH2)
@TestPropertySource(locations = "classpath:application-test.properties") //separation app from test
class PersonalInfoRepositoryCrudTest {

    @Autowired
    private PersonalInfoRepository personalInfoRepository;

    @Autowired
    private DataSource dataSource;  // creates essential beans injected because TestDBHelper needs access to DB

    @BeforeEach
    void setUp() throws SQLException {
        TestDBHelper.eraseData(dataSource);
    }

    /**
     * Test basic save operation
     */
    @Test
    void testSavePersonalInfo() {
        // given
        PersonalInfo pi = TestDataFactory.createDefaultPersonalInfo();

        // act
        PersonalInfo savedPi = personalInfoRepository.save(pi);

        // assert
        assertNotNull(savedPi.getId());
        assertEquals("John", savedPi.getFirstName());
        assertEquals("Doe", savedPi.getLastName());
        assertEquals("john.doe@aueb.gr", savedPi.getEmail());
        assertEquals("6971234567", savedPi.getPhone());
        assertEquals(LocalDate.of(1990, 5, 15), savedPi.getDateOfBirth());
        assertEquals(GenderType.MALE, savedPi.getGender());
        assertEquals("Test Street 12", savedPi.getAddress());

        // Check City
        assertNotNull(savedPi.getCity());
        assertEquals("Athens", savedPi.getCity().getName());
        assertEquals("12345", savedPi.getCity().getPostalCode());
    }

    /**
     * Test update operation
     */
    @Test
    void testUpdatePersonalInfo() {
        // given
        PersonalInfo pi = TestDataFactory.createDefaultPersonalInfo();
        PersonalInfo saved = personalInfoRepository.save(pi);

        // act
        saved.setFirstName("UpdatedJohn");
        saved.setEmail("john.doeUpdate@aueb.gr");
        PersonalInfo updated = personalInfoRepository.save(saved);

        // assert
        assertEquals(saved.getId(), updated.getId());
        assertEquals("john.doeUpdate@aueb.gr", updated.getEmail());
    }

    /**
     * Test delete operation
     */
    @Test
    void testDeletePersonalInfo() {
        // given
        PersonalInfo pi = TestDataFactory.createDefaultPersonalInfo();
        PersonalInfo saved = personalInfoRepository.save(pi);

        // act
        personalInfoRepository.deleteById(saved.getId());

        // assert
        Optional<PersonalInfo> deleted = personalInfoRepository.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }
}