package gr.aueb.cf.system_management_restAPI.dto;

import gr.aueb.cf.system_management_restAPI.core.enums.GenderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonalInfoReadOnlyDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private GenderType gender;
    private String address;
    private String cityName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
