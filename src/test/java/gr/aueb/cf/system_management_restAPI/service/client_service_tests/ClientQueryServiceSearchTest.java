package gr.aueb.cf.system_management_restAPI.service.client_service_tests;

import gr.aueb.cf.system_management_restAPI.dto.ClientReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.ClientRepository;
import gr.aueb.cf.system_management_restAPI.repository.PersonalInfoRepository;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import gr.aueb.cf.system_management_restAPI.service.ClientQueryService;
import gr.aueb.cf.system_management_restAPI.util.TestDBHelper;
import gr.aueb.cf.system_management_restAPI.util.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Search query tests for ClientQueryService
 * Tests: searchClientsByName, getClientsByLastName
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class ClientQueryServiceSearchTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PersonalInfoRepository personalInfoRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ClientQueryService queryService;

    @BeforeAll
    void setupClass() throws SQLException {
        TestDBHelper.eraseData(dataSource);
    }

    @BeforeEach
    void setup() {
        createDummyClients();
    }

    // conflict with @Transactional due to  erase data timeout fails
//    @AfterEach
//    void tearDown() throws SQLException {
//        TestDBHelper.eraseData(dataSource);
//    }


    /**
     * Should find clients by first name & last name.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void searchClientsByName_ShouldFindClients_WhenNameMatches() {
        // given
        String existingFirstname = personalInfoRepository.findAll().get(0).getFirstName();

        // act
        List<ClientReadOnlyDTO> result = queryService.searchClientsByName(existingFirstname);

        // assert
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Should find at least one client");


        // we check both first/last because searchClientsByName searches for both by default
        result.forEach(client -> {
            String firstName = client.getPersonalInfo().getFirstName();
            String lastName = client.getPersonalInfo().getLastName();
            assertTrue(
                    firstName.contains(existingFirstname) || lastName.contains(existingFirstname) ,
                    "Client should match the search term"
            );
        });
    }

    /**
     * Should find clients by partial name match.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void searchClientsByName_ShouldFindByPartialMatch_WhenExists() {
        // given
        String existingFirstname = personalInfoRepository.findAll().get(0).getFirstName();
        //math min = gives 1 letter if has 2 or 2 if has 3 but not more or less
        String partialName = existingFirstname.substring(0, Math.min(2, existingFirstname.length()));

        // act
        List<ClientReadOnlyDTO> result = queryService.searchClientsByName(partialName);

        // assert
        assertFalse(result.isEmpty(), "Expected to find at least one matching client");
        result.forEach(client -> {
            String firstName = client.getPersonalInfo().getFirstName();
            String lastName = client.getPersonalInfo().getLastName();
            assertTrue(
                    firstName.contains(partialName) || lastName.contains(partialName),
                    "Client should match the search term"
            );
        });
    }

    /**
     * Should return empty list when no matches.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void searchClientsByName_ShouldReturnEmpty_WhenNoMatches() {
        // given
        String  nonExistingName = "non-existing###";

        // act
        List<ClientReadOnlyDTO> result = queryService.searchClientsByName(nonExistingName);

        // assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected no results");

    }

    /**
     * Should return only user's clients for regular user.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"CLIENT"})
    void searchClientsByName_ShouldReturnUserClients_WhenRegularUser() {
        // given
        String existingName = "Maria";

        // act
        List<ClientReadOnlyDTO> result = queryService.searchClientsByName(existingName);


        // assert
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Result should not be empty for existing client's name");
        result.forEach(client -> {
            String firstName = client.getPersonalInfo().getFirstName();
            String lastName = client.getPersonalInfo().getLastName();
            assertTrue(
                    firstName.contains(existingName) || lastName.contains(existingName),
                    "Client should match the search term"
            );
        });
    }

    /**
     * Should find clients by exact last name.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void getClientsByLastName_ShouldFind_WhenExactMatch() {
        // given
        String existinglastName = "Papadopoulos";

        // act
        List<ClientReadOnlyDTO> result = queryService.searchClientsByName(existinglastName);

        // assert
        assertNotNull(result);
        result.forEach(client -> {
            String lastName = client.getPersonalInfo().getLastName();
            assertTrue(lastName.contains(existinglastName),
                    "Client should match the search term"
            );
        });
    }

    /**
     * Should be case insensitive.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void getClientsByLastName_ShouldBeCaseInsensitive() {
        // given
        String existinglastName = "Papadopoulos";
        String lastNameDifferentCase = existinglastName.toUpperCase();

        // act
        List<ClientReadOnlyDTO> result = queryService.searchClientsByName(lastNameDifferentCase);

        // assert
        result.forEach(client -> {
            String lastName = client.getPersonalInfo().getLastName();
            assertTrue(lastName.toLowerCase().contains(existinglastName.toLowerCase()),
                    "Client should match the search term"
            );
        });
    }

    //Helper Method
    /**
     * Creates dummy data for validation tests.
     */
    private void createDummyClients() {
        if (clientRepository.count() > 0) return;

        // Client 1 - "admin"
        Client client1 = TestDataFactory.createDefaultClient();
        client1.getUser().setUsername("admin");
        client1.getUser().setEmail(TestDataFactory.generateUniqueEmail());
        User savedUser1 = userRepository.save(client1.getUser());
        client1.setUser(savedUser1);

        client1.getPersonalInfo().setEmail(TestDataFactory.generateUniqueEmail());
        client1.getPersonalInfo().setPhone(TestDataFactory.generateUniquePhone());
        clientRepository.save(client1);

        // Client 2 - "testuser1"
        Client client2 = TestDataFactory.createDefaultClient();
        client2.setVat("987654321");
        client2.setNotes("Testuser client 1");
        client2.getUser().setUsername("testuser");
        client2.getUser().setEmail(TestDataFactory.generateUniqueEmail());
        User savedUser2 = userRepository.save(client2.getUser());
        client2.setUser(savedUser2);

        client2.getPersonalInfo().setFirstName("Maria");
        client2.getPersonalInfo().setLastName("Smith");
        client2.getPersonalInfo().setEmail(TestDataFactory.generateUniqueEmail());
        client2.getPersonalInfo().setPhone(TestDataFactory.generateUniquePhone());
        clientRepository.save(client2);

        // Client 3 -  "testuser2"
        Client client3 = TestDataFactory.createDefaultClient();
        client3.setVat("555666777");
        client3.setNotes("Testuser client 2");
        client3.getUser().setUsername("john");
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
