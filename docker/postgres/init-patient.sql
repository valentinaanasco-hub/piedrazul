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
