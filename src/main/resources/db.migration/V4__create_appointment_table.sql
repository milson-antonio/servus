CREATE TABLE IF NOT EXISTS servus.appointment (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    service VARCHAR(100) NOT NULL,
    applicant_type VARCHAR(20) NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_appointment_user FOREIGN KEY (user_id) REFERENCES servus."user"(id)
);

CREATE INDEX IF NOT EXISTS idx_appointment_user_time ON servus.appointment (user_id, start_at, end_at);
