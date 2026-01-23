package gr.aueb.cf.system_management_restAPI.rest;


import gr.aueb.cf.system_management_restAPI.core.exceptions.*;
import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentInsertDTO;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentUpdateDTO;
import gr.aueb.cf.system_management_restAPI.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AppointmentRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentRestController.class);
    private final AppointmentService appointmentService;

    @Operation(
            summary = "Get all appointments paginated",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Appointments Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AppointmentReadOnlyDTO.class)
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
    @GetMapping("/appointments")
    public ResponseEntity<Page<AppointmentReadOnlyDTO>> getPaginatedAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<AppointmentReadOnlyDTO> appointmentsPage = appointmentService.getPaginatedAppointments(page, size);
        return new ResponseEntity<>(appointmentsPage, HttpStatus.OK);
    }

    @Operation(
            summary = "Get appointments paginated with custom sorting",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Appointments Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AppointmentReadOnlyDTO.class)
                            )
                    )
            }
    )
    //to do.create enum type(appointmentSortField) for auto validation from Spring
    @GetMapping("/appointments/sorted")
    public ResponseEntity<Page<AppointmentReadOnlyDTO>> getPaginatedSortedAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDateTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<AppointmentReadOnlyDTO> appointmentsPage =
                appointmentService.getPaginatedSortedAppointments(page, size, sortBy, sortDirection);
        return new ResponseEntity<>(appointmentsPage, HttpStatus.OK);
    }

    @Operation(
            summary = "Save an appointment",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Appointment created",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AppointmentReadOnlyDTO.class)
                            )
                    )
            }
    )
    @PostMapping("/appointments/save")
    public ResponseEntity<AppointmentReadOnlyDTO> saveAppointment(
            @Valid @RequestBody AppointmentInsertDTO appointmentInsertDTO,
            BindingResult bindingResult) throws ValidationException, AppObjectNotFoundException, AppObjectAlreadyExists, AppObjectInvalidArgumentException ,AppObjectNotAuthorizedException{

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        try {
            AppointmentReadOnlyDTO appointmentReadOnlyDTO = appointmentService.saveAppointment(appointmentInsertDTO);
            LOGGER.info("Appointment saved with id: {}", appointmentReadOnlyDTO.getId());
            return new ResponseEntity<>(appointmentReadOnlyDTO, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Could not save appointment.", e);
            throw e;
        }
    }

    @Operation(
            summary = "Update an appointment",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Appointment updated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AppointmentReadOnlyDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Appointment not found",
                            content = @Content
                    )
            }
    )
    @PutMapping("/appointments/update/{id}")
    public ResponseEntity<AppointmentReadOnlyDTO> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentUpdateDTO appointmentUpdateDTO,
            BindingResult bindingResult) throws ValidationException, AppObjectNotFoundException,
            AppObjectAlreadyExists, AppObjectNotAuthorizedException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        try {
            AppointmentReadOnlyDTO appointmentReadOnlyDTO = appointmentService.updateAppointment(id, appointmentUpdateDTO);
            LOGGER.info("Appointment updated with id: {}", id);
            return new ResponseEntity<>(appointmentReadOnlyDTO, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Could not update appointment.", e);
            throw e;
        }
    }

    @Operation(
            summary = "Get appointment by ID",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Appointment found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AppointmentReadOnlyDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Appointment not found",
                            content = @Content
                    )
            }
    )
    @GetMapping("/appointments/{id}")
    public ResponseEntity<AppointmentReadOnlyDTO> getAppointmentById(@PathVariable Long id) throws AppObjectNotFoundException, AppObjectNotAuthorizedException {
        try {
            AppointmentReadOnlyDTO appointmentReadOnlyDTO = appointmentService.getAppointmentById(id);
            return new ResponseEntity<>(appointmentReadOnlyDTO, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Could not get appointment by id: {}", id, e);
            throw e;
        }
    }

    @Operation(
            summary = "Get appointments by client phone",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Appointments found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AppointmentReadOnlyDTO.class)
                            )
                    )
            }
    )
    @GetMapping("/appointments/phone/{phone}")
    public ResponseEntity<List<AppointmentReadOnlyDTO>> getAppointmentsByClientPhone(@PathVariable String phone) throws AppObjectNotFoundException {
        List<AppointmentReadOnlyDTO> appointments = appointmentService.getAppointmentsByClientPhone(phone);
        return ResponseEntity.ok(appointments);
    }

    @Operation(
            summary = "Get appointment by UUID",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Appointment found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AppointmentReadOnlyDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Appointment not found",
                            content = @Content
                    )
            }
    )
    //to do.add try/catch for logging consistent
    @GetMapping("/appointments/uuid/{uuid}")
    public ResponseEntity<AppointmentReadOnlyDTO> getAppointmentByUuid(@PathVariable String uuid) throws AppObjectNotFoundException {
        AppointmentReadOnlyDTO appointmentReadOnlyDTO = appointmentService.getAppointmentByUuid(uuid);
        return new ResponseEntity<>(appointmentReadOnlyDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "Delete appointment",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Appointment deleted"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Appointment not found",
                            content = @Content
                    )
            }
    )
