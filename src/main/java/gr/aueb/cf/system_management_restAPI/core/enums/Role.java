package gr.aueb.cf.system_management_restAPI.core.enums;

public enum Role {
    CLIENT,
    PATIENT,
    SUPER_ADMIN;

    public String getAuthority() {

        return "ROLE_" + this.name();
    }
}

