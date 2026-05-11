package br.com.escalas.api.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record RegisterRequest(
    @NotBlank(message = "Nome completo e obrigatorio.")
    @Size(max = 150, message = "Nome completo deve ter no maximo 150 caracteres.")
    String fullName,
    @NotBlank(message = "Usuario e obrigatorio.")
    @Size(max = 80, message = "Usuario deve ter no maximo 80 caracteres.")
    String username,
    @Size(max = 255, message = "E-mail deve ter no maximo 255 caracteres.")
    String email,
    @Size(max = 30, message = "Telefone deve ter no maximo 30 caracteres.")
    String phone,
    @Size(max = 500, message = "Observacoes devem ter no maximo 500 caracteres.")
    String notes,
    @NotEmpty(message = "Selecione pelo menos um ministerio.")
    Set<Long> ministryIds,
    @NotBlank(message = "Senha e obrigatoria.")
    @Size(min = 4, max = 60, message = "Senha deve ter entre 4 e 60 caracteres.")
    String password
) {
}
