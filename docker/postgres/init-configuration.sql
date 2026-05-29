-- ============================================================
-- Configuration Service — Script de inicialización
-- Base de datos: configuration_db
-- ============================================================

CREATE TABLE IF NOT EXISTS system_parameters (
    parameter_key VARCHAR(100),
    parameter_value TEXT NOT NULL,
    parameter_description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_system_parameters PRIMARY KEY (parameter_key)
);

CREATE TABLE IF NOT EXISTS doctor_schedule_configurations (
    config_id SERIAL,
    doctor_id INTEGER NOT NULL,
    day_of_week INTEGER NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    interval_minutes INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_doctor_schedule_configurations PRIMARY KEY (config_id),
    CONSTRAINT ck_day_valid CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT ck_interval_positive CHECK (interval_minutes > 0)
);

-- Insertar parámetro por defecto: ventana de tiempo para agendar citas (4 semanas)
INSERT INTO system_parameters (parameter_key, parameter_value, parameter_description) 
VALUES ('appointment_window_weeks', '4', 'Ventana de tiempo en semanas para agendar citas')
ON CONFLICT (parameter_key) DO NOTHING;

-- ============================================================
-- DATOS DE PRUEBA
-- ============================================================

-- Configuración de horarios de ejemplo para el doctor de prueba
-- Lunes a Viernes, 8:00 AM - 6:00 PM, intervalos de 30 minutos
INSERT INTO doctor_schedule_configurations (doctor_id, day_of_week, start_time, end_time, interval_minutes)
VALUES 
    (1000000002, 1, '08:00', '18:00', 30),  -- Lunes
    (1000000002, 2, '08:00', '18:00', 30),  -- Martes
    (1000000002, 3, '08:00', '18:00', 30),  -- Miércoles
    (1000000002, 4, '08:00', '18:00', 30),  -- Jueves
    (1000000002, 5, '08:00', '18:00', 30);  -- Viernes

-- ============================================================
-- CONFIGURACIONES ADICIONALES PARA OTROS DOCTORES
-- ============================================================

-- Dra. Ana Martínez (Pediatría) - Lunes a Viernes, 7:00 AM - 3:00 PM, intervalos de 20 minutos
INSERT INTO doctor_schedule_configurations (doctor_id, day_of_week, start_time, end_time, interval_minutes)
VALUES 
    (1000000005, 1, '07:00', '15:00', 20),
    (1000000005, 2, '07:00', '15:00', 20),
    (1000000005, 3, '07:00', '15:00', 20),
    (1000000005, 4, '07:00', '15:00', 20),
    (1000000005, 5, '07:00', '15:00', 20);

-- Dr. Pedro Gómez (Cardiología) - Lunes, Miércoles, Viernes, 9:00 AM - 5:00 PM, intervalos de 45 minutos
INSERT INTO doctor_schedule_configurations (doctor_id, day_of_week, start_time, end_time, interval_minutes)
VALUES 
    (1000000006, 1, '09:00', '17:00', 45),
    (1000000006, 3, '09:00', '17:00', 45),
    (1000000006, 5, '09:00', '17:00', 45);

-- Dra. Laura Torres (Dermatología) - Martes y Jueves, 10:00 AM - 6:00 PM, intervalos de 30 minutos
INSERT INTO doctor_schedule_configurations (doctor_id, day_of_week, start_time, end_time, interval_minutes)
VALUES 
    (1000000007, 2, '10:00', '18:00', 30),
    (1000000007, 4, '10:00', '18:00', 30);

-- Dr. Miguel Castro (Medicina General) - Lunes a Sábado, 8:00 AM - 4:00 PM, intervalos de 30 minutos
INSERT INTO doctor_schedule_configurations (doctor_id, day_of_week, start_time, end_time, interval_minutes)
VALUES 
    (1000000008, 1, '08:00', '16:00', 30),
    (1000000008, 2, '08:00', '16:00', 30),
    (1000000008, 3, '08:00', '16:00', 30),
    (1000000008, 4, '08:00', '16:00', 30),
    (1000000008, 5, '08:00', '16:00', 30),
    (1000000008, 6, '08:00', '14:00', 30);  -- Sábado medio día
