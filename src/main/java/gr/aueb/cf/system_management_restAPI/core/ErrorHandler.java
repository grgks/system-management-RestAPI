package gr.aueb.cf.system_management_restAPI.core;

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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

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

    @ExceptionHandler({AppObjectNotFoundException.class})
    public ResponseEntity<ResponseMessageDTO> handleNotFoundException(AppObjectNotFoundException e) {
        LOGGER.warn("Object not found: {} - {}", e.getCode(), e.getMessage());
        return new ResponseEntity<>(ResponseMessageDTO.error(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({AppObjectAlreadyExists.class})
    public ResponseEntity<ResponseMessageDTO> handleAlreadyExistsException(AppObjectAlreadyExists e) {
        LOGGER.warn("Object already exists: {} - {}", e.getCode(), e.getMessage());
        return new ResponseEntity<>(ResponseMessageDTO.error(e.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler({AppObjectInvalidArgumentException.class})
    public ResponseEntity<ResponseMessageDTO> handleInvalidArgumentException(AppObjectInvalidArgumentException e) {
        LOGGER.warn("Invalid argument: {} - {}", e.getCode(), e.getMessage());
        return new ResponseEntity<>(ResponseMessageDTO.error(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AppObjectNotAuthorizedException.class})
    public ResponseEntity<ResponseMessageDTO> handleNotAuthorizedException(AppObjectNotAuthorizedException e) {
        LOGGER.warn("Not authorized: {} - {}", e.getCode(), e.getMessage());
        return new ResponseEntity<>(ResponseMessageDTO.error(e.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({AppServerException.class})
    public ResponseEntity<ResponseMessageDTO> handleServerException(AppServerException e) {
        LOGGER.error("Server error: {} - {}", e.getCode(), e.getMessage(), e);
        return new ResponseEntity<>(ResponseMessageDTO.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}