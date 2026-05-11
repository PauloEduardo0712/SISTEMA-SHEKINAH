package br.com.escalas.api.schedule;

import br.com.escalas.domain.schedule.TimeSlot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleRequest(
    @NotNull(message = "Ministério é obrigatório.")
    Long ministryId,
    @NotNull(message = "Voluntário é obrigatório.")
    Long volunteerId,
    @NotNull(message = "Data é obrigatória.")
    LocalDate serviceDate,
    @NotNull(message = "Horário é obrigatório.")
    LocalTime serviceTime,
    @NotNull(message = "Turno é obrigatório.")
    TimeSlot timeSlot,
    @Size(max = 120, message = "Função deve ter no máximo 120 caracteres.")
    String roleName,
    @Size(max = 120, message = "Local deve ter no máximo 120 caracteres.")
    String location,
    @Size(max = 120, message = "Evento deve ter no máximo 120 caracteres.")
    String eventName,
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres.")
    String notes
) {
}
