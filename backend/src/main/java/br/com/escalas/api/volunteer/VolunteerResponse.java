package br.com.escalas.api.volunteer;

import br.com.escalas.api.ministry.MinistryResponse;
import br.com.escalas.domain.auth.Role;
import java.util.Set;

public record VolunteerResponse(
    Long id,
    String fullName,
    String username,
    String email,
    String phone,
    String notes,
    boolean active,
    Role role,
    Set<MinistryResponse> ministries
) {
}
