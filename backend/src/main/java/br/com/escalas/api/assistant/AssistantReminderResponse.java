package br.com.escalas.api.assistant;

import br.com.escalas.api.schedule.ScheduleResponse;

public record AssistantReminderResponse(
    String title,
    String message,
    ScheduleResponse schedule
) {
}
