-- Curated seed data for local manual/Postman testing.
-- Only applied when spring.flyway.locations includes classpath:db/seed (profile "local").
-- All accounts share the password "Padel2026!" — never used outside local dev.

CREATE TEMP TABLE temp_admin AS SELECT gen_random_uuid() AS admin_id;

-- ── Admin user (bootstraps itself: there is no public API path to grant ADMIN) ──
INSERT INTO users (user_id, first_name, last_name, email, password_hash, phone_number, created_by)
VALUES (
    (SELECT admin_id FROM temp_admin),
    'Admin', 'Padel',
    'admin@padelcenter.local',
    '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i',
    '600000001',
    (SELECT admin_id FROM temp_admin)
);

-- ── Manager + 18 regular demo users, all with known credentials ──
INSERT INTO users (user_id, first_name, last_name, email, password_hash, phone_number, created_by)
VALUES
    (gen_random_uuid(), 'Marta',   'Suárez',   'manager@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000002', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Ana',     'García',   'ana.garcia@padelcenter.local',     '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000003', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Luis',    'Pérez',    'luis.perez@padelcenter.local',     '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000004', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Carla',   'Romero',   'carla.romero@padelcenter.local',  '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000005', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Diego',   'Molina',   'diego.molina@padelcenter.local',  '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000006', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Elena',   'Navarro',  'elena.navarro@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000007', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Pablo',   'Ortega',   'pablo.ortega@padelcenter.local',  '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000008', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Sara',    'Iglesias', 'sara.iglesias@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000009', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Javier',  'Castro',   'javier.castro@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000010', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Lucía',   'Vidal',    'lucia.vidal@padelcenter.local',   '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000011', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Hugo',    'Serrano',  'hugo.serrano@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000012', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Marina',  'Delgado',  'marina.delgado@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000013', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Adrián',  'Vázquez',  'adrian.vazquez@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000014', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Paula',   'Ramos',    'paula.ramos@padelcenter.local',  '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000015', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Álvaro',  'Cano',     'alvaro.cano@padelcenter.local',  '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000016', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Claudia', 'Reyes',    'claudia.reyes@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000017', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Mario',   'Gil',      'mario.gil@padelcenter.local',    '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000018', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Irene',   'Pascual',  'irene.pascual@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000019', (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Rubén',   'Soto',     'ruben.soto@padelcenter.local',   '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000020', (SELECT admin_id FROM temp_admin));

