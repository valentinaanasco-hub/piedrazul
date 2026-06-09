-- ============================================================
-- Patient Service — Script de inicialización
-- Base de datos: patient_db
-- Red de Servicios Médicos de Piedrazul
-- ============================================================

CREATE TABLE IF NOT EXISTS patients (
    pat_user_id    BIGINT,
    pat_phone      VARCHAR(20)  NOT NULL,
    pat_gender     VARCHAR(20)  NOT NULL,
    pat_birth_day  VARCHAR(2),
    pat_birth_month VARCHAR(2),
    pat_birth_year VARCHAR(4),
    pat_email      VARCHAR(150),
    CONSTRAINT pk_patients        PRIMARY KEY (pat_user_id),
    CONSTRAINT ck_pat_gender_valid CHECK (pat_gender IN ('Hombre', 'Mujer', 'Otro'))
);

-- ============================================================
-- PACIENTES — coinciden con identity_db (user_id = documento CC)
-- ============================================================
INSERT INTO patients (pat_user_id, pat_phone, pat_gender, pat_birth_day, pat_birth_month, pat_birth_year, pat_email)
VALUES
    (1094567890, '3101234501', 'Mujer',  '14', '03', '1992', 'paciente1@gmail.com'),
    (1023456789, '3101234502', 'Hombre', '22', '07', '1988', 'paciente2@gmail.com'),
    (52345678,   '3101234503', 'Mujer',  '05', '11', '1995', 'paciente3@gmail.com'),
    (71890234,   '3101234504', 'Hombre', '30', '01', '1980', 'paciente4@gmail.com'),
    (1067890123, '3101234505', 'Mujer',  '18', '09', '2000', 'paciente5@gmail.com'),
    (43567891,   '3101234506', 'Hombre', '09', '06', '1975', 'paciente6@gmail.com'),
    (1012345678, '3101234507', 'Mujer',  '27', '04', '1997', 'paciente7@gmail.com'),
    (1056789012, '3101234508', 'Hombre', '12', '12', '1985', 'paciente8@gmail.com'),
    (1078901234, '3101234509', 'Mujer',  '03', '08', '1993', 'paciente9@gmail.com'),
    (1090123456, '3101234510', 'Hombre', '21', '02', '1990', 'paciente10@gmail.com')
ON CONFLICT (pat_user_id) DO NOTHING;

-- ============================================================
-- PACIENTES DEMO — 360 pacientes con cédulas colombianas reales
-- ============================================================
INSERT INTO patients (pat_user_id, pat_phone, pat_gender, pat_birth_day, pat_birth_month, pat_birth_year, pat_email)
SELECT
    10000000 + n * 248579,
    '310' || LPAD(n::text, 7, '0'),
    CASE WHEN n % 2 = 1 THEN 'Mujer' ELSE 'Hombre' END,
    LPAD((n % 28 + 1)::text, 2, '0'),
    LPAD((n % 12 + 1)::text, 2, '0'),
    (1960 + n % 50)::text,
    'demo' || n || '@piedrazul.demo'
FROM generate_series(1, 360) AS n
ON CONFLICT (pat_user_id) DO NOTHING;
