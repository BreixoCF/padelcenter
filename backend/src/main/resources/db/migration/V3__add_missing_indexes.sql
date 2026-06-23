-- FK indexes for high-cardinality foreign keys
CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON bookings (user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_field_id ON bookings (field_id);
CREATE INDEX IF NOT EXISTS idx_center_roles_user_id ON center_roles (user_id);
CREATE INDEX IF NOT EXISTS idx_center_roles_center_id ON center_roles (center_id);

-- Index on users.email (used in findByEmail / existsByEmail)
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- Optimistic locking column for bookings
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
