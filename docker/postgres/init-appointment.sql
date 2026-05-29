-- ============================================================
-- Appointment Service — Script de inicialización
-- Base de datos: appointment_db
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
    CONSTRAINT ck_appt_status_valid CHECK (appt_status IN ('AGENDADA', 'REAGENDADA', 'CANCELADA', 'ATENDIDA', 'NO_ASISTIDO'))
);

-- Índice único parcial para evitar citas duplicadas en el mismo slot
CREATE UNIQUE INDEX IF NOT EXISTS uk_active_appointments
ON appointments (appt_doct_id, appt_date, appt_start_time)
WHERE appt_status IN ('AGENDADA', 'REAGENDADA', 'ATENDIDA');

-- ============================================================
-- CITAS PASADAS (ATENDIDAS) — historial de los pacientes
-- ============================================================

INSERT INTO appointments (appt_doct_id, appt_pat_id, appt_date, appt_start_time, appt_end_time, appt_status, appt_reason, appt_notes)
VALUES
    -- Carlos (1000000004)
    (1000000002, 1000000004, '2026-05-15', '09:00', '09:30', 'ATENDIDA', 'Consulta general',      'Paciente presenta síntomas de gripe'),
    (1000000002, 1000000004, '2026-05-20', '10:00', '10:30', 'ATENDIDA', 'Control',               'Evolución favorable'),

    -- María (1000000010)
    (1000000002, 1000000010, '2026-05-10', '14:00', '14:30', 'ATENDIDA', 'Chequeo general',       'Paciente en buen estado'),
    (1000000005, 1000000010, '2026-05-22', '08:00', '08:20', 'ATENDIDA', 'Control pediátrico',    'Niño sano'),

    -- José (1000000011)
    (1000000008, 1000000011, '2026-05-12', '09:00', '09:30', 'ATENDIDA', 'Dolor en el pecho',     'Remitido a cardiología'),
    (1000000006, 1000000011, '2026-05-18', '10:00', '10:45', 'ATENDIDA', 'Evaluación cardiológica','Electrocardiograma normal'),

    -- Sofía (1000000012)
    (1000000002, 1000000012, '2026-05-14', '11:00', '11:30', 'ATENDIDA', 'Consulta dermatológica','Remitida a especialista'),

    -- Andrés (1000000013)
    (1000000005, 1000000013, '2026-05-16', '09:00', '09:20', 'ATENDIDA', 'Control de crecimiento','Desarrollo normal'),

    -- Camila (1000000014)
    (1000000008, 1000000014, '2026-05-19', '10:00', '10:30', 'ATENDIDA', 'Consulta general',      'Paciente sana'),

    -- Diego (1000000015)
    (1000000002, 1000000015, '2026-05-21', '09:00', '09:30', 'ATENDIDA', 'Primera consulta',      'Sin hallazgos relevantes'),

    -- Valentina (1000000016)
    (1000000007, 1000000016, '2026-05-23', '08:00', '08:30', 'ATENDIDA', 'Dermatitis',            'Se receta crema tópica'),

    -- Santiago (1000000017)
    (1000000005, 1000000017, '2026-05-24', '10:00', '10:20', 'ATENDIDA', 'Vacunación',            'Vacuna aplicada sin incidentes');

-- ============================================================
-- CITAS CANCELADAS — para probar el filtro de estado
-- ============================================================

INSERT INTO appointments (appt_doct_id, appt_pat_id, appt_date, appt_start_time, appt_end_time, appt_status, appt_reason, appt_notes)
VALUES
    (1000000002, 1000000017, '2026-05-25', '15:00', '15:30', 'CANCELADA', 'Consulta general',  'Paciente canceló por motivos personales'),
    (1000000005, 1000000010, '2026-05-26', '11:00', '11:20', 'CANCELADA', 'Control',            'Reprogramada para otra fecha'),
    (1000000006, 1000000014, '2026-05-28', '09:00', '09:45', 'CANCELADA', 'Cardiología',        'Cancelada por el profesional'),
    (1000000007, 1000000012, '2026-05-29', '10:00', '10:30', 'CANCELADA', 'Revisión lunar',     'Paciente no se presentó');

-- ============================================================
-- CITAS FUTURAS AGENDADAS — máximo 1 por paciente
-- Doctores: Juan Pérez (1000000002), Ana Martínez (1000000005),
--           Pedro Gómez (1000000006), Laura Torres (1000000007),
--           Miguel Castro (1000000008)
-- ============================================================

INSERT INTO appointments (appt_doct_id, appt_pat_id, appt_date, appt_start_time, appt_end_time, appt_status, appt_reason, appt_notes)
VALUES
    -- Carlos (1000000004) → Juan Pérez, 02 Jun
    (1000000002, 1000000004, '2026-06-02', '08:00', '08:30', 'AGENDADA', 'Control mensual',          NULL),
    -- María (1000000010) → Miguel Castro, 02 Jun
    (1000000008, 1000000010, '2026-06-02', '08:00', '08:30', 'AGENDADA', 'Consulta general',         NULL),
    -- José (1000000011) → Pedro Gómez, 03 Jun
    (1000000006, 1000000011, '2026-06-03', '10:00', '10:45', 'AGENDADA', 'Control cardiológico',     NULL),
    -- Sofía (1000000012) → Laura Torres, 03 Jun
    (1000000007, 1000000012, '2026-06-03', '08:00', '08:30', 'AGENDADA', 'Consulta dermatológica',   NULL),
    -- Andrés (1000000013) → Ana Martínez, 04 Jun
    (1000000005, 1000000013, '2026-06-04', '09:00', '09:20', 'AGENDADA', 'Control de crecimiento',   NULL),
    -- Camila (1000000014) → Pedro Gómez, 05 Jun
    (1000000006, 1000000014, '2026-06-05', '11:00', '11:45', 'AGENDADA', 'Evaluación cardiológica',  NULL),
    -- Diego (1000000015) → Juan Pérez, 05 Jun
    (1000000002, 1000000015, '2026-06-05', '09:00', '09:30', 'AGENDADA', 'Primera consulta',         NULL),
    -- Valentina (1000000016) → Laura Torres, 06 Jun
    (1000000007, 1000000016, '2026-06-06', '08:00', '08:30', 'AGENDADA', 'Dermatitis',               NULL),
    -- Santiago (1000000017) → Ana Martínez, 06 Jun
    (1000000005, 1000000017, '2026-06-06', '08:00', '08:20', 'AGENDADA', 'Vacunación',               NULL);
