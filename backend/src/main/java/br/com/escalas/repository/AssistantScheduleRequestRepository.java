package br.com.escalas.repository;

import br.com.escalas.domain.assistant.AssistantRequestStatus;
import br.com.escalas.domain.assistant.AssistantScheduleRequest;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistantScheduleRequestRepository extends JpaRepository<AssistantScheduleRequest, Long> {

    @Override
    @EntityGraph(attributePaths = {"requester", "targetVolunteer", "ministry"})
    List<AssistantScheduleRequest> findAll();

    @EntityGraph(attributePaths = {"requester", "targetVolunteer", "ministry"})
    List<AssistantScheduleRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    @EntityGraph(attributePaths = {"requester", "targetVolunteer", "ministry"})
    List<AssistantScheduleRequest> findByStatusOrderByCreatedAtAsc(AssistantRequestStatus status);
}
