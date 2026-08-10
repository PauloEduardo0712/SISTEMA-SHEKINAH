CREATE TABLE assistant_schedule_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    requester_volunteer_id BIGINT NOT NULL,
    target_volunteer_id BIGINT,
    ministry_id BIGINT,
    service_date DATE,
    service_time TIME,
    time_slot VARCHAR(20),
    role_name VARCHAR(120),
    location VARCHAR(120),
    event_name VARCHAR(120),
    notes VARCHAR(500),
    original_message VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    admin_notes VARCHAR(500),
    decided_at DATETIME,
    approved_schedule_id BIGINT,
    CONSTRAINT pk_assistant_schedule_requests PRIMARY KEY (id),
    CONSTRAINT fk_asr_requester FOREIGN KEY (requester_volunteer_id) REFERENCES volunteers(id),
    CONSTRAINT fk_asr_target_volunteer FOREIGN KEY (target_volunteer_id) REFERENCES volunteers(id),
    CONSTRAINT fk_asr_ministry FOREIGN KEY (ministry_id) REFERENCES ministries(id),
    CONSTRAINT fk_asr_approved_schedule FOREIGN KEY (approved_schedule_id) REFERENCES schedules(id)
);

CREATE INDEX idx_asr_status ON assistant_schedule_requests (status);
CREATE INDEX idx_asr_requester ON assistant_schedule_requests (requester_volunteer_id);
