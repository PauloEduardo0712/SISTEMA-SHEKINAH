package br.com.escalas.domain.assistant;

import br.com.escalas.domain.common.BaseEntity;
import br.com.escalas.domain.ministry.Ministry;
import br.com.escalas.domain.schedule.TimeSlot;
import br.com.escalas.domain.volunteer.Volunteer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "assistant_schedule_requests")
public class AssistantScheduleRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_volunteer_id", nullable = false)
    private Volunteer requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_volunteer_id")
    private Volunteer targetVolunteer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ministry_id")
    private Ministry ministry;

    @Column
    private LocalDate serviceDate;

    @Column
    private LocalTime serviceTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TimeSlot timeSlot;

    @Column(length = 120)
    private String roleName;

    @Column(length = 120)
    private String location;

    @Column(length = 120)
    private String eventName;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, length = 1000)
    private String originalMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssistantRequestStatus status = AssistantRequestStatus.PENDENTE;

    @Column(length = 500)
    private String adminNotes;

    @Column
    private LocalDateTime decidedAt;

    @Column
    private Long approvedScheduleId;
}
