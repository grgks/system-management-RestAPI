package gr.aueb.cf.system_management_restAPI.service;

import gr.aueb.cf.system_management_restAPI.core.enums.AppointmentStatus;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.system_management_restAPI.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.system_management_restAPI.core.filters.AppointmentFilters;
import gr.aueb.cf.system_management_restAPI.core.filters.Paginated;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentInsertDTO;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentReadOnlyDTO;
import gr.aueb.cf.system_management_restAPI.dto.AppointmentUpdateDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface IAppointmentService {
    AppointmentReadOnlyDTO saveAppointment(AppointmentInsertDTO dto) throws AppObjectNotFoundException, AppObjectAlreadyExists, AppObjectInvalidArgumentException;
    AppointmentReadOnlyDTO updateAppointment(Long id, AppointmentUpdateDTO dto) throws AppObjectNotFoundException, AppObjectAlreadyExists;
    AppointmentReadOnlyDTO getAppointmentById(Long id) throws AppObjectNotFoundException;
    AppointmentReadOnlyDTO getAppointmentByUuid(String uuid) throws AppObjectNotFoundException;
    void deleteAppointment(Long id) throws AppObjectNotFoundException;
    Page<AppointmentReadOnlyDTO> getPaginatedAppointments(int page, int size);
    Page<AppointmentReadOnlyDTO> getPaginatedSortedAppointments(int page, int size, String sortBy, String sortDirection);
    Paginated<AppointmentReadOnlyDTO> getAppointmentsFilteredPaginated(AppointmentFilters filters);
    List<AppointmentReadOnlyDTO> getAppointmentsFiltered(AppointmentFilters filters);
    List<AppointmentReadOnlyDTO> getAppointmentsByClient(Long clientId);
    List<AppointmentReadOnlyDTO> getAppointmentsByUser(Long userId);
    List<AppointmentReadOnlyDTO> getAppointmentsByStatus(AppointmentStatus status);
    List<AppointmentReadOnlyDTO> getAppointmentsByClientPhone(String phone) throws AppObjectNotFoundException;
    List<AppointmentReadOnlyDTO> getUpcomingAppointments();
    List<AppointmentReadOnlyDTO> getAppointmentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
    List<AppointmentReadOnlyDTO> getPendingEmailReminders();
    List<AppointmentReadOnlyDTO> getClientAppointmentsBetweenDates(Long clientId, LocalDateTime startDate, LocalDateTime endDate);
    void markReminderAsSent(Long appointmentId) throws AppObjectNotFoundException;
}