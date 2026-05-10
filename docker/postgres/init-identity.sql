-- ============================================================
-- Identity Service — Script de inicialización
-- Base de datos: identity_db
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

INSERT INTO roles (role_name) SELECT 'ADMIN'     WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'ADMIN');
INSERT INTO roles (role_name) SELECT 'DOCTOR'    WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'DOCTOR');
INSERT INTO roles (role_name) SELECT 'PACIENTE'  WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'PACIENTE');
INSERT INTO roles (role_name) SELECT 'AGENDADOR' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'AGENDADOR');
