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

-- Índice único parcial para evitar citas duplicadas en el mismo slot
-- Solo aplica a citas activas (no canceladas)
CREATE UNIQUE INDEX IF NOT EXISTS uk_active_appointments 
ON appointments (appt_doct_id, appt_date, appt_start_time)
WHERE appt_status IN ('AGENDADA', 'REAGENDADA', 'ATENDIDA');

-- ============================================================
-- DATOS DE PRUEBA - CITAS
-- ============================================================

-- Citas pasadas (ATENDIDAS) para varios pacientes
INSERT INTO appointments (appt_doct_id, appt_pat_id, appt_date, appt_start_time, appt_end_time, appt_status, appt_reason, appt_notes)
VALUES 
    -- Paciente Carlos (1000000004) - tiene historial con Medicina General
    (1000000002, 1000000004, '2026-05-15', '09:00', '09:30', 'ATENDIDA', 'Consulta general', 'Paciente presenta síntomas de gripe'),
    (1000000002, 1000000004, '2026-05-20', '10:00', '10:30', 'ATENDIDA', 'Control', 'Evolución favorable'),
    
    -- Paciente María (1000000010) - tiene historial con Medicina General y Pediatría
    (1000000002, 1000000010, '2026-05-10', '14:00', '14:30', 'ATENDIDA', 'Chequeo general', 'Paciente en buen estado'),
    (1000000005, 1000000010, '2026-05-22', '08:00', '08:20', 'ATENDIDA', 'Control pediátrico hijo', 'Niño sano'),
    
    -- Paciente José (1000000011) - tiene historial con Medicina General y Cardiología
    (1000000008, 1000000011, '2026-05-12', '09:00', '09:30', 'ATENDIDA', 'Dolor en el pecho', 'Remitido a cardiología'),
    (1000000006, 1000000011, '2026-05-18', '10:00', '10:45', 'ATENDIDA', 'Evaluación cardiológica', 'Electrocardiograma normal'),
    
    -- Paciente Sofía (1000000012) - solo Medicina General
    (1000000002, 1000000012, '2026-05-14', '11:00', '11:30', 'ATENDIDA', 'Consulta dermatológica', 'Remitida a especialista'),
    
    -- Paciente Andrés (1000000013) - menor de edad, solo Pediatría
    (1000000005, 1000000013, '2026-05-16', '09:00', '09:20', 'ATENDIDA', 'Control de crecimiento', 'Desarrollo normal'),
    
    -- Paciente Camila (1000000014) - tiene historial
    (1000000008, 1000000014, '2026-05-19', '10:00', '10:30', 'ATENDIDA', 'Consulta general', 'Paciente sana');

-- Citas futuras (AGENDADAS) para probar el sistema
INSERT INTO appointments (appt_doct_id, appt_pat_id, appt_date, appt_start_time, appt_end_time, appt_status, appt_reason, appt_notes)
VALUES 
    -- Citas para la próxima semana
    (1000000002, 1000000015, '2026-06-02', '09:00', '09:30', 'AGENDADA', 'Primera consulta', NULL),
    (1000000005, 1000000013, '2026-06-03', '10:00', '10:20', 'AGENDADA', 'Control mensual', NULL),
    (1000000006, 1000000011, '2026-06-04', '11:00', '11:45', 'AGENDADA', 'Control cardiológico', NULL),
    (1000000007, 1000000012, '2026-06-05', '14:00', '14:30', 'AGENDADA', 'Consulta dermatológica', NULL),
    (1000000008, 1000000016, '2026-06-06', '08:00', '08:30', 'AGENDADA', 'Chequeo general', NULL);

-- Citas canceladas (para probar diferentes estados)
INSERT INTO appointments (appt_doct_id, appt_pat_id, appt_date, appt_start_time, appt_end_time, appt_status, appt_reason, appt_notes)
VALUES
    (1000000002, 1000000017, '2026-05-25', '15:00', '15:30', 'CANCELADA', 'Consulta general', 'Paciente canceló por motivos personales'),
    (1000000005, 1000000010, '2026-05-26', '11:00', '11:20', 'CANCELADA', 'Control', 'Reprogramada para otra fecha');

-- ============================================================
-- CITAS EXTENDIDAS — semanas de junio 2026 para pruebas completas
-- Doctores: 1000000002 Juan Pérez, 1000000008 Miguel Castro,
--           1000000005 Ana Martínez, 1000000006 Pedro Gómez,
--           1000000007 Laura Torres
-- ============================================================

