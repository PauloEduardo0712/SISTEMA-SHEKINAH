package br.com.escalas.service;

import br.com.escalas.api.exception.BusinessException;
import br.com.escalas.api.exception.NotFoundException;
import br.com.escalas.api.schedule.ConflictResponse;
import br.com.escalas.api.schedule.ScheduleRequest;
import br.com.escalas.api.schedule.ScheduleResponse;
import br.com.escalas.domain.ministry.Ministry;
import br.com.escalas.domain.schedule.AvailabilityStatus;
import br.com.escalas.domain.schedule.Schedule;
import br.com.escalas.domain.volunteer.Volunteer;
import br.com.escalas.repository.AvailabilityRepository;
import br.com.escalas.repository.ScheduleRepository;
import br.com.escalas.security.AuthenticatedUser;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final AvailabilityRepository availabilityRepository;
    private final MinistryService ministryService;
    private final VolunteerService volunteerService;

    @Transactional(readOnly = true)
    public List<ScheduleResponse> findAll(LocalDate startDate, LocalDate endDate, Long ministryId, Long volunteerId) {
        List<Schedule> schedules;
        if (volunteerId != null) {
            schedules = scheduleRepository.findByVolunteerIdOrderByServiceDateAscServiceTimeAsc(volunteerId);
        } else if (ministryId != null) {
            schedules = scheduleRepository.findByMinistryIdOrderByServiceDateAscServiceTimeAsc(ministryId);
        } else if (startDate != null && endDate != null) {
            schedules = scheduleRepository.findByServiceDateBetweenOrderByServiceDateAscServiceTimeAsc(startDate, endDate);
        } else {
            schedules = scheduleRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(Schedule::getServiceDate).thenComparing(Schedule::getServiceTime))
                .toList();
        }
        return schedules.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> findMine(AuthenticatedUser user) {
        if (user.getVolunteerId() == null) {
            throw new BusinessException("Usuário atual não está vinculado a um voluntário.");
        }
        return scheduleRepository.findByVolunteerIdOrderByServiceDateAscServiceTimeAsc(user.getVolunteerId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ScheduleResponse create(ScheduleRequest request) {
        Schedule schedule = new Schedule();
        apply(schedule, request);
        validateConflicts(schedule, null);
        return toResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public ScheduleResponse update(Long id, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Escala não encontrada."));
        apply(schedule, request);
        validateConflicts(schedule, id);
        return toResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public void delete(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Escala não encontrada."));
        scheduleRepository.delete(schedule);
    }

    @Transactional(readOnly = true)
    public List<ConflictResponse> findConflicts() {
        return scheduleRepository.findAll().stream()
            .map(schedule -> {
                String message = detectConflict(schedule, schedule.getId());
                if (message == null) {
                    return null;
                }
                return new ConflictResponse(
                    schedule.getId(),
                    schedule.getVolunteer().getFullName(),
                    schedule.getMinistry().getName(),
                    message
                );
            })
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    private void apply(Schedule schedule, ScheduleRequest request) {
        Ministry ministry = ministryService.findEntityById(request.ministryId());
        Volunteer volunteer = volunteerService.findEntityById(request.volunteerId());

        schedule.setMinistry(ministry);
        schedule.setVolunteer(volunteer);
        schedule.setServiceDate(request.serviceDate());
        schedule.setServiceTime(request.serviceTime());
        schedule.setTimeSlot(request.timeSlot());
        schedule.setRoleName(request.roleName());
        schedule.setLocation(request.location());
        schedule.setEventName(request.eventName());
        schedule.setNotes(request.notes());
    }

    private void validateConflicts(Schedule schedule, Long currentId) {
        String message = detectConflict(schedule, currentId);
        if (message != null) {
            throw new BusinessException(message);
        }
    }

    private String detectConflict(Schedule schedule, Long currentId) {
        boolean unavailable = availabilityRepository
            .findByVolunteerIdAndDayOfWeekAndTimeSlot(
                schedule.getVolunteer().getId(),
                schedule.getServiceDate().getDayOfWeek(),
                schedule.getTimeSlot()
            )
            .filter(item -> item.getStatus() == AvailabilityStatus.INDISPONIVEL)
            .isPresent();

        if (unavailable) {
            return schedule.getVolunteer().getFullName() + " está indisponível para este dia e turno.";
        }

        boolean duplicate = currentId == null
            ? scheduleRepository.existsByVolunteerIdAndServiceDateAndServiceTime(
                schedule.getVolunteer().getId(),
                schedule.getServiceDate(),
                schedule.getServiceTime()
            )
            : scheduleRepository.existsByVolunteerIdAndServiceDateAndServiceTimeAndIdNot(
                schedule.getVolunteer().getId(),
                schedule.getServiceDate(),
                schedule.getServiceTime(),
                currentId
            );

        if (duplicate) {
            return schedule.getVolunteer().getFullName() + " já possui outra escala nesse mesmo horário.";
        }

        return null;
    }

    private ScheduleResponse toResponse(Schedule schedule) {
        String conflictMessage = detectConflict(schedule, schedule.getId());
        return new ScheduleResponse(
            schedule.getId(),
            ministryService.toResponse(schedule.getMinistry()),
            volunteerService.toResponse(schedule.getVolunteer()),
            schedule.getServiceDate(),
            schedule.getServiceTime(),
            schedule.getTimeSlot(),
            schedule.getRoleName(),
            schedule.getLocation(),
            schedule.getEventName(),
            schedule.getNotes(),
            conflictMessage != null,
            conflictMessage
        );
    }
}
