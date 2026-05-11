package br.com.escalas.repository;

import br.com.escalas.domain.volunteer.Volunteer;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {

    @EntityGraph(attributePaths = "ministries")
    Optional<Volunteer> findById(Long id);

    boolean existsByUsernameIgnoreCase(String username);

    Optional<Volunteer> findByUsernameIgnoreCase(String username);
}
