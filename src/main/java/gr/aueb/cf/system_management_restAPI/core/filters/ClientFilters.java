package gr.aueb.cf.system_management_restAPI.core.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.lang.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
@Schema(description = "Client filtering criteria")
public class ClientFilters extends GenericFilters {

    @Nullable
    @Schema(description = "Client UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String uuid;

    @Nullable
    @Schema(description = "User VAT number", example = "123456789")
    private String userVat;

    @Nullable
    @Schema(description = "Username", example = "client12")
    private String userUsername;

    @Nullable
    @Schema(description = "Client VAT number", example = "987654321")
    private String clientVat;

    @Nullable
    @Schema(description = "First name", example = "John")
    private String firstName;

    @Nullable
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Nullable
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Nullable
    @Schema(description = "Phone number", example = "+30123456789")
    private String phone;

    @Nullable
    @Schema(description = "Active status", example = "true")
    private Boolean active;
}