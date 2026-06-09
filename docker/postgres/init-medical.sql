-- ============================================================
-- Medical Staff Service — Script de inicialización
-- Base de datos: medical_db
-- Red de Servicios Médicos de Piedrazul
-- ============================================================

CREATE TABLE IF NOT EXISTS doct_type (
    doct_type_id   SERIAL,
    doct_type_name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_doc_type  PRIMARY KEY (doct_type_id),
    CONSTRAINT uk_type_name UNIQUE (doct_type_name)
);

CREATE TABLE IF NOT EXISTS doctors (
    doct_user_id         INTEGER      NOT NULL,
    doct_professional_id VARCHAR(50)  NOT NULL,
    doct_first_name      VARCHAR(100) NOT NULL,
    doct_first_surname   VARCHAR(100) NOT NULL,
    doct_type_id         INTEGER      NOT NULL,
    CONSTRAINT pk_doctors              PRIMARY KEY (doct_user_id),
    CONSTRAINT uk_doct_professional_id UNIQUE (doct_professional_id),
    CONSTRAINT fk_doct_type            FOREIGN KEY (doct_type_id) REFERENCES doct_type (doct_type_id)
);

CREATE TABLE IF NOT EXISTS specialties (
    spec_id   SERIAL,
    spec_name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_specialties PRIMARY KEY (spec_id),
    CONSTRAINT uk_spec_name   UNIQUE (spec_name)
);

