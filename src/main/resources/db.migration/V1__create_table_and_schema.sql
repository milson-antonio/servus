CREATE SCHEMA IF NOT EXISTS servus;

CREATE TABLE servus.userEntity (
                             id UUID PRIMARY KEY,
                             full_name VARCHAR(255) NOT NULL,
                             email VARCHAR(100) NOT NULL UNIQUE,
                             phone VARCHAR(20),
                             password VARCHAR(255) NOT NULL,
                             created_at TIMESTAMP NOT NULL,
                             updated_at TIMESTAMP,
                             role VARCHAR(50) NOT NULL,
                             active BOOLEAN NOT NULL DEFAULT FALSE
);