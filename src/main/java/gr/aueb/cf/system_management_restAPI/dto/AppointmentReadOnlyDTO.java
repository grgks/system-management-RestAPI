package gr.aueb.cf.system_management_restAPI.dto;

import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AppointmentReadOnlyDTO {

    private Long id;
    private String uuid;
    private String username;
    private String clientName;
    private LocalDateTime appointmentDateTime;
    private AppointmentStatus status;
    private Boolean emailReminder;
    private LocalDateTime reminderDateTime;
    private Boolean reminderSent;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}