//to do.add DeleteResponse DTO not mapper.1. Not Type-Safe,2. Runtime ClassCastException Risk,
// 5. No Validation Support,6. Hard to Refactor,8. No Null Safety ,10. Serialization Issues
    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<Map<String, Object>> deleteAppointment(@PathVariable Long id) throws AppObjectNotFoundException, AppObjectNotAuthorizedException {
        try {
            appointmentService.deleteAppointment(id);
            LOGGER.info("Appointment deleted with id: {}", id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment deleted successfully");
            response.put("appointmentId", id);
            response.put("timestamp", LocalDateTime.now());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Could not delete appointment with id: {}", id, e);
            throw e;
        }
    }
    @GetMapping("/appointments/client/{clientId}")
    public ResponseEntity<List<AppointmentReadOnlyDTO>> getAppointmentsByClient(@PathVariable Long clientId) {
        List<AppointmentReadOnlyDTO> appointments = appointmentService.getAppointmentsByClient(clientId);
        return ResponseEntity.ok(appointments);
    }

    @Operation(
            summary = "Get appointments by user ID",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Appointments found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AppointmentReadOnlyDTO.class)
                            )
                    )
            }
    )
    @GetMapping("/appointments/user/{userId}")
    public ResponseEntity<List<AppointmentReadOnlyDTO>> getAppointmentsByUser(@PathVariable Long userId) {
        List<AppointmentReadOnlyDTO> appointments = appointmentService.getAppointmentsByUser(userId);
        return ResponseEntity.ok(appointments);
    }

    @Operation(
            summary = "Get appointments by status",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Appointments found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AppointmentReadOnlyDTO.class)
                            )
                    )
            }
    )
    @GetMapping("/appointments/status/{status}")
    public ResponseEntity<List<AppointmentReadOnlyDTO>> getAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        List<AppointmentReadOnlyDTO> appointments = appointmentService.getAppointmentsByStatus(status);
        return ResponseEntity.ok(appointments);
    }

    @Operation(
            summary = "Get upcoming appointments",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Upcoming appointments found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AppointmentReadOnlyDTO.class)
                            )
                    )
            }
    )
    @GetMapping("/appointments/upcoming")
    public ResponseEntity<List<AppointmentReadOnlyDTO>> getUpcomingAppointments() {
        List<AppointmentReadOnlyDTO> appointments = appointmentService.getUpcomingAppointments();
        return ResponseEntity.ok(appointments);
    }

    @Operation(
            summary = "Get appointments between dates",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Appointments found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AppointmentReadOnlyDTO.class)
                            )
                    )
            }
    )
    @GetMapping("/appointments/between")
    public ResponseEntity<List<AppointmentReadOnlyDTO>> getAppointmentsBetweenDates(
            @Parameter(description = "Start date and time", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date and time", example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<AppointmentReadOnlyDTO> appointments = appointmentService.getAppointmentsBetweenDates(startDate, endDate);
        return ResponseEntity.ok(appointments);
    }

    @Operation(
            summary = "Get pending email reminders",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Pending reminders found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AppointmentReadOnlyDTO.class)
                            )
                    )
            }
    )
    @GetMapping("/appointments/reminders/pending")
    public ResponseEntity<List<AppointmentReadOnlyDTO>> getPendingEmailReminders() {
        List<AppointmentReadOnlyDTO> appointments = appointmentService.getPendingEmailReminders();
        return ResponseEntity.ok(appointments);
    }

    @Operation(
            summary = "Mark reminder as sent",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reminder marked as sent"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Appointment not found",
                            content = @Content
                    )
            }
    )
    @PutMapping("/appointments/{id}/reminder/sent")
    public ResponseEntity<Map<String, Object>> markReminderAsSent(@PathVariable Long id) throws AppObjectNotFoundException {
        try {
            appointmentService.markReminderAsSent(id);
            LOGGER.info("Reminder marked as sent for appointment id: {}", id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reminder marked as sent successfully");
            response.put("appointmentId", id);
            response.put("timestamp", LocalDateTime.now());


            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("Could not mark reminder as sent for appointment id: {}", id, e);
            throw e;
        }
    }

//    @PutMapping("/appointments/test")
//    public ResponseEntity<String> testPutEndpoint() {
//        return ResponseEntity.ok("PUT test successful");
//    }
  }