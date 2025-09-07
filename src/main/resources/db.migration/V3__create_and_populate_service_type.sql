-- Create the new service_type table in the servus schema
CREATE TABLE servus.service_type (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    label VARCHAR(255) NOT NULL,
    description VARCHAR(512)
);

-- Create the table for the required documents in the servus schema
CREATE TABLE servus.service_type_required_documents (
    service_type_id UUID NOT NULL,
    document VARCHAR(255),
    FOREIGN KEY (service_type_id) REFERENCES servus.service_type(id) ON DELETE CASCADE
);

-- Populate the service_type table with data from the old enum
INSERT INTO servus.service_type (id, name, label, description) VALUES
('a1f1f1f1-b2b2-c3c3-d4d4-e5e5e5e5e5e5', 'passportRenewal', 'Passport Renewal', 'Renew your passport if it is expired or about to expire.'),
('a2f1f1f1-b2b2-c3c3-d4d4-e5e5e5e5e5e5', 'visaApplication', 'Visa Application', 'Apply for a visa to enter a foreign country.'),
('a3f1f1f1-b2b2-c3c3-d4d4-e5e5e5e5e5e5', 'consularRegistration', 'Consular Registration', 'Register your presence at the consulate for assistance.'),
('a4f1f1f1-b2b2-c3c3-d4d4-e5e5e5e5e5e5', 'documentLegalization', 'Document Legalization', 'Legalize documents for international use.'),
('a5f1f1f1-b2b2-c3c3-d4d4-e5e5e5e5e5e5', 'notarialServices', 'Notarial Services', 'Authenticate documents for legal validity.'),
('a6f1f1f1-b2b2-c3c3-d4d4-e5e5e5e5e5e5', 'emergencyServices', 'Emergency Services', 'Seek assistance in case of emergencies.');

-- Add a temporary column to the appointment table to hold the new foreign key
ALTER TABLE servus.appointment ADD COLUMN service_type_id UUID;

-- Update the new column with the corresponding UUIDs from the service_type table
UPDATE servus.appointment a SET service_type_id = st.id
FROM servus.service_type st
WHERE a.appointment_service_type = st.name;

-- Drop the old varchar column
ALTER TABLE servus.appointment DROP COLUMN appointment_service_type;

-- Add the foreign key constraint
ALTER TABLE servus.appointment ADD CONSTRAINT fk_appointment_service_type
    FOREIGN KEY (service_type_id) REFERENCES servus.service_type(id);
