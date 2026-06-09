-- ============================================================
-- Appointment Service — Script de inicialización
-- Base de datos: appointment_db
-- Red de Servicios Médicos de Piedrazul
-- ============================================================
-- Prueba: 2026-06-09 (martes) a las 08:00
-- Agenda completa: 2026-06-09, 10, 11 (mar-jue)
-- Horario de todos los profesionales: Lun-Vie 09:00-17:00
--   30 min → 16 slots/día (Clara, José, Ibis, Christian, Zarama)
--   45 min → 10 slots/día (Armando)
-- ============================================================

CREATE TABLE IF NOT EXISTS appointments (
    appt_id         SERIAL,
    appt_doct_id    BIGINT       NOT NULL,
    appt_doct_name  VARCHAR(100) NOT NULL,
    appt_pat_id     BIGINT       NOT NULL,
    appt_date       DATE         NOT NULL,
    appt_start_time TIME         NOT NULL,
    appt_end_time   TIME         NOT NULL,
    appt_status     VARCHAR(20)  DEFAULT 'AGENDADA',
    appt_reason     VARCHAR(255) NOT NULL DEFAULT 'Sin especificar',
    appt_notes      VARCHAR(500),
    appt_service    VARCHAR(30)  NOT NULL DEFAULT 'CONSULTA_GENERAL',
    CONSTRAINT pk_appointments       PRIMARY KEY (appt_id),
    CONSTRAINT ck_appt_status_valid  CHECK (appt_status  IN ('AGENDADA', 'REAGENDADA', 'CANCELADA', 'ATENDIDA')),
    CONSTRAINT ck_appt_service_valid CHECK (appt_service IN ('CONSULTA_GENERAL', 'FISIOTERAPIA', 'QUIROPRAXIA', 'TERAPIA_NEURAL'))
);

CREATE TABLE IF NOT EXISTS patient_service_authorizations (
    auth_id                 SERIAL PRIMARY KEY,
    patient_id              BIGINT      NOT NULL,
    service_type            VARCHAR(30) NOT NULL,
    authorized_at           TIMESTAMP   NOT NULL DEFAULT NOW(),
    expires_at              TIMESTAMP   NOT NULL,
    used                    BOOLEAN     NOT NULL DEFAULT FALSE,
    authorized_by_doctor_id BIGINT      NOT NULL,
    appointment_id          INTEGER     NOT NULL,
    CONSTRAINT ck_auth_service_valid CHECK (service_type IN ('FISIOTERAPIA', 'QUIROPRAXIA', 'TERAPIA_NEURAL'))
);

-- Índice único parcial: un slot activo por profesional/fecha/hora
CREATE UNIQUE INDEX IF NOT EXISTS uk_active_appointments
    ON appointments (appt_doct_id, appt_date, appt_start_time)
    WHERE appt_status IN ('AGENDADA', 'REAGENDADA', 'ATENDIDA');


-- ============================================================
-- 1. CITA PASADA (ATENDIDA) — origen de la autorización
--    Zarama Velasco atendió a paciente1@gmail.com el 2026-06-05
--    y le autorizó Terapia Neural para su próxima cita.
-- ============================================================
INSERT INTO appointments
    (appt_doct_id, appt_doct_name, appt_pat_id, appt_date,
     appt_start_time, appt_end_time, appt_status, appt_reason, appt_service)
VALUES
    (2000000005, 'Zarama Velasco', 1094567890, '2026-06-05',
     '09:00', '09:30', 'ATENDIDA',
     'Consulta general - evaluación inicial', 'CONSULTA_GENERAL');

-- Autorización activa: paciente1@gmail.com puede agendar TERAPIA_NEURAL
-- Vigente del 05/06/2026 al 05/07/2026
INSERT INTO patient_service_authorizations
    (patient_id, service_type, authorized_at, expires_at, authorized_by_doctor_id, appointment_id)
SELECT
    1094567890,
    'TERAPIA_NEURAL',
    '2026-06-05 09:30:00',
    '2026-07-05 09:30:00',
    2000000005,
    appt_id
FROM appointments
WHERE appt_pat_id  = 1094567890
  AND appt_doct_id = 2000000005
  AND appt_date    = '2026-06-05'
  AND appt_status  = 'ATENDIDA';


-- ============================================================
-- 2. AGENDA COMPLETA — 2026-06-09 al 2026-06-12 (mar–vie)
--
--    5 profesionales × 16 slots (30 min) = 80 citas/día
--    1 profesional  × 10 slots (45 min) = 10 citas/día
--    Total por día: 90 citas  ×  4 días = 360 citas
--
--    Servicios por profesional:
--      Clara, José, Ibis, Christian → TERAPIA_NEURAL
--      Zarama                       → CONSULTA_GENERAL (slots pares)
--                                     FISIOTERAPIA    (slots impares)
--      Armando                      → QUIROPRAXIA
--
--    Pacientes: 360 pacientes únicos (cédulas 10,248,579–99,488,440)
--    Cada paciente aparece exactamente una vez → ninguno acumula
--    más de 1 cita AGENDADA, respetando la regla de negocio.
-- ============================================================

