package br.com.escalas.api.schedule;

import br.com.escalas.api.ministry.MinistryResponse;
import br.com.escalas.api.volunteer.VolunteerResponse;
import br.com.escalas.domain.schedule.TimeSlot;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleResponse(
    Long id,
    MinistryResponse ministry,
    VolunteerResponse volunteer,
    LocalDate serviceDate,
    LocalTime serviceTime,
    TimeSlot timeSlot,
    String roleName,
    String location,
    String eventName,
    String notes,
    boolean conflict,
    String conflictMessage
) {
}
