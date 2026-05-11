package br.com.escalas.config;

import br.com.escalas.domain.auth.Role;
import br.com.escalas.domain.auth.UserAccount;
import br.com.escalas.domain.ministry.Ministry;
import br.com.escalas.domain.schedule.Availability;
import br.com.escalas.domain.schedule.AvailabilityStatus;
import br.com.escalas.domain.schedule.Schedule;
import br.com.escalas.domain.schedule.TimeSlot;
import br.com.escalas.domain.volunteer.Volunteer;
import br.com.escalas.repository.AvailabilityRepository;
import br.com.escalas.repository.MinistryRepository;
import br.com.escalas.repository.ScheduleRepository;
import br.com.escalas.repository.UserAccountRepository;
import br.com.escalas.repository.VolunteerRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final MinistryRepository ministryRepository;
    private final VolunteerRepository volunteerRepository;
    private final UserAccountRepository userAccountRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ScheduleRepository scheduleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Ministry diaconos = ensureMinistry("Diaconos");
        Ministry ebd = ensureMinistry("EBD");
        Ministry intercessao = ensureMinistry("Intercessao");
        Ministry recepcao = ensureMinistry("Recepcao");
        Ministry louvor = ensureMinistry("Louvor");

        Volunteer joao = ensureVolunteer("Joao Silva", "joao", diaconos);
        Volunteer maria = ensureVolunteer("Maria Santos", "maria", ebd);
        Volunteer carlos = ensureVolunteer("Carlos Pereira", "carlos", intercessao);
        Volunteer ana = ensureVolunteer("Ana Lima", "ana", recepcao);
        Volunteer pedro = ensureVolunteer("Pedro Costa", "pedro", louvor);

        ensureUser("admin", "1234", Role.ADMIN, null);
        ensureUser("joao", "1234", Role.VOLUNTARIO, joao);
        ensureUser("maria", "1234", Role.VOLUNTARIO, maria);
        ensureUser("carlos", "1234", Role.VOLUNTARIO, carlos);
        ensureUser("ana", "1234", Role.VOLUNTARIO, ana);
        ensureUser("pedro", "1234", Role.VOLUNTARIO, pedro);

        if (availabilityRepository.count() == 0) {
            createAvailability(joao, DayOfWeek.SUNDAY, TimeSlot.MANHA, AvailabilityStatus.DISPONIVEL);
            createAvailability(joao, DayOfWeek.SUNDAY, TimeSlot.NOITE, AvailabilityStatus.DISPONIVEL);
            createAvailability(maria, DayOfWeek.TUESDAY, TimeSlot.NOITE, AvailabilityStatus.DISPONIVEL);
            createAvailability(carlos, DayOfWeek.WEDNESDAY, TimeSlot.NOITE, AvailabilityStatus.DISPONIVEL);
            createAvailability(ana, DayOfWeek.TUESDAY, TimeSlot.NOITE, AvailabilityStatus.INDISPONIVEL);
            createAvailability(pedro, DayOfWeek.SUNDAY, TimeSlot.NOITE, AvailabilityStatus.DISPONIVEL);
        }

        if (scheduleRepository.count() == 0) {
            createSchedule(diaconos, joao, LocalDate.now().plusDays(3), LocalTime.of(9, 0), TimeSlot.MANHA, "Recepcao", "Entrada principal");
            createSchedule(ebd, maria, LocalDate.now().plusDays(3), LocalTime.of(9, 30), TimeSlot.MANHA, "Professora", "Sala Adultos");
            createSchedule(intercessao, carlos, LocalDate.now().plusDays(5), LocalTime.of(19, 0), TimeSlot.NOITE, "Intercessor", "Sala de oracao");
        }
    }

    private Ministry ensureMinistry(String name) {
        return ministryRepository.findByNameIgnoreCase(name)
            .orElseGet(() -> createMinistry(name));
    }

    private Ministry createMinistry(String name) {
        Ministry ministry = new Ministry();
        ministry.setName(name);
        ministry.setDescription(name);
        ministry.setActive(true);
        return ministryRepository.save(ministry);
    }

    private Volunteer ensureVolunteer(String fullName, String username, Ministry ministry) {
        return volunteerRepository.findByUsernameIgnoreCase(username)
            .map(existing -> ensureVolunteerMinistry(existing, ministry))
            .orElseGet(() -> createVolunteer(fullName, username, ministry));
    }

    private Volunteer createVolunteer(String fullName, String username, Ministry ministry) {
        Volunteer volunteer = new Volunteer();
        volunteer.setFullName(fullName);
        volunteer.setUsername(username);
        volunteer.setActive(true);
        volunteer.getMinistries().add(ministry);
        return volunteerRepository.save(volunteer);
    }

    private Volunteer ensureVolunteerMinistry(Volunteer volunteer, Ministry ministry) {
        volunteer.setActive(true);
        if (volunteer.getMinistries().stream().noneMatch(item -> item.getId().equals(ministry.getId()))) {
            volunteer.getMinistries().add(ministry);
        }
        return volunteerRepository.save(volunteer);
    }

    private void ensureUser(String username, String rawPassword, Role role, Volunteer volunteer) {
        if (userAccountRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }
        createUser(username, rawPassword, role, volunteer);
    }

    private void createUser(String username, String rawPassword, Role role, Volunteer volunteer) {
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(rawPassword));
        account.setRole(role);
        account.setVolunteer(volunteer);
        account.setActive(true);
        userAccountRepository.save(account);
    }

    private void createAvailability(Volunteer volunteer, DayOfWeek dayOfWeek, TimeSlot timeSlot, AvailabilityStatus status) {
        Availability availability = new Availability();
        availability.setVolunteer(volunteer);
        availability.setDayOfWeek(dayOfWeek);
        availability.setTimeSlot(timeSlot);
        availability.setStatus(status);
        availabilityRepository.save(availability);
    }

    private void createSchedule(
        Ministry ministry,
        Volunteer volunteer,
        LocalDate date,
        LocalTime time,
        TimeSlot timeSlot,
        String roleName,
        String location
    ) {
        Schedule schedule = new Schedule();
        schedule.setMinistry(ministry);
        schedule.setVolunteer(volunteer);
        schedule.setServiceDate(date);
        schedule.setServiceTime(time);
        schedule.setTimeSlot(timeSlot);
        schedule.setRoleName(roleName);
        schedule.setLocation(location);
        schedule.setEventName("Culto");
        scheduleRepository.save(schedule);
    }
}
