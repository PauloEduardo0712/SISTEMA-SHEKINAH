package br.com.escalas.repository;

import br.com.escalas.domain.ministry.Ministry;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MinistryRepository extends JpaRepository<Ministry, Long> {

    Optional<Ministry> findByNameIgnoreCase(String name);
}
