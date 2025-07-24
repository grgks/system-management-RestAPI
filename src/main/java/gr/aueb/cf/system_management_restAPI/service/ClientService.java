package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.enums.Role;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotAuthorizedException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import gr.aueb.cf.system_management_restAPI.core.specifications.ClientSpecification;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;


//    /**
//     * Check if current user is SUPER_ADMIN
//     */
//    private boolean isCurrentUserSuperAdmin() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated() ||
//                authentication instanceof AnonymousAuthenticationToken) {
//            return false;
//        }
//
//        return authentication.getAuthorities().stream()
//                .anyMatch(authority -> authority.getAuthority().equals("SUPER_ADMIN"));
//    }
//
//    /**
//     * Get current username
//     */
//    private String getCurrentUsername() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        return authentication != null ? authentication.getName() : null;
//    }

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
            throws AppObjectAlreadyExists, AppObjectInvalidArgumentException, AppObjectNotFoundException, AppObjectNotAuthorizedException {

        // Check if username already exists
        if (userRepository.findByUsername(clientInsertDTO.getUser().getUsername()).isPresent()) {
            throw new AppObjectAlreadyExists("User",
                    "User with username: " + clientInsertDTO.getUser().getUsername() + " already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(clientInsertDTO.getUser().getEmail()).isPresent()) {
            throw new AppObjectAlreadyExists("User",
                    "User with email: " + clientInsertDTO.getUser().getEmail() + " already exists");
        }

        // Check if client with same VAT exists
        if (clientInsertDTO.getVat() != null && clientRepository.findByVat(clientInsertDTO.getVat()).isPresent()) {
            throw new AppObjectAlreadyExists("Client",
                    "Client with VAT: " + clientInsertDTO.getVat() + " already exists");
        }

        // Check if personal info with same phone exists
        if (clientInsertDTO.getPersonalInfo().getPhone() != null &&
                personalInfoRepository.findByPhone(clientInsertDTO.getPersonalInfo().getPhone()).isPresent()) {
            throw new AppObjectAlreadyExists("PersonalInfo",
                    "Personal info with phone: " + clientInsertDTO.getPersonalInfo().getPhone() + " already exists");
        }

        // Check if personal info with same email exists
        if (clientInsertDTO.getPersonalInfo().getEmail() != null &&
                !clientInsertDTO.getPersonalInfo().getEmail().trim().isEmpty() &&
                personalInfoRepository.findByEmail(clientInsertDTO.getPersonalInfo().getEmail()).isPresent()) {
            throw new AppObjectAlreadyExists("PersonalInfo",
                    "Personal info with email: " + clientInsertDTO.getPersonalInfo().getEmail() + " already exists");
        }

        // Only SUPER_ADMIN can create SUPER_ADMIN users
        if (clientInsertDTO.getUser().getRole() == Role.SUPER_ADMIN) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    authentication instanceof AnonymousAuthenticationToken) {
                throw new AppObjectNotAuthorizedException("User",
                        "Authentication required to create SUPER_ADMIN users");
            }

            boolean isSuperAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("SUPER_ADMIN"));

            if (!isSuperAdmin) {
                throw new AppObjectNotAuthorizedException("User",
                        "Only SUPER_ADMIN can create SUPER_ADMIN users");
            }
        }

        // Create NEW User first
        User newUser = mapper.mapToUserEntity(clientInsertDTO.getUser());
        newUser.setPassword(passwordEncoder.encode(clientInsertDTO.getUser().getPassword()));
        newUser.setUuid(UUID.randomUUID().toString());
        newUser.setCreatedAt(java.time.LocalDateTime.now());
        newUser.setUpdatedAt(java.time.LocalDateTime.now());

        User savedUser = userRepository.save(newUser);

        // Map DTO to Client Entity
        Client client = mapper.mapToClientEntity(clientInsertDTO);
        client.setUser(savedUser);
        client.setUuid(UUID.randomUUID().toString());

        // Save client
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
     * find Client by phoneNumber
     */
    @Transactional(readOnly = true)
    public ClientReadOnlyDTO getClientByPhone(String phone) throws AppObjectNotFoundException {
        Client client = clientRepository.findByPersonalInfoPhone(phone)
                .orElseThrow(() -> new AppObjectNotFoundException("Client", "Client with phone: " + phone + " not found"));

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

       // if (isCurrentUserSuperAdmin()) {
        return clientRepository.findAll(pageable).map(mapper::mapToClientReadOnlyDTO);
    }
