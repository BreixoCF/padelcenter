DROP INDEX IF EXISTS idx_users_keycloak_id;

ALTER TABLE users
    DROP COLUMN IF EXISTS keycloak_id;
