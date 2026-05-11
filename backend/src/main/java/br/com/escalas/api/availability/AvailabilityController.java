package br.com.escalas.api.availability;

import br.com.escalas.security.AuthenticatedUser;
import br.com.escalas.service.AvailabilityService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/availabilities")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/volunteer/{volunteerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AvailabilityResponse> findByVolunteer(@PathVariable Long volunteerId) {
        return availabilityService.findByVolunteer(volunteerId);
    }

    @GetMapping("/me")
    public List<AvailabilityResponse> findMine(@AuthenticationPrincipal AuthenticatedUser user) {
        return availabilityService.findMine(user);
    }

    @PutMapping("/volunteer/{volunteerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public List<AvailabilityResponse> replaceForVolunteer(
        @PathVariable Long volunteerId,
        @Valid @RequestBody List<AvailabilityRequest> requests
    ) {
        return availabilityService.replaceForVolunteer(volunteerId, requests);
    }

    @PutMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public List<AvailabilityResponse> replaceMine(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody List<AvailabilityRequest> requests
    ) {
        return availabilityService.replaceForVolunteer(user.getVolunteerId(), requests);
    }
}
