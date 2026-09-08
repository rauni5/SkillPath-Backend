ALTER TABLE users
    ADD COLUMN public_profile_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN public_profile_token VARCHAR(64) UNIQUE;

CREATE INDEX idx_users_public_profile_token ON users(public_profile_token)
    WHERE public_profile_token IS NOT NULL;
