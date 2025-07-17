package gr.aueb.cf.system_management_restAPI.core;


import gr.aueb.cf.system_management_restAPI.core.exceptions.AppGenericException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppServerException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.ValidationException;
import gr.aueb.cf.system_management_restAPI.dto.ResponseMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException ex) {
        LOGGER.warn("Validation error occurred: {}", ex.getMessage());

        BindingResult bindingResult = ex.getBindingResult();
        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Spring's built-in validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        LOGGER.warn("Method argument validation error occurred: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle object not found exceptions
     */
    @ExceptionHandler(AppObjectNotFoundException.class)
    public ResponseEntity<ResponseMessageDTO> handleAppObjectNotFoundException(AppObjectNotFoundException e) {
        LOGGER.warn("Object not found: {} - {}", e.getCode(), e.getMessage());
        return new ResponseEntity<>(
                ResponseMessageDTO.error(e.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handle object already exists exceptions
     */
    @ExceptionHandler(AppObjectAlreadyExists.class)
    public ResponseEntity<ResponseMessageDTO> handleAppObjectAlreadyExistsException(AppObjectAlreadyExists e) {
        LOGGER.warn("Object already exists: {} - {}", e.getCode(), e.getMessage());
        return new ResponseEntity<>(
                ResponseMessageDTO.error(e.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    /**
     * Handle invalid argument exceptions
     */
    @ExceptionHandler(AppObjectInvalidArgumentException.class)
    public ResponseEntity<ResponseMessageDTO> handleAppObjectInvalidArgumentException(AppObjectInvalidArgumentException e) {
        LOGGER.warn("Invalid argument: {} - {}", e.getCode(), e.getMessage());
        return new ResponseEntity<>(
                ResponseMessageDTO.error(e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handle not authorized exceptions
     */
    @ExceptionHandler(AppObjectNotAuthorizedException.class)
    public ResponseEntity<ResponseMessageDTO> handleAppObjectNotAuthorizedException(AppObjectNotAuthorizedException e) {
        LOGGER.warn("Not authorized: {} - {}", e.getCode(), e.getMessage());
        return new ResponseEntity<>(
                ResponseMessageDTO.error(e.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }

    /**
     * Handle server exceptions
     */
    @ExceptionHandler(AppServerException.class)
    public ResponseEntity<ResponseMessageDTO> handleAppServerException(AppServerException e) {
        LOGGER.error("Server error: {} - {}", e.getCode(), e.getMessage(), e);
        return new ResponseEntity<>(
                ResponseMessageDTO.error(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * Handle generic application exceptions
     */
    @ExceptionHandler(AppGenericException.class)
    public ResponseEntity<ResponseMessageDTO> handleAppGenericException(AppGenericException e) {
        LOGGER.warn("Generic application error: {} - {}", e.getCode(), e.getMessage());
        return new ResponseEntity<>(
                ResponseMessageDTO.error(e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handle Spring Security authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseMessageDTO> handleAuthenticationException(AuthenticationException e) {
        LOGGER.warn("Authentication error: {}", e.getMessage());
        return new ResponseEntity<>(
                ResponseMessageDTO.error("Authentication failed: " + e.getMessage()),
                HttpStatus.UNAUTHORIZED
        );
    }

    /**
     * Handle Spring Security access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseMessageDTO> handleAccessDeniedException(AccessDeniedException e) {
        LOGGER.warn("Access denied: {}", e.getMessage());
        return new ResponseEntity<>(
                ResponseMessageDTO.error("Access denied: " + e.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }

    /**
     * Handle all other unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessageDTO> handleGenericException(Exception e) {
        LOGGER.error("Unexpected error occurred: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                ResponseMessageDTO.error("An unexpected error occurred. Please try again later."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
