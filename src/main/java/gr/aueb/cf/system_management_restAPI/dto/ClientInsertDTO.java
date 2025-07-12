package gr.aueb.cf.system_management_restAPI.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientInsertDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @Valid
    @NotNull(message = "Personal info is required")
    private PersonalInfoInsertDTO personalInfo;

    @Pattern(regexp = "^[0-9]{10}$", message = "VAT must be exactly 10 digits")
    private String vat;

    private String notes;
}