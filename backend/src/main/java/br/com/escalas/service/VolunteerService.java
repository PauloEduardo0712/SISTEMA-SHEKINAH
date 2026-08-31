package br.com.escalas.service;

import br.com.escalas.api.exception.BusinessException;
import br.com.escalas.api.exception.NotFoundException;
import br.com.escalas.api.ministry.MinistryResponse;
import br.com.escalas.api.volunteer.VolunteerRequest;
import br.com.escalas.api.volunteer.VolunteerResponse;
import br.com.escalas.domain.auth.Role;
import br.com.escalas.domain.auth.UserAccount;
import br.com.escalas.domain.ministry.Ministry;
import br.com.escalas.domain.volunteer.Volunteer;
import br.com.escalas.repository.AvailabilityRepository;
import br.com.escalas.repository.ScheduleRepository;
import br.com.escalas.repository.UserAccountRepository;
import br.com.escalas.repository.VolunteerRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;
    private final UserAccountRepository userAccountRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ScheduleRepository scheduleRepository;
    private final MinistryService ministryService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<VolunteerResponse> findAll() {
        return volunteerRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public VolunteerResponse findById(Long id) {
        return toResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Volunteer findEntityById(Long id) {
        return volunteerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Voluntario nao encontrado."));
    }

    @Transactional
    public VolunteerResponse create(VolunteerRequest request) {
        validateUniqueUsername(null, request.username());
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessException("Senha e obrigatoria para criar um voluntario.");
        }

        Volunteer volunteer = new Volunteer();
        apply(volunteer, request);
        Volunteer saved = volunteerRepository.save(volunteer);

        UserAccount account = new UserAccount();
        account.setUsername(saved.getUsername());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(resolveVolunteerRole(request.role()));
        account.setActive(saved.isActive());
        account.setVolunteer(saved);
        userAccountRepository.save(account);

        return toResponse(saved);
    }

    @Transactional
    public VolunteerResponse update(Long id, VolunteerRequest request) {
        Volunteer volunteer = findEntityById(id);
        validateUniqueUsername(id, request.username());
        apply(volunteer, request);
        Volunteer saved = volunteerRepository.save(volunteer);

        UserAccount account = userAccountRepository.findByVolunteerId(id)
            .orElseThrow(() -> new NotFoundException("Conta do voluntario nao encontrada."));
        account.setUsername(saved.getUsername());
        account.setActive(saved.isActive());
        account.setRole(resolveVolunteerRole(request.role()));
        if (request.password() != null && !request.password().isBlank()) {
            account.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        userAccountRepository.save(account);

        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Volunteer volunteer = findEntityById(id);
        if (scheduleRepository.existsByVolunteerId(id)) {
            throw new BusinessException("Nao e possivel excluir voluntario com escalas cadastradas.");
        }
        availabilityRepository.deleteByVolunteerId(id);
        userAccountRepository.deleteByVolunteerId(id);
        volunteerRepository.delete(volunteer);
    }

    public VolunteerResponse toResponse(Volunteer volunteer) {
        Set<MinistryResponse> ministries = volunteer.getMinistries().stream()
            .map(ministryService::toResponse)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        return new VolunteerResponse(
            volunteer.getId(),
            volunteer.getFullName(),
            volunteer.getUsername(),
            volunteer.getEmail(),
            volunteer.getPhone(),
            volunteer.getNotes(),
            volunteer.isActive(),
            userAccountRepository.findByVolunteerId(volunteer.getId()).map(UserAccount::getRole).orElse(Role.VOLUNTARIO),
            ministries
        );
    }

    private Role resolveVolunteerRole(Role requestedRole) {
        if (requestedRole == Role.ADMIN) {
            throw new BusinessException("Voluntarios nao podem receber perfil de administrador por esta tela.");
        }
        return requestedRole == Role.LIDER ? Role.LIDER : Role.VOLUNTARIO;
    }

    private void apply(Volunteer volunteer, VolunteerRequest request) {
        volunteer.setFullName(request.fullName().trim());
        volunteer.setUsername(request.username().trim());
        volunteer.setEmail(request.email());
        volunteer.setPhone(request.phone());
        volunteer.setNotes(request.notes());
        volunteer.setActive(request.active());
        volunteer.setMinistries(resolveMinistries(request.ministryIds()));
    }

    private Set<Ministry> resolveMinistries(Set<Long> ministryIds) {
        Set<Ministry> ministries = new LinkedHashSet<>();
        for (Long ministryId : ministryIds) {
            ministries.add(ministryService.findEntityById(ministryId));
        }
        return ministries;
    }

    private void validateUniqueUsername(Long currentVolunteerId, String username) {
        String normalizedUsername = username.trim();

        volunteerRepository.findByUsernameIgnoreCase(normalizedUsername)
            .filter(found -> !found.getId().equals(currentVolunteerId))
            .ifPresent(found -> {
                throw new BusinessException("Ja existe um voluntario com esse usuario.");
            });

        userAccountRepository.findByUsernameIgnoreCase(normalizedUsername)
            .filter(found -> currentVolunteerId == null || found.getVolunteer() == null || !found.getVolunteer().getId().equals(currentVolunteerId))
            .ifPresent(found -> {
                throw new BusinessException("Ja existe uma conta com esse usuario.");
            });
    }
}
