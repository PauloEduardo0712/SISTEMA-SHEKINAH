package br.com.escalas.api.assistant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantChatRequest(
    @NotBlank(message = "Mensagem e obrigatoria.")
    @Size(max = 1000, message = "Mensagem deve ter no maximo 1000 caracteres.")
    String message
) {
}
