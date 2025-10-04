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
import org.springframework.data.domain.Page;

public interface IClientService {
    ClientReadOnlyDTO saveClient(ClientInsertDTO dto) throws AppObjectAlreadyExists, AppObjectInvalidArgumentException, AppObjectNotFoundException, AppObjectNotAuthorizedException;
    ClientReadOnlyDTO updateClient(Long id, ClientUpdateDTO dto) throws AppObjectNotFoundException, AppObjectAlreadyExists, AppObjectNotAuthorizedException;
    ClientReadOnlyDTO getClientById(Long id) throws AppObjectNotFoundException, AppObjectNotAuthorizedException;
    ClientReadOnlyDTO getClientByUuid(String uuid) throws AppObjectNotFoundException, AppObjectNotAuthorizedException;
    ClientReadOnlyDTO getClientByPhone(String phone) throws AppObjectNotFoundException, AppObjectNotAuthorizedException;
    ClientReadOnlyDTO getClientByUsername(String username) throws AppObjectNotFoundException;
    void deleteClient(Long id) throws AppObjectNotFoundException, AppObjectNotAuthorizedException;
    Page<ClientReadOnlyDTO> getPaginatedClients(int page, int size);
    Paginated<ClientReadOnlyDTO> getClientsFilteredPaginated(ClientFilters filters);
}