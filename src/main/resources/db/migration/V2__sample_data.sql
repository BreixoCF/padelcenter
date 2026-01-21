CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Definición del ID del Auditor antes de la inserción de usuarios
-- Creamos un UUID que usaremos como auditor en todas las tablas
CREATE TEMP TABLE temp_auditor_uuid AS
SELECT gen_random_uuid() AS auditor_id;

-- 2. INSERCIÓN DEL USUARIO AUDITOR (Usuario 0)
-- Este usuario será referenciado en todos los campos created_by.
INSERT INTO users (user_id, first_name, last_name, email, password_hash, created_by)
VALUES (
           (SELECT auditor_id FROM temp_auditor_uuid),
           'System',
           'Auditor',
           'system@padelcenter.com',
           'system_hash',
           (SELECT auditor_id FROM temp_auditor_uuid) -- UUID es insertado como UUID
       );

-- 3. INSERCIÓN DE USUARIOS RESTANTES (25)
WITH users_data AS (
    SELECT
        gen_random_uuid() AS user_id,
        'FName_' || i AS first_name,
        'LName_' || i AS last_name,
        'user_' || i || '@test.com' AS email,
        'hashed_password_' || i AS password_hash
    FROM generate_series(1, 25) AS i
)
INSERT INTO users (user_id, first_name, last_name, email, password_hash, created_by)
SELECT
    user_id,
    first_name,
    last_name,
    email,
    password_hash,
    (SELECT auditor_id FROM temp_auditor_uuid) -- Usar el UUID del auditor
FROM users_data;

-- Variables temporales para simplificar las FKs
CREATE TEMP TABLE temp_users AS SELECT user_id FROM users;
CREATE TEMP TABLE temp_centers AS SELECT center_id FROM centers;
CREATE TEMP TABLE temp_fields AS SELECT field_id, center_id FROM fields;


-- 4. INSERCIÓN DE CENTROS (10)
WITH centers_data AS (
    SELECT
        gen_random_uuid() AS center_id,
        'Center Padel ' || i AS name,
        'Address ' || i AS address,
        CASE i % 3
            WHEN 0 THEN 'Barcelona'
            WHEN 1 THEN 'Madrid'
            ELSE 'Valencia'
            END AS city,
        'center_' || i || '@padel.com' AS email
    FROM generate_series(1, 10) AS i
)
INSERT INTO centers (center_id, name, address, city, email, created_by)
SELECT
    center_id,
    name,
    address,
    city,
    email,
    (SELECT auditor_id FROM temp_auditor_uuid) -- Usar el UUID del auditor
FROM centers_data;

-- 5. INSERCIÓN DE CANCHAS (FIELDS) (4 por centro, Total: 40)
INSERT INTO fields (field_id, center_id, name, type, price_per_hour, created_by)
SELECT
    gen_random_uuid(),
    c.center_id,
    'Cancha ' || (ROW_NUMBER() OVER (PARTITION BY c.center_id)),
    CASE i % 2
        WHEN 0 THEN 'Individual'
        ELSE 'Doble'
        END,
    50.00 + (i * 5.00),
    (SELECT auditor_id FROM temp_auditor_uuid) -- Usar el UUID del auditor
FROM centers c
         CROSS JOIN generate_series(1, 4) AS i;

-- 6. INSERCIÓN DE ROLES POR CENTRO (40 Roles)
INSERT INTO center_roles (user_id, center_id, role_name, created_by)
SELECT
    u.user_id,
    c.center_id,
    CASE (ROW_NUMBER() OVER (ORDER BY gen_random_uuid())) % 3
        WHEN 0 THEN 'ADMIN'
        WHEN 1 THEN 'MANAGER'
        ELSE 'USER'
        END AS role_name,
    (SELECT auditor_id FROM temp_auditor_uuid) -- Usar el UUID del auditor
FROM users u, centers c
LIMIT 40;

-- 7. INSERCIÓN DE RESERVAS (BOOKINGS) (100)
WITH random_users AS (
    SELECT user_id FROM users ORDER BY created_at LIMIT 10
),
     random_fields AS (
         SELECT field_id FROM fields ORDER BY created_at LIMIT 10
     ),
     booking_sequences AS (
         SELECT
             (SELECT user_id FROM random_users ORDER BY RANDOM() LIMIT 1) AS random_user_id,
             (SELECT field_id FROM random_fields ORDER BY RANDOM() LIMIT 1) AS random_field_id,
             i
         FROM generate_series(1, 100) AS i
     )
INSERT INTO bookings (user_id, field_id, status, start_time, end_time, total_price, created_by)
SELECT
    random_user_id,
    random_field_id,
    (i % 3) + 1,
    NOW() + (i * INTERVAL '1 day'),
    NOW() + (i * INTERVAL '1 day') + INTERVAL '1 hour 30 minutes',
    75.00 + (i % 10),
    (SELECT auditor_id FROM temp_auditor_uuid) -- Usar el UUID del auditor
FROM booking_sequences;

-- Obtener IDs de reserva y el usuario que reservó para participantes
CREATE TEMP TABLE temp_bookings AS SELECT booking_id, user_id AS booked_by_user_id FROM bookings;

-- 8. INSERCIÓN DE PARTICIPANTES (4 por reserva, Total: 400)
-- 8.1 Insertar el usuario que reservó como participante
INSERT INTO booking_participants (booking_id, user_id, created_at, created_by)
SELECT
    booking_id,
    booked_by_user_id,
    NOW(),
    (SELECT auditor_id FROM temp_auditor_uuid) -- Usar el UUID del auditor
FROM temp_bookings;

-- 8.2 Insertar 3 participantes adicionales (diferentes del que reservó)
INSERT INTO booking_participants (booking_id, user_id, created_at, created_by)
SELECT
    b.booking_id,
    u.user_id,
    NOW(),
    (SELECT auditor_id FROM temp_auditor_uuid) -- Usar el UUID del auditor
FROM bookings b
         JOIN users u ON u.user_id IN (
    SELECT u2.user_id
    FROM users u2
    WHERE u2.user_id != b.user_id
    ORDER BY RANDOM()
    LIMIT 3
)
ORDER BY b.booking_id;

-- LIMPIEZA
DROP TABLE temp_auditor_uuid;
DROP TABLE temp_users;
DROP TABLE temp_centers;
DROP TABLE temp_fields;
DROP TABLE temp_bookings;