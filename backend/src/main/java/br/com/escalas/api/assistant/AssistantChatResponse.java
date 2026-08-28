package br.com.escalas.api.assistant;

import java.util.List;

public record AssistantChatResponse(
    String reply,
    AssistantScheduleRequestResponse createdRequest,
    List<AssistantReminderResponse> reminders
) {
}
