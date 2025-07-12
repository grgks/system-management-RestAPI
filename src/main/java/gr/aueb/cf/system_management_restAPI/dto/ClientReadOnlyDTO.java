package gr.aueb.cf.system_management_restAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientReadOnlyDTO {

    private Long id;
    private String uuid;
    private PersonalInfoReadOnlyDTO personalInfo;
    private String vat;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
