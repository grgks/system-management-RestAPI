package gr.aueb.cf.system_management_restAPI.rest;

import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.dto.UserReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRestController.class);
    private final UserService userService;

    @Operation(
            summary = "Get current user info",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/me")
    public ResponseEntity<UserReadOnlyDTO> getCurrentUser(Principal principal) throws AppObjectNotFoundException {
        try {
            String username = principal.getName();
            UserReadOnlyDTO userDTO = userService.getUserByUsername(username);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Could not get current user info", e);
            throw e;
        }
    }

    @Operation(
            summary = "Get user by username",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/username/{username}")
    public ResponseEntity<UserReadOnlyDTO> getUserByUsername(@PathVariable String username) throws AppObjectNotFoundException {
        try {
            UserReadOnlyDTO userDTO = userService.getUserByUsername(username);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Could not get user by username: {}", username, e);
            throw e;
        }
    }
}