//        //if client own data
//        String currentUsername = getCurrentUsername();
//        if (currentUsername != null) {
//            return clientRepository.findByUserUsername(currentUsername, pageable)
//                    .map(mapper::mapToClientReadOnlyDTO);
//        }
//
//        // If no authentication, return empty page
//        return Page.empty(pageable);
//    }


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

        //if (isCurrentUserSuperAdmin()) {
        var filtered = clientRepository.findAll(getSpecsFromFilters(filters), filters.getPageable());
        return new Paginated<>(filtered.map(mapper::mapToClientReadOnlyDTO));
    }

//    String currentUsername = getCurrentUsername();
//    if (currentUsername != null) {
//        // Create a copy of filters with username restriction
//        ClientFilters restrictedFilters = filters.toBuilder()
//                .userUsername(currentUsername)
//                .build();
//
//        var filtered = clientRepository.findAll(getSpecsFromFilters(restrictedFilters), restrictedFilters.getPageable());
//        return new Paginated<>(filtered.map(mapper::mapToClientReadOnlyDTO));
//    }
//
//    // If no authentication, return empty paginated result
//    return new Paginated<>(Page.empty());
//}

    /**
     * Filtered search no pagination
     */
    @Transactional(readOnly = true)
    public List<ClientReadOnlyDTO> getClientsFiltered(ClientFilters filters) {
       // if (isCurrentUserSuperAdmin()) {
        return clientRepository.findAll(getSpecsFromFilters(filters))
                .stream().map(mapper::mapToClientReadOnlyDTO).toList();
    }
//    String currentUsername = getCurrentUsername();
//    if (currentUsername != null) {
//        // Create a copy of filters with username restriction
//        ClientFilters restrictedFilters = filters.toBuilder()
//                .userUsername(currentUsername)
//                .build();
//
//        return clientRepository.findAll(getSpecsFromFilters(restrictedFilters))
//                .stream().map(mapper::mapToClientReadOnlyDTO).toList();
//    }
//
//    // If no authentication, return empty list
//    return List.of();
//}


/**
     * Helper method για specifications
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
     * Search clients by full name
     */
    @Transactional(readOnly = true)
    public List<ClientReadOnlyDTO> searchClientsByName(String name) {
        //if (isCurrentUserSuperAdmin()) {
        String jpql = "SELECT c FROM Client c " +
                "JOIN FETCH c.user " +
                "JOIN FETCH c.personalInfo " +
                "WHERE c.personalInfo.firstName LIKE :name OR c.personalInfo.lastName LIKE :name";

        List<Client> clients = entityManager
                .createQuery(jpql, Client.class)
                .setParameter("name", "%" + name + "%")
                .getResultList();

        return clients.stream().map(mapper::mapToClientReadOnlyDTO).toList();
    }
//        String currentUsername = getCurrentUsername();
//        if (currentUsername != null) {
//            String jpql = "SELECT c FROM Client c " +
//                    "JOIN FETCH c.user " +
//                    "JOIN FETCH c.personalInfo " +
//                    "WHERE c.user.username = :username " +
//                    "AND (c.personalInfo.firstName LIKE :name OR c.personalInfo.lastName LIKE :name)";
//
//            List<Client> clients = entityManager
//                    .createQuery(jpql, Client.class)
//                    .setParameter("username", currentUsername)
//                    .setParameter("name", "%" + name + "%")
//                    .getResultList();
//
//            return clients.stream().map(mapper::mapToClientReadOnlyDTO).toList();
//        }
//
//        // If no authentication, return empty list
//        return List.of();
//    }

    /**
     * Get clients by last name
     */
    @Transactional(readOnly = true)
    public List<ClientReadOnlyDTO> getClientsByLastName(String lastName) {

       // if (isCurrentUserSuperAdmin()) {
        String jpql = "SELECT c FROM Client c " +
                "JOIN FETCH c.user " +
                "JOIN FETCH c.personalInfo " +
                "WHERE LOWER(c.personalInfo.lastName) LIKE LOWER(:lastName)";

        List<Client> clients = entityManager
                .createQuery(jpql, Client.class)
                .setParameter("lastName", "%" + lastName + "%")
                .getResultList();

        return clients.stream().map(mapper::mapToClientReadOnlyDTO).toList();
    }

//    String currentUsername = getCurrentUsername();
//    if (currentUsername != null) {
//        String jpql = "SELECT c FROM Client c " +
//                "JOIN FETCH c.user " +
//                "JOIN FETCH c.personalInfo " +
//                "WHERE c.user.username = :username " +
//                "AND LOWER(c.personalInfo.lastName) LIKE LOWER(:lastName)";
//
//        List<Client> clients = entityManager
//                .createQuery(jpql, Client.class)
//                .setParameter("username", currentUsername)
//                .setParameter("lastName", "%" + lastName + "%")
//                .getResultList();
//
//        return clients.stream().map(mapper::mapToClientReadOnlyDTO).toList();
//    }
//
//    // If no authentication, return empty list
//    return List.of();

}