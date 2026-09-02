package br.com.escalas.api.volunteer;

import br.com.escalas.domain.auth.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record VolunteerRequest(
    @NotBlank(message = "Nome completo é obrigatório.")
    @Size(max = 150, message = "Nome completo deve ter no máximo 150 caracteres.")
    String fullName,
    @NotBlank(message = "Usuário é obrigatório.")
    @Size(max = 80, message = "Usuário deve ter no máximo 80 caracteres.")
    String username,
    @Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres.")
    String email,
    @Size(max = 30, message = "Telefone deve ter no máximo 30 caracteres.")
    String phone,
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres.")
    String notes,
    @NotEmpty(message = "Selecione pelo menos um ministério.")
    Set<Long> ministryIds,
    boolean active,
    Role role,
    @Size(min = 4, max = 60, message = "Senha deve ter entre 4 e 60 caracteres.")
    String password
) {
}
