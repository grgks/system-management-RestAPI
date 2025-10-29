package gr.aueb.cf.system_management_restAPI.service.client_service_tests;

import gr.aueb.cf.system_management_restAPI.core.filters.ClientFilters;
import gr.aueb.cf.system_management_restAPI.core.filters.Paginated;
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
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic query tests for ClientQueryService
 * Tests: getPaginatedClients, getClientsFiltered, getClientsFilteredPaginated
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional                                   //keep session open,lazy fields access,
class ClientQueryServiceBasicTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private  PersonalInfoRepository personalInfoRepository;

    @Autowired
    private UserRepository userRepository;

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
     * Should return paginated clients for super admin.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void getPaginatedClients_ShouldReturnAll_WhenSuperAdmin() {
        // given- define input data (page start -> 0 size = 10 results per page)
        int page = 0;
        int size = 10;

        // act
        Page<ClientReadOnlyDTO> result = queryService.getPaginatedClients(page, size);

        // assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertTrue(result.hasContent());
        assertEquals(size, result.getSize());
        assertEquals(page, result.getNumber());
    }

    /**
     * Should return only user's clients for regular user.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"CLIENT"})
    void getPaginatedClients_ShouldReturnUserClients_WhenRegularUser() {
        // given
        int page = 0;
        int size = 10;
        long totalClients = clientRepository.count();

        // act
        Page<ClientReadOnlyDTO> result = queryService.getPaginatedClients(page, size);

        // assert
        assertNotNull(result);
        assertTrue(result.hasContent());
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getTotalElements() < totalClients);

        result.getContent().forEach(client -> {
            assertNotNull(client);
        });
    }

    /**
     * Should handle pagination correctly.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void getPaginatedClients_ShouldHandlePagination_Correctly() {
        // given
        int page = 0;
        int size = 2;

        // act
        Page<ClientReadOnlyDTO> result = queryService.getPaginatedClients(page, size);

        // assert
        // content size = 2
        // totalElements > size
        //has next page
        assertEquals(2, result.getContent().size());
        assertTrue(result.getTotalElements() > size);
        assertTrue(result.hasNext());

    }

    /**
     * Should return filtered clients by VAT.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void getClientsFiltered_ShouldReturnByVat_WhenVatFilter() {
        // given
        String existingVat = clientRepository.findAll().get(0).getVat();
        //create ClientFilters with vat
        ClientFilters filters = ClientFilters.builder().clientVat(existingVat).build();

        // act
        List<ClientReadOnlyDTO> result = queryService.getClientsFiltered(filters);

        // assert
        assertNotNull(result);
        //assertEquals(existingVat, result.get(0).getVat());
        result.forEach(client -> {
            assertEquals(existingVat, client.getVat(),
                    "All returned clients should have the filtered VAT");
        });
    }

    /**
     * Should return filtered clients by first name.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void getClientsFiltered_ShouldReturnByFirstName_WhenFirstNameFilter() {
        // given
        String existingFirstname = personalInfoRepository.findAll().get(0).getFirstName();
        ClientFilters filters = ClientFilters.builder().clientVat(existingFirstname).build();

        // act
        List<ClientReadOnlyDTO> result = queryService.getClientsFiltered(filters);

        // assert
        assertNotNull(result);
        result.forEach(client -> {
                    assertEquals(existingFirstname, client.getPersonalInfo().getFirstName(),
                            "All returned clients should have the filtered Firstname");
        });
    }

    /**
     * Should return empty list when no matches.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void getClientsFiltered_ShouldReturnEmpty_WhenNoMatches() {
        // given
        ClientFilters filters = ClientFilters.builder().clientVat("non1existingVat").build();

        // act
        List<ClientReadOnlyDTO> result = queryService.getClientsFiltered(filters);

        // assert
        assertTrue(result.isEmpty());

    }

    /**
     * Should return paginated filtered clients.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void getClientsFilteredPaginated_ShouldReturnPaginated_WhenFilters() {
        // given
        int page = 0;
        int size = 10;
        ClientFilters filters = ClientFilters.builder().build();

        // act
        Paginated<ClientReadOnlyDTO> result =
                queryService.getClientsFilteredPaginated(filters);

        // assert
        assertNotNull(result);
        assertNotNull(result.getCurrentPage());
        assertEquals(0, result.getCurrentPage());
        assertNotNull(result.getPageSize());
        assertEquals(10, result.getPageSize());
        assertNotNull(result.getTotalElements());
    }
    /**
     * Should filter and paginate for regular user.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getClientsFilteredPaginated_ShouldReturnUserClients_WhenRegularUser() {
        // given
        int page = 0;
        int size = 10;
        ClientFilters filters = ClientFilters.builder().build();

        // act
        Paginated<ClientReadOnlyDTO> result =
                queryService.getClientsFilteredPaginated(filters);
        // assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements(), "Testuser should have only 1 client");
        assertEquals(1, result.getData().size());
        assertFalse(result.getData().isEmpty(), "Should return testuser's client");
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
