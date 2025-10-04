package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.filters.ClientFilters;
import gr.aueb.cf.system_management_restAPI.core.filters.Paginated;
import gr.aueb.cf.system_management_restAPI.core.specifications.ClientSpecification;
import gr.aueb.cf.system_management_restAPI.dto.ClientReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.mapper.Mapper;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientQueryService {

    private final ClientRepository clientRepository;
    private final SecurityService securityService;
    private final Mapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    public Page<ClientReadOnlyDTO> getPaginatedClients(int page, int size) {
        String defaultSort = "id";
        Pageable pageable = PageRequest.of(page, size, Sort.by(defaultSort).ascending());

        if (securityService.isCurrentUserSuperAdmin()) {
            return clientRepository.findAll(pageable).map(mapper::mapToClientReadOnlyDTO);
        }

        return getUserSpecificClients(pageable);
    }

    public Paginated<ClientReadOnlyDTO> getClientsFilteredPaginated(ClientFilters filters) {
        if (securityService.isCurrentUserSuperAdmin()) {
            return getSuperAdminFilteredClients(filters);
        }

        return getUserFilteredClients(filters);
    }

    public List<ClientReadOnlyDTO> getClientsFiltered(ClientFilters filters) {
        if (securityService.isCurrentUserSuperAdmin()) {
            return clientRepository.findAll(buildSpecification(filters))
                    .stream().map(mapper::mapToClientReadOnlyDTO).toList();
        }

        String currentUsername = securityService.getCurrentUsername();
        if (currentUsername != null) {
            ClientFilters restrictedFilters = filters.toBuilder()
                    .userUsername(currentUsername)
                    .build();

            return clientRepository.findAll(buildSpecification(restrictedFilters))
                    .stream().map(mapper::mapToClientReadOnlyDTO).toList();
        }

        return List.of();
    }

    public List<ClientReadOnlyDTO> searchClientsByName(String name) {
        if (securityService.isCurrentUserSuperAdmin()) {
            return searchAllClientsByName(name);
        }

        return searchUserClientsByName(name);
    }

    public List<ClientReadOnlyDTO> getClientsByLastName(String lastName) {
        if (securityService.isCurrentUserSuperAdmin()) {
            return getAllClientsByLastName(lastName);
        }

        return getUserClientsByLastName(lastName);
    }

    // Private helper methods
    private Page<ClientReadOnlyDTO> getUserSpecificClients(Pageable pageable) {
        String currentUsername = securityService.getCurrentUsername();
        if (currentUsername != null) {
            Specification<Client> spec = ClientSpecification.clientUserUsernameIs(currentUsername);
            return clientRepository.findAll(spec, pageable).map(mapper::mapToClientReadOnlyDTO);
        }
        return Page.empty(pageable);
    }

    private Paginated<ClientReadOnlyDTO> getSuperAdminFilteredClients(ClientFilters filters) {
        var filtered = clientRepository.findAll(buildSpecification(filters), filters.getPageable());
        return new Paginated<>(filtered.map(mapper::mapToClientReadOnlyDTO));
    }

    private Paginated<ClientReadOnlyDTO> getUserFilteredClients(ClientFilters filters) {
        String currentUsername = securityService.getCurrentUsername();
        if (currentUsername != null) {
            ClientFilters restrictedFilters = filters.toBuilder()
                    .userUsername(currentUsername)
                    .build();
            var filtered = clientRepository.findAll(buildSpecification(restrictedFilters),
                    restrictedFilters.getPageable());
            return new Paginated<>(filtered.map(mapper::mapToClientReadOnlyDTO));
        }
        return new Paginated<>(Page.empty());
    }

    private List<ClientReadOnlyDTO> searchAllClientsByName(String name) {
        String jpql = """
            SELECT c FROM Client c 
            JOIN FETCH c.user 
            JOIN FETCH c.personalInfo 
            WHERE c.personalInfo.firstName LIKE :name OR c.personalInfo.lastName LIKE :name
            """;

        return executeNameQuery(jpql, name, null);
    }

    private List<ClientReadOnlyDTO> searchUserClientsByName(String name) {
        String currentUsername = securityService.getCurrentUsername();
        if (currentUsername == null) return List.of();

        String jpql = """
            SELECT c FROM Client c 
            JOIN FETCH c.user 
            JOIN FETCH c.personalInfo 
            WHERE c.user.username = :username 
            AND (c.personalInfo.firstName LIKE :name OR c.personalInfo.lastName LIKE :name)
            """;

        return executeNameQuery(jpql, name, currentUsername);
    }

    private List<ClientReadOnlyDTO> getAllClientsByLastName(String lastName) {
        String jpql = """
            SELECT c FROM Client c 
            JOIN FETCH c.user 
            JOIN FETCH c.personalInfo 
            WHERE LOWER(c.personalInfo.lastName) LIKE LOWER(:lastName)
            """;

        return executeLastNameQuery(jpql, lastName, null);
    }

    private List<ClientReadOnlyDTO> getUserClientsByLastName(String lastName) {
        String currentUsername = securityService.getCurrentUsername();
        if (currentUsername == null) return List.of();

        String jpql = """
            SELECT c FROM Client c 
            JOIN FETCH c.user 
            JOIN FETCH c.personalInfo 
            WHERE c.user.username = :username 
            AND LOWER(c.personalInfo.lastName) LIKE LOWER(:lastName)
            """;

        return executeLastNameQuery(jpql, lastName, currentUsername);
    }

    private List<ClientReadOnlyDTO> executeNameQuery(String jpql, String name, String username) {
        var query = entityManager.createQuery(jpql, Client.class)
                .setParameter("name", "%" + name + "%");

        if (username != null) {
            query.setParameter("username", username);
        }

        return query.getResultList().stream()
                .map(mapper::mapToClientReadOnlyDTO)
                .toList();
    }

    private List<ClientReadOnlyDTO> executeLastNameQuery(String jpql, String lastName, String username) {
        var query = entityManager.createQuery(jpql, Client.class)
                .setParameter("lastName", "%" + lastName + "%");

        if (username != null) {
            query.setParameter("username", username);
        }

        return query.getResultList().stream()
                .map(mapper::mapToClientReadOnlyDTO)
                .toList();
    }

    private Specification<Client> buildSpecification(ClientFilters filters) {
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
}