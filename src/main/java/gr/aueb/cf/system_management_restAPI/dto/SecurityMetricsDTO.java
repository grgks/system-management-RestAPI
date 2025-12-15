package gr.aueb.cf.system_management_restAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityMetricsDTO {

    private Long totalEvents;
    private Long failedLoginsLast24h;
    private Long successfulLoginsLast24h;
    private Long tokenErrorsLast24h;
    private Long authorizationFailuresLast24h;
    private Double successRate;
    private List<String> suspiciousIPs;
    private List<Map<String, Object>> recentFailures;
}
