-- Curated seed data for local manual/Postman testing.
-- Only applied when spring.flyway.locations includes classpath:db/seed (profile "local").
-- All accounts share the password "Padel2026!" — never used outside local dev.
--
-- IDs are fixed literals (not gen_random_uuid()) on purpose: this lets the Postman
-- collection/environment reference stable IDs that survive a "docker compose down -v"
-- reseed. Namespaced by entity type so they're easy to recognize when debugging:
--   a0000000-... users   b0000000-... centers   c0000000-... fields
--   d0000000-... tournaments   e0000000-... tournament_pairs

-- ── Admin user (bootstraps itself: there is no public API path to grant ADMIN) ──
INSERT INTO users (user_id, first_name, last_name, email, password_hash, phone_number, created_by)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'Admin', 'Padel',
    'admin@padelcenter.local',
    '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i',
    '600000001',
    'a0000000-0000-0000-0000-000000000001'
);

-- ── Manager + 18 regular demo users, all with known credentials ──
INSERT INTO users (user_id, first_name, last_name, email, password_hash, phone_number, created_by)
VALUES
    ('a0000000-0000-0000-0000-000000000002', 'Marta',   'Suárez',   'manager@padelcenter.local',       '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000002', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000003', 'Ana',     'García',   'ana.garcia@padelcenter.local',    '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000003', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000004', 'Luis',    'Pérez',    'luis.perez@padelcenter.local',    '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000004', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000005', 'Carla',   'Romero',   'carla.romero@padelcenter.local',  '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000005', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000006', 'Diego',   'Molina',   'diego.molina@padelcenter.local',  '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000006', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000007', 'Elena',   'Navarro',  'elena.navarro@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000007', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000008', 'Pablo',   'Ortega',   'pablo.ortega@padelcenter.local',  '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000008', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000009', 'Sara',    'Iglesias', 'sara.iglesias@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000009', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000010', 'Javier',  'Castro',   'javier.castro@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000010', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000011', 'Lucía',   'Vidal',    'lucia.vidal@padelcenter.local',   '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000011', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000012', 'Hugo',    'Serrano',  'hugo.serrano@padelcenter.local',  '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000012', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000013', 'Marina',  'Delgado',  'marina.delgado@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000013', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000014', 'Adrián',  'Vázquez',  'adrian.vazquez@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000014', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000015', 'Paula',   'Ramos',    'paula.ramos@padelcenter.local',   '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000015', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000016', 'Álvaro',  'Cano',     'alvaro.cano@padelcenter.local',   '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000016', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000017', 'Claudia', 'Reyes',    'claudia.reyes@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000017', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000018', 'Mario',   'Gil',      'mario.gil@padelcenter.local',     '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000018', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000019', 'Irene',   'Pascual',  'irene.pascual@padelcenter.local', '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000019', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000020', 'Rubén',   'Soto',     'ruben.soto@padelcenter.local',    '$2a$10$x2jVOqXjB78cY56HzO2zW.hOWczA69XWrtBlY5BBsMtCPljCaB39i', '600000020', 'a0000000-0000-0000-0000-000000000001');

-- ── 6 curated centers ──
INSERT INTO centers (center_id, name, address, city, phone_number, email, created_by)
VALUES
    ('b0000000-0000-0000-0000-000000000001', 'Padel Club Madrid Norte', 'Av. de la Ilustración 45', 'Madrid',    '910000001', 'madrid.norte@padelcenter.local', 'a0000000-0000-0000-0000-000000000001'),
    ('b0000000-0000-0000-0000-000000000002', 'Barcelona Padel Center',  'Carrer de la Marina 16',  'Barcelona', '930000002', 'barcelona@padelcenter.local',     'a0000000-0000-0000-0000-000000000001'),
    ('b0000000-0000-0000-0000-000000000003', 'Valencia Pádel Indoor',   'Av. del Cid 78',          'Valencia',  '960000003', 'valencia@padelcenter.local',      'a0000000-0000-0000-0000-000000000001'),
    ('b0000000-0000-0000-0000-000000000004', 'Sevilla Pádel Sur',       'Calle Sierpes 23',        'Sevilla',   '950000004', 'sevilla@padelcenter.local',       'a0000000-0000-0000-0000-000000000001'),
    ('b0000000-0000-0000-0000-000000000005', 'Bilbao Pádel Ría',        'Gran Vía 12',             'Bilbao',    '940000005', 'bilbao@padelcenter.local',        'a0000000-0000-0000-0000-000000000001'),
    ('b0000000-0000-0000-0000-000000000006', 'Málaga Pádel Costa',      'Paseo Marítimo 9',        'Málaga',    '950000006', 'malaga@padelcenter.local',        'a0000000-0000-0000-0000-000000000001');

