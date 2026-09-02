package br.com.escalas.service;

import br.com.escalas.api.assistant.AssistantChatRequest;
import br.com.escalas.api.assistant.AssistantChatResponse;
import br.com.escalas.api.assistant.AssistantDecisionRequest;
import br.com.escalas.api.assistant.AssistantReminderResponse;
import br.com.escalas.api.assistant.AssistantScheduleRequestResponse;
import br.com.escalas.api.volunteer.VolunteerResponse;
import br.com.escalas.api.exception.BusinessException;
import br.com.escalas.api.exception.NotFoundException;
import br.com.escalas.api.schedule.ScheduleRequest;
import br.com.escalas.api.schedule.ScheduleResponse;
import br.com.escalas.domain.auth.Role;
import br.com.escalas.domain.assistant.AssistantRequestStatus;
import br.com.escalas.domain.assistant.AssistantScheduleRequest;
import br.com.escalas.domain.ministry.Ministry;
import br.com.escalas.domain.schedule.TimeSlot;
import br.com.escalas.domain.volunteer.Volunteer;
import br.com.escalas.repository.AssistantScheduleRequestRepository;
import br.com.escalas.security.AuthenticatedUser;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class AssistantService {

    private static final Pattern ISO_DATE = Pattern.compile("\\b(20\\d{2}-\\d{2}-\\d{2})\\b");
    private static final Pattern BR_DATE = Pattern.compile("\\b(\\d{1,2})/(\\d{1,2})/(20\\d{2})\\b");
    private static final Pattern BR_SHORT_DATE = Pattern.compile("\\b(\\d{1,2})/(\\d{1,2})\\b");
    private static final Pattern DAY_WORD_DATE = Pattern.compile("\\bdia\\s+(\\d{1,2})(?:\\s+de\\s+([a-z]+))?\\b");
    private static final Pattern TIME = Pattern.compile("\\b(?:as|às|a|para)?\\s*(\\d{1,2})(?::|h)(\\d{2})?\\b");
    private static final Pattern LOOSE_HOUR = Pattern.compile("\\b(\\d{1,2})\\s*(?:da\\s*)?(manha|tarde|noite)\\b");
    private static final List<String> SCHEDULE_INTENT_TERMS = List.of(
        "agend", "escala", "escalar", "coloca", "coloque", "bota", "bote", "marca", "marque",
        "servir", "participar", "disponivel", "posso", "quero", "gostaria", "preciso", "quero ir", "quero ficar",
        "me poe", "me poem"
    );
    private static final Map<String, Month> MONTHS = Map.ofEntries(
        Map.entry("janeiro", Month.JANUARY),
        Map.entry("fevereiro", Month.FEBRUARY),
        Map.entry("marco", Month.MARCH),
        Map.entry("março", Month.MARCH),
        Map.entry("abril", Month.APRIL),
        Map.entry("maio", Month.MAY),
        Map.entry("junho", Month.JUNE),
        Map.entry("julho", Month.JULY),
        Map.entry("agosto", Month.AUGUST),
        Map.entry("setembro", Month.SEPTEMBER),
        Map.entry("outubro", Month.OCTOBER),
        Map.entry("novembro", Month.NOVEMBER),
        Map.entry("dezembro", Month.DECEMBER)
    );
    private static final Map<String, java.time.DayOfWeek> WEEKDAYS = Map.ofEntries(
        Map.entry("domingo", java.time.DayOfWeek.SUNDAY),
        Map.entry("segunda", java.time.DayOfWeek.MONDAY),
        Map.entry("segunda feira", java.time.DayOfWeek.MONDAY),
        Map.entry("terca", java.time.DayOfWeek.TUESDAY),
        Map.entry("terça", java.time.DayOfWeek.TUESDAY),
        Map.entry("terca feira", java.time.DayOfWeek.TUESDAY),
        Map.entry("terça feira", java.time.DayOfWeek.TUESDAY),
        Map.entry("quarta", java.time.DayOfWeek.WEDNESDAY),
        Map.entry("quarta feira", java.time.DayOfWeek.WEDNESDAY),
        Map.entry("quinta", java.time.DayOfWeek.THURSDAY),
        Map.entry("quinta feira", java.time.DayOfWeek.THURSDAY),
        Map.entry("sexta", java.time.DayOfWeek.FRIDAY),
        Map.entry("sexta feira", java.time.DayOfWeek.FRIDAY),
        Map.entry("sabado", java.time.DayOfWeek.SATURDAY),
        Map.entry("sábado", java.time.DayOfWeek.SATURDAY)
    );

    private final AssistantScheduleRequestRepository requestRepository;
    private final ScheduleService scheduleService;
    private final MinistryService ministryService;
    private final VolunteerService volunteerService;
    private final RestClient.Builder restClientBuilder;

    @Value("${app.assistant.ollama.enabled:false}")
    private boolean ollamaEnabled;

    @Value("${app.assistant.ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${app.assistant.ollama.model:llama3.2:1b}")
    private String ollamaModel;

    @Transactional
    public AssistantChatResponse chat(AuthenticatedUser user, AssistantChatRequest request) {
        Volunteer requester = user.getVolunteerId() == null ? null : volunteerService.findEntityById(user.getVolunteerId());
        String message = request.message().trim();
        List<AssistantReminderResponse> reminders = findReminders(user);

        if (isScheduleRequest(message)) {
            Volunteer requestVolunteer = canOperateSchedules(user.getRole())
                ? findVolunteerInMessage(message).orElse(requester)
                : requester;
            if (requestVolunteer == null) {
                String reply = "Para criar um pedido de escala como lideranca, informe o voluntario. "
                    + "Exemplo: \"coloque a Maria no louvor domingo a noite\".";
                return new AssistantChatResponse(reply, null, reminders);
            }
            AssistantScheduleRequest created = createScheduleRequest(requestVolunteer, message);
            String reply = "Pronto, deixei esse pedido pendente para a lideranca aprovar. "
                + describeMissingFields(created)
                + " Voce pode acompanhar o status em Meus pedidos da IA.";
            return new AssistantChatResponse(reply, toResponse(created), reminders);
        }

        if (asksAboutSchedule(message)) {
            if (requester == null) {
                String reply = "Como lideranca, voce pode ver as escalas na tela Escalas e os pedidos da IA aqui. "
                    + "Para consultar uma escala pessoal no chat, entre com um usuario voluntario ou cite o voluntario no pedido.";
                return new AssistantChatResponse(reply, null, reminders);
            }
            String reply = buildScheduleSummary(user);
            return new AssistantChatResponse(reply, null, reminders);
        }

        if (asksAboutReminders(message)) {
            String reply = reminders.isEmpty()
                ? "Voce nao tem escala marcada para amanha."
                : "Voce tem " + reminders.size() + " lembrete(s) para amanha. Confira a lista abaixo.";
            return new AssistantChatResponse(reply, null, reminders);
        }

        String displayName = requester != null ? requester.getFullName() : user.getUsername();
        String llamaReply = askLlama(message, displayName, reminders, user.getRole()).orElse(null);
        String fallback = "Posso te ajudar a consultar suas escalas, criar um pedido de escala para aprovacao da lideranca, "
            + "ver lembretes de amanha e explicar como usar disponibilidade. Exemplo: "
            + "\"pedir escala para 2026-08-02 as 19:00 no louvor\".";
        return new AssistantChatResponse(llamaReply != null ? llamaReply : fallback, null, reminders);
    }

    @Transactional(readOnly = true)
    public List<AssistantScheduleRequestResponse> findRequests(AssistantRequestStatus status) {
        List<AssistantScheduleRequest> requests = status == null
            ? requestRepository.findAll()
            : requestRepository.findByStatusOrderByCreatedAtAsc(status);
        return requests.stream()
            .sorted(Comparator.comparing(AssistantScheduleRequest::getCreatedAt).reversed())
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AssistantScheduleRequestResponse> findMyRequests(AuthenticatedUser user) {
        return requestRepository.findByRequesterIdOrderByCreatedAtDesc(currentVolunteer(user).getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public AssistantScheduleRequestResponse approve(Long id, AssistantDecisionRequest decision) {
        AssistantScheduleRequest item = getPending(id);
        Volunteer targetVolunteer = item.getTargetVolunteer() != null ? item.getTargetVolunteer() : item.getRequester();
        if (item.getMinistry() == null || item.getServiceDate() == null || item.getServiceTime() == null || item.getTimeSlot() == null) {
            throw new BusinessException("Pedido incompleto. Edite ou recuse e peca para o voluntario informar ministerio, data e horario.");
        }

        ScheduleResponse schedule = scheduleService.create(new ScheduleRequest(
            item.getMinistry().getId(),
            targetVolunteer.getId(),
            item.getServiceDate(),
            item.getServiceTime(),
            item.getTimeSlot(),
            item.getRoleName(),
            item.getLocation(),
            item.getEventName(),
            appendAssistantNote(item.getNotes(), item.getOriginalMessage())
        ));

        item.setStatus(AssistantRequestStatus.APROVADO);
        item.setAdminNotes(decision.adminNotes());
        item.setDecidedAt(LocalDateTime.now());
        item.setApprovedScheduleId(schedule.id());
        return toResponse(requestRepository.save(item));
    }

    @Transactional
    public AssistantScheduleRequestResponse reject(Long id, AssistantDecisionRequest decision) {
        AssistantScheduleRequest item = getPending(id);
        item.setStatus(AssistantRequestStatus.REJEITADO);
        item.setAdminNotes(decision.adminNotes());
        item.setDecidedAt(LocalDateTime.now());
        return toResponse(requestRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<AssistantReminderResponse> findReminders(AuthenticatedUser user) {
        if (user.getVolunteerId() == null) {
            return List.of();
        }
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return scheduleService.findAll(tomorrow, tomorrow, null, user.getVolunteerId()).stream()
            .filter(schedule -> tomorrow.equals(schedule.serviceDate()))
            .map(schedule -> new AssistantReminderResponse(
                "Escala amanha",
                "Voce esta escalado em " + formatDate(schedule.serviceDate()) + " as " + schedule.serviceTime().toString().substring(0, 5)
                    + " no ministerio " + schedule.ministry().name() + ".",
                schedule
            ))
            .toList();
    }

    private AssistantScheduleRequest createScheduleRequest(Volunteer requester, String message) {
        AssistantScheduleRequest item = new AssistantScheduleRequest();
        item.setRequester(requester);
        item.setTargetVolunteer(findVolunteerInMessage(message).orElse(requester));
        item.setOriginalMessage(message);
        item.setServiceDate(extractDate(message).orElse(null));
        item.setServiceTime(extractTime(message).orElse(LocalTime.of(19, 0)));
        item.setTimeSlot(resolveTimeSlot(item.getServiceTime(), message));
        item.setMinistry(findMinistryInMessage(message).orElseGet(() -> requester.getMinistries().stream().findFirst().orElse(null)));
        item.setRoleName(extractAfterAnyKeyword(message, "funcao", "função", "cargo", "como", "servindo de").orElse(null));
        item.setLocation(extractAfterAnyKeyword(message, "local", "lugar", "onde").orElse(null));
        item.setEventName(extractAfterAnyKeyword(message, "evento", "culto", "reuniao", "reunião").orElse(null));
        item.setNotes("Pedido criado pela IA. Revise antes de aprovar.");
        return requestRepository.save(item);
    }

    private AssistantScheduleRequest getPending(Long id) {
        AssistantScheduleRequest item = requestRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Pedido da IA nao encontrado."));
        if (item.getStatus() != AssistantRequestStatus.PENDENTE) {
            throw new BusinessException("Este pedido ja foi analisado.");
        }
        return item;
    }

    private Volunteer currentVolunteer(AuthenticatedUser user) {
        if (user.getVolunteerId() == null) {
            throw new BusinessException("Usuario atual nao esta vinculado a um voluntario.");
        }
        return volunteerService.findEntityById(user.getVolunteerId());
    }

    private boolean isScheduleRequest(String message) {
        String text = normalize(message);
        boolean hasIntent = SCHEDULE_INTENT_TERMS.stream().anyMatch(text::contains);
        boolean hasScheduleContext = findMinistryInMessage(message).isPresent()
            || extractDate(message).isPresent()
            || extractTime(message).isPresent()
            || text.contains("manha")
            || text.contains("tarde")
            || text.contains("noite");
        return hasIntent && hasScheduleContext;
    }

    private boolean asksAboutSchedule(String message) {
        String text = normalize(message);
        return text.contains("minha escala") || text.contains("como esta a escala") || text.contains("proxima escala") || text.contains("estou escalado");
    }

    private boolean asksAboutReminders(String message) {
        String text = normalize(message);
        return text.contains("amanha") || text.contains("lembrete") || text.contains("notificacao") || text.contains("aviso");
    }

    private String buildScheduleSummary(AuthenticatedUser user) {
        List<ScheduleResponse> schedules = scheduleService.findMine(user).stream()
            .filter(item -> !item.serviceDate().isBefore(LocalDate.now()))
            .limit(5)
            .toList();
        if (schedules.isEmpty()) {
            return "Voce nao tem proximas escalas cadastradas.";
        }
        return "Suas proximas escalas: " + schedules.stream()
            .map(item -> formatDate(item.serviceDate()) + " as " + item.serviceTime().toString().substring(0, 5) + " - " + item.ministry().name())
            .reduce((a, b) -> a + "; " + b)
            .orElse("");
    }

    private Optional<Ministry> findMinistryInMessage(String message) {
        String normalized = normalize(message);
        return ministryService.findAll().stream()
            .map(ministry -> ministryService.findEntityById(ministry.id()))
            .filter(ministry -> normalized.contains(normalize(ministry.getName())))
            .findFirst();
    }

    private Optional<Volunteer> findVolunteerInMessage(String message) {
        String normalized = normalize(message);
        return volunteerService.findAll().stream()
            .filter(volunteer -> normalized.contains(normalize(volunteer.fullName())) || normalized.contains(normalize(volunteer.username())))
            .findFirst()
            .map(VolunteerResponse::id)
            .map(volunteerService::findEntityById);
    }

    private Optional<LocalDate> extractDate(String message) {
        String normalized = normalize(message);
        LocalDate today = LocalDate.now();
        if (normalized.contains("depois de amanha")) {
            return Optional.of(today.plusDays(2));
        }
        if (normalized.contains("amanha")) {
            return Optional.of(today.plusDays(1));
        }
        if (normalized.contains("hoje")) {
            return Optional.of(today);
        }

        Matcher iso = ISO_DATE.matcher(message);
        if (iso.find()) {
            return Optional.of(LocalDate.parse(iso.group(1)));
        }
        Matcher br = BR_DATE.matcher(message);
        if (br.find()) {
            return Optional.of(LocalDate.of(Integer.parseInt(br.group(3)), Integer.parseInt(br.group(2)), Integer.parseInt(br.group(1))));
        }
        Matcher brShort = BR_SHORT_DATE.matcher(message);
        if (brShort.find()) {
            int day = Integer.parseInt(brShort.group(1));
            int month = Integer.parseInt(brShort.group(2));
            LocalDate date = LocalDate.of(today.getYear(), month, day);
            return Optional.of(date.isBefore(today) ? date.plusYears(1) : date);
        }
        Matcher dayWord = DAY_WORD_DATE.matcher(normalized);
        if (dayWord.find()) {
            int day = Integer.parseInt(dayWord.group(1));
            Month month = Optional.ofNullable(dayWord.group(2))
                .map(MONTHS::get)
                .orElse(today.getMonth());
            LocalDate date = LocalDate.of(today.getYear(), month, day);
            return Optional.of(date.isBefore(today) ? date.plusYears(1) : date);
        }
        Optional<java.time.DayOfWeek> weekday = WEEKDAYS.entrySet().stream()
            .filter(entry -> normalized.contains(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst();
        if (weekday.isPresent()) {
            LocalDate date = today.with(TemporalAdjusters.nextOrSame(weekday.get()));
            if (date.equals(today) && (normalized.contains("proxim") || normalized.contains("que vem"))) {
                date = today.with(TemporalAdjusters.next(weekday.get()));
            }
            return Optional.of(date);
        }
        return Optional.empty();
    }

    private Optional<LocalTime> extractTime(String message) {
        Matcher matcher = TIME.matcher(normalize(message));
        while (matcher.find()) {
            int hour = Integer.parseInt(matcher.group(1));
            if (hour <= 23) {
                int minute = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));
                String text = normalize(message);
                if (hour < 12 && text.contains("noite")) hour += 12;
                if (hour < 12 && text.contains("tarde")) hour += 12;
                return Optional.of(LocalTime.of(hour, minute));
            }
        }
        Matcher looseHour = LOOSE_HOUR.matcher(normalize(message));
        if (looseHour.find()) {
            int hour = Integer.parseInt(looseHour.group(1));
            String period = looseHour.group(2);
            if (hour < 12 && (period.equals("tarde") || period.equals("noite"))) {
                hour += 12;
            }
            return Optional.of(LocalTime.of(hour, 0));
        }
        String text = normalize(message);
        if (text.contains("manha")) return Optional.of(LocalTime.of(9, 0));
        if (text.contains("tarde")) return Optional.of(LocalTime.of(15, 0));
        if (text.contains("noite")) return Optional.of(LocalTime.of(19, 0));
        return Optional.empty();
    }

    private TimeSlot resolveTimeSlot(LocalTime time, String message) {
        String text = normalize(message);
        if (text.contains("manha")) return TimeSlot.MANHA;
        if (text.contains("noite")) return TimeSlot.NOITE;
        return time != null && time.getHour() < 13 ? TimeSlot.MANHA : TimeSlot.NOITE;
    }

    private Optional<String> extractAfterKeyword(String message, String keyword) {
        String text = message.toLowerCase(Locale.ROOT);
        int index = text.indexOf(keyword + ":");
        int offset = keyword.length() + 1;
        if (index < 0) {
            index = normalize(message).indexOf(normalize(keyword) + " ");
            offset = keyword.length() + 1;
        }
        if (index < 0) return Optional.empty();
        String value = message.substring(Math.min(message.length(), index + offset)).trim();
        int separator = value.indexOf(";");
        String result = separator >= 0 ? value.substring(0, separator) : value;
        return Optional.of(cleanExtractedValue(result)).filter(v -> !v.isBlank());
    }

    private Optional<String> extractAfterAnyKeyword(String message, String... keywords) {
        return Arrays.stream(keywords)
            .map(keyword -> extractAfterKeyword(message, keyword))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    private String cleanExtractedValue(String value) {
        String result = value.trim();
        for (String stop : List.of(" no ", " na ", " em ", " dia ", " as ", " às ", " para ")) {
            int index = normalize(result).indexOf(stop.trim() + " ");
            if (index > 0) {
                result = result.substring(0, index).trim();
            }
        }
        return result.length() > 80 ? result.substring(0, 80).trim() : result;
    }

    private Optional<String> askLlama(String message, String displayName, List<AssistantReminderResponse> reminders, Role role) {
        if (!ollamaEnabled) {
            return Optional.empty();
        }
        try {
            OllamaRequest request = new OllamaRequest(
                ollamaModel,
                "Voce e a IA do Sistema Shekinah. Responda em portugues, curto e pratico. "
                    + "Usuario: " + displayName + ". Perfil: " + role + ". Lembretes de amanha: " + reminders.size()
                    + ". Pergunta: " + message,
                false
            );
            OllamaResponse response = restClientBuilder.baseUrl(ollamaUrl).build()
                .post()
                .uri("/api/generate")
                .body(request)
                .retrieve()
                .body(OllamaResponse.class);
            return Optional.ofNullable(response).map(OllamaResponse::response).filter(text -> !text.isBlank());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private boolean canOperateSchedules(Role role) {
        return role == Role.ADMIN || role == Role.LIDER;
    }

    private String describeMissingFields(AssistantScheduleRequest item) {
        StringBuilder builder = new StringBuilder();
        if (item.getMinistry() == null) builder.append("Faltou identificar o ministerio. ");
        if (item.getServiceDate() == null) builder.append("Faltou informar a data. ");
        if (item.getServiceTime() == null) builder.append("Faltou informar o horario. ");
        return builder.length() == 0 ? "" : builder.toString();
    }

    private String appendAssistantNote(String notes, String originalMessage) {
        String base = notes == null ? "" : notes.trim();
        String extra = "Pedido original via IA: " + originalMessage;
        if (base.isBlank()) return extra;
        return (base + " | " + extra).substring(0, Math.min(500, base.length() + extra.length() + 3));
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String normalize(String value) {
        return java.text.Normalizer.normalize(value == null ? "" : value, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT);
    }

    private AssistantScheduleRequestResponse toResponse(AssistantScheduleRequest item) {
        return new AssistantScheduleRequestResponse(
            item.getId(),
            volunteerService.toResponse(item.getRequester()),
            item.getTargetVolunteer() == null ? null : volunteerService.toResponse(item.getTargetVolunteer()),
            item.getMinistry() == null ? null : ministryService.toResponse(item.getMinistry()),
            item.getServiceDate(),
            item.getServiceTime(),
            item.getTimeSlot(),
            item.getRoleName(),
            item.getLocation(),
            item.getEventName(),
            item.getNotes(),
            item.getOriginalMessage(),
            item.getStatus(),
            item.getAdminNotes(),
            item.getCreatedAt(),
            item.getDecidedAt(),
            item.getApprovedScheduleId()
        );
    }

    private record OllamaRequest(String model, String prompt, boolean stream) {
    }

    private record OllamaResponse(String response) {
    }
}
