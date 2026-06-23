ALTER TABLE users
    ADD COLUMN keycloak_id TEXT UNIQUE;

COMMENT ON COLUMN users.keycloak_id IS
    'Keycloak subject (sub claim) for SSO identity mapping';

CREATE INDEX idx_users_keycloak_id
    ON users (keycloak_id)
    WHERE keycloak_id IS NOT NULL;
