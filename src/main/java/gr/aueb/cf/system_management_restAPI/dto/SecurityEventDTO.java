package gr.aueb.cf.system_management_restAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEventDTO {

    private Long id;
    private String eventType;
    private String username;
    private String ipAddress;
    private String userAgent;
    private Boolean success;
    private LocalDateTime timestamp;
    private Map<String, Object> details;
}
