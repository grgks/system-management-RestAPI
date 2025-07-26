package gr.aueb.cf.system_management_restAPI.repository;

import gr.aueb.cf.system_management_restAPI.model.Appointment;
import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Page<Appointment> findByClientUserUsername(String username, Pageable pageable);
    List<Appointment> findByAppointmentDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
}

