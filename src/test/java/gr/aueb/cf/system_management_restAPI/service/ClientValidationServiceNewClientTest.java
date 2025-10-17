package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.dto.ClientInsertDTO;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.ClientRepository;
import gr.aueb.cf.system_management_restAPI.repository.PersonalInfoRepository;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import gr.aueb.cf.system_management_restAPI.util.TestDBHelper;
import gr.aueb.cf.system_management_restAPI.util.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation tests for ClientValidationService - validateNewClient
 * Tests: Validation for new client creation
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientValidationServiceNewClientTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonalInfoRepository personalInfoRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ClientValidationService validationService;

    @BeforeAll
    void setupClass() throws SQLException {
        TestDBHelper.eraseData(dataSource);
    }

    @BeforeEach
    void setup() {
        createDummyClients();
    }

    @AfterEach
    void tearDown() throws SQLException {
        TestDBHelper.eraseData(dataSource);
    }

    /**
     * Should pass validation when all data is valid and unique.
     */
    @Test
    void validateNewClient_ShouldPass_WhenAllDataValid() throws AppObjectAlreadyExists {
        // given
        ClientInsertDTO clientInsertDTO = TestDataFactory.createValidClientInsertDTO();

        // act & assert
        assertDoesNotThrow(() -> validationService.validateNewClient(clientInsertDTO));
    }

    /**
     * Should throw exception when username already exists.
     */
    @Test
    void validateNewClient_ShouldThrowException_WhenUsernameExists() {
        // given
        String existingUsername = userRepository.findAll().get(0).getUsername();
        ClientInsertDTO dto = TestDataFactory.createValidClientInsertDTO();
        dto.getUser().setUsername(existingUsername);  // Override με existing username

        // act & assert
        assertThrows(AppObjectAlreadyExists.class,
                () -> validationService.validateNewClient(dto));
    }

    /**
     * Should throw exception when user email already exists.
     */
    @Test
    void validateNewClient_ShouldThrowException_WhenUserEmailExists() {
        // given
        String existingEmail = userRepository.findAll().get(0).getEmail();
        ClientInsertDTO dto = TestDataFactory.createValidClientInsertDTO();
        dto.getUser().setEmail(existingEmail);

        // act & assert
        assertThrows(AppObjectAlreadyExists.class,
                () -> validationService.validateNewClient(dto));
    }

    /**
     * Should throw exception when VAT already exists.
     */
    @Test
    void validateNewClient_ShouldThrowException_WhenVatExists() {
        // given
        String existingVat = clientRepository.findAll().get(0).getVat();
        ClientInsertDTO dto = TestDataFactory.createValidClientInsertDTO();
        dto.setVat(existingVat);

        // act & assert
        assertThrows(AppObjectAlreadyExists.class,
                () -> validationService.validateNewClient(dto));
    }

    /**
     * Should throw exception when phone already exists.
     */
    @Test
    void validateNewClient_ShouldThrowException_WhenPhoneExists() {
        // given
        String existingPhone = personalInfoRepository.findAll().get(0).getPhone();
        ClientInsertDTO dto = TestDataFactory.createValidClientInsertDTO();
        dto.getPersonalInfo().setPhone(existingPhone);

        // act & assert
        assertThrows(AppObjectAlreadyExists.class,
                () -> validationService.validateNewClient(dto));
    }

    /**
     * Should throw exception when personal info email already exists.
     */
    @Test
    void validateNewClient_ShouldThrowException_WhenPersonalInfoEmailExists() {
        // given
        String existingEmail = personalInfoRepository.findAll().get(0).getEmail();
        ClientInsertDTO dto = TestDataFactory.createValidClientInsertDTO();
        dto.getPersonalInfo().setEmail(existingEmail);
        // act & assert
        assertThrows(AppObjectAlreadyExists.class,
                () -> validationService.validateNewClient(dto));
    }

    /**
     * Creates dummy data for validation tests.
     */
    private void createDummyClients() {
        if (clientRepository.count() > 0) return;

        // Client 1
        Client client1 = TestDataFactory.createDefaultClient();
        client1.getUser().setUsername(TestDataFactory.generateUniqueUsername());
        client1.getUser().setEmail(TestDataFactory.generateUniqueEmail());
        User savedUser1 = userRepository.save(client1.getUser());  //  Save User FIRST!
        client1.setUser(savedUser1);  //  Set the saved User

        client1.getPersonalInfo().setEmail(TestDataFactory.generateUniqueEmail());
        client1.getPersonalInfo().setPhone(TestDataFactory.generateUniquePhone());
        clientRepository.save(client1);

        // Client 2
        Client client2 = TestDataFactory.createDefaultClient();
        client2.setVat("987654321");
        client2.setNotes("Second test client");
        client2.getUser().setUsername(TestDataFactory.generateUniqueUsername());
        client2.getUser().setEmail(TestDataFactory.generateUniqueEmail());
        User savedUser2 = userRepository.save(client2.getUser());  //  Save User FIRST!
        client2.setUser(savedUser2);  //  Set the saved User

        client2.getPersonalInfo().setFirstName("Maria");
        client2.getPersonalInfo().setLastName("Smith");
        client2.getPersonalInfo().setEmail(TestDataFactory.generateUniqueEmail());
        client2.getPersonalInfo().setPhone(TestDataFactory.generateUniquePhone());
        clientRepository.save(client2);

        // Client 3
        Client client3 = TestDataFactory.createDefaultClient();
        client3.setVat("555666777");
        client3.setNotes("Third test client");
        client3.getUser().setUsername(TestDataFactory.generateUniqueUsername());
        client3.getUser().setEmail(TestDataFactory.generateUniqueEmail());
        User savedUser3 = userRepository.save(client3.getUser());  // Save User FIRST!
        client3.setUser(savedUser3);  //  Set the saved User

        client3.getPersonalInfo().setFirstName("George");
        client3.getPersonalInfo().setLastName("Papadopoulos");
        client3.getPersonalInfo().setEmail(TestDataFactory.generateUniqueEmail());
        client3.getPersonalInfo().setPhone(TestDataFactory.generateUniquePhone());
        clientRepository.save(client3);
    }
}