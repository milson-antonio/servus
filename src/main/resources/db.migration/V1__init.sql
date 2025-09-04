-- Criação do schema
CREATE SCHEMA IF NOT EXISTS servus;

-- Criação da tabela de usuários
CREATE TABLE servus."user" (
                               id UUID PRIMARY KEY,
                               full_name VARCHAR(255) NOT NULL,
                               email VARCHAR(100) NOT NULL UNIQUE,
                               phone VARCHAR(20),
                               password VARCHAR(255) NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               active BOOLEAN NOT NULL,
                               token_version INTEGER,
                               last_password_reset_request_at TIMESTAMP,
                               updated_at TIMESTAMP,
                               role VARCHAR(50) NOT NULL
);

-- Criação da tabela de agendamentos
CREATE TABLE servus.appointment (
                                    id UUID PRIMARY KEY,
                                    user_id UUID NOT NULL,
                                    appointment_service_type VARCHAR(100) NOT NULL,
                                    applicant_type VARCHAR(20) NOT NULL,
                                    start_at TIMESTAMP NOT NULL,
                                    end_at TIMESTAMP NOT NULL,
                                    status VARCHAR(20) NOT NULL,
                                    created_at TIMESTAMP NOT NULL,
                                    updated_at TIMESTAMP,
                                    CONSTRAINT fk_appointment_user FOREIGN KEY (user_id) REFERENCES servus."user"(id)
);

-- Índices úteis
CREATE INDEX idx_appointment_user_time ON servus.appointment (user_id, start_at, end_at);
