package gr.aueb.cf.system_management_restAPI.repository;

import gr.aueb.cf.system_management_restAPI.model.Appointment;
import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    Optional<Appointment> findByUuid(String uuid);

    List<Appointment> findByClientId(Long clientId);

    List<Appointment> findByUserId(Long userId);

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByAppointmentDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.client.id = :clientId AND a.status = :status")
    List<Appointment> findByClientIdAndStatus(@Param("clientId") Long clientId, @Param("status") AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime >= :date AND a.status = 'PENDING' ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findUpcomingAppointments(@Param("date") LocalDateTime date);

    @Query("SELECT a FROM Appointment a WHERE a.emailReminder = true AND a.reminderSent = false AND a.reminderDateTime <= :now ORDER BY a.reminderDateTime ASC")
    List<Appointment> findPendingReminders(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM Appointment a WHERE a.client.id = :clientId AND a.appointmentDateTime BETWEEN :start AND :end ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findClientAppointmentsBetween(@Param("clientId") Long clientId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.user " +
            "JOIN FETCH a.client c " +
            "JOIN FETCH c.personalInfo " +
            "WHERE a.client.id = :clientId")
    List<Appointment> findByClientIdWithDetails(@Param("clientId") Long clientId);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.user " +
            "JOIN FETCH a.client c " +
            "JOIN FETCH c.personalInfo " +
            "WHERE a.user.id = :userId")
    List<Appointment> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.user " +
            "JOIN FETCH a.client c " +
            "JOIN FETCH c.personalInfo " +
            "WHERE a.appointmentDateTime >= :date AND a.status = 'PENDING' " +
            "ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findUpcomingAppointmentsWithDetails(@Param("date") LocalDateTime date);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.user " +
            "JOIN FETCH a.client c " +
            "JOIN FETCH c.personalInfo " +
            "WHERE a.appointmentDateTime BETWEEN :startDate AND :endDate " +
            "ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findByAppointmentDateTimeBetweenWithDetails(@Param("startDate") LocalDateTime startDate,
                                                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.user " +
            "JOIN FETCH a.client c " +
            "JOIN FETCH c.personalInfo " +
            "WHERE a.status = :status")
    List<Appointment> findByStatusWithDetails(@Param("status") AppointmentStatus status);


    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.user " +
            "JOIN FETCH a.client c " +
            "JOIN FETCH c.personalInfo " +
            "WHERE a.emailReminder = true AND a.reminderSent = false AND a.reminderDateTime <= :now " +
            "ORDER BY a.reminderDateTime ASC")
    List<Appointment> findPendingRemindersWithDetails(@Param("now") LocalDateTime now);
}

