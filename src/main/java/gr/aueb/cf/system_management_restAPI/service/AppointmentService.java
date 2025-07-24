package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import gr.aueb.cf.system_management_restAPI.core.filters.AppointmentFilters;
import gr.aueb.cf.system_management_restAPI.core.filters.Paginated;
import gr.aueb.cf.system_management_restAPI.core.specifications.AppointmentSpecification;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectInvalidArgumentException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Create new Appointment για validations
     */
    @Transactional(rollbackFor = { Exception.class })
    public AppointmentReadOnlyDTO saveAppointment(AppointmentInsertDTO appointmentInsertDTO)
            throws AppObjectNotFoundException, AppObjectAlreadyExists, AppObjectInvalidArgumentException {

        // Find user
        User existingUser = userRepository.findById(appointmentInsertDTO.getUserId())
                .orElseThrow(() -> new AppObjectNotFoundException("User", "User with id: " + appointmentInsertDTO.getUserId() + " not found"));

        // Find client
        Client existingClient = clientRepository.findById(appointmentInsertDTO.getClientId())
                .orElseThrow(() -> new AppObjectNotFoundException("Client", "Client with id: " + appointmentInsertDTO.getClientId() + " not found"));

        // Check if client already has appointment at this exact time
        String jpql = "SELECT a FROM Appointment a WHERE a.client.id = :clientId AND a.appointmentDateTime = :dateTime";
        List<Appointment> existingAppointments = entityManager
                .createQuery(jpql, Appointment.class)
                .setParameter("clientId", appointmentInsertDTO.getClientId())
                .setParameter("dateTime", appointmentInsertDTO.getAppointmentDateTime())
                .getResultList();

        if (!existingAppointments.isEmpty()) {
            throw new AppObjectAlreadyExists("Appointment",
                    "Client already has an appointment at " + appointmentInsertDTO.getAppointmentDateTime());
        }

        // Add validation
        if (appointmentInsertDTO.getReminderDateTime() != null &&
                appointmentInsertDTO.getReminderDateTime()
                        .isAfter(appointmentInsertDTO.getAppointmentDateTime())) {
            throw new AppObjectInvalidArgumentException("Appointment",
                    "Reminder time must be before appointment time");
        }

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
            throws AppObjectNotFoundException, AppObjectAlreadyExists {

        // Find existing appointment
        Appointment existingAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("Appointment", "Appointment with id: " + id + " not found"));

        // Check if appointment time changed and validate new time
        if (appointmentUpdateDTO.getAppointmentDateTime() != null &&
                !appointmentUpdateDTO.getAppointmentDateTime().equals(existingAppointment.getAppointmentDateTime())) {

            // Check if client already has another appointment at the new time (excluding current appointment)
            String jpql = "SELECT a FROM Appointment a WHERE a.client.id = :clientId AND a.appointmentDateTime = :dateTime AND a.id != :currentId";
            List<Appointment> conflictingAppointments = entityManager
                    .createQuery(jpql, Appointment.class)
                    .setParameter("clientId", existingAppointment.getClient().getId())
                    .setParameter("dateTime", appointmentUpdateDTO.getAppointmentDateTime())
                    .setParameter("currentId", id)
                    .getResultList();

            if (!conflictingAppointments.isEmpty()) {
                throw new AppObjectAlreadyExists("Appointment",
                        "Client already has an appointment at " + appointmentUpdateDTO.getAppointmentDateTime());
            }
        }

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
     * Paginated list Appointments
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
     * Filtered search - pagination
     */
    @Transactional(readOnly = true)
    public Paginated<AppointmentReadOnlyDTO> getAppointmentsFilteredPaginated(AppointmentFilters filters) {
        Specification<Appointment> spec = getSpecsFromFilters(filters);
        var filtered = appointmentRepository.findAll(spec, filters.getPageable());
        return new Paginated<>(filtered.map(mapper::mapToAppointmentReadOnlyDTO));
    }

    /**
     * Filtered search no pagination
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsFiltered(AppointmentFilters filters) {
        Specification<Appointment> spec = getSpecsFromFilters(filters);
        return appointmentRepository.findAll(spec)
                .stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }

    /**
     * Helper method για specifications
     */
    private Specification<Appointment> getSpecsFromFilters(AppointmentFilters filters) {
        return Specification
                .where(AppointmentSpecification.apStringFieldLike("uuid", filters.getUuid()))
                .and(AppointmentSpecification.appointmentUserIdIs(filters.getUserId()))
                .and(AppointmentSpecification.appointmentClientIdIs(filters.getClientId()))
                .and(AppointmentSpecification.appointmentUserUsernameIs(filters.getUserUsername()))
                .and(AppointmentSpecification.appointmentClientVatIs(filters.getClientVat()))
                .and(AppointmentSpecification.appointmentStatusIs(filters.getStatus()))
                .and(AppointmentSpecification.appointmentEmailReminderIs(filters.getEmailReminder()))
                .and(AppointmentSpecification.appointmentReminderSentIs(filters.getReminderSent()))
                .and(AppointmentSpecification.apUserIsActive(filters.getActive()));
    }

    /**
     * Get appointments by client
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsByClient(Long clientId) {
        String jpql = "SELECT a FROM Appointment a " +
                "JOIN FETCH a.user " +
                "JOIN FETCH a.client c " +
                "JOIN FETCH c.personalInfo " +
                "WHERE a.client.id = :clientId";

        List<Appointment> appointments = entityManager
                .createQuery(jpql, Appointment.class)
                .setParameter("clientId", clientId)
                .getResultList();

        return appointments.stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }

    /**
     * Get appointments by user
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsByUser(Long userId) {
        String jpql = "SELECT a FROM Appointment a " +
                "JOIN FETCH a.user " +
                "JOIN FETCH a.client c " +
                "JOIN FETCH c.personalInfo " +
                "WHERE a.user.id = :userId";

        List<Appointment> appointments = entityManager
                .createQuery(jpql, Appointment.class)
                .setParameter("userId", userId)
                .getResultList();

        return appointments.stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }

    /**
     * Get appointments by status
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsByStatus(AppointmentStatus status) {
        String jpql = "SELECT a FROM Appointment a " +
                "JOIN FETCH a.user " +
                "JOIN FETCH a.client c " +
                "JOIN FETCH c.personalInfo " +
                "WHERE a.status = :status";

        List<Appointment> appointments = entityManager
                .createQuery(jpql, Appointment.class)
                .setParameter("status", status)
                .getResultList();

        return appointments.stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }


    /**
     * Get appointments by client phone
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsByClientPhone(String phone) throws AppObjectNotFoundException {
        String jpql = "SELECT a FROM Appointment a " +
                "JOIN FETCH a.user " +
                "JOIN FETCH a.client c " +
                "JOIN FETCH c.personalInfo " +
                "WHERE c.personalInfo.phone = :phone";

        List<Appointment> appointments = entityManager
                .createQuery(jpql, Appointment.class)
                .setParameter("phone", phone)
                .getResultList();

        if (appointments.isEmpty()) {
            throw new AppObjectNotFoundException("Appointments", "No appointments found for phone: " + phone);
        }

        return appointments.stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }



    /**
     * Get upcoming appointments
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getUpcomingAppointments() {
        String jpql = "SELECT a FROM Appointment a " +
                "JOIN FETCH a.user " +
                "JOIN FETCH a.client c " +
                "JOIN FETCH c.personalInfo " +
                "WHERE a.appointmentDateTime >= :date AND a.status = :status " +
                "ORDER BY a.appointmentDateTime ASC";

        List<Appointment> appointments = entityManager
                .createQuery(jpql, Appointment.class)
                .setParameter("date", LocalDateTime.now())
                .setParameter("status", AppointmentStatus.PENDING)
                .getResultList();

        return appointments.stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }

    /**
     * Get appointments for a date range
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getAppointmentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        String jpql = "SELECT a FROM Appointment a " +
                "JOIN FETCH a.user " +
                "JOIN FETCH a.client c " +
                "JOIN FETCH c.personalInfo " +
                "WHERE a.appointmentDateTime BETWEEN :startDate AND :endDate " +
                "ORDER BY a.appointmentDateTime ASC";

        List<Appointment> appointments = entityManager
                .createQuery(jpql, Appointment.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();

        return appointments.stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }

    /**
     * Get pending email reminders
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getPendingEmailReminders() {
        String jpql = "SELECT a FROM Appointment a " +
                "JOIN FETCH a.user " +
                "JOIN FETCH a.client c " +
                "JOIN FETCH c.personalInfo " +
                "WHERE a.emailReminder = :emailReminder " +
                "AND a.reminderSent = :reminderSent " +
                "AND a.reminderDateTime <= :dateTime " +
                "ORDER BY a.reminderDateTime ASC";

        List<Appointment> appointments = entityManager
                .createQuery(jpql, Appointment.class)
                .setParameter("emailReminder", true)
                .setParameter("reminderSent", false)
                .setParameter("dateTime", LocalDateTime.now())
                .getResultList();

        return appointments.stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }

    /**
     * Get client appointments between dates
     */
    @Transactional(readOnly = true)
    public List<AppointmentReadOnlyDTO> getClientAppointmentsBetweenDates(Long clientId, LocalDateTime startDate, LocalDateTime endDate) {
        String jpql = "SELECT a FROM Appointment a " +
                "JOIN FETCH a.user " +
                "JOIN FETCH a.client c " +
                "JOIN FETCH c.personalInfo " +
                "WHERE a.client.id = :clientId " +
                "AND a.appointmentDateTime BETWEEN :startDate AND :endDate " +
                "ORDER BY a.appointmentDateTime ASC";

        List<Appointment> appointments = entityManager
                .createQuery(jpql, Appointment.class)
                .setParameter("clientId", clientId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();

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