-- ============================================================
-- Patient Service — Script de inicialización
-- Base de datos: patient_db
-- ============================================================

CREATE TABLE IF NOT EXISTS patients (
    pat_user_id INTEGER,
    pat_phone VARCHAR(20) NOT NULL,
    pat_gender VARCHAR(20) NOT NULL,
    pat_birth_day VARCHAR(2),
    pat_birth_month VARCHAR(2),
    pat_birth_year VARCHAR(4),
    pat_email VARCHAR(150),
    CONSTRAINT pk_patients PRIMARY KEY (pat_user_id),
    CONSTRAINT ck_pat_gender_valid CHECK (pat_gender IN ('Hombre', 'Mujer', 'Otro'))
);

-- ============================================================
-- DATOS DE PRUEBA
-- ============================================================

-- Paciente de prueba (debe coincidir con el usuario PACIENTE de identity-service)
INSERT INTO patients (pat_user_id, pat_phone, pat_gender, pat_birth_day, pat_birth_month, pat_birth_year, pat_email)
VALUES (1000000004, '3001234567', 'Hombre', '15', '06', '1990', 'paciente@piedrazul.com')
ON CONFLICT (pat_user_id) DO NOTHING;

-- ============================================================
-- PACIENTES ADICIONALES
-- ============================================================

INSERT INTO patients (pat_user_id, pat_phone, pat_gender, pat_birth_day, pat_birth_month, pat_birth_year, pat_email)
VALUES 
    (1000000010, '3101234567', 'Mujer', '22', '03', '1985', 'maria.lopez@ejemplo.com'),
    (1000000011, '3201234567', 'Hombre', '10', '11', '1978', 'jose.hernandez@ejemplo.com'),
    (1000000012, '3301234567', 'Mujer', '05', '07', '1992', 'sofia.ramirez@ejemplo.com'),
    (1000000013, '3401234567', 'Hombre', '18', '09', '2005', 'andres.silva@ejemplo.com'),
    (1000000014, '3501234567', 'Mujer', '30', '01', '1988', 'camila.ortiz@ejemplo.com'),
    (1000000015, '3601234567', 'Hombre', '12', '04', '1995', 'diego.morales@ejemplo.com'),
    (1000000016, '3701234567', 'Mujer', '25', '08', '2000', 'valentina.ruiz@ejemplo.com'),
    (1000000017, '3801234567', 'Hombre', '08', '12', '2008', 'santiago.pena@ejemplo.com')
ON CONFLICT (pat_user_id) DO NOTHING;
