package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.dto.ClientInsertDTO;
import gr.aueb.cf.system_management_restAPI.dto.ClientReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.dto.UserReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.mapper.Mapper;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.ClientRepository;
import gr.aueb.cf.system_management_restAPI.repository.PersonalInfoRepository;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import gr.aueb.cf.system_management_restAPI.util.TestDBHelper;
import gr.aueb.cf.system_management_restAPI.util.TestDataFactory;
import jakarta.persistence.Id;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Service layer tests for ClientService
 * Tests: getClientById, getClientByUuid, getClientByPhone, getClientByUsername
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientServiceReadTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonalInfoRepository personalInfoRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Mapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ClientValidationService clientValidationService;

    @Autowired
    private ClientQueryService clientQueryService;

    @Autowired
    private ClientService clientService;


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
     * Should return a ClientReadOnlyDTO when a client with the given ID exists.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"SUPER_ADMIN"})
    void getClientById_ShouldReturnDTO_WhenClientExists()
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {
        // given
        Long existingId = clientRepository.findAll().get(0).getId();

        // act
        ClientReadOnlyDTO result = clientService.getClientById(existingId);

        // assert
        assertNotNull(result);
        assertEquals(existingId,result.getId());
        assertNotNull(result.getPersonalInfo());


        //if i want to check if user exists also
//        Client client = clientRepository.findById(existingId).orElseThrow();
//        assertNotNull(client.getUser());
    }

    /**
     * Should throw AppObjectNotFoundException when no client exists with given ID.
     */
    @Test
    void getClientById_ShouldThrowException_WhenNotFound() {
        // given
        Long nonExistingId = 9999L;

        // act & assert
        assertThrows(AppObjectNotFoundException.class,
                () -> clientService.getClientById(nonExistingId));
    }

    /**
     * Should return a ClientReadOnlyDTO when a client with the given UUID exists.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"SUPER_ADMIN"})
    void getClientByUuid_ShouldReturnDTO_WhenClientExists()
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {
        // given
        String existingUuid = clientRepository.findAll().get(0).getUuid();

        // act
        ClientReadOnlyDTO result = clientService.getClientByUuid(existingUuid);

        // assert
        assertNotNull(result);
        assertEquals(existingUuid,result.getUuid());
        assertNotNull(result.getPersonalInfo());

//        //if i want to check exceptions do not throw but now i
//        check it when i call client service in act.if throws an exception test won;t work
//        assertDoesNotThrow(() -> {
//                    clientService.getClientByUuid(existingUuid);
//                });

        //if i want to check if user exists also
//        Client client = clientRepository.findByUuid(existingUuid).orElseThrow();
//        assertNotNull(client.getUser());
    }

    /**
     * Should throw AppObjectNotFoundException when no client exists with given UUID.
     */
    @Test
    void getClientByUuid_ShouldThrowException_WhenNotFound() {
        // given
        String  nonExistingUuid = "non-existing-uuid-12345";

        // act & assert
        assertThrows(AppObjectNotFoundException.class,
                () -> clientService.getClientByUuid(nonExistingUuid));
    }

    /**
     * Should return a ClientReadOnlyDTO when a client with the given phone exists.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"SUPER_ADMIN"})
    void getClientByPhone_ShouldReturnDTO_WhenClientExists()
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {
        // given
        String existingPhone = personalInfoRepository.findAll().get(0).getPhone();

        // act
        ClientReadOnlyDTO result = clientService.getClientByPhone(existingPhone);

        // assert
        assertNotNull(result);
        assertEquals(existingPhone,result.getPersonalInfo().getPhone());
    }

    /**
     * Should throw AppObjectNotFoundException when no client exists with given phone.
     */
    @Test
    void getClientByPhone_ShouldThrowException_WhenNotFound() {
        // given
        String nonExistingPhone = "0000009999";

        // act & assert
        assertThrows(AppObjectNotFoundException.class,
                () -> clientService.getClientByPhone(nonExistingPhone));

    }

    /**
     * Should return a ClientReadOnlyDTO when a client with the given username exists.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"SUPER_ADMIN"})
    void getClientByUsername_ShouldReturnDTO_WhenClientExists()
            throws AppObjectNotFoundException {
        // given
        String existingUsername = userRepository.findAll().get(0).getUsername();

        // act
        ClientReadOnlyDTO result = clientService.getClientByUsername(existingUsername);

        // assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getUuid());
        assertNotNull(result.getPersonalInfo());
    }

    /**
     * Should throw AppObjectNotFoundException when no client exists with given username.
     */
    @Test
    void getClientByUsername_ShouldThrowException_WhenNotFound() {
        // given
        String nonExistingUsername = "nonUser123";

        // act & assert
        assertThrows(AppObjectNotFoundException.class,
                () -> clientService.getClientByUsername(nonExistingUsername));
    }

    /**
     * Creates dummy clients for testing purposes.
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