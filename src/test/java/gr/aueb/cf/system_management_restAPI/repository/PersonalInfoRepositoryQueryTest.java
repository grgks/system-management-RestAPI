package gr.aueb.cf.system_management_restAPI.repository;

import gr.aueb.cf.system_management_restAPI.core.enums.GenderType;
import gr.aueb.cf.system_management_restAPI.model.PersonalInfo;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Custom query method tests for PersonalInfoRepository
 * Tests: findBy*, existsBy* methods
 */
@DataJpaTest    // creates only components(repositories, EntityManager, DataSource).
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) //uses real db(notH2)
@TestPropertySource(locations = "classpath:application-test.properties")  //separation from app & test
class PersonalInfoRepositoryQueryTest {

    @Autowired
    private PersonalInfoRepository personalInfoRepository;

    @Autowired
    private DataSource dataSource;   // creates essential beans injected becauseTestDBHelper needs access to DB

    @BeforeEach
    void setUp() throws SQLException {
        TestDBHelper.eraseData(dataSource);
    }

    /**
     * Test findByLastNameContainingIgnoreCase - when exists
     */
    @Test
    void testFindByLastNameContainingIgnoreCase_whenExists() {
        // given
        PersonalInfo pi = TestDataFactory.createDefaultPersonalInfo();
        pi.setEmail(TestDataFactory.generateUniqueEmail());
        pi.setPhone(TestDataFactory.generateUniquePhone());
        personalInfoRepository.save(pi);

        PersonalInfo pi2 = TestDataFactory.createDefaultPersonalInfo();
        pi2.setLastName("Papadopoulos");
        pi2.setEmail(TestDataFactory.generateUniqueEmail());
        pi2.setPhone(TestDataFactory.generateUniquePhone());
        personalInfoRepository.save(pi2);

        PersonalInfo pi3 = TestDataFactory.createDefaultPersonalInfo();
        pi3.setLastName("Papakostas");
        pi3.setEmail(TestDataFactory.generateUniqueEmail());
        pi3.setPhone(TestDataFactory.generateUniquePhone());
        personalInfoRepository.save(pi3);

        // act
        List<PersonalInfo> found = personalInfoRepository
                .findByLastNameContainingIgnoreCase("Pap");

        // assert
        assertNotNull(found);
        assertEquals(2, found.size());
        assertTrue(found.stream()
                .allMatch(p -> p.getLastName().toLowerCase().contains("pap")));
        assertTrue(found.stream()
                .anyMatch(p -> p.getLastName().equals("Papadopoulos")));
        assertTrue(found.stream()
                .anyMatch(p -> p.getLastName().equals("Papakostas")));

        List<PersonalInfo> foundUppercase = personalInfoRepository
                .findByLastNameContainingIgnoreCase("PAP");
        assertEquals(2, foundUppercase.size());

        assertFalse(found.stream()
                .anyMatch(p -> p.getLastName().equals("Doe")));
    }

    /**
     * Test findByLastNameContainingIgnoreCase - when not found
     */
    @Test
    void testFindByLastNameContainingIgnoreCase_whenNotFound() {
        // act
        List<PersonalInfo> found = personalInfoRepository
                .findByLastNameContainingIgnoreCase("xyz");

        // assert
        assertNotNull(found);
        assertTrue(found.isEmpty());
    }

    /**
     * Test findByFirstNameContainingIgnoreCase - when exists
     */
    @Test
    void testFindByFirstNameContainingIgnoreCase_whenExists() {
        // given
        PersonalInfo pi = TestDataFactory.createDefaultPersonalInfo();
        pi.setFirstName("Test");
        pi.setEmail(TestDataFactory.generateUniqueEmail());
        pi.setPhone(TestDataFactory.generateUniquePhone());
        personalInfoRepository.save(pi);

        PersonalInfo pi2 = TestDataFactory.createDefaultPersonalInfo();
        pi2.setFirstName("Maria");
        pi2.setEmail(TestDataFactory.generateUniqueEmail());
        pi2.setPhone(TestDataFactory.generateUniquePhone());
        personalInfoRepository.save(pi2);

        PersonalInfo pi3 = TestDataFactory.createDefaultPersonalInfo();
        pi3.setFirstName("Test");
        pi3.setEmail(TestDataFactory.generateUniqueEmail());
        pi3.setPhone(TestDataFactory.generateUniquePhone());
        personalInfoRepository.save(pi3);

        // act
        List<PersonalInfo> found = personalInfoRepository
                .findByFirstNameContainingIgnoreCase("Test");

        // assert
        assertNotNull(found);
        assertEquals(2, found.size());
        assertTrue(found.stream()
                .allMatch(p -> p.getFirstName().toLowerCase().contains("test")));

        List<PersonalInfo> foundUppercase = personalInfoRepository
                .findByFirstNameContainingIgnoreCase("TES");
        assertEquals(2, foundUppercase.size());

        assertFalse(found.stream()
                .anyMatch(p -> p.getFirstName().equals("Maria")));
    }

