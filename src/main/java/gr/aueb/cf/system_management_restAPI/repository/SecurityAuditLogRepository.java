package gr.aueb.cf.system_management_restAPI.repository;

import gr.aueb.cf.system_management_restAPI.model.SecurityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {

    // Basic queries με method naming
    List<SecurityAuditLog> findByEventType(String eventType);

    List<SecurityAuditLog> findByUsername(String username);

    List<SecurityAuditLog> findBySuccessFalse();  // Failed events

    // Time-based queries
    List<SecurityAuditLog> findByTimestampAfter(LocalDateTime timestamp);

    List<SecurityAuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // Combined queries
    List<SecurityAuditLog> findByEventTypeAndSuccessFalse(String eventType);

    // Custom query - Failed logins from specific IP
    @Query("SELECT s FROM SecurityAuditLog s WHERE s.ipAddress = :ip AND s.success = false")
    List<SecurityAuditLog> findFailedAttemptsByIp(@Param("ip") String ipAddress);

    // Custom query - Recent events (last N)
    @Query("SELECT s FROM SecurityAuditLog s ORDER BY s.timestamp DESC LIMIT :limit")
    List<SecurityAuditLog> findRecentEvents(@Param("limit") int limit);

    // Count failed attempts in time window
    @Query("SELECT COUNT(s) FROM SecurityAuditLog s WHERE s.eventType = :eventType " +
            "AND s.success = false AND s.timestamp >= :since")
    long countFailedAttemptsSince(@Param("eventType") String eventType,
                                  @Param("since") LocalDateTime since);
}
