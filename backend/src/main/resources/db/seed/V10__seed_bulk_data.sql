-- Bulk volume seed data for local pagination/filter/performance testing.
-- Only applied when spring.flyway.locations includes classpath:db/seed (profile "local").
-- Filler accounts share one fixed bcrypt hash — nobody is expected to log in as them.

-- ── ~180 filler users ──
INSERT INTO users (user_id, first_name, last_name, email, password_hash, phone_number, created_by)
SELECT
    gen_random_uuid(),
    'Jugador',
    'Apellido' || i,
    'player' || LPAD(i::text, 4, '0') || '@padelcenter.local',
    '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i',
    '6' || LPAD((10000000 + i)::text, 8, '0'),
    (SELECT user_id FROM users WHERE email = 'admin@padelcenter.local')
FROM generate_series(1, 180) AS i;

CREATE TEMP TABLE temp_bulk_pool AS
SELECT user_id FROM users WHERE email LIKE 'player%@padelcenter.local';

-- ── ~1000 bookings spread across every field, 61-day window, hourly slots ──
-- Each (field_id, day_offset, hour) combination is unique by construction, so no
-- two sampled rows ever land on the same field at the same time.
WITH slot_candidates AS (
    SELECT
        f.field_id,
        f.price_per_hour,
        d.day_offset,
        h.hour_slot
    FROM fields f
    CROSS JOIN generate_series(-30, 30) AS d(day_offset)
    CROSS JOIN generate_series(9, 21) AS h(hour_slot)
),
sampled_slots AS (
    SELECT *,
           ROW_NUMBER() OVER (ORDER BY RANDOM()) AS rn
    FROM slot_candidates
    ORDER BY RANDOM()
    LIMIT 1000
)
INSERT INTO bookings (user_id, field_id, status, start_time, end_time, total_price, created_by)
SELECT
    (SELECT user_id FROM temp_bulk_pool ORDER BY RANDOM() LIMIT 1),
    s.field_id,
    CASE
        WHEN s.day_offset < 0 THEN (CASE WHEN s.rn % 5 = 0 THEN 3 ELSE 4 END) -- past: mostly COMPLETED, some CANCELLED
        ELSE (CASE WHEN s.rn % 4 = 0 THEN 1 ELSE 2 END)                       -- future: mostly CONFIRMED, some PENDING
    END,
    (CURRENT_DATE + (s.day_offset || ' days')::INTERVAL) + (s.hour_slot || ':00')::TIME,
    (CURRENT_DATE + (s.day_offset || ' days')::INTERVAL) + (s.hour_slot || ':00')::TIME + INTERVAL '1 hour',
    s.price_per_hour,
    (SELECT user_id FROM users WHERE email = 'admin@padelcenter.local')
FROM sampled_slots s;

CREATE TEMP TABLE temp_bulk_bookings AS SELECT booking_id, user_id AS owner_id FROM bookings WHERE created_by = (SELECT user_id FROM users WHERE email = 'admin@padelcenter.local');

-- Booking participants: the owner plus 1-3 more distinct filler users per bulk booking.
INSERT INTO booking_participants (booking_id, user_id, created_by)
SELECT booking_id, owner_id, (SELECT user_id FROM users WHERE email = 'admin@padelcenter.local')
FROM temp_bulk_bookings;

INSERT INTO booking_participants (booking_id, user_id, created_by)
SELECT b.booking_id, u.user_id, (SELECT user_id FROM users WHERE email = 'admin@padelcenter.local')
FROM temp_bulk_bookings b
CROSS JOIN LATERAL (
    SELECT user_id FROM temp_bulk_pool
    WHERE user_id != b.owner_id
    ORDER BY RANDOM()
    LIMIT (1 + FLOOR(RANDOM() * 3))::int
) AS u;

DROP TABLE temp_bulk_pool;
DROP TABLE temp_bulk_bookings;
