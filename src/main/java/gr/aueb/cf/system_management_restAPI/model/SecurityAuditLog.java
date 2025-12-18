package gr.aueb.cf.system_management_restAPI.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "security_audit_logs", indexes = {
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_event_type", columnList = "eventType"),
        @Index(name = "idx_username", columnList = "username")  // --> quick queries
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "username")
    private String username;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @JdbcTypeCode(SqlTypes.JSON)  //Tells Hibernate: JSON handling
    @Column(name = "details", columnDefinition = "JSON")   //tells MySQL: JSON column
    private Map<String, Object> details  = new HashMap<>();

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;


    // auto timestamp
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

}
