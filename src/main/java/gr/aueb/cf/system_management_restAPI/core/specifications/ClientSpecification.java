package gr.aueb.cf.system_management_restAPI.core.specifications;

import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.PersonalInfo;
import gr.aueb.cf.system_management_restAPI.model.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class ClientSpecification {

    private ClientSpecification() {
    }

    public static Specification<Client> clientUserVatIs(String vat) {
        return ((root, query, criteriaBuilder) -> {
            if (vat == null || vat.isBlank()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            return criteriaBuilder.like(criteriaBuilder.upper(root.get("vat")), "%" + vat.toUpperCase() + "%");
        });
    }

    public static Specification<Client> clientUserUsernameIs(String username) {
        return ((root, query, criteriaBuilder) -> {
            if (username == null || username.isBlank()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            Join<Client, User> user = root.join("user");
            return criteriaBuilder.equal(user.get("username"), username);
        });
    }

    public static Specification<Client> clUserIsActive(Boolean isActive) {
        return ((root, query, criteriaBuilder) -> {
            if (isActive == null) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            Join<Client, User> user = root.join("user");
            return criteriaBuilder.equal(user.get("isActive"), isActive);
        });
    }

    public static Specification<Client> clPersonalInfoFirstNameIs(String firstName) {
        return ((root, query, criteriaBuilder) -> {
            if (firstName == null || firstName.isBlank()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            Join<Client, PersonalInfo> personalInfo = root.join("personalInfo");
            return criteriaBuilder.like(criteriaBuilder.upper(personalInfo.get("firstName")), "%" + firstName.toUpperCase() + "%");
        });
    }

    public static Specification<Client> clPersonalInfoLastNameIs(String lastName) {
        return ((root, query, criteriaBuilder) -> {
            if (lastName == null || lastName.isBlank()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            Join<Client, PersonalInfo> personalInfo = root.join("personalInfo");
            return criteriaBuilder.like(criteriaBuilder.upper(personalInfo.get("lastName")), "%" + lastName.toUpperCase() + "%");
        });
    }

    public static Specification<Client> clPersonalInfoEmailIs(String email) {
        return ((root, query, criteriaBuilder) -> {
            if (email == null || email.isBlank()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            Join<Client, PersonalInfo> personalInfo = root.join("personalInfo");
            return criteriaBuilder.like(criteriaBuilder.upper(personalInfo.get("email")), "%" + email.toUpperCase() + "%");
        });
    }

    public static Specification<Client> clPersonalInfoPhoneIs(String phone) {
        return ((root, query, criteriaBuilder) -> {
            if (phone == null || phone.isBlank()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            Join<Client, PersonalInfo> personalInfo = root.join("personalInfo");
            return criteriaBuilder.like(personalInfo.get("phone"), "%" + phone + "%");
        });
    }

    public static Specification<Client> clStringFieldLike(String field, String value) {
        return ((root, query, criteriaBuilder) -> {
            if (value == null || value.trim().isEmpty()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            return criteriaBuilder.like(criteriaBuilder.upper(root.get(field)), "%" + value.toUpperCase() + "%");   // case-insensitive search
        });
    }
}