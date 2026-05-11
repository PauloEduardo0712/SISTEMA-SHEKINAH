package br.com.escalas.repository;

import br.com.escalas.domain.schedule.Schedule;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Override
    @EntityGraph(attributePaths = {"ministry", "volunteer"})
    List<Schedule> findAll();

    @EntityGraph(attributePaths = {"ministry", "volunteer"})
    List<Schedule> findByServiceDateBetweenOrderByServiceDateAscServiceTimeAsc(LocalDate startDate, LocalDate endDate);

    @EntityGraph(attributePaths = {"ministry", "volunteer"})
    List<Schedule> findByVolunteerIdOrderByServiceDateAscServiceTimeAsc(Long volunteerId);

    @EntityGraph(attributePaths = {"ministry", "volunteer"})
    List<Schedule> findByMinistryIdOrderByServiceDateAscServiceTimeAsc(Long ministryId);

    boolean existsByVolunteerIdAndServiceDateAndServiceTimeAndIdNot(Long volunteerId, LocalDate serviceDate, LocalTime serviceTime, Long id);

    boolean existsByVolunteerIdAndServiceDateAndServiceTime(Long volunteerId, LocalDate serviceDate, LocalTime serviceTime);

    boolean existsByVolunteerId(Long volunteerId);
}
