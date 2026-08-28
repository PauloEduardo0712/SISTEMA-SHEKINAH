package br.com.escalas.api.assistant;

import jakarta.validation.constraints.Size;

public record AssistantDecisionRequest(
    @Size(max = 500, message = "Observacao deve ter no maximo 500 caracteres.")
    String adminNotes
) {
}
