package gr.aueb.cf.system_management_restAPI.dto;

import gr.aueb.cf.system_management_restAPI.core.enums.GenderType;
import gr.aueb.cf.system_management_restAPI.core.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserInsertDTO {

    @NotNull(message = "Is active must not be null")
    private Boolean isActive;

    @NotEmpty(message = "Username must not be empty")
    private String username;

    @Pattern(regexp = "^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[@#$%!^&*]).{8,}$", message = "Invalid Password")
    private String password;

    @NotEmpty(message = "Email must not be empty")
    @Email(message = "Invalid email format")
    private String email;


    @NotNull(message = "Role must not be null")
    private Role role;
}