CREATE TABLE IF NOT EXISTS doctor_specialties (
    ds_doct_id INTEGER NOT NULL,
    ds_spec_id INTEGER NOT NULL,
    CONSTRAINT pk_doctor_specialties PRIMARY KEY (ds_doct_id, ds_spec_id),
    CONSTRAINT fk_ds_doct FOREIGN KEY (ds_doct_id) REFERENCES doctors (doct_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_ds_spec FOREIGN KEY (ds_spec_id) REFERENCES specialties (spec_id)  ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS doctor_schedules (
    sched_id               SERIAL,
    sched_doctor_id        INTEGER NOT NULL,
    sched_day_of_week      INTEGER NOT NULL,
    sched_start_time       TIME    NOT NULL,
    sched_end_time         TIME    NOT NULL,
    sched_interval_minutes INTEGER NOT NULL,
    CONSTRAINT pk_doctor_schedules    PRIMARY KEY (sched_id),
    CONSTRAINT fk_sched_doctor        FOREIGN KEY (sched_doctor_id) REFERENCES doctors (doct_user_id) ON DELETE CASCADE,
    CONSTRAINT ck_sched_day_valid     CHECK (sched_day_of_week BETWEEN 1 AND 7),
    CONSTRAINT ck_sched_interval_pos  CHECK (sched_interval_minutes > 0)
);

CREATE TABLE IF NOT EXISTS system_parameters (
    parameter_key   VARCHAR(100),
    parameter_value TEXT NOT NULL,
    CONSTRAINT pk_system_parameters PRIMARY KEY (parameter_key)
);

-- ============================================================
-- TIPOS DE PROFESIONAL
-- ============================================================
INSERT INTO doct_type (doct_type_name) VALUES ('Médico')    ON CONFLICT (doct_type_name) DO NOTHING;
INSERT INTO doct_type (doct_type_name) VALUES ('Terapeuta') ON CONFLICT (doct_type_name) DO NOTHING;

-- ============================================================
-- ESPECIALIDADES
--   Consulta General y Fisioterapia son servicios base:
--   los atiende cualquier profesional SIN especialidad asignada.
--   Terapia Neural y Quiropraxia requieren especialidad.
-- ============================================================
INSERT INTO specialties (spec_name) VALUES ('Terapia Neural') ON CONFLICT (spec_name) DO NOTHING;
INSERT INTO specialties (spec_name) VALUES ('Quiropraxia')    ON CONFLICT (spec_name) DO NOTHING;
INSERT INTO specialties (spec_name) VALUES ('Fisioterapia')   ON CONFLICT (spec_name) DO NOTHING;

-- ============================================================
-- PROFESIONALES (deben coincidir con identity_db)
-- ============================================================
INSERT INTO doctors (doct_user_id, doct_professional_id, doct_first_name, doct_first_surname, doct_type_id)
VALUES
    (2000000001, 'TN-2026-01', 'Clara Inés',  'Córdoba',  (SELECT doct_type_id FROM doct_type WHERE doct_type_name = 'Terapeuta')),
    (2000000002, 'TN-2026-02', 'José Ignacio','García',   (SELECT doct_type_id FROM doct_type WHERE doct_type_name = 'Terapeuta')),
    (2000000003, 'TN-2026-03', 'Ibis',        'González', (SELECT doct_type_id FROM doct_type WHERE doct_type_name = 'Terapeuta')),
    (2000000004, 'TN-2026-04', 'Christian',   'González', (SELECT doct_type_id FROM doct_type WHERE doct_type_name = 'Terapeuta')),
    (2000000005, 'FIS-2026-01','Zarama',       'Velasco',  (SELECT doct_type_id FROM doct_type WHERE doct_type_name = 'Terapeuta')),
    (2000000006, 'QUI-2026-01','Armando',      'Peña',     (SELECT doct_type_id FROM doct_type WHERE doct_type_name = 'Terapeuta'))
ON CONFLICT (doct_user_id) DO NOTHING;

-- ============================================================
-- ASIGNACIÓN DE ESPECIALIDADES
--   Regla (frontend): doctores CON especialidad solo aparecen
--   para ese servicio. Doctores SIN especialidad aparecen para
--   Consulta General y Fisioterapia.
--
--   Clara, José, Ibis, Christian  →  Terapia Neural
--   Armando                       →  Quiropraxia
--   Zarama                        →  sin especialidad (Consulta General + Fisioterapia)
-- ============================================================
INSERT INTO doctor_specialties (ds_doct_id, ds_spec_id)
SELECT d.doct_user_id, s.spec_id
FROM doctors d CROSS JOIN specialties s
WHERE s.spec_name = 'Terapia Neural'
  AND d.doct_user_id IN (2000000001, 2000000002, 2000000003, 2000000004)
ON CONFLICT DO NOTHING;

INSERT INTO doctor_specialties (ds_doct_id, ds_spec_id)
SELECT d.doct_user_id, s.spec_id
FROM doctors d CROSS JOIN specialties s
WHERE s.spec_name = 'Quiropraxia'
  AND d.doct_user_id = 2000000006
ON CONFLICT DO NOTHING;

-- ============================================================
-- HORARIOS — Lunes a Viernes (ISO DOW 1–5)
--   Clara, José, Ibis, Christian, Zarama : 09:00–17:00, 30 min
--   Armando                              : 09:00–17:00, 45 min
-- ============================================================

-- 5 profesionales con intervalos de 30 minutos
INSERT INTO doctor_schedules (sched_doctor_id, sched_day_of_week, sched_start_time, sched_end_time, sched_interval_minutes)
SELECT doc_id, d, '09:00'::time, '17:00'::time, 30
FROM (VALUES (2000000001),(2000000002),(2000000003),(2000000004),(2000000005)) AS t(doc_id)
CROSS JOIN generate_series(1, 5) AS d
WHERE NOT EXISTS (
    SELECT 1 FROM doctor_schedules
    WHERE sched_doctor_id = doc_id AND sched_day_of_week = d
);

-- Armando Peña — intervalos de 45 minutos (Quiropraxia)
INSERT INTO doctor_schedules (sched_doctor_id, sched_day_of_week, sched_start_time, sched_end_time, sched_interval_minutes)
SELECT 2000000006, d, '09:00'::time, '17:00'::time, 45
FROM generate_series(1, 5) AS d
WHERE NOT EXISTS (
    SELECT 1 FROM doctor_schedules
    WHERE sched_doctor_id = 2000000006 AND sched_day_of_week = d
);
