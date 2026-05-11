package br.com.escalas.api.availability;

import br.com.escalas.domain.schedule.AvailabilityStatus;
import br.com.escalas.domain.schedule.TimeSlot;
import java.time.DayOfWeek;

public record AvailabilityResponse(
    Long id,
    DayOfWeek dayOfWeek,
    TimeSlot timeSlot,
    AvailabilityStatus status
) {
}
