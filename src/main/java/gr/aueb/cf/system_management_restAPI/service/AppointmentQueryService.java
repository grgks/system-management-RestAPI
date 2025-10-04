package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.core.filters.AppointmentFilters;
import gr.aueb.cf.system_management_restAPI.core.filters.Paginated;
import gr.aueb.cf.system_management_restAPI.core.specifications.AppointmentSpecification;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.mapper.Mapper;
import gr.aueb.cf.system_management_restAPI.model.Appointment;
import gr.aueb.cf.system_management_restAPI.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentQueryService {

    private final AppointmentRepository appointmentRepository;
    private final SecurityService securityService;
    private final Mapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    public Page<AppointmentReadOnlyDTO> getPaginatedAppointments(int page, int size) {
        String defaultSort = "appointmentDateTime";
        Pageable pageable = PageRequest.of(page, size, Sort.by(defaultSort).descending());

        if (securityService.isCurrentUserSuperAdmin()) {
            return appointmentRepository.findAll(pageable).map(mapper::mapToAppointmentReadOnlyDTO);
        }

        return getUserAppointments(pageable);
    }

    public Page<AppointmentReadOnlyDTO> getPaginatedSortedAppointments(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        if (securityService.isCurrentUserSuperAdmin()) {
            return appointmentRepository.findAll(pageable).map(mapper::mapToAppointmentReadOnlyDTO);
        }

        return getUserAppointments(pageable);
    }

    public Paginated<AppointmentReadOnlyDTO> getAppointmentsFilteredPaginated(AppointmentFilters filters) {
        Specification<Appointment> spec = buildSpecification(filters);
        var filtered = appointmentRepository.findAll(spec, filters.getPageable());
        return new Paginated<>(filtered.map(mapper::mapToAppointmentReadOnlyDTO));
    }

    public List<AppointmentReadOnlyDTO> getAppointmentsFiltered(AppointmentFilters filters) {
        Specification<Appointment> spec = buildSpecification(filters);
        return appointmentRepository.findAll(spec)
                .stream().map(mapper::mapToAppointmentReadOnlyDTO).toList();
    }

    public List<AppointmentReadOnlyDTO> getAppointmentsByClient(Long clientId) {
        if (securityService.isCurrentUserSuperAdmin()) {
            return executeQuery(
                    "SELECT a FROM Appointment a " +
                            "JOIN FETCH a.user " +
                            "JOIN FETCH a.client c " +
                            "JOIN FETCH c.personalInfo " +
                            "WHERE a.client.id = :clientId",
                    query -> query.setParameter("clientId", clientId)
            );
        }

        return executeUserRestrictedQuery(
                "SELECT a FROM Appointment a " +
                        "JOIN FETCH a.user " +
                        "JOIN FETCH a.client c " +
                        "JOIN FETCH c.personalInfo " +
                        "WHERE a.client.id = :clientId AND c.user.username = :username",
                query -> query.setParameter("clientId", clientId)
        );
    }

    public List<AppointmentReadOnlyDTO> getAppointmentsByUser(Long userId) {
        if (securityService.isCurrentUserSuperAdmin()) {
            return executeQuery(
                    "SELECT a FROM Appointment a " +
                            "JOIN FETCH a.user " +
                            "JOIN FETCH a.client c " +
                            "JOIN FETCH c.personalInfo " +
                            "WHERE a.user.id = :userId",
                    query -> query.setParameter("userId", userId)
            );
        }

        return executeUserRestrictedQuery(
                "SELECT a FROM Appointment a " +
                        "JOIN FETCH a.user " +
                        "JOIN FETCH a.client c " +
                        "JOIN FETCH c.personalInfo " +
                        "WHERE a.user.id = :userId AND c.user.username = :username",
                query -> query.setParameter("userId", userId)
        );
    }

    public List<AppointmentReadOnlyDTO> getAppointmentsByStatus(AppointmentStatus status) {
        if (securityService.isCurrentUserSuperAdmin()) {
            return executeQuery(
                    "SELECT a FROM Appointment a " +
                            "JOIN FETCH a.user " +
                            "JOIN FETCH a.client c " +
                            "JOIN FETCH c.personalInfo " +
                            "WHERE a.status = :status",
                    query -> query.setParameter("status", status)
            );
        }

        return executeUserRestrictedQuery(
                "SELECT a FROM Appointment a " +
                        "JOIN FETCH a.user " +
                        "JOIN FETCH a.client c " +
                        "JOIN FETCH c.personalInfo " +
                        "WHERE a.status = :status AND c.user.username = :username",
                query -> query.setParameter("status", status)
        );
    }

    public List<AppointmentReadOnlyDTO> getAppointmentsByClientPhone(String phone) throws AppObjectNotFoundException {
        List<AppointmentReadOnlyDTO> appointments;

        if (securityService.isCurrentUserSuperAdmin()) {
            appointments = executeQuery(
                    "SELECT a FROM Appointment a " +
                            "JOIN FETCH a.user " +
                            "JOIN FETCH a.client c " +
                            "JOIN FETCH c.personalInfo " +
                            "WHERE c.personalInfo.phone = :phone",
                    query -> query.setParameter("phone", phone)
            );
        } else {
            appointments = executeUserRestrictedQuery(
                    "SELECT a FROM Appointment a " +
                            "JOIN FETCH a.user " +
                            "JOIN FETCH a.client c " +
                            "JOIN FETCH c.personalInfo " +
                            "WHERE c.personalInfo.phone = :phone AND c.user.username = :username",
                    query -> query.setParameter("phone", phone)
            );
        }

        if (appointments.isEmpty()) {
            throw new AppObjectNotFoundException("Appointments", "No appointments found for phone: " + phone);
        }

        return appointments;
    }

    public List<AppointmentReadOnlyDTO> getUpcomingAppointments() {
        if (securityService.isCurrentUserSuperAdmin()) {
            return executeQuery(
                    "SELECT a FROM Appointment a " +
                            "JOIN FETCH a.user " +
                            "JOIN FETCH a.client c " +
                            "JOIN FETCH c.personalInfo " +
                            "WHERE a.appointmentDateTime >= :date AND a.status = :status " +
                            "ORDER BY a.appointmentDateTime ASC",
                    query -> {
                        query.setParameter("date", LocalDateTime.now());
                        query.setParameter("status", AppointmentStatus.PENDING);
                    }
            );
        }

        return executeUserRestrictedQuery(
                "SELECT a FROM Appointment a " +
                        "JOIN FETCH a.user " +
                        "JOIN FETCH a.client c " +
                        "JOIN FETCH c.personalInfo " +
                        "WHERE a.appointmentDateTime >= :date AND a.status = :status " +
                        "AND c.user.username = :username " +
                        "ORDER BY a.appointmentDateTime ASC",
                query -> {
                    query.setParameter("date", LocalDateTime.now());
                    query.setParameter("status", AppointmentStatus.PENDING);
                }
        );
    }

    public List<AppointmentReadOnlyDTO> getAppointmentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (securityService.isCurrentUserSuperAdmin()) {
            return executeQuery(
                    "SELECT a FROM Appointment a " +
                            "JOIN FETCH a.user " +
                            "JOIN FETCH a.client c " +
                            "JOIN FETCH c.personalInfo " +
                            "WHERE a.appointmentDateTime BETWEEN :startDate AND :endDate " +
                            "ORDER BY a.appointmentDateTime ASC",
                    query -> {
                        query.setParameter("startDate", startDate);
                        query.setParameter("endDate", endDate);
                    }
            );
        }

        return executeUserRestrictedQuery(
                "SELECT a FROM Appointment a " +
                        "JOIN FETCH a.user " +
                        "JOIN FETCH a.client c " +
                        "JOIN FETCH c.personalInfo " +
                        "WHERE a.appointmentDateTime BETWEEN :startDate AND :endDate " +
                        "AND c.user.username = :username " +
                        "ORDER BY a.appointmentDateTime ASC",
                query -> {
                    query.setParameter("startDate", startDate);
                    query.setParameter("endDate", endDate);
                }
        );
    }

    public List<AppointmentReadOnlyDTO> getPendingEmailReminders() {
        if (securityService.isCurrentUserSuperAdmin()) {
            return executeQuery(
                    "SELECT a FROM Appointment a " +
                            "JOIN FETCH a.user " +
                            "JOIN FETCH a.client c " +
                            "JOIN FETCH c.personalInfo " +
                            "WHERE a.emailReminder = :emailReminder " +
                            "AND a.reminderSent = :reminderSent " +
                            "AND a.reminderDateTime <= :dateTime " +
                            "ORDER BY a.reminderDateTime ASC",
                    query -> {
                        query.setParameter("emailReminder", true);
                        query.setParameter("reminderSent", false);
                        query.setParameter("dateTime", LocalDateTime.now());
                    }
            );
        }

        return executeUserRestrictedQuery(
                "SELECT a FROM Appointment a " +
                        "JOIN FETCH a.user " +
                        "JOIN FETCH a.client c " +
                        "JOIN FETCH c.personalInfo " +
                        "WHERE a.emailReminder = :emailReminder " +
                        "AND a.reminderSent = :reminderSent " +
                        "AND a.reminderDateTime <= :dateTime " +
                        "AND c.user.username = :username " +
                        "ORDER BY a.reminderDateTime ASC",
                query -> {
                    query.setParameter("emailReminder", true);
                    query.setParameter("reminderSent", false);
                    query.setParameter("dateTime", LocalDateTime.now());
                }
        );
    }

    public List<AppointmentReadOnlyDTO> getClientAppointmentsBetweenDates(Long clientId, LocalDateTime startDate, LocalDateTime endDate) {
        if (securityService.isCurrentUserSuperAdmin()) {
            return executeQuery(
                    "SELECT a FROM Appointment a " +
                            "JOIN FETCH a.user " +
                            "JOIN FETCH a.client c " +
                            "JOIN FETCH c.personalInfo " +
                            "WHERE a.client.id = :clientId " +
                            "AND a.appointmentDateTime BETWEEN :startDate AND :endDate " +
                            "ORDER BY a.appointmentDateTime ASC",
                    query -> {
                        query.setParameter("clientId", clientId);
                        query.setParameter("startDate", startDate);
                        query.setParameter("endDate", endDate);
                    }
            );
        }

        return executeUserRestrictedQuery(
                "SELECT a FROM Appointment a " +
                        "JOIN FETCH a.user " +
                        "JOIN FETCH a.client c " +
                        "JOIN FETCH c.personalInfo " +
                        "WHERE a.client.id = :clientId " +
                        "AND a.appointmentDateTime BETWEEN :startDate AND :endDate " +
                        "AND c.user.username = :username " +
                        "ORDER BY a.appointmentDateTime ASC",
                query -> {
                    query.setParameter("clientId", clientId);
                    query.setParameter("startDate", startDate);
                    query.setParameter("endDate", endDate);
                }
        );
    }

    // Private helper methods
    private Page<AppointmentReadOnlyDTO> getUserAppointments(Pageable pageable) {
        String currentUsername = securityService.getCurrentUsername();
        if (currentUsername != null) {
            return appointmentRepository.findByClientUserUsername(currentUsername, pageable)
                    .map(mapper::mapToAppointmentReadOnlyDTO);
        }
        return Page.empty(pageable);
    }

    private List<AppointmentReadOnlyDTO> executeQuery(String jpql, QueryParameterSetter parameterSetter) {
        var query = entityManager.createQuery(jpql, Appointment.class);
        parameterSetter.setParameters(query);
        return query.getResultList().stream()
                .map(mapper::mapToAppointmentReadOnlyDTO)
                .toList();
    }

    private List<AppointmentReadOnlyDTO> executeUserRestrictedQuery(String jpql, QueryParameterSetter parameterSetter) {
        String currentUsername = securityService.getCurrentUsername();
        if (currentUsername == null) return List.of();

        var query = entityManager.createQuery(jpql, Appointment.class);
        query.setParameter("username", currentUsername);
        parameterSetter.setParameters(query);

        return query.getResultList().stream()
                .map(mapper::mapToAppointmentReadOnlyDTO)
                .toList();
    }

    private Specification<Appointment> buildSpecification(AppointmentFilters filters) {
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

    @FunctionalInterface
    private interface QueryParameterSetter {
        void setParameters(jakarta.persistence.Query query);
    }
}