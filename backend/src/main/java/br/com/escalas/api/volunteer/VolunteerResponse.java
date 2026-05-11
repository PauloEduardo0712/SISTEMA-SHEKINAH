package br.com.escalas.api.volunteer;

import br.com.escalas.api.ministry.MinistryResponse;
import java.util.Set;

public record VolunteerResponse(
    Long id,
    String fullName,
    String username,
    String email,
    String phone,
    String notes,
    boolean active,
    Set<MinistryResponse> ministries
) {
}
