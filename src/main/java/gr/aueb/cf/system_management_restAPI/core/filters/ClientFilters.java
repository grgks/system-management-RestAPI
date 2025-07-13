package gr.aueb.cf.system_management_restAPI.core.filters;

import lombok.*;
import org.springframework.lang.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ClientFilters extends GenericFilters {

    @Nullable
    private String uuid;

    @Nullable
    private String userVat;

    @Nullable
    private String userUsername;

    @Nullable
    private String clientVat;

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    @Nullable
    private String email;

    @Nullable
    private String phone;

    @Nullable
    private Boolean active;
}