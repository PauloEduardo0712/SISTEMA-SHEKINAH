package br.com.escalas.service;

import br.com.escalas.api.availability.AvailabilityRequest;
import br.com.escalas.api.availability.AvailabilityResponse;
import br.com.escalas.api.exception.BusinessException;
import br.com.escalas.domain.schedule.Availability;
import br.com.escalas.domain.volunteer.Volunteer;
import br.com.escalas.security.AuthenticatedUser;
import br.com.escalas.repository.AvailabilityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final VolunteerService volunteerService;

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> findByVolunteer(Long volunteerId) {
        return availabilityRepository.findByVolunteerIdOrderByDayOfWeekAscTimeSlotAsc(volunteerId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> findMine(AuthenticatedUser user) {
        if (user.getVolunteerId() == null) {
            throw new BusinessException("Usuário atual não está vinculado a um voluntário.");
        }
        return findByVolunteer(user.getVolunteerId());
    }

    @Transactional
    public List<AvailabilityResponse> replaceForVolunteer(Long volunteerId, List<AvailabilityRequest> requests) {
        Volunteer volunteer = volunteerService.findEntityById(volunteerId);
        availabilityRepository.deleteByVolunteerId(volunteerId);

        List<Availability> saved = requests.stream().map(request -> {
            Availability availability = new Availability();
            availability.setVolunteer(volunteer);
            availability.setDayOfWeek(request.dayOfWeek());
            availability.setTimeSlot(request.timeSlot());
            availability.setStatus(request.status());
            return availabilityRepository.save(availability);
        }).toList();

        return saved.stream().map(this::toResponse).toList();
    }

    public AvailabilityResponse toResponse(Availability availability) {
        return new AvailabilityResponse(
            availability.getId(),
            availability.getDayOfWeek(),
            availability.getTimeSlot(),
            availability.getStatus()
        );
    }
}
