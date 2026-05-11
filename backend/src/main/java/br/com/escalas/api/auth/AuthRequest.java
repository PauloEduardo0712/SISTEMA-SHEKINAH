package br.com.escalas.api.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
    @NotBlank(message = "Usuario e obrigatorio.")
    String username,
    @NotBlank(message = "Senha e obrigatoria.")
    String password
) {
}
