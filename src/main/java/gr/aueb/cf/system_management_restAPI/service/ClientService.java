package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.core.filters.ClientFilters;
import gr.aueb.cf.system_management_restAPI.core.filters.Paginated;
import gr.aueb.cf.system_management_restAPI.dto.ClientInsertDTO;
import gr.aueb.cf.system_management_restAPI.dto.ClientReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.dto.ClientUpdateDTO;
import gr.aueb.cf.system_management_restAPI.mapper.Mapper;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.ClientRepository;
import gr.aueb.cf.system_management_restAPI.repository.PersonalInfoRepository;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import gr.aueb.cf.system_management_restAPI.core.specifications.ClientSpecification;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private final Mapper mapper;

    /**
     * Create new Client
     *
     * @param clientInsertDTO - Client data insert
     * @return ClientReadOnlyDTO - Created client
     * @throws AppObjectAlreadyExists - if already exist client
     * @throws AppObjectInvalidArgumentException - if not data
     * @throws AppObjectNotFoundException - if not user exists
     */
    @Transactional(rollbackFor = { Exception.class })
    public ClientReadOnlyDTO saveClient(ClientInsertDTO clientInsertDTO)
            throws AppObjectAlreadyExists, AppObjectInvalidArgumentException, AppObjectNotFoundException {

        //  Check if user exists
        User existingUser = userRepository.findById(clientInsertDTO.getUserId())
                .orElseThrow(() -> new AppObjectNotFoundException("User", "User with id: " + clientInsertDTO.getUserId() + " not found"));

        //  Check if user already has a client
        if (clientRepository.findByUserUsername(existingUser.getUsername()).isPresent()) {
            throw new AppObjectAlreadyExists("Client",
                    "User with username: " + existingUser.getUsername() + " already has a client");
        }

        //  Check if client with same VAT exists
        if (clientInsertDTO.getVat() != null && clientRepository.findByVat(clientInsertDTO.getVat()).isPresent()) {
            throw new AppObjectAlreadyExists("Client",
                    "Client with VAT: " + clientInsertDTO.getVat() + " already exists");
        }

        //  Check if personal info with same phone exists
        if (clientInsertDTO.getPersonalInfo().getPhone() != null &&
                personalInfoRepository.findByPhone(clientInsertDTO.getPersonalInfo().getPhone()).isPresent()) {
            throw new AppObjectAlreadyExists("PersonalInfo",
                    "Personal info with phone: " + clientInsertDTO.getPersonalInfo().getPhone() + " already exists");
        }

        //  Check if personal info with same email exists (if unique)
        if (clientInsertDTO.getPersonalInfo().getEmail() != null &&
                personalInfoRepository.findByEmail(clientInsertDTO.getPersonalInfo().getEmail()).isPresent()) {
            throw new AppObjectAlreadyExists("PersonalInfo",
                    "Personal info with email: " + clientInsertDTO.getPersonalInfo().getEmail() + " already exists");
        }

        // Map DTO to Entity
        Client client = mapper.mapToClientEntity(clientInsertDTO);

        // Set the existing user
        client.setUser(existingUser);

        // Generate UUID  client
        client.setUuid(UUID.randomUUID().toString());

        // Save client (cascade saves personalInfo)
        Client savedClient = clientRepository.save(client);

        return mapper.mapToClientReadOnlyDTO(savedClient);
    }

    /**
     * update Client
     */
    @Transactional(rollbackFor= { Exception.class })
    public ClientReadOnlyDTO updateClient(Long id, ClientUpdateDTO clientUpdateDTO)
            throws AppObjectNotFoundException, AppObjectAlreadyExists {

        // Find existing client
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("Client", "Client with id: " + id + " not found"));

        //  Check VAT uniqueness
        if (clientUpdateDTO.getVat() != null && !clientUpdateDTO.getVat().equals(existingClient.getVat())) {
            if (clientRepository.findByVat(clientUpdateDTO.getVat()).isPresent()) {
                throw new AppObjectAlreadyExists("Client",
                        "Client with VAT: " + clientUpdateDTO.getVat() + " already exists");
            }
        }

        // Update client fields
        mapper.updateClientFromDTO(clientUpdateDTO, existingClient);

        Client updatedClient = clientRepository.save(existingClient);
        return mapper.mapToClientReadOnlyDTO(updatedClient);
    }

    /**
     * find Client by ID
     */
    @Transactional(readOnly = true)
    public ClientReadOnlyDTO getClientById(Long id) throws AppObjectNotFoundException {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("Client", "Client with id: " + id + " not found"));

        return mapper.mapToClientReadOnlyDTO(client);
    }

    /**
     * find Client by UUID
     */
    @Transactional(readOnly = true)
    public ClientReadOnlyDTO getClientByUuid(String uuid) throws AppObjectNotFoundException {
        Client client = clientRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppObjectNotFoundException("Client", "Client with uuid: " + uuid + " not found"));

        return mapper.mapToClientReadOnlyDTO(client);
    }

    /**
     * find Client by Username
     */
    @Transactional(readOnly = true)
    public ClientReadOnlyDTO getClientByUsername(String username) throws AppObjectNotFoundException {
        Client client = clientRepository.findByUserUsername(username)
                .orElseThrow(() -> new AppObjectNotFoundException("Client", "Client with username: " + username + " not found"));

        return mapper.mapToClientReadOnlyDTO(client);
    }

    /**
     * delete Client
     */
    @Transactional( rollbackFor =  {Exception.class}  )
    public void deleteClient(Long id) throws AppObjectNotFoundException {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("Client", "Client with id: " + id + " not found"));

        clientRepository.delete(client);
    }

    /**
     * Paginated list all Clients
     */
    @Transactional(readOnly = true)
    public Page<ClientReadOnlyDTO> getPaginatedClients(int page, int size) {
        String defaultSort = "id";
        Pageable pageable = PageRequest.of(page, size, Sort.by(defaultSort).ascending());
        return clientRepository.findAll(pageable).map(mapper::mapToClientReadOnlyDTO);
    }

    /**
     * Paginated list - custom sorting
     */
    @Transactional(readOnly = true)
    public Page<ClientReadOnlyDTO> getPaginatedSortedClients(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return clientRepository.findAll(pageable).map(mapper::mapToClientReadOnlyDTO);
    }

    /**
     * Filtered search - pagination
     */
    @Transactional(readOnly = true)
    public Paginated<ClientReadOnlyDTO> getClientsFilteredPaginated(ClientFilters filters) {
        var filtered = clientRepository.findAll(getSpecsFromFilters(filters), filters.getPageable());
        return new Paginated<>(filtered.map(mapper::mapToClientReadOnlyDTO));
    }

    /**
     * Filtered search no pagination
     */
    @Transactional(readOnly = true)
    public List<ClientReadOnlyDTO> getClientsFiltered(ClientFilters filters) {
        return clientRepository.findAll(getSpecsFromFilters(filters))
                .stream().map(mapper::mapToClientReadOnlyDTO).toList();
    }

    /**
     * Helper method  specifications
     */
    private Specification<Client> getSpecsFromFilters(ClientFilters filters) {
        return Specification
                .where(ClientSpecification.clStringFieldLike("uuid", filters.getUuid()))
                .and(ClientSpecification.clientUserVatIs(filters.getClientVat()))
                .and(ClientSpecification.clientUserUsernameIs(filters.getUserUsername()))
                .and(ClientSpecification.clPersonalInfoFirstNameIs(filters.getFirstName()))
                .and(ClientSpecification.clPersonalInfoLastNameIs(filters.getLastName()))
                .and(ClientSpecification.clPersonalInfoEmailIs(filters.getEmail()))
                .and(ClientSpecification.clPersonalInfoPhoneIs(filters.getPhone()))
                .and(ClientSpecification.clUserIsActive(filters.getActive()));
    }

    /**
     *  Search clients by full name
     */
    @Transactional(readOnly = true)
    public List<ClientReadOnlyDTO> searchClientsByName(String name) {
        List<Client> clients = clientRepository.findByFullNameContaining(name);
        return clients.stream().map(mapper::mapToClientReadOnlyDTO).toList();
    }

    /**
     *  Get clients by last name
     */
    @Transactional(readOnly = true)
    public List<ClientReadOnlyDTO> getClientsByLastName(String lastName) {
        List<Client> clients = clientRepository.findByPersonalInfoLastNameContainingIgnoreCase(lastName);
        return clients.stream().map(mapper::mapToClientReadOnlyDTO).toList();
    }
}