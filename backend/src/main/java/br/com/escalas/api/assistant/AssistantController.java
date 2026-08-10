package br.com.escalas.api.assistant;

import br.com.escalas.domain.assistant.AssistantRequestStatus;
import br.com.escalas.security.AuthenticatedUser;
import br.com.escalas.service.AssistantService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping("/chat")
    public AssistantChatResponse chat(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody AssistantChatRequest request
    ) {
        return assistantService.chat(user, request);
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AssistantScheduleRequestResponse> findRequests(
        @RequestParam(required = false) AssistantRequestStatus status
    ) {
        return assistantService.findRequests(status);
    }

    @GetMapping("/requests/me")
    public List<AssistantScheduleRequestResponse> findMyRequests(@AuthenticationPrincipal AuthenticatedUser user) {
        return assistantService.findMyRequests(user);
    }

    @PostMapping("/requests/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public AssistantScheduleRequestResponse approve(
        @PathVariable Long id,
        @Valid @RequestBody AssistantDecisionRequest request
    ) {
        return assistantService.approve(id, request);
    }

    @PostMapping("/requests/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public AssistantScheduleRequestResponse reject(
        @PathVariable Long id,
        @Valid @RequestBody AssistantDecisionRequest request
    ) {
        return assistantService.reject(id, request);
    }

    @GetMapping("/reminders")
    public List<AssistantReminderResponse> reminders(@AuthenticationPrincipal AuthenticatedUser user) {
        return assistantService.findReminders(user);
    }
}