-- ── Admin is ADMIN on every curated center; manager is MANAGER on the first two ──
INSERT INTO center_roles (user_id, center_id, role_name, created_by)
SELECT 'a0000000-0000-0000-0000-000000000001', center_id, 'ADMIN', 'a0000000-0000-0000-0000-000000000001'
FROM centers WHERE email LIKE '%@padelcenter.local';

INSERT INTO center_roles (user_id, center_id, role_name, created_by)
VALUES
    ('a0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', 'MANAGER', 'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002', 'MANAGER', 'a0000000-0000-0000-0000-000000000001');

-- ── 4 fields per curated center (24 total), deterministic field_id per (center, slot) ──
INSERT INTO fields (field_id, center_id, name, type, price_per_hour, is_available, created_by)
SELECT
    ('c0000000-0000-0000-0000-' || LPAD(((c.idx - 1) * 4 + f.slot)::text, 12, '0'))::uuid,
    c.center_id,
    'Pista ' || f.slot,
    CASE WHEN f.slot <= 2 THEN 'Indoor' ELSE 'Outdoor' END,
    20.00 + (f.slot * 2.50),
    TRUE,
    'a0000000-0000-0000-0000-000000000001'
FROM (SELECT center_id, ROW_NUMBER() OVER (ORDER BY center_id) AS idx FROM centers WHERE email LIKE '%@padelcenter.local') c
CROSS JOIN generate_series(1, 4) AS f(slot);

-- ── A handful of curated bookings (past/today/future) for the availability endpoint ──
INSERT INTO bookings (user_id, field_id, status, start_time, end_time, total_price, created_by)
VALUES
    ('a0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000001', 4, -- COMPLETED, yesterday — Madrid Norte / Pista 1
     (CURRENT_DATE - INTERVAL '1 day') + TIME '10:00', (CURRENT_DATE - INTERVAL '1 day') + TIME '11:00', 25.00,
     'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000002', 2, -- CONFIRMED, today — Madrid Norte / Pista 2
     CURRENT_DATE + TIME '18:00', CURRENT_DATE + TIME '19:00', 22.50,
     'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000005', 1, -- PENDING, tomorrow — Barcelona / Pista 1
     (CURRENT_DATE + INTERVAL '1 day') + TIME '09:00', (CURRENT_DATE + INTERVAL '1 day') + TIME '10:00', 22.50,
     'a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000011', 3, -- CANCELLED, next week — Valencia / Pista 3
     (CURRENT_DATE + INTERVAL '7 days') + TIME '20:00', (CURRENT_DATE + INTERVAL '7 days') + TIME '21:00', 25.00,
     'a0000000-0000-0000-0000-000000000001');

-- Booking participants: the owner plus 3 distinct curated users per curated booking.
INSERT INTO booking_participants (booking_id, user_id, created_by)
SELECT b.booking_id, b.user_id, 'a0000000-0000-0000-0000-000000000001'
FROM bookings b
JOIN users owner ON owner.user_id = b.user_id AND owner.email LIKE '%@padelcenter.local';

INSERT INTO booking_participants (booking_id, user_id, created_by)
SELECT b.booking_id, u.user_id, 'a0000000-0000-0000-0000-000000000001'
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
VALUES (
    'd0000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000001',
    'Copa Primavera 2026',
    'Torneo abierto de pádel por parejas, formato liguilla.',
    'ROUND_ROBIN', 'REGISTRATION_OPEN', 8,
    CURRENT_DATE + INTERVAL '14 days', CURRENT_DATE + INTERVAL '21 days',
    'a0000000-0000-0000-0000-000000000001'
);

-- 16 distinct demo players (everyone except admin/manager) paired up into 8 confirmed pairs
-- (the tournament's max_pairs is 8 — leaves 2 regular users free to register via the live API).
INSERT INTO tournament_pairs (pair_id, tournament_id, player1_id, player2_id, status, team_name)
VALUES
    ('e0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000004', 'CONFIRMED', 'Pareja 1'),
    ('e0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000006', 'CONFIRMED', 'Pareja 2'),
    ('e0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000007', 'a0000000-0000-0000-0000-000000000008', 'CONFIRMED', 'Pareja 3'),
    ('e0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000009', 'a0000000-0000-0000-0000-000000000010', 'CONFIRMED', 'Pareja 4'),
    ('e0000000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000011', 'a0000000-0000-0000-0000-000000000012', 'CONFIRMED', 'Pareja 5'),
    ('e0000000-0000-0000-0000-000000000006', 'd0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000013', 'a0000000-0000-0000-0000-000000000014', 'CONFIRMED', 'Pareja 6'),
    ('e0000000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000015', 'a0000000-0000-0000-0000-000000000016', 'CONFIRMED', 'Pareja 7'),
    ('e0000000-0000-0000-0000-000000000008', 'd0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000017', 'a0000000-0000-0000-0000-000000000018', 'CONFIRMED', 'Pareja 8');
-- a...0019 (irene.pascual) and a...0020 (ruben.soto) are intentionally left unpaired.
