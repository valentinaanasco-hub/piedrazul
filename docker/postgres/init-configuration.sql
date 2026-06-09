-- ============================================================
-- Configuration Service — Script de inicialización
-- Base de datos: configuration_db
-- Red de Servicios Médicos de Piedrazul
-- ============================================================

CREATE TABLE IF NOT EXISTS system_parameters (
    parameter_key         VARCHAR(100),
    parameter_value       TEXT NOT NULL,
    parameter_description TEXT,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_system_parameters PRIMARY KEY (parameter_key)
);

CREATE TABLE IF NOT EXISTS doctor_schedule_configurations (
    config_id        SERIAL,
    doctor_id        INTEGER NOT NULL,
    day_of_week      INTEGER NOT NULL,
    start_time       TIME    NOT NULL,
    end_time         TIME    NOT NULL,
    interval_minutes INTEGER NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_doctor_schedule_configurations PRIMARY KEY (config_id),
    CONSTRAINT ck_day_valid    CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT ck_interval_pos CHECK (interval_minutes > 0)
);

-- ============================================================
-- PARÁMETRO GLOBAL: ventana de agendamiento = 4 semanas
-- ============================================================
INSERT INTO system_parameters (parameter_key, parameter_value, parameter_description)
VALUES ('appointment_window_weeks', '4', 'Ventana de tiempo en semanas para agendar citas')
ON CONFLICT (parameter_key) DO NOTHING;

-- ============================================================
-- HORARIOS DE PROFESIONALES
-- Deben ser idénticos a doctor_schedules en medical_db
-- (ambas tablas sirven a microservicios distintos)
--
--   Clara, José, Ibis, Christian, Zarama : Lun-Vie 09:00-17:00 30 min
--   Armando                              : Lun-Vie 09:00-17:00 45 min
-- ============================================================

-- 5 profesionales — intervalo 30 minutos
INSERT INTO doctor_schedule_configurations (doctor_id, day_of_week, start_time, end_time, interval_minutes)
SELECT doc_id, d, '09:00'::time, '17:00'::time, 30
FROM (VALUES (2000000001),(2000000002),(2000000003),(2000000004),(2000000005)) AS t(doc_id)
CROSS JOIN generate_series(1, 5) AS d;

-- Armando Peña — intervalo 45 minutos (Quiropraxia)
INSERT INTO doctor_schedule_configurations (doctor_id, day_of_week, start_time, end_time, interval_minutes)
SELECT 2000000006, d, '09:00'::time, '17:00'::time, 45
FROM generate_series(1, 5) AS d;
