package br.com.escalas.api.availability;

import br.com.escalas.domain.schedule.AvailabilityStatus;
import br.com.escalas.domain.schedule.TimeSlot;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;

public record AvailabilityRequest(
    @NotNull(message = "Dia da semana é obrigatório.")
    DayOfWeek dayOfWeek,
    @NotNull(message = "Turno é obrigatório.")
    TimeSlot timeSlot,
    @NotNull(message = "Status é obrigatório.")
    AvailabilityStatus status
) {
}