    /**
     * Test findByFirstNameContainingIgnoreCase - when not found
     */
    @Test
    void testFindByFirstNameContainingIgnoreCase_whenNotFound() {
        // act
        List<PersonalInfo> found = personalInfoRepository
                .findByFirstNameContainingIgnoreCase("xyz");

        // assert
        assertNotNull(found);
        assertTrue(found.isEmpty());
    }

    /**
     * Test findByPhone - when exists
     */
    @Test
    void testFindByPhone_whenExists() {
        // given
        PersonalInfo pi = TestDataFactory.createDefaultPersonalInfo();
        personalInfoRepository.save(pi);

        // act
        Optional<PersonalInfo> found = personalInfoRepository
                .findByPhone("6971234567");

        // assert
        assertTrue(found.isPresent());
        assertEquals("6971234567", found.get().getPhone());
        assertEquals("John", found.get().getFirstName());
    }

    /**
     * Test findByPhone - when not exists
     */
    @Test
    void testFindByPhone_whenNotExists() {
        // given : when we search for notExists we do no need given

        // act
        Optional<PersonalInfo> found = personalInfoRepository
                .findByPhone("6971234567");

        // assert
        assertTrue(found.isEmpty());
    }

    /**
     * Test findByEmail - when exists
     */
    @Test
    void testFindByEmail_whenExists() {
        // given
        PersonalInfo pi = TestDataFactory.createDefaultPersonalInfo();
        personalInfoRepository.save(pi);

        // act
        Optional<PersonalInfo> found = personalInfoRepository
                .findByEmail("john.doe@aueb.gr");

        // assert
        assertTrue(found.isPresent());
        assertEquals("john.doe@aueb.gr", found.get().getEmail());
    }

    /**
     * Test findByEmail - when not exists
     */
    @Test
    void testFindByEmail_whenNotExists() {
        // given : when we search for notExists we do no need given

        // act
        Optional<PersonalInfo> found = personalInfoRepository
                .findByEmail("john.doe@aueb.gr");

        // assert
        assertTrue(found.isEmpty());
    }

    /**
     * Test findByGender - multiple results
     */
    @Test
    void testFindByGender() {
        // given
        PersonalInfo pi = TestDataFactory.createDefaultPersonalInfo();
        personalInfoRepository.save(pi);

        PersonalInfo pi2 = TestDataFactory.createDefaultPersonalInfo();
        personalInfoRepository.save(pi2);

        PersonalInfo pi3 = TestDataFactory.createDefaultPersonalInfo();
        pi3.setGender(GenderType.FEMALE);
        personalInfoRepository.save(pi3);

        // act
        List<PersonalInfo> males = personalInfoRepository
                .findByGender(GenderType.MALE);

        List<PersonalInfo> females = personalInfoRepository
                .findByGender(GenderType.FEMALE);

        // assert
        assertEquals(2, males.size(), "must find 2 MALE");
        assertTrue(males.stream().allMatch(p -> p.getGender() == GenderType.MALE));

        assertEquals(1, females.size(), "must find 1 FEMALE");
        assertTrue(females.stream().allMatch(p -> p.getGender() == GenderType.FEMALE));
    }

    /**
     * Test findByGender - when no results
     */
    @Test
    void testFindByGender_whenNoResults() {
        // given
        PersonalInfo pi = TestDataFactory.createDefaultPersonalInfo();
        personalInfoRepository.save(pi);

        // act
        List<PersonalInfo> female = personalInfoRepository
                .findByGender(GenderType.FEMALE);

        // assert
        assertEquals(0, female.size(), "must find 0 FEMALE");
    }

    /**
     * Test existsByPhone - true case
     */
    @Test
    void testExistsByPhone_whenExists() {
        PersonalInfo pi = TestDataFactory.createDefaultPersonalInfo();
        pi.setPhone("6989898989");
        personalInfoRepository.save(pi);

        // act
        boolean exists = personalInfoRepository.existsByPhone("6989898989");

        // assert
        assertTrue(exists);
    }

    /**
     * Test existsByPhone - false case
     */
    @Test
    void testExistsByPhone_whenNotExists() {
        // act
        boolean exists = personalInfoRepository.existsByPhone("6989898989");

        // assert
        assertFalse(exists);
    }

    /**
     * Test existsByEmail - true case
     */
    @Test
    void testExistsByEmail_whenExists() {
        PersonalInfo pi = TestDataFactory.createDefaultPersonalInfo();
        personalInfoRepository.save(pi);

        // act
        boolean exists = personalInfoRepository.existsByEmail("john.doe@aueb.gr");

        // assert
        assertTrue(exists);
    }

    /**
     * Test existsByEmail - false case
     */
    @Test
    void testExistsByEmail_whenNotExists() {
        // act
        boolean exists = personalInfoRepository.existsByEmail("john.doe@aueb.gr");

        // assert
        assertFalse(exists);
    }
}

