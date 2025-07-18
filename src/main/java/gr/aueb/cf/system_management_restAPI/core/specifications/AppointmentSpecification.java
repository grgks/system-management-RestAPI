package gr.aueb.cf.system_management_restAPI.core.specifications;

import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import gr.aueb.cf.system_management_restAPI.model.Appointment;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class AppointmentSpecification {

    private AppointmentSpecification() {
    }

    public static Specification<Appointment> appointmentUserIdIs(Long userId) {
        return ((root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            Join<Appointment, User> user = root.join("user");
            return criteriaBuilder.equal(user.get("id"), userId);
        });
    }

    public static Specification<Appointment> appointmentClientIdIs(Long clientId) {
        return ((root, query, criteriaBuilder) -> {
            if (clientId == null) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            Join<Appointment, Client> client = root.join("client");
            return criteriaBuilder.equal(client.get("id"), clientId);
        });
    }

    public static Specification<Appointment> appointmentUserUsernameIs(String username) {
        return ((root, query, criteriaBuilder) -> {
            if (username == null || username.isBlank()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            Join<Appointment, User> user = root.join("user");
            return criteriaBuilder.like(criteriaBuilder.upper(user.get("username")), "%" + username.toUpperCase() + "%");
        });
    }

    public static Specification<Appointment> appointmentClientVatIs(String clientVat) {
        return ((root, query, criteriaBuilder) -> {
            if (clientVat == null || clientVat.isBlank()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            Join<Appointment, Client> client = root.join("client");
            return criteriaBuilder.like(criteriaBuilder.upper(client.get("vat")), "%" + clientVat.toUpperCase() + "%");
        });
    }

    public static Specification<Appointment> appointmentStatusIs(AppointmentStatus status) {
        return ((root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            return criteriaBuilder.equal(root.get("status"), status);
        });
    }

    public static Specification<Appointment> appointmentEmailReminderIs(Boolean emailReminder) {
        return ((root, query, criteriaBuilder) -> {
            if (emailReminder == null) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            return criteriaBuilder.equal(root.get("emailReminder"), emailReminder);
        });
    }

    public static Specification<Appointment> appointmentReminderSentIs(Boolean reminderSent) {
        return ((root, query, criteriaBuilder) -> {
            if (reminderSent == null) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            return criteriaBuilder.equal(root.get("reminderSent"), reminderSent);
        });
    }

    public static Specification<Appointment> apUserIsActive(Boolean isActive) {
        return ((root, query, criteriaBuilder) -> {
            if (isActive == null) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            Join<Appointment, User> user = root.join("user");
            return criteriaBuilder.equal(user.get("isActive"), isActive);
        });
    }

    public static Specification<Appointment> apStringFieldLike(String field, String value) {
        return ((root, query, criteriaBuilder) -> {
            if (value == null || value.trim().isEmpty()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            return criteriaBuilder.like(criteriaBuilder.upper(root.get(field)), "%" + value.toUpperCase() + "%");   // case-insensitive search
        });
    }
}