-- Adds the 'active' column required by the application
ALTER TABLE IF EXISTS servus."user"
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT FALSE;