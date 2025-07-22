package gr.aueb.cf.system_management_restAPI.rest;

import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.ValidationException;
import gr.aueb.cf.system_management_restAPI.core.filters.ClientFilters;
import gr.aueb.cf.system_management_restAPI.core.filters.Paginated;
import gr.aueb.cf.system_management_restAPI.dto.ClientInsertDTO;
import gr.aueb.cf.system_management_restAPI.dto.ClientReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.dto.ClientUpdateDTO;
import gr.aueb.cf.system_management_restAPI.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClientRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRestController.class);
    private final ClientService clientService;

    @Operation(
            summary = "Get all clients paginated",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Clients Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ClientReadOnlyDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access Denied",
                            content = @Content
                    )
            }
    )
    @GetMapping("/clients")
    public ResponseEntity<Page<ClientReadOnlyDTO>> getPaginatedClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ClientReadOnlyDTO> clientsPage = clientService.getPaginatedClients(page, size);
        return new ResponseEntity<>(clientsPage, HttpStatus.OK);
    }

    @Operation(
            summary = "Save a client",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Client inserted",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ClientReadOnlyDTO.class)
                            )
                    )
            }
    )
    @PostMapping("/clients/save")
    public ResponseEntity<ClientReadOnlyDTO> saveClient(
            @Valid @RequestBody ClientInsertDTO clientInsertDTO,
            BindingResult bindingResult) throws AppObjectInvalidArgumentException, ValidationException, AppObjectAlreadyExists, AppObjectNotFoundException, AppObjectNotAuthorizedException  {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        try {
            ClientReadOnlyDTO clientReadOnlyDTO = clientService.saveClient(clientInsertDTO);
            LOGGER.info("Client saved with id: {}", clientReadOnlyDTO.getId());
            return new ResponseEntity<>(clientReadOnlyDTO, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Could not save client.", e);
            throw e;
        }
    }

    @Operation(
            summary = "Update a client",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Client updated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ClientReadOnlyDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Client not found",
                            content = @Content
                    )
            }
    )
    @PutMapping("/clients/{id}")
    public ResponseEntity<ClientReadOnlyDTO> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientUpdateDTO clientUpdateDTO,
            BindingResult bindingResult) throws ValidationException, AppObjectNotFoundException, AppObjectAlreadyExists {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        try {
            ClientReadOnlyDTO clientReadOnlyDTO = clientService.updateClient(id, clientUpdateDTO);
            LOGGER.info("Client updated with id: {}", id);
            return new ResponseEntity<>(clientReadOnlyDTO, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Could not update client.", e);
            throw e;
        }
    }

    @Operation(
            summary = "Get client by ID",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Client found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ClientReadOnlyDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Client not found",
                            content = @Content
                    )
            }
    )
    @GetMapping("/clients/{id}")
    public ResponseEntity<ClientReadOnlyDTO> getClientById(@PathVariable Long id) throws AppObjectNotFoundException {
        try {
            ClientReadOnlyDTO clientReadOnlyDTO = clientService.getClientById(id);
            return new ResponseEntity<>(clientReadOnlyDTO, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Could not get client by id: {}", id, e);
            throw e;
        }
    }

    @Operation(
            summary = "Get client by UUID",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Client found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ClientReadOnlyDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Client not found",
                            content = @Content
                    )
            }
    )
    @GetMapping("/clients/uuid/{uuid}")
    public ResponseEntity<ClientReadOnlyDTO> getClientByUuid(@PathVariable String uuid) throws AppObjectNotFoundException {
        try {
            ClientReadOnlyDTO clientReadOnlyDTO = clientService.getClientByUuid(uuid);
            return new ResponseEntity<>(clientReadOnlyDTO, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Could not get client by uuid: {}", uuid, e);
            throw e;
        }
    }

    @Operation(
            summary = "Get client by phone",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Client found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ClientReadOnlyDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Client not found",
                            content = @Content
                    )
            }
    )
    @GetMapping("/clients/phone/{phone}")
    public ResponseEntity<ClientReadOnlyDTO> getClientByPhone(@PathVariable String phone) throws AppObjectNotFoundException {
        try {
            ClientReadOnlyDTO clientReadOnlyDTO = clientService.getClientByPhone(phone);
            return new ResponseEntity<>(clientReadOnlyDTO, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Could not get client by phone: {}", phone, e);
            throw e;
        }
    }


    @Operation(
            summary = "Get client by username",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Client found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ClientReadOnlyDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Client not found",
                            content = @Content
                    )
            }
    )
    @GetMapping("/clients/username/{username}")
    public ResponseEntity<ClientReadOnlyDTO> getClientByUsername(@PathVariable String username) throws AppObjectNotFoundException {
        try {
            ClientReadOnlyDTO clientReadOnlyDTO = clientService.getClientByUsername(username);
            return new ResponseEntity<>(clientReadOnlyDTO, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Could not get client by username: {}", username, e);
            throw e;
        }
    }

    @Operation(
            summary = "Delete client",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Client deleted"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Client not found",
                            content = @Content
                    )
            }
    )
    @DeleteMapping("/clients/{id}")
    public ResponseEntity<Map<String, Object>> deleteClient(@PathVariable Long id) throws AppObjectNotFoundException {
        try {
            clientService.deleteClient(id);
            LOGGER.info("Client deleted with id: {}", id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Client deleted successfully");
            response.put("clientId", id);
            response.put("timestamp", LocalDateTime.now());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Could not delete client with id: {}", id, e);
            throw e;
        }
    }

    @Operation(
            summary = "Get all clients filtered",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Clients Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ClientReadOnlyDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access Denied",
                            content = @Content
                    )
            }
    )
    @PostMapping("/clients/all")
    public ResponseEntity<List<ClientReadOnlyDTO>> getClients(@Nullable @RequestBody ClientFilters filters,
                                                              Principal principal)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {
        try {
            if (filters == null) filters = ClientFilters.builder().build();
            return ResponseEntity.ok(clientService.getClientsFiltered(filters));
        } catch (Exception e) {
            LOGGER.warn("Could not get clients.", e);
            throw e;
        }
    }

    @Operation(
            summary = "Get all clients filtered paginated",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Clients Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ClientReadOnlyDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access Denied",
                            content = @Content
                    )
            }
    )
    @PostMapping("/clients/all/paginated")
    public ResponseEntity<Paginated<ClientReadOnlyDTO>> getClientsFilteredPaginated(@Nullable @RequestBody ClientFilters filters,
                                                                                    Principal principal)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {
        try {
            if (filters == null) filters = ClientFilters.builder().build();
            return ResponseEntity.ok(clientService.getClientsFilteredPaginated(filters));
        } catch (Exception e) {
            LOGGER.warn("Could not get clients.", e);
            throw e;
        }
    }

    @Operation(
            summary = "Search clients by name",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Clients Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ClientReadOnlyDTO.class)
                            )
                    )
            }
    )
    @GetMapping("/clients/search")
    public ResponseEntity<List<ClientReadOnlyDTO>> searchClientsByName(@RequestParam String name) {
        try {
            List<ClientReadOnlyDTO> clients = clientService.searchClientsByName(name);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            LOGGER.warn("Could not search clients by name: {}", name, e);
            throw e;
        }
    }
}