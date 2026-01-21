CREATE TABLE IF NOT EXISTS users
(
    user_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number  VARCHAR(20),
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by    UUID,
    modified_at   TIMESTAMP WITH TIME ZONE,
    modified_by   UUID,
    deleted_at    TIMESTAMP WITH TIME ZONE,
    deleted_by    UUID
);

CREATE TABLE IF NOT EXISTS centers
(
    center_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL,
    address      VARCHAR(255) NOT NULL,
    city         VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    email        VARCHAR(255) UNIQUE,
    manager_id   UUID REFERENCES users (user_id) ON DELETE SET NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   UUID,
    modified_at  TIMESTAMP WITH TIME ZONE,
    modified_by  UUID,
    deleted_at   TIMESTAMP WITH TIME ZONE,
    deleted_by   UUID
);

CREATE TABLE IF NOT EXISTS fields
(
    field_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    center_id      UUID           NOT NULL REFERENCES centers (center_id) ON DELETE CASCADE,
    name   VARCHAR(50)    NOT NULL,
    type           VARCHAR(50),
    price_per_hour NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    is_available   BOOLEAN                 DEFAULT TRUE,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by     UUID,
    modified_at    TIMESTAMP WITH TIME ZONE,
    modified_by    UUID,
    deleted_at     TIMESTAMP WITH TIME ZONE,
    deleted_by     UUID,
    UNIQUE (center_id, name)
);

CREATE TABLE IF NOT EXISTS bookings
(
    booking_id  BIGSERIAL PRIMARY KEY,
    user_id     UUID                     NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    field_id    UUID                     NOT NULL REFERENCES fields (field_id) ON DELETE RESTRICT,
    status      INT                      NOT NULL,
    start_time  TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time    TIMESTAMP WITH TIME ZONE NOT NULL,
    total_price NUMERIC(10, 2)           NOT NULL,
    booked_at   TIMESTAMP WITH TIME ZONE          DEFAULT CURRENT_TIMESTAMP,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  UUID,
    modified_at TIMESTAMP WITH TIME ZONE,
    modified_by UUID,
    deleted_at  TIMESTAMP WITH TIME ZONE,
    deleted_by  UUID
);

CREATE TABLE IF NOT EXISTS booking_participants
(
    booking_participant_id BIGSERIAL PRIMARY KEY,
    booking_id             BIGINT NOT NULL REFERENCES bookings (booking_id) ON DELETE CASCADE,
    user_id                UUID   NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by             UUID,
    modified_at            TIMESTAMP WITH TIME ZONE,
    modified_by            UUID,
    deleted_at             TIMESTAMP WITH TIME ZONE,
    deleted_by             UUID
);

CREATE TABLE IF NOT EXISTS center_roles
(
    center_role_id   BIGSERIAL   PRIMARY KEY,
    user_id          UUID        NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    center_id        UUID        NOT NULL REFERENCES centers (center_id) ON DELETE CASCADE,
    role_name        VARCHAR(50) NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       UUID,
    modified_at      TIMESTAMP WITH TIME ZONE,
    modified_by      UUID,
    deleted_at       TIMESTAMP WITH TIME ZONE,
    deleted_by       UUID,
    UNIQUE (user_id, center_id),
    CONSTRAINT chk_membership_role_name CHECK (role_name IN ('ADMIN', 'MANAGER', 'USER'))
);