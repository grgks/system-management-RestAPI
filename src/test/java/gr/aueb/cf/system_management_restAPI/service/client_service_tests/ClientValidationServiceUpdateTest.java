package gr.aueb.cf.system_management_restAPI.service.client_service_tests;

import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.dto.ClientUpdateDTO;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.ClientRepository;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import gr.aueb.cf.system_management_restAPI.service.ClientValidationService;
import gr.aueb.cf.system_management_restAPI.util.TestDBHelper;
import gr.aueb.cf.system_management_restAPI.util.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation tests for ClientValidationService - validateClientUpdate
 * Tests: Validation for client updates
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientValidationServiceUpdateTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

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
     * Should pass validation when VAT is null.
     */
    @Test
    void validateClientUpdate_ShouldPass_WhenVatIsNull() throws AppObjectAlreadyExists {
        // given
        Long existingClientId = userRepository.findAll().get(0).getId();
        ClientUpdateDTO dto = new ClientUpdateDTO();
        dto.setVat(null);

        // act & assert(no exception = pass)
         validationService.validateClientUpdate(existingClientId,dto);
    }

    /**
     * Should pass validation when VAT is the same as existing.
     */
    @Test
    void validateClientUpdate_ShouldPass_WhenVatIsSameAsExisting() throws AppObjectAlreadyExists {
        // given
        Client  existingClient = clientRepository.findAll().get(0);
        Long clientId = existingClient.getId();
        String currentVat = existingClient.getVat();

        ClientUpdateDTO dto = new ClientUpdateDTO();
        dto.setVat(currentVat);

        // act & assert(no exception thrown)
        validationService.validateClientUpdate(clientId, dto);
    }

    /**
     * Should pass validation when new VAT is unique.
     */
    @Test
    void validateClientUpdate_ShouldPass_WhenNewVatIsUnique() throws AppObjectAlreadyExists {
        // given
        Client  existingClient = clientRepository.findAll().get(0);
        Long clientId = existingClient.getId();
        ClientUpdateDTO dto = new ClientUpdateDTO();
        dto.setVat("9999999999");

        // act & assert(no exception thrown)
        validationService.validateClientUpdate(clientId, dto);
    }

    /**
     * Should throw exception when new VAT already exists for another client.
     */
    @Test
    void validateClientUpdate_ShouldThrowException_WhenNewVatExists() {
        // given
        Client client1 = clientRepository.findAll().get(0);
        Client client2 = clientRepository.findAll().get(1);

        Long client1Id = client1.getId();
        String client2Vat = client2.getVat();

        ClientUpdateDTO dto = new ClientUpdateDTO();
        dto.setVat(client2Vat);

        // act & assert
        assertThrows(RuntimeException.class,
                () -> validationService.validateClientUpdate(client1Id, dto));
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

