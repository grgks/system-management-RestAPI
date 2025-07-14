package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentInsertDTO;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentUpdateDTO;
import gr.aueb.cf.system_management_restAPI.mapper.Mapper;
import gr.aueb.cf.system_management_restAPI.model.Appointment;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.AppointmentRepository;
import gr.aueb.cf.system_management_restAPI.repository.ClientRepository;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final Mapper mapper;

    /**
     * Create new Appointment with validations
     */
    @Transactional(rollbackFor = { Exception.class })
    public AppointmentReadOnlyDTO saveAppointment(AppointmentInsertDTO appointmentInsertDTO)
            throws AppObjectNotFoundException {

        // Find user
        User existingUser = userRepository.findById(appointmentInsertDTO.getUserId())
                .orElseThrow(() -> new AppObjectNotFoundException("User", "User with id: " + appointmentInsertDTO.getUserId() + " not found"));

        // Find client
        Client existingClient = clientRepository.findById(appointmentInsertDTO.getClientId())
                .orElseThrow(() -> new AppObjectNotFoundException("Client", "Client with id: " + appointmentInsertDTO.getClientId() + " not found"));

        // Map DTO to Entity
        Appointment appointment = mapper.mapToAppointmentEntity(appointmentInsertDTO);

        // Set relationships
        appointment.setUser(existingUser);
        appointment.setClient(existingClient);
        appointment.setUuid(UUID.randomUUID().toString());

        // Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);

        return mapper.mapToAppointmentReadOnlyDTO(savedAppointment);
    }

    /**
     * Update Appointment
     */
    @Transactional(rollbackFor = { Exception.class })
    public AppointmentReadOnlyDTO updateAppointment(Long id, AppointmentUpdateDTO appointmentUpdateDTO)
            throws AppObjectNotFoundException {

        // Find existing appointment
        Appointment existingAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("Appointment", "Appointment with id: " + id + " not found"));

        // Update appointment fields
        mapper.updateAppointmentFromDTO(appointmentUpdateDTO, existingAppointment);

        Appointment updatedAppointment = appointmentRepository.save(existingAppointment);
        return mapper.mapToAppointmentReadOnlyDTO(updatedAppointment);
    }

    /**
     * Find Appointment by ID
     */
    @Transactional(readOnly = true)
    public AppointmentReadOnlyDTO getAppointmentById(Long id) throws AppObjectNotFoundException {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("Appointment", "Appointment with id: " + id + " not found"));

        return mapper.mapToAppointmentReadOnlyDTO(appointment);
    }

    /**
     * Find Appointment by UUID
     */
    @Transactional(readOnly = true)
    public AppointmentReadOnlyDTO getAppointmentByUuid(String uuid) throws AppObjectNotFoundException {
        Appointment appointment = appointmentRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppObjectNotFoundException("Appointment", "Appointment with uuid: " + uuid + " not found"));

        return mapper.mapToAppointmentReadOnlyDTO(appointment);
    }

    /**
     * Delete Appointment
     */
    @Transactional(rollbackFor = { Exception.class })
    public void deleteAppointment(Long id) throws AppObjectNotFoundException {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("Appointment", "Appointment with id: " + id + " not found"));

        appointmentRepository.delete(appointment);
    }

    /**
     * Paginated list όλων των Appointments
     */
    @Transactional(readOnly = true)
    public Page<AppointmentReadOnlyDTO> getPaginatedAppointments(int page, int size) {
        String defaultSort = "appointmentDateTime";
        Pageable pageable = PageRequest.of(page, size, Sort.by(defaultSort).descending());
        return appointmentRepository.findAll(pageable).map(mapper::mapToAppointmentReadOnlyDTO);
    }

    /**
     * Paginated list with custom sorting
     */
    @Transactional(readOnly = true)
    public Page<AppointmentReadOnlyDTO> getPaginatedSortedAppointments(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return appointmentRepository.findAll(pageable).map(mapper::mapToAppointmentReadOnlyDTO);
    }

    /**
     *  Get appointments by client
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsByClient(Long clientId) {
        List<Appointment> appointments = appointmentRepository.findByClientId(clientId);
        return appointments.stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }

    /**
     *  Get appointments by user
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsByUser(Long userId) {
        List<Appointment> appointments = appointmentRepository.findByUserId(userId);
        return appointments.stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }

    /**
     * Get appointments by status
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsByStatus(AppointmentStatus status) {
        List<Appointment> appointments = appointmentRepository.findByStatus(status);
        return appointments.stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }

    /**
     * Get upcoming appointments
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getUpcomingAppointments() {
        List<Appointment> appointments = appointmentRepository.findUpcomingAppointments(LocalDateTime.now());
        return appointments.stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }

    /**
     * Get appointments for a date range
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        List<Appointment> appointments = appointmentRepository.findByAppointmentDateTimeBetween(startDate, endDate);
        return appointments.stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }

    /**
     * Get pending email reminders
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getPendingEmailReminders() {
        List<Appointment> appointments = appointmentRepository.findPendingReminders(LocalDateTime.now());
        return appointments.stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }

    /**
     * Mark reminder as sent
     */
    @Transactional(rollbackFor = { Exception.class })
    public void markReminderAsSent(Long appointmentId) throws AppObjectNotFoundException {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppObjectNotFoundException("Appointment", "Appointment with id: " + appointmentId + " not found"));

        appointment.setReminderSent(true);
        appointmentRepository.save(appointment);
    }
}
