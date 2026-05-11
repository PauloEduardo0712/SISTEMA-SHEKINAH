package br.com.escalas.api.volunteer;

import br.com.escalas.security.AuthenticatedUser;
import br.com.escalas.service.VolunteerService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/volunteers")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<VolunteerResponse> findAll() {
        return volunteerService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public VolunteerResponse findById(@PathVariable Long id) {
        return volunteerService.findById(id);
    }

    @GetMapping("/me")
    public VolunteerResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return volunteerService.findById(user.getVolunteerId());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public VolunteerResponse create(@Valid @RequestBody VolunteerRequest request) {
        return volunteerService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public VolunteerResponse update(@PathVariable Long id, @Valid @RequestBody VolunteerRequest request) {
        return volunteerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        volunteerService.delete(id);
    }
}