-- Semana 09-13 Jun 2026
INSERT INTO appointments (appt_doct_id, appt_pat_id, appt_date, appt_start_time, appt_end_time, appt_status, appt_reason, appt_notes)
VALUES
    -- Miguel Castro (Med. General) — lunes 09 Jun
    (1000000008, 1000000010, '2026-06-09', '08:00', '08:30', 'AGENDADA', 'Consulta general', NULL),
    (1000000008, 1000000011, '2026-06-09', '08:30', '09:00', 'AGENDADA', 'Control tensión arterial', NULL),
    (1000000008, 1000000012, '2026-06-09', '09:00', '09:30', 'AGENDADA', 'Chequeo rutinario', NULL),
    (1000000008, 1000000014, '2026-06-09', '09:30', '10:00', 'AGENDADA', 'Dolor de cabeza persistente', NULL),
    (1000000008, 1000000015, '2026-06-09', '10:00', '10:30', 'AGENDADA', 'Seguimiento post-consulta', NULL),
    -- Miguel Castro — martes 10 Jun
    (1000000008, 1000000016, '2026-06-10', '08:00', '08:30', 'AGENDADA', 'Primera consulta', NULL),
    (1000000008, 1000000017, '2026-06-10', '08:30', '09:00', 'AGENDADA', 'Consulta general', NULL),
    (1000000008, 1000000004, '2026-06-10', '09:00', '09:30', 'AGENDADA', 'Control mensual', NULL),
    -- Miguel Castro — miércoles 11 Jun
    (1000000008, 1000000010, '2026-06-11', '08:00', '08:30', 'AGENDADA', 'Seguimiento', NULL),
    -- Miguel Castro — jueves 12 Jun
    (1000000008, 1000000011, '2026-06-12', '08:00', '08:30', 'AGENDADA', 'Control', NULL),
    (1000000008, 1000000012, '2026-06-12', '09:00', '09:30', 'AGENDADA', 'Consulta general', NULL),

    -- Juan Pérez (Med. General) — semana 09-13 Jun
    (1000000002, 1000000015, '2026-06-09', '08:00', '08:30', 'AGENDADA', 'Dolor abdominal', NULL),
    (1000000002, 1000000016, '2026-06-09', '08:30', '09:00', 'AGENDADA', 'Chequeo anual', NULL),
    (1000000002, 1000000017, '2026-06-10', '09:00', '09:30', 'AGENDADA', 'Control general', NULL),
    (1000000002, 1000000004, '2026-06-11', '10:00', '10:30', 'AGENDADA', 'Gripe', NULL),
    (1000000002, 1000000010, '2026-06-12', '11:00', '11:30', 'AGENDADA', 'Seguimiento', NULL),

    -- Ana Martínez (Pediatría) — semana 09-13 Jun
    (1000000005, 1000000013, '2026-06-09', '08:00', '08:20', 'AGENDADA', 'Control de crecimiento', NULL),
    (1000000005, 1000000017, '2026-06-10', '08:30', '08:50', 'AGENDADA', 'Vacunación', NULL),
    (1000000005, 1000000013, '2026-06-12', '09:00', '09:20', 'AGENDADA', 'Control mensual', NULL),

    -- Pedro Gómez (Cardiología) — semana 09-13 Jun
    (1000000006, 1000000011, '2026-06-09', '10:00', '10:45', 'AGENDADA', 'Evaluación cardiológica', NULL),
    (1000000006, 1000000014, '2026-06-11', '09:00', '09:45', 'AGENDADA', 'Control post-procedimiento', NULL),

    -- Laura Torres (Dermatología) — semana 09-13 Jun
    (1000000007, 1000000012, '2026-06-10', '08:00', '08:30', 'AGENDADA', 'Revisión lunar', NULL),
    (1000000007, 1000000015, '2026-06-12', '09:00', '09:30', 'AGENDADA', 'Acné severo', NULL);

-- Semana 16-20 Jun 2026
INSERT INTO appointments (appt_doct_id, appt_pat_id, appt_date, appt_start_time, appt_end_time, appt_status, appt_reason, appt_notes)
VALUES
    (1000000008, 1000000010, '2026-06-16', '08:00', '08:30', 'AGENDADA', 'Consulta general', NULL),
    (1000000008, 1000000011, '2026-06-16', '09:00', '09:30', 'AGENDADA', 'Control tensión', NULL),
    (1000000008, 1000000004, '2026-06-17', '08:30', '09:00', 'AGENDADA', 'Seguimiento', NULL),
    (1000000002, 1000000016, '2026-06-16', '10:00', '10:30', 'AGENDADA', 'Dolor de espalda', NULL),
    (1000000002, 1000000012, '2026-06-17', '09:00', '09:30', 'AGENDADA', 'Chequeo general', NULL),
    (1000000005, 1000000013, '2026-06-16', '08:00', '08:20', 'AGENDADA', 'Control pediátrico', NULL),
    (1000000006, 1000000011, '2026-06-18', '10:00', '10:45', 'AGENDADA', 'Electrocardiograma', NULL),
    (1000000007, 1000000014, '2026-06-19', '09:00', '09:30', 'AGENDADA', 'Dermatitis', NULL);
