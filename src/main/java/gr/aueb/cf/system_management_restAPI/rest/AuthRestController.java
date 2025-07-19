package gr.aueb.cf.system_management_restAPI.rest;

import gr.aueb.cf.system_management_restAPI.authentication.AuthenticationService;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.ValidationException;
import gr.aueb.cf.system_management_restAPI.dto.AuthenticationRequestDTO;
import gr.aueb.cf.system_management_restAPI.dto.AuthenticationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthRestController.class);
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Authenticate user",
            description = "Authenticate user with username and password to get JWT token",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User authenticated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthenticationResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error",
                            content = @Content
                    )
            }
    )
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDTO> authenticate(
            @Valid @RequestBody AuthenticationRequestDTO authenticationRequestDTO,
            BindingResult bindingResult) throws AppObjectNotAuthorizedException, ValidationException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        try {
            AuthenticationResponseDTO authenticationResponseDTO =
                    authenticationService.authenticate(authenticationRequestDTO);
            LOGGER.info("User authenticated: {}", authenticationRequestDTO.getUsername());
            return new ResponseEntity<>(authenticationResponseDTO, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Authentication failed for user: {}", authenticationRequestDTO.getUsername(), e);
            throw e;
        }
    }
}


