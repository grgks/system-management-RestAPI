package gr.aueb.cf.system_management_restAPI.core.filters;

import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.lang.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Schema(description = "Appointment filtering criteria")
public class AppointmentFilters extends GenericFilters {

    @Nullable
    @Schema(description = "Appointment UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String uuid;

    @Nullable
    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Nullable
    @Schema(description = "Client ID", example = "1")
    private Long clientId;

    @Nullable
    @Schema(description = "Username", example = "john_doe")
    private String userUsername;

    @Nullable
    @Schema(description = "Client VAT number", example = "123456789")
    private String clientVat;

    @Nullable
    @Schema(description = "Appointment status", example = "PENDING")
    private AppointmentStatus status;

    @Nullable
    @Schema(description = "Email reminder enabled", example = "true")
    private Boolean emailReminder;

    @Nullable
    @Schema(description = "Reminder sent status", example = "false")
    private Boolean reminderSent;

    @Nullable
    @Schema(description = "Active status", example = "true")
    private Boolean active;
}