-- ── Profesionales con intervalo de 30 minutos ────────────────
-- Clara Inés Córdoba · José Ignacio García · Ibis González
-- Christian González · Zarama Velasco
-- global_rn: 1–320  →  pat_id: 10000000 + global_rn × 248579
WITH slots AS (
    SELECT
        d.doc_id,
        d.doc_name,
        d.service,
        a.appt_date::date                                                  AS appt_date,
        s.slot_num,
        ROW_NUMBER() OVER (ORDER BY d.doc_id, a.appt_date, s.slot_num)    AS global_rn
    FROM (VALUES
        (2000000001::bigint, 'Clara Inés Córdoba',  'TERAPIA_NEURAL'::varchar),
        (2000000002,         'José Ignacio García',  'TERAPIA_NEURAL'),
        (2000000003,         'Ibis González',        'TERAPIA_NEURAL'),
        (2000000004,         'Christian González',   'TERAPIA_NEURAL'),
        (2000000005,         'Zarama Velasco',       'CONSULTA_GENERAL')
    ) AS d(doc_id, doc_name, service)
    CROSS JOIN generate_series('2026-06-09'::date, '2026-06-12'::date, interval '1 day') AS a(appt_date)
    CROSS JOIN generate_series(0, 15) AS s(slot_num)
    WHERE EXTRACT(ISODOW FROM a.appt_date) BETWEEN 1 AND 5
)
INSERT INTO appointments
    (appt_doct_id, appt_doct_name, appt_pat_id, appt_date,
     appt_start_time, appt_end_time, appt_status, appt_reason, appt_service)
SELECT
    doc_id,
    doc_name,
    10000000 + global_rn * 248579,
    appt_date,
    ('09:00'::time + slot_num * interval '30 minutes')::time,
    ('09:30'::time + slot_num * interval '30 minutes')::time,
    'AGENDADA',
    -- Motivo según servicio real (Zarama alterna CG y Fisio)
    CASE
        WHEN service = 'TERAPIA_NEURAL' THEN
            CASE slot_num % 4
                WHEN 0 THEN 'Evaluación inicial'
                WHEN 1 THEN 'Sesión de terapia neural'
                WHEN 2 THEN 'Seguimiento neurológico'
                ELSE        'Control post-sesión'
            END
        WHEN doc_id = 2000000005 AND slot_num % 2 = 1 THEN  -- Zarama · Fisioterapia
            CASE (slot_num / 2) % 3
                WHEN 0 THEN 'Evaluación fisioterapia'
                WHEN 1 THEN 'Sesión de fisioterapia'
                ELSE        'Seguimiento fisioterapia'
            END
        ELSE  -- Zarama · Consulta General
            CASE slot_num % 4
                WHEN 0 THEN 'Consulta general'
                WHEN 2 THEN 'Primera consulta'
                ELSE        'Seguimiento médico'
            END
    END,
    -- Servicio: Zarama alterna slot par=CONSULTA_GENERAL / impar=FISIOTERAPIA
    CASE WHEN doc_id = 2000000005 AND slot_num % 2 = 1
         THEN 'FISIOTERAPIA'::varchar
         ELSE service
    END
FROM slots
ON CONFLICT (appt_doct_id, appt_date, appt_start_time)
    WHERE appt_status IN ('AGENDADA', 'REAGENDADA', 'ATENDIDA')
DO NOTHING;

-- ── Armando Peña — intervalos de 45 minutos (Quiropraxia) ───
-- global_rn: 321–360  →  pat_id: 10000000 + global_rn × 248579
WITH slots AS (
    SELECT
        a.appt_date::date                                                  AS appt_date,
        s.slot_num,
        320 + ROW_NUMBER() OVER (ORDER BY a.appt_date, s.slot_num)        AS global_rn
    FROM generate_series('2026-06-09'::date, '2026-06-12'::date, interval '1 day') AS a(appt_date)
    CROSS JOIN generate_series(0, 9) AS s(slot_num)
    WHERE EXTRACT(ISODOW FROM a.appt_date) BETWEEN 1 AND 5
)
INSERT INTO appointments
    (appt_doct_id, appt_doct_name, appt_pat_id, appt_date,
     appt_start_time, appt_end_time, appt_status, appt_reason, appt_service)
SELECT
    2000000006,
    'Armando Peña',
    10000000 + global_rn * 248579,
    appt_date,
    ('09:00'::time + slot_num * interval '45 minutes')::time,
    ('09:45'::time + slot_num * interval '45 minutes')::time,
    'AGENDADA',
    CASE slot_num % 3
        WHEN 0 THEN 'Evaluación quiropraxia'
        WHEN 1 THEN 'Ajuste vertebral'
        ELSE        'Sesión de quiropraxia'
    END,
    'QUIROPRAXIA'
FROM slots
ON CONFLICT (appt_doct_id, appt_date, appt_start_time)
    WHERE appt_status IN ('AGENDADA', 'REAGENDADA', 'ATENDIDA')
DO NOTHING;

-- ============================================================
-- NOTA: a partir del 2026-06-13 (semana siguiente) la agenda
-- queda libre para que los pacientes agenden citas en la demo.
-- La ventana permitida es de 4 semanas (hasta 2026-07-07).
-- ============================================================
