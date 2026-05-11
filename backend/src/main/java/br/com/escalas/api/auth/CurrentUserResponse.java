package br.com.escalas.api.auth;

import br.com.escalas.domain.auth.Role;

public record CurrentUserResponse(
    Long userId,
    Long volunteerId,
    String username,
    Role role
) {
}
