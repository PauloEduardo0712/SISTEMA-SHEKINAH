package br.com.escalas.repository;

import br.com.escalas.domain.auth.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    Optional<UserAccount> findByVolunteerId(Long volunteerId);

    void deleteByVolunteerId(Long volunteerId);
}
