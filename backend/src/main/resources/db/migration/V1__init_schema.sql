CREATE TABLE ministries (
    id BIGSERIAL NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL,
    CONSTRAINT pk_ministries PRIMARY KEY (id),
    CONSTRAINT uk_ministries_name UNIQUE (name)
);

CREATE TABLE volunteers (
    id BIGSERIAL NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    username VARCHAR(80) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(30),
    notes VARCHAR(500),
    active BOOLEAN NOT NULL,
    CONSTRAINT pk_volunteers PRIMARY KEY (id),
    CONSTRAINT uk_volunteers_username UNIQUE (username)
);

CREATE TABLE volunteer_ministries (
    volunteer_id BIGINT NOT NULL,
    ministry_id BIGINT NOT NULL,
    CONSTRAINT pk_volunteer_ministries PRIMARY KEY (volunteer_id, ministry_id),
    CONSTRAINT fk_vm_volunteer FOREIGN KEY (volunteer_id) REFERENCES volunteers(id),
    CONSTRAINT fk_vm_ministry FOREIGN KEY (ministry_id) REFERENCES ministries(id)
);

CREATE TABLE availabilities (
    id BIGSERIAL NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    volunteer_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    time_slot VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT pk_availabilities PRIMARY KEY (id),
    CONSTRAINT fk_availability_volunteer FOREIGN KEY (volunteer_id) REFERENCES volunteers(id),
    CONSTRAINT uk_availability_rule UNIQUE (volunteer_id, day_of_week, time_slot)
);

CREATE TABLE schedules (
    id BIGSERIAL NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    ministry_id BIGINT NOT NULL,
    volunteer_id BIGINT NOT NULL,
    service_date DATE NOT NULL,
    service_time TIME NOT NULL,
    time_slot VARCHAR(20) NOT NULL,
    role_name VARCHAR(120),
    location VARCHAR(120),
    event_name VARCHAR(120),
    notes VARCHAR(500),
    CONSTRAINT pk_schedules PRIMARY KEY (id),
    CONSTRAINT fk_schedule_ministry FOREIGN KEY (ministry_id) REFERENCES ministries(id),
    CONSTRAINT fk_schedule_volunteer FOREIGN KEY (volunteer_id) REFERENCES volunteers(id)
);

CREATE TABLE user_accounts (
    id BIGSERIAL NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    username VARCHAR(80) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL,
    volunteer_id BIGINT,
    CONSTRAINT pk_user_accounts PRIMARY KEY (id),
    CONSTRAINT uk_user_accounts_username UNIQUE (username),
    CONSTRAINT uk_user_accounts_volunteer UNIQUE (volunteer_id),
    CONSTRAINT fk_user_accounts_volunteer FOREIGN KEY (volunteer_id) REFERENCES volunteers(id)
);

CREATE INDEX idx_schedules_date_time ON schedules (service_date, service_time);
CREATE INDEX idx_schedules_volunteer ON schedules (volunteer_id);
