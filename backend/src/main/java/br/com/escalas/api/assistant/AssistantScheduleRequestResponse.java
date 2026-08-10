package br.com.escalas.api.assistant;

import br.com.escalas.api.ministry.MinistryResponse;
import br.com.escalas.api.volunteer.VolunteerResponse;
import br.com.escalas.domain.assistant.AssistantRequestStatus;
import br.com.escalas.domain.schedule.TimeSlot;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AssistantScheduleRequestResponse(
    Long id,
    VolunteerResponse requester,
    VolunteerResponse targetVolunteer,
    MinistryResponse ministry,
    LocalDate serviceDate,
    LocalTime serviceTime,
    TimeSlot timeSlot,
    String roleName,
    String location,
    String eventName,
    String notes,
    String originalMessage,
    AssistantRequestStatus status,
    String adminNotes,
    LocalDateTime createdAt,
    LocalDateTime decidedAt,
    Long approvedScheduleId
) {
}
