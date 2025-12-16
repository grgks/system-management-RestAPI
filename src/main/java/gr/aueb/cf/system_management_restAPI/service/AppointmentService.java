package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import gr.aueb.cf.system_management_restAPI.core.enums.Role;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.core.filters.AppointmentFilters;
import gr.aueb.cf.system_management_restAPI.core.filters.Paginated;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentInsertDTO;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentUpdateDTO;
import gr.aueb.cf.system_management_restAPI.mapper.Mapper;
import gr.aueb.cf.system_management_restAPI.model.Appointment;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.AppointmentRepository;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class AppointmentService implements IAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final Mapper mapper;
    private final AppointmentValidationService validationService;
    private final UserRepository userRepository;
    private final AppointmentQueryService queryService;
    private final SecurityService securityService;
    private final SecurityCrudAuditService crudAuditService;

    @Autowired
    private HttpServletRequest request;

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public AppointmentReadOnlyDTO saveAppointment(AppointmentInsertDTO dto)
            throws AppObjectNotFoundException, AppObjectAlreadyExists, AppObjectInvalidArgumentException, AppObjectNotAuthorizedException {

        // Validation and entity retrieval
        User existingUser = validationService.validateAndGetUser(dto.getUserId());
        Client existingClient = validationService.validateAndGetClient(dto.getClientId());
        validationService.validateNewAppointment(dto);

        // Create and save appointment
        Appointment appointment = createAppointment(dto, existingUser, existingClient);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // LOG  CREATE ACTION
        String username = securityService.getCurrentUsername();
        String ipAddress = securityService.getClientIpAddress(request);
        String clientName = existingClient.getPersonalInfo().getFirstName() + " " +
                existingClient.getPersonalInfo().getLastName();

        crudAuditService.logAppointmentCreated(
                username,
                ipAddress,
                request,
                savedAppointment.getId(),
                clientName
        );

        return mapper.mapToAppointmentReadOnlyDTO(savedAppointment);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public AppointmentReadOnlyDTO updateAppointment(Long id, AppointmentUpdateDTO dto)
            throws AppObjectNotFoundException, AppObjectAlreadyExists, AppObjectNotAuthorizedException {

        Appointment existingAppointment = findAppointmentOrThrow(id);
        securityService.validateUserAccess(
                existingAppointment.getUser().getUsername(),
                "Appointment",
                id.toString()
        );
        validationService.validateAppointmentUpdate(id, dto, existingAppointment);

        mapper.updateAppointmentFromDTO(dto, existingAppointment);
        Appointment updatedAppointment = appointmentRepository.save(existingAppointment);

        //LOG  UPDATE ACTION
        String ipAddress = securityService.getClientIpAddress(request);
        String clientName = existingAppointment.getClient().getPersonalInfo().getFirstName() + " " +
                existingAppointment.getClient().getPersonalInfo().getLastName();

        // Determine what changed (optional - better logging)
        String changes = "Appointment updated";
        if (dto.getStatus() != null) {
            changes = "Status changed to " + dto.getStatus();
        }

        crudAuditService.logAppointmentUpdated(
                securityService.getCurrentUsername(),
                ipAddress,
                request,
                updatedAppointment.getId(),
                clientName,
                changes
        );

        return mapper.mapToAppointmentReadOnlyDTO(updatedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentReadOnlyDTO getAppointmentById(Long id) throws AppObjectNotFoundException, AppObjectNotAuthorizedException {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("Appointment", "Appointment not found"));

        // Get current authenticated user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppObjectNotFoundException("User", "User not found"));

        // Authorization check
        if (!currentUser.getRole().equals(Role.SUPER_ADMIN)) {
            // If not SUPER_ADMIN, check if appointment belongs to user's client
            if (!appointment.getClient().getUser().getId().equals(currentUser.getId())) {
                throw new AppObjectNotAuthorizedException("Appointment", "You don't have permission to view this appointment");
            }
        }

        return mapper.mapToAppointmentReadOnlyDTO(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentReadOnlyDTO getAppointmentByUuid(String uuid) throws AppObjectNotFoundException {
        Appointment appointment = appointmentRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppObjectNotFoundException("Appointment", "Appointment with uuid: " + uuid + " not found"));
        return mapper.mapToAppointmentReadOnlyDTO(appointment);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void deleteAppointment(Long id) throws AppObjectNotFoundException, AppObjectNotAuthorizedException {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("Appointment", "Appointment not found"));

        // Get current authenticated user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppObjectNotFoundException("User", "User not found"));

        // Authorization check
        if (!currentUser.getRole().equals(Role.SUPER_ADMIN)) {
            if (!appointment.getClient().getUser().getId().equals(currentUser.getId())) {
                throw new AppObjectNotAuthorizedException("Appointment", "You don't have permission to delete this appointment");
            }
        }

        // LOG DELETE ACTION
        String ipAddress = securityService.getClientIpAddress(request);
        String clientName = appointment.getClient().getPersonalInfo().getFirstName() + " " +
                appointment.getClient().getPersonalInfo().getLastName();

        crudAuditService.logAppointmentDeleted(
                username,
                ipAddress,
                request,
                appointment.getId(),
                clientName,
                "Appointment deleted by user"
        );

        appointmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentReadOnlyDTO> getPaginatedAppointments(int page, int size) {
        return queryService.getPaginatedAppointments(page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentReadOnlyDTO> getPaginatedSortedAppointments(int page, int size, String sortBy, String sortDirection) {
        return queryService.getPaginatedSortedAppointments(page, size, sortBy, sortDirection);
    }

    @Override
    @Transactional(readOnly = true)
    public Paginated<AppointmentReadOnlyDTO> getAppointmentsFilteredPaginated(AppointmentFilters filters) {
        return queryService.getAppointmentsFilteredPaginated(filters);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsByClient(Long clientId) {
        return queryService.getAppointmentsByClient(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsByUser(Long userId) {
        return queryService.getAppointmentsByUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsByStatus(AppointmentStatus status) {
        return queryService.getAppointmentsByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsByClientPhone(String phone) throws AppObjectNotFoundException {
        return queryService.getAppointmentsByClientPhone(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getUpcomingAppointments() {
        return queryService.getUpcomingAppointments();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return queryService.getAppointmentsBetweenDates(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getPendingEmailReminders() {
        return queryService.getPendingEmailReminders();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getClientAppointmentsBetweenDates(Long clientId, LocalDateTime startDate, LocalDateTime endDate) {
        return queryService.getClientAppointmentsBetweenDates(clientId, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void markReminderAsSent(Long appointmentId) throws AppObjectNotFoundException {
        Appointment appointment = findAppointmentOrThrow(appointmentId);
        appointment.setReminderSent(true);
        appointmentRepository.save(appointment);
    }

    // Additional methods που είναι τώρα στο interface
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsFiltered(AppointmentFilters filters) {
        return queryService.getAppointmentsFiltered(filters);
    }

    // Private helper methods
    private Appointment findAppointmentOrThrow(Long id) throws AppObjectNotFoundException {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("Appointment", "Appointment with id: " + id + " not found"));
    }

    private Appointment createAppointment(AppointmentInsertDTO dto, User user, Client client) {
        Appointment appointment = mapper.mapToAppointmentEntity(dto);
        appointment.setUser(user);
        appointment.setClient(client);
        appointment.setUuid(UUID.randomUUID().toString());
        return appointment;
    }
}