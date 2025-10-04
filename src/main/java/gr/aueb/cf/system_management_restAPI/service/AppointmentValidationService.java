package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentInsertDTO;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentUpdateDTO;
import gr.aueb.cf.system_management_restAPI.model.Appointment;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.repository.AppointmentRepository;
import gr.aueb.cf.system_management_restAPI.repository.ClientRepository;
import gr.aueb.cf.system_management_restAPI.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentValidationService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final SecurityService securityService;

    @PersistenceContext
    private EntityManager entityManager;

    public User validateAndGetUser(Long userId) throws AppObjectNotFoundException {
        String currentUsername = securityService.getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppObjectNotFoundException("User", "Current user not found"));

        if (securityService.isCurrentUserSuperAdmin()) {
            // Αν το userId που στάλθηκε δεν υπάρχει, χρησιμοποίησε τον τρέχοντα user
            if (userId != null && !userId.equals(currentUser.getId())) {
                Optional<User> requestedUser = userRepository.findById(userId);
                if (requestedUser.isPresent()) {
                    return requestedUser.get(); // Υπάρχει ο user που ζήτησε
                }
                // Αν δεν υπάρχει, χρησιμοποίησε τον τρέχοντα
            }
        }

        // Για όλες τις άλλες περιπτώσεις
        return currentUser;
    }


    public Client validateAndGetClient(Long clientId) throws AppObjectNotFoundException {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new AppObjectNotFoundException("Client", "Client with id: " + clientId + " not found"));
    }

    public void validateNewAppointment(AppointmentInsertDTO dto) throws AppObjectAlreadyExists, AppObjectInvalidArgumentException {
        validateAppointmentTimeConflict(dto.getClientId(), dto.getAppointmentDateTime(), null);
        validateReminderTime(dto.getReminderDateTime(), dto.getAppointmentDateTime());
    }

    public void validateAppointmentUpdate(Long appointmentId, AppointmentUpdateDTO dto, Appointment existingAppointment)
            throws AppObjectAlreadyExists {

        if (dto.getAppointmentDateTime() != null &&
                !dto.getAppointmentDateTime().equals(existingAppointment.getAppointmentDateTime())) {
            validateAppointmentTimeConflict(
                    existingAppointment.getClient().getId(),
                    dto.getAppointmentDateTime(),
                    appointmentId
            );
        }
    }

    private void validateAppointmentTimeConflict(Long clientId, java.time.LocalDateTime dateTime, Long excludeAppointmentId)
            throws AppObjectAlreadyExists {

        String jpql;
        if (excludeAppointmentId == null) {
            jpql = "SELECT a FROM Appointment a WHERE a.client.id = :clientId AND a.appointmentDateTime = :dateTime";
        } else {
            jpql = "SELECT a FROM Appointment a WHERE a.client.id = :clientId AND a.appointmentDateTime = :dateTime AND a.id != :currentId";
        }

        var query = entityManager.createQuery(jpql, Appointment.class)
                .setParameter("clientId", clientId)
                .setParameter("dateTime", dateTime);

        if (excludeAppointmentId != null) {
            query.setParameter("currentId", excludeAppointmentId);
        }

        List<Appointment> conflictingAppointments = query.getResultList();

        if (!conflictingAppointments.isEmpty()) {
            throw new AppObjectAlreadyExists("Appointment",
                    "Client already has an appointment at " + dateTime);
        }
    }

    private void validateReminderTime(java.time.LocalDateTime reminderDateTime, java.time.LocalDateTime appointmentDateTime)
            throws AppObjectInvalidArgumentException {

        if (reminderDateTime != null && reminderDateTime.isAfter(appointmentDateTime)) {
            throw new AppObjectInvalidArgumentException("Appointment",
                    "Reminder time must be before appointment time");
        }
    }
}