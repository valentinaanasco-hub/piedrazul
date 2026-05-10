-- ============================================================
-- Appointment Service — Script de inicialización
-- Base de datos: appointment_db
-- La caché de usuarios vive en Redis, no en PostgreSQL
-- ============================================================

CREATE TABLE IF NOT EXISTS appointments (
    appt_id SERIAL,
    appt_doct_id INTEGER NOT NULL,
    appt_pat_id INTEGER NOT NULL,
    appt_date DATE NOT NULL,
    appt_start_time TIME NOT NULL,
    appt_end_time TIME NOT NULL,
    appt_status VARCHAR(20) DEFAULT 'AGENDADA',
    appt_reason VARCHAR(255) NOT NULL DEFAULT 'Sin especificar',
    appt_notes VARCHAR(500),
    CONSTRAINT pk_appointments PRIMARY KEY (appt_id),
    CONSTRAINT ck_appt_status_valid CHECK (appt_status IN ('AGENDADA', 'REAGENDADA', 'CANCELADA', 'ATENDIDA'))
);
