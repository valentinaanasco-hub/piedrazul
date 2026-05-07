-- ============================================================
-- Piedrazul - Script de inicialización de base de datos
-- Se ejecuta automáticamente al levantar el contenedor Docker
-- ============================================================

CREATE TABLE IF NOT EXISTS roles (
    role_id SERIAL,
    role_name VARCHAR(50) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (role_id),
    CONSTRAINT uk_role_name UNIQUE (role_name),
    CONSTRAINT ck_role_name_valid CHECK (role_name IN ('ADMIN', 'DOCTOR', 'PACIENTE', 'AGENDADOR'))
);

CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER,
    user_username VARCHAR(150) NOT NULL,
    user_password VARCHAR(255) NOT NULL,
    user_first_name VARCHAR(100) NOT NULL,
    user_middle_name VARCHAR(100),
    user_first_surname VARCHAR(100) NOT NULL,
    user_last_name VARCHAR(100),
    user_state VARCHAR(20) DEFAULT 'ACTIVO',
    user_type_id VARCHAR(5) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uk_user_username UNIQUE (user_username),
    CONSTRAINT ck_user_id_positive CHECK (user_id > 0),
    CONSTRAINT ck_user_type_id CHECK (user_type_id IN ('CC', 'TI', 'CE', 'PA', 'RC')),
    CONSTRAINT ck_user_state_valid CHECK (user_state IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE IF NOT EXISTS users_roles (
    ur_user_id INTEGER NOT NULL,
    ur_role_id INTEGER NOT NULL,
    CONSTRAINT pk_users_roles PRIMARY KEY (ur_user_id, ur_role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (ur_user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (ur_role_id) REFERENCES roles (role_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS doctors (
    doct_user_id INTEGER,
    doct_professional_id VARCHAR(50) NOT NULL,
    CONSTRAINT pk_doctors PRIMARY KEY (doct_user_id),
    CONSTRAINT uk_doct_professional_id UNIQUE (doct_professional_id),
    CONSTRAINT fk_doct_user FOREIGN KEY (doct_user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS specialties (
    spec_id SERIAL,
    spec_name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_specialties PRIMARY KEY (spec_id),
    CONSTRAINT uk_spec_name UNIQUE (spec_name)
);

CREATE TABLE IF NOT EXISTS doctor_specialties (
    ds_doct_id INTEGER NOT NULL,
    ds_spec_id INTEGER NOT NULL,
    CONSTRAINT pk_doctor_specialties PRIMARY KEY (ds_doct_id, ds_spec_id),
    CONSTRAINT fk_ds_doct FOREIGN KEY (ds_doct_id) REFERENCES doctors (doct_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_ds_spec FOREIGN KEY (ds_spec_id) REFERENCES specialties (spec_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS patients (
    pat_user_id INTEGER,
    pat_phone VARCHAR(20) NOT NULL,
    pat_gender VARCHAR(20) NOT NULL,
    pat_birth_day VARCHAR(2),
    pat_birth_month VARCHAR(2),
    pat_birth_year VARCHAR(4),
    pat_email VARCHAR(150),
    CONSTRAINT pk_patients PRIMARY KEY (pat_user_id),
    CONSTRAINT fk_pat_user FOREIGN KEY (pat_user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT ck_pat_gender_valid CHECK (pat_gender IN ('Hombre', 'Mujer', 'Otro'))
);

CREATE TABLE IF NOT EXISTS doctor_schedules (
    sched_id SERIAL,
    sched_doctor_id INTEGER NOT NULL,
    sched_day_of_week INTEGER NOT NULL,
    sched_start_time TIME NOT NULL,
    sched_end_time TIME NOT NULL,
    sched_interval_minutes INTEGER NOT NULL,
    CONSTRAINT pk_doctor_schedules PRIMARY KEY (sched_id),
    CONSTRAINT fk_sched_doctor FOREIGN KEY (sched_doctor_id) REFERENCES doctors (doct_user_id) ON DELETE CASCADE,
    CONSTRAINT ck_sched_day_valid CHECK (sched_day_of_week BETWEEN 1 AND 7),
    CONSTRAINT ck_sched_interval_positive CHECK (sched_interval_minutes > 0)
);

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
    CONSTRAINT fk_appt_doct FOREIGN KEY (appt_doct_id) REFERENCES doctors (doct_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_appt_pat FOREIGN KEY (appt_pat_id) REFERENCES patients (pat_user_id) ON DELETE CASCADE,
    CONSTRAINT ck_appt_status_valid CHECK (appt_status IN ('AGENDADA', 'CANCELADA', 'ATENDIDA'))
);

CREATE TABLE IF NOT EXISTS system_parameters (
    parameter_key VARCHAR(100),
    parameter_value TEXT NOT NULL,
    CONSTRAINT pk_system_parameters PRIMARY KEY (parameter_key)
);

INSERT INTO roles (role_name) SELECT 'ADMIN'     WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'ADMIN');
INSERT INTO roles (role_name) SELECT 'DOCTOR'    WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'DOCTOR');
INSERT INTO roles (role_name) SELECT 'PACIENTE'  WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'PACIENTE');
INSERT INTO roles (role_name) SELECT 'AGENDADOR' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'AGENDADOR');
