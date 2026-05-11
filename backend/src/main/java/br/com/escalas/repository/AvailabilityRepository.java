package br.com.escalas.repository;

import br.com.escalas.domain.schedule.Availability;
import br.com.escalas.domain.schedule.TimeSlot;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByVolunteerIdOrderByDayOfWeekAscTimeSlotAsc(Long volunteerId);

    Optional<Availability> findByVolunteerIdAndDayOfWeekAndTimeSlot(Long volunteerId, DayOfWeek dayOfWeek, TimeSlot timeSlot);

    void deleteByVolunteerId(Long volunteerId);
}
