-- ============================================================
-- Medical Staff Service — Script de inicialización
-- Base de datos: medical_db
-- ============================================================

CREATE TABLE IF NOT EXISTS doctors (
    doct_user_id         INTEGER      NOT NULL,
    doct_professional_id VARCHAR(50)  NOT NULL,
    doct_first_name      VARCHAR(100) NOT NULL,
    doct_first_surname   VARCHAR(100) NOT NULL,
    CONSTRAINT pk_doctors PRIMARY KEY (doct_user_id),
    CONSTRAINT uk_doct_professional_id UNIQUE (doct_professional_id)
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

CREATE TABLE IF NOT EXISTS system_parameters (
    parameter_key VARCHAR(100),
    parameter_value TEXT NOT NULL,
    CONSTRAINT pk_system_parameters PRIMARY KEY (parameter_key)
);