-- ── 6 curated centers ──
INSERT INTO centers (center_id, name, address, city, phone_number, email, created_by)
VALUES
    (gen_random_uuid(), 'Padel Club Madrid Norte',  'Av. de la Ilustración 45', 'Madrid',    '910000001', 'madrid.norte@padelcenter.local',   (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Barcelona Padel Center',    'Carrer de la Marina 16',  'Barcelona', '930000002', 'barcelona@padelcenter.local',       (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Valencia Pádel Indoor',      'Av. del Cid 78',          'Valencia',  '960000003', 'valencia@padelcenter.local',        (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Sevilla Pádel Sur',          'Calle Sierpes 23',        'Sevilla',   '950000004', 'sevilla@padelcenter.local',         (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Bilbao Pádel Ría',           'Gran Vía 12',             'Bilbao',    '940000005', 'bilbao@padelcenter.local',          (SELECT admin_id FROM temp_admin)),
    (gen_random_uuid(), 'Málaga Pádel Costa',         'Paseo Marítimo 9',        'Málaga',    '950000006', 'malaga@padelcenter.local',          (SELECT admin_id FROM temp_admin));

CREATE TEMP TABLE temp_curated_centers AS SELECT center_id, name FROM centers WHERE email LIKE '%@padelcenter.local';

-- ── Admin is ADMIN on every curated center; manager is MANAGER on the first two ──
INSERT INTO center_roles (user_id, center_id, role_name, created_by)
SELECT (SELECT admin_id FROM temp_admin), center_id, 'ADMIN', (SELECT admin_id FROM temp_admin)
FROM temp_curated_centers;

INSERT INTO center_roles (user_id, center_id, role_name, created_by)
SELECT (SELECT user_id FROM users WHERE email = 'manager@padelcenter.local'), center_id, 'MANAGER', (SELECT admin_id FROM temp_admin)
FROM temp_curated_centers
ORDER BY name
LIMIT 2;

-- ── 4 fields per curated center (24 total) ──
INSERT INTO fields (field_id, center_id, name, type, price_per_hour, is_available, created_by)
SELECT
    gen_random_uuid(),
    c.center_id,
    'Pista ' || i,
    CASE WHEN i <= 2 THEN 'Indoor' ELSE 'Outdoor' END,
    20.00 + (i * 2.50),
    TRUE,
    (SELECT admin_id FROM temp_admin)
FROM temp_curated_centers c
CROSS JOIN generate_series(1, 4) AS i;

-- ── A handful of curated bookings (past/today/future) for the availability endpoint ──
INSERT INTO bookings (user_id, field_id, status, start_time, end_time, total_price, created_by)
SELECT
    (SELECT user_id FROM users WHERE email = 'ana.garcia@padelcenter.local'),
    f.field_id, 4, -- COMPLETED, yesterday
    (CURRENT_DATE - INTERVAL '1 day') + TIME '10:00', (CURRENT_DATE - INTERVAL '1 day') + TIME '11:00', 25.00,
    (SELECT admin_id FROM temp_admin)
FROM fields f JOIN temp_curated_centers c ON c.center_id = f.center_id AND c.name = 'Padel Club Madrid Norte' AND f.name = 'Pista 1';

INSERT INTO bookings (user_id, field_id, status, start_time, end_time, total_price, created_by)
SELECT
    (SELECT user_id FROM users WHERE email = 'luis.perez@padelcenter.local'),
    f.field_id, 2, -- CONFIRMED, today
    CURRENT_DATE + TIME '18:00', CURRENT_DATE + TIME '19:00', 22.50,
    (SELECT admin_id FROM temp_admin)
FROM fields f JOIN temp_curated_centers c ON c.center_id = f.center_id AND c.name = 'Padel Club Madrid Norte' AND f.name = 'Pista 2';

INSERT INTO bookings (user_id, field_id, status, start_time, end_time, total_price, created_by)
SELECT
    (SELECT user_id FROM users WHERE email = 'carla.romero@padelcenter.local'),
    f.field_id, 1, -- PENDING, tomorrow
    (CURRENT_DATE + INTERVAL '1 day') + TIME '09:00', (CURRENT_DATE + INTERVAL '1 day') + TIME '10:00', 22.50,
    (SELECT admin_id FROM temp_admin)
FROM fields f JOIN temp_curated_centers c ON c.center_id = f.center_id AND c.name = 'Barcelona Padel Center' AND f.name = 'Pista 1';

INSERT INTO bookings (user_id, field_id, status, start_time, end_time, total_price, created_by)
SELECT
    (SELECT user_id FROM users WHERE email = 'diego.molina@padelcenter.local'),
    f.field_id, 3, -- CANCELLED, next week
    (CURRENT_DATE + INTERVAL '7 days') + TIME '20:00', (CURRENT_DATE + INTERVAL '7 days') + TIME '21:00', 25.00,
    (SELECT admin_id FROM temp_admin)
FROM fields f JOIN temp_curated_centers c ON c.center_id = f.center_id AND c.name = 'Valencia Pádel Indoor' AND f.name = 'Pista 3';

-- Booking participants: the owner plus 3 distinct curated users per curated booking.
INSERT INTO booking_participants (booking_id, user_id, created_by)
SELECT b.booking_id, b.user_id, (SELECT admin_id FROM temp_admin)
FROM bookings b
JOIN users owner ON owner.user_id = b.user_id AND owner.email LIKE '%@padelcenter.local';

INSERT INTO booking_participants (booking_id, user_id, created_by)
SELECT b.booking_id, u.user_id, (SELECT admin_id FROM temp_admin)
FROM bookings b
JOIN users owner ON owner.user_id = b.user_id AND owner.email LIKE '%@padelcenter.local'
CROSS JOIN LATERAL (
    SELECT user_id FROM users
    WHERE email LIKE '%@padelcenter.local' AND user_id != b.user_id
    ORDER BY RANDOM()
    LIMIT 3
) AS u;

-- ── 1 curated tournament, open for registration, with 8 confirmed pairs ──
-- Left in REGISTRATION_OPEN on purpose: closing it (and generating matches) is meant
-- to be exercised manually through PATCH /tournaments/{id}/registration/close.
INSERT INTO tournaments (tournament_id, center_id, name, description, format, status, max_pairs, start_date, end_date, created_by)
SELECT
    gen_random_uuid(), c.center_id,
    'Copa Primavera 2026',
    'Torneo abierto de pádel por parejas, formato liguilla.',
    'ROUND_ROBIN', 'REGISTRATION_OPEN', 8,
    CURRENT_DATE + INTERVAL '14 days', CURRENT_DATE + INTERVAL '21 days',
    (SELECT admin_id FROM temp_admin)
FROM temp_curated_centers c
WHERE c.name = 'Padel Club Madrid Norte';

CREATE TEMP TABLE temp_tournament AS
SELECT tournament_id FROM tournaments WHERE name = 'Copa Primavera 2026';

-- 16 distinct demo players (everyone except admin/manager) paired up into 8 confirmed pairs
-- (the tournament's max_pairs is 8 — leaves 2 regular users free to register via the live API).
WITH eligible_players AS (
    SELECT user_id, ROW_NUMBER() OVER (ORDER BY email) AS rn
    FROM users
    WHERE email LIKE '%@padelcenter.local'
      AND email NOT IN ('admin@padelcenter.local', 'manager@padelcenter.local')
    ORDER BY email
    LIMIT 16
),
pairs AS (
    SELECT
        p1.user_id AS player1_id,
        p2.user_id AS player2_id,
        p1.rn AS pair_index
    FROM eligible_players p1
    JOIN eligible_players p2 ON p2.rn = p1.rn + 1
    WHERE p1.rn % 2 = 1
)
INSERT INTO tournament_pairs (tournament_id, player1_id, player2_id, status, team_name)
SELECT
    (SELECT tournament_id FROM temp_tournament),
    player1_id, player2_id,
    'CONFIRMED',
    'Pareja ' || ((pair_index + 1) / 2)
FROM pairs;

DROP TABLE temp_admin;
DROP TABLE temp_curated_centers;
DROP TABLE temp_tournament;
