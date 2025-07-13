package gr.aueb.cf.system_management_restAPI.core.filters;

import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import lombok.*;
import org.springframework.lang.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AppointmentFilters extends GenericFilters {

    @Nullable
    private String uuid;

    @Nullable
    private Long userId;

    @Nullable
    private Long clientId;

    @Nullable
    private String userUsername;

    @Nullable
    private String clientVat;

    @Nullable
    private AppointmentStatus status;

    @Nullable
    private Boolean emailReminder;

    @Nullable
    private Boolean reminderSent;

    @Nullable
    private Boolean active;
}