package gr.aueb.cf.system_management_restAPI.service;


import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.dto.*;
import gr.aueb.cf.system_management_restAPI.mapper.Mapper;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.ClientRepository;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import gr.aueb.cf.system_management_restAPI.util.TestDBHelper;
import gr.aueb.cf.system_management_restAPI.util.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import javax.sql.DataSource;
import java.sql.SQLException;


import static org.junit.jupiter.api.Assertions.*;
/**
 * CRUD TESTS.
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClientServiceCrudTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

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
     * Should create a new client successfully.
     */
    @Test
    void saveClient_ShouldReturnDTO_WhenValidData()
            throws Exception {
        // given
        ClientInsertDTO clientInsertDTO = TestDataFactory.createValidClientInsertDTO();
        // act
        ClientReadOnlyDTO result = clientService.saveClient(clientInsertDTO);

        // assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getUuid());
        assertTrue(clientRepository.existsById(result.getId()));
    }

    /**
     * Should throw exception when trying to create client with existing username.
     */
    @Test
    void saveClient_ShouldThrowException_WhenUsernameExists() {
        // given
        // username not from clientRepository because of lazy loading
        String existingUsername = userRepository.findAll().get(0).getUsername();

        // create ClientInsertDTO with existing username
        ClientInsertDTO dto = TestDataFactory.createValidClientInsertDTO();
        dto.getUser().setUsername(existingUsername);  // Override με existing username

        // act & assert
        assertThrows(AppObjectAlreadyExists.class,
                () -> clientService.saveClient(dto));
    }

    /**
     * Should throw exception when trying to create client with existing email.
     */
    @Test
    void saveClient_ShouldThrowException_WhenEmailExists() {
        //given
        String existingEmail = userRepository.findAll().get(0).getEmail();

        // create ClientInsertDTO with existing email
        ClientInsertDTO dto = TestDataFactory.createValidClientInsertDTO();
        dto.getUser().setEmail(existingEmail);  // Override με existing email

        // act & assert
        assertThrows(AppObjectAlreadyExists.class,
                () -> clientService.saveClient(dto));   //service must decline duplicates not DB
    }

    //  (UPDATE)

    /**
     * Should update an existing client successfully.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"SUPER_ADMIN"}) //when auth needed
    void updateClient_ShouldReturnUpdatedDTO_WhenValidData()
            throws Exception {
        // given
        Long existingId = clientRepository.findAll().get(0).getId();

        // create ClientUpdateDTO με new data
        ClientUpdateDTO dto = new ClientUpdateDTO();
        dto.setVat("9999999999");  // New VAT
        dto.setNotes("Updated notes for testing");  // New notes

        // act
        // call clientService.updateClient()
        ClientReadOnlyDTO result = clientService.updateClient(existingId, dto);

        // assert
        assertNotNull(result);
        assertEquals(existingId, result.getId());
        assertEquals("9999999999", result.getVat());
        assertEquals("Updated notes for testing", result.getNotes());
    }

    /**
     * Should throw exception when trying to update non-existent client.
     */
    @Test
    void updateClient_ShouldThrowException_WhenNotFound() {
        // given
        Long nonExistingId = 9999L;

        ClientUpdateDTO dto = new ClientUpdateDTO();
        dto.setVat("1111111111");
        dto.setNotes("This won't be saved");

        // act & assert
        assertThrows(AppObjectNotFoundException.class,
                () -> clientService.updateClient(nonExistingId, dto));
    }

    // (DELETE)

    /**
     * Should delete a client and associated user successfully.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"SUPER_ADMIN"})  //auth!
    void deleteClient_ShouldRemoveClientAndUser_WhenExists()
            throws Exception {
        // given
        Client existingClient = clientRepository.findAll().get(0);
        Long clientId = existingClient.getId();
        Long userId = existingClient.getUser().getId(); // check if cascade delete works(service deletes user also)

        // act
        clientService.deleteClient(clientId);

        // assert
        assertFalse(clientRepository.existsById(clientId));

        // (cascade delete)
        assertFalse(userRepository.existsById(userId)); // check if cascade delete works(service deletes user also)
    }

    /**
     * Should throw exception when trying to delete non-existent client.
     */
    @Test
    void deleteClient_ShouldThrowException_WhenNotFound() {
        // given
        Long nonExistingId = 9999L;

        // act & assert
        assertThrows(AppObjectNotFoundException.class,
                () -> clientService.deleteClient(nonExistingId));

    }

    //Helper Methods
    /**
     * Creates dummy clients for testing purposes.
     */
    private void createDummyClients() {
        if (clientRepository.count() > 0) return;

        // Client 1 - Using default factory
        Client client1 = TestDataFactory.createDefaultClient();
        client1.getUser().setUsername(TestDataFactory.generateUniqueUsername());
        client1.getUser().setEmail(TestDataFactory.generateUniqueEmail());
        User savedUser1 = userRepository.save(client1.getUser());
        client1.setUser(savedUser1);

        client1.getPersonalInfo().setEmail(TestDataFactory.generateUniqueEmail());
        client1.getPersonalInfo().setPhone(TestDataFactory.generateUniquePhone());
        clientRepository.save(client1);

        // Client 2 - Custom data
        Client client2 = TestDataFactory.createDefaultClient();
        client2.setVat("987654321");
        client2.setNotes("Second test client");
        client2.getUser().setUsername(TestDataFactory.generateUniqueUsername());
        client2.getUser().setEmail(TestDataFactory.generateUniqueEmail());
        User savedUser2 = userRepository.save(client2.getUser());
        client2.setUser(savedUser2);

        client2.getPersonalInfo().setFirstName("Maria");
        client2.getPersonalInfo().setLastName("Smith");
        client2.getPersonalInfo().setEmail(TestDataFactory.generateUniqueEmail());
        client2.getPersonalInfo().setPhone(TestDataFactory.generateUniquePhone());
        clientRepository.save(client2);

        // Client 3 - Another variation
        Client client3 = TestDataFactory.createDefaultClient();
        client3.setVat("555666777");
        client3.setNotes("Third test client");
        client3.getUser().setUsername(TestDataFactory.generateUniqueUsername());
        client3.getUser().setEmail(TestDataFactory.generateUniqueEmail());
        User savedUser3 = userRepository.save(client3.getUser());
        client3.setUser(savedUser3);

        client3.getPersonalInfo().setFirstName("George");
        client3.getPersonalInfo().setLastName("Papadopoulos");
        client3.getPersonalInfo().setEmail(TestDataFactory.generateUniqueEmail());
        client3.getPersonalInfo().setPhone(TestDataFactory.generateUniquePhone());
        clientRepository.save(client3);
    }
}
