package gr.aueb.cf.system_management_restAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthenticationResponseDTO {

    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String role;
    private Long expiresIn;

    public AuthenticationResponseDTO(String token, String username, String role, Long expiresIn) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.expiresIn = expiresIn;
    }
}