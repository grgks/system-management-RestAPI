package gr.aueb.cf.system_management_restAPI.mapper;

import gr.aueb.cf.system_management_restAPI.dto.*;
import gr.aueb.cf.system_management_restAPI.model.Appointment;
import gr.aueb.cf.system_management_restAPI.model.Client;
import gr.aueb.cf.system_management_restAPI.model.PersonalInfo;
import gr.aueb.cf.system_management_restAPI.model.User;
import gr.aueb.cf.system_management_restAPI.model.static_data.City;
import gr.aueb.cf.system_management_restAPI.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Mapper {

    private final PasswordEncoder passwordEncoder;
    private final CityRepository cityRepository;
    // CLIENT MAPPINGS

    /**
     * Map Client Entity → ClientReadOnlyDTO
     */
    public ClientReadOnlyDTO mapToClientReadOnlyDTO(Client client) {
        var dto = new ClientReadOnlyDTO();

        dto.setId(client.getId());
        dto.setUuid(client.getUuid());
        dto.setVat(client.getVat());
        dto.setNotes(client.getNotes());
        dto.setCreatedAt(client.getCreatedAt());
        dto.setUpdatedAt(client.getUpdatedAt());

        // Map PersonalInfo to PersonalInfoReadOnlyDTO
        if (client.getPersonalInfo() != null) {
            dto.setPersonalInfo(mapToPersonalInfoReadOnlyDTO(client.getPersonalInfo()));
        }

        return dto;
    }

    /**
     * Map ClientInsertDTO → Client Entity
     */
    public Client mapToClientEntity(ClientInsertDTO clientInsertDTO) {
        Client client = new Client();

        client.setVat(clientInsertDTO.getVat());
        client.setNotes(clientInsertDTO.getNotes());


        // Map PersonalInfo
        if (clientInsertDTO.getPersonalInfo() != null) {
            client.setPersonalInfo(mapToPersonalInfoEntity(clientInsertDTO.getPersonalInfo()));
        }


        return client;
    }

    /**
     * Update Client Entity από ClientUpdateDTO
     */
    public void updateClientFromDTO(ClientUpdateDTO clientUpdateDTO, Client existingClient) {
        existingClient.setVat(clientUpdateDTO.getVat());
        existingClient.setNotes(clientUpdateDTO.getNotes());

        // Update PersonalInfo
        if (clientUpdateDTO.getPersonalInfo() != null && existingClient.getPersonalInfo() != null) {
            updatePersonalInfoFromDTO(clientUpdateDTO.getPersonalInfo(), existingClient.getPersonalInfo());
        }
    }

    //  PERSONAL INFO MAPPINGS

    /**
     * Map PersonalInfo Entity → PersonalInfoReadOnlyDTO
     */
    public PersonalInfoReadOnlyDTO mapToPersonalInfoReadOnlyDTO(PersonalInfo personalInfo) {
        var dto = new PersonalInfoReadOnlyDTO();

        dto.setId(personalInfo.getId());
        dto.setFirstName(personalInfo.getFirstName());
        dto.setLastName(personalInfo.getLastName());
        dto.setEmail(personalInfo.getEmail());
        dto.setPhone(personalInfo.getPhone());
        dto.setDateOfBirth(personalInfo.getDateOfBirth());
        dto.setGender(personalInfo.getGender());
        dto.setAddress(personalInfo.getAddress());
        dto.setCreatedAt(personalInfo.getCreatedAt());
        dto.setUpdatedAt(personalInfo.getUpdatedAt());

        // Map City name
        //System.out.println("City object: " + personalInfo.getCity());
        if (personalInfo.getCity() != null) {
           // System.out.println("City name: " + personalInfo.getCity().getName());
            dto.setCityName(personalInfo.getCity().getName());
        }

        return dto;
    }

    /**
     * Map PersonalInfoInsertDTO → PersonalInfo Entity
     */
    public PersonalInfo mapToPersonalInfoEntity(PersonalInfoInsertDTO personalInfoInsertDTO) {
        PersonalInfo personalInfo = new PersonalInfo();

        personalInfo.setFirstName(personalInfoInsertDTO.getFirstName());
        personalInfo.setLastName(personalInfoInsertDTO.getLastName());
        personalInfo.setEmail(personalInfoInsertDTO.getEmail());
        personalInfo.setPhone(personalInfoInsertDTO.getPhone());
        personalInfo.setDateOfBirth(personalInfoInsertDTO.getDateOfBirth());
        personalInfo.setGender(personalInfoInsertDTO.getGender());
        personalInfo.setAddress(personalInfoInsertDTO.getAddress());


        if (personalInfoInsertDTO.getCityId() != null) {
            City city =  cityRepository.findById(personalInfoInsertDTO.getCityId())
                    .orElse(null);
            personalInfo.setCity(city);
        }
        return personalInfo;
    }

    /**
     * Update PersonalInfo Entity από PersonalInfoUpdateDTO
     */
    public void updatePersonalInfoFromDTO(PersonalInfoUpdateDTO personalInfoUpdateDTO, PersonalInfo existingPersonalInfo) {
        existingPersonalInfo.setFirstName(personalInfoUpdateDTO.getFirstName());
        existingPersonalInfo.setLastName(personalInfoUpdateDTO.getLastName());
        existingPersonalInfo.setEmail(personalInfoUpdateDTO.getEmail());
        existingPersonalInfo.setPhone(personalInfoUpdateDTO.getPhone());
        existingPersonalInfo.setDateOfBirth(personalInfoUpdateDTO.getDateOfBirth());
        existingPersonalInfo.setGender(personalInfoUpdateDTO.getGender());
        existingPersonalInfo.setAddress(personalInfoUpdateDTO.getAddress());

        if (personalInfoUpdateDTO.getCityId() != null) {

            City city = new City();
            city.setId(personalInfoUpdateDTO.getCityId());
            existingPersonalInfo.setCity(city);
        }
    }
    //  USER MAPPINGS

    /**
     * Map User Entity → UserReadOnlyDTO
     */
    public UserReadOnlyDTO mapToUserReadOnlyDTO(User user) {
        var dto = new UserReadOnlyDTO();

        dto.setId(user.getId());
        dto.setUuid(user.getUuid());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        return dto;
    }

    /**
     * Map UserInsertDTO → User Entity
     */
    public User mapToUserEntity(UserInsertDTO userInsertDTO) {
        User user = new User();

        user.setUuid(UUID.randomUUID().toString());
        user.setUsername(userInsertDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userInsertDTO.getPassword())); // Encrypt password
        user.setEmail(userInsertDTO.getEmail());
        user.setFirstName(userInsertDTO.getFirstName());
        user.setLastName(userInsertDTO.getLastName());
        user.setPhone(userInsertDTO.getPhone());
        user.setDateOfBirth(userInsertDTO.getDateOfBirth());
        user.setGender(userInsertDTO.getGender());
        user.setRole(userInsertDTO.getRole());
        user.setIsActive(userInsertDTO.getIsActive());

        return user;
    }

    //  APPOINTMENT MAPPINGS

    /**
     * Map Appointment Entity → AppointmentReadOnlyDTO
     */
    public AppointmentReadOnlyDTO mapToAppointmentReadOnlyDTO(Appointment appointment) {
        var dto = new AppointmentReadOnlyDTO();

        dto.setId(appointment.getId());
        dto.setUuid(appointment.getUuid());
        dto.setAppointmentDateTime(appointment.getAppointmentDateTime());
        dto.setStatus(appointment.getStatus());
        dto.setEmailReminder(appointment.getEmailReminder());
        dto.setReminderDateTime(appointment.getReminderDateTime());
        dto.setReminderSent(appointment.getReminderSent());
        dto.setNotes(appointment.getNotes());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());

        // Map User username
        if (appointment.getUser() != null) {
            dto.setUsername(appointment.getUser().getUsername());
        }

        // Map Client name (from PersonalInfo)
        if (appointment.getClient() != null && appointment.getClient().getPersonalInfo() != null) {
            PersonalInfo personalInfo = appointment.getClient().getPersonalInfo();
            dto.setClientName(personalInfo.getFirstName() + " " + personalInfo.getLastName());
            dto.setClientLastName(personalInfo.getLastName());
            dto.setClientPhone(personalInfo.getPhone());
        }

        return dto;
    }

    /**
     * Map AppointmentInsertDTO → Appointment Entity
     */
    public Appointment mapToAppointmentEntity(AppointmentInsertDTO appointmentInsertDTO) {
        Appointment appointment = new Appointment();

        appointment.setAppointmentDateTime(appointmentInsertDTO.getAppointmentDateTime());
        appointment.setStatus(appointmentInsertDTO.getStatus());
        appointment.setEmailReminder(appointmentInsertDTO.getEmailReminder());
        appointment.setReminderDateTime(appointmentInsertDTO.getReminderDateTime());
        appointment.setNotes(appointmentInsertDTO.getNotes());
        appointment.setReminderSent(false); // Default value


        return appointment;
    }

    /**
     * Update Appointment Entity από AppointmentUpdateDTO
     */
    public void updateAppointmentFromDTO(AppointmentUpdateDTO appointmentUpdateDTO, Appointment existingAppointment) {
        existingAppointment.setAppointmentDateTime(appointmentUpdateDTO.getAppointmentDateTime());
        existingAppointment.setStatus(appointmentUpdateDTO.getStatus());
        existingAppointment.setEmailReminder(appointmentUpdateDTO.getEmailReminder());
        existingAppointment.setReminderDateTime(appointmentUpdateDTO.getReminderDateTime());
        existingAppointment.setNotes(appointmentUpdateDTO.getNotes());
    }
}