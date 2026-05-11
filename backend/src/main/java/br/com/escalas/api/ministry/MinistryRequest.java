package br.com.escalas.api.ministry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MinistryRequest(
    @NotBlank(message = "Nome do ministério é obrigatório.")
    @Size(max = 120, message = "Nome do ministério deve ter no máximo 120 caracteres.")
    String name,
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres.")
    String description,
    boolean active
) {
}
