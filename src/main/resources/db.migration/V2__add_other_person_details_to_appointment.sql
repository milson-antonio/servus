ALTER TABLE servus.appointment ADD COLUMN for_other BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE servus.appointment_other_person_details (
    appointment_id UUID NOT NULL,
    detail_key VARCHAR(255) NOT NULL,
    detail_value VARCHAR(255),
    PRIMARY KEY (appointment_id, detail_key),
    FOREIGN KEY (appointment_id) REFERENCES servus.appointment(id) ON DELETE CASCADE
);
