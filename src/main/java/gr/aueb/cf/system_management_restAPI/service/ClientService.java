package gr.aueb.cf.system_management_restAPI.service;

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
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService implements IClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityService securityService;
    private final ClientValidationService validationService;
    private final ClientQueryService queryService;

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public ClientReadOnlyDTO saveClient(ClientInsertDTO dto)
            throws AppObjectAlreadyExists, AppObjectInvalidArgumentException, AppObjectNotFoundException, AppObjectNotAuthorizedException {

        // Validation
        validationService.validateNewClient(dto);

        // Business Logic
        User savedUser = createAndSaveUser(dto);
        Client savedClient = createAndSaveClient(dto, savedUser);

        return mapper.mapToClientReadOnlyDTO(savedClient);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public ClientReadOnlyDTO updateClient(Long id, ClientUpdateDTO dto)
            throws AppObjectNotFoundException, AppObjectAlreadyExists, AppObjectNotAuthorizedException {

        Client existingClient = findClientOrThrow(id);
        securityService.validateUserAccess(existingClient.getUser().getUsername(), "Client", id.toString());
        validationService.validateClientUpdate(id, dto);

        mapper.updateClientFromDTO(dto, existingClient);
        Client updatedClient = clientRepository.save(existingClient);

        return mapper.mapToClientReadOnlyDTO(updatedClient);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientReadOnlyDTO getClientById(Long id)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        Client client = findClientOrThrow(id);
        securityService.validateUserAccess(client.getUser().getUsername(), "Client", id.toString());

        return mapper.mapToClientReadOnlyDTO(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientReadOnlyDTO getClientByUuid(String uuid)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        Client client = clientRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppObjectNotFoundException("Client", "Client with uuid: " + uuid + " not found"));
        securityService.validateUserAccess(client.getUser().getUsername(), "Client", uuid);

        return mapper.mapToClientReadOnlyDTO(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientReadOnlyDTO getClientByPhone(String phone)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        Client client = clientRepository.findByPersonalInfoPhone(phone)
                .orElseThrow(() -> new AppObjectNotFoundException("Client", "Client with phone: " + phone + " not found"));
        securityService.validateUserAccess(client.getUser().getUsername(), "Client", phone);

        return mapper.mapToClientReadOnlyDTO(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientReadOnlyDTO getClientByUsername(String username) throws AppObjectNotFoundException {
        if (!securityService.isCurrentUserSuperAdmin()) {
            String currentUsername = securityService.getCurrentUsername();
            if (currentUsername == null || !currentUsername.equals(username)) {
                throw new AppObjectNotFoundException("Client", "Client with username: " + username + " not found");
            }
        }

        Client client = clientRepository.findByUserUsername(username)
                .orElseThrow(() -> new AppObjectNotFoundException("Client", "Client with username: " + username + " not found"));

        return mapper.mapToClientReadOnlyDTO(client);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void deleteClient(Long id) throws AppObjectNotFoundException, AppObjectNotAuthorizedException {
        Client client = findClientOrThrow(id);
        securityService.validateUserAccess(client.getUser().getUsername(), "Client", id.toString());

        User user = client.getUser();

        clientRepository.delete(client);
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientReadOnlyDTO> getPaginatedClients(int page, int size) {
        return queryService.getPaginatedClients(page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Paginated<ClientReadOnlyDTO> getClientsFilteredPaginated(ClientFilters filters) {
        return queryService.getClientsFilteredPaginated(filters);
    }

    // Delegate search methods to query service
    @Transactional(readOnly = true)
    public List<ClientReadOnlyDTO> searchClientsByName(String name) {
        return queryService.searchClientsByName(name);
    }

    @Transactional(readOnly = true)
    public List<ClientReadOnlyDTO> getClientsByLastName(String lastName) {
        return queryService.getClientsByLastName(lastName);
    }

    @Transactional(readOnly = true)
    public List<ClientReadOnlyDTO> getClientsFiltered(ClientFilters filters) {
        return queryService.getClientsFiltered(filters);
    }

    // Private helper methods
    private Client findClientOrThrow(Long id) throws AppObjectNotFoundException {
        return clientRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("Client", "Client with id: " + id + " not found"));
    }

    private User createAndSaveUser(ClientInsertDTO dto) {
        User newUser = mapper.mapToUserEntity(dto.getUser());
        newUser.setPassword(passwordEncoder.encode(dto.getUser().getPassword()));
        newUser.setUuid(UUID.randomUUID().toString());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(newUser);
    }

    private Client createAndSaveClient(ClientInsertDTO dto, User savedUser) {
        Client client = mapper.mapToClientEntity(dto);
        client.setUser(savedUser);
        client.setUuid(UUID.randomUUID().toString());

        return clientRepository.save(client);
    }
}