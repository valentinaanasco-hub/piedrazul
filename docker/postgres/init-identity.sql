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

-- ============================================================
-- DATOS DE PRUEBA
-- Contraseñas en texto plano (el servicio tiene compatibilidad)
-- ============================================================

-- Usuario ADMIN (password: admin123)
INSERT INTO users (user_id, user_username, user_password, user_first_name, user_first_surname, user_state, user_type_id)
VALUES (1000000001, 'admin@piedrazul.com', 'admin123', 'Admin', 'Sistema', 'ACTIVO', 'CC')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000001, role_id FROM roles WHERE role_name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Usuario DOCTOR (password: doctor123)
INSERT INTO users (user_id, user_username, user_password, user_first_name, user_first_surname, user_state, user_type_id)
VALUES (1000000002, 'doctor@piedrazul.com', 'doctor123', 'Juan', 'Pérez', 'ACTIVO', 'CC')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000002, role_id FROM roles WHERE role_name = 'DOCTOR'
ON CONFLICT DO NOTHING;

-- Usuario AGENDADOR (password: agendador123)
INSERT INTO users (user_id, user_username, user_password, user_first_name, user_first_surname, user_state, user_type_id)
VALUES (1000000003, 'agendador@piedrazul.com', 'agendador123', 'María', 'González', 'ACTIVO', 'CC')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000003, role_id FROM roles WHERE role_name = 'AGENDADOR'
ON CONFLICT DO NOTHING;

-- Usuario PACIENTE (password: paciente123)
INSERT INTO users (user_id, user_username, user_password, user_first_name, user_first_surname, user_state, user_type_id)
VALUES (1000000004, 'paciente@piedrazul.com', 'paciente123', 'Carlos', 'Rodríguez', 'ACTIVO', 'CC')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000004, role_id FROM roles WHERE role_name = 'PACIENTE'
ON CONFLICT DO NOTHING;

-- ============================================================
-- DATOS DE PRUEBA ADICIONALES
-- ============================================================

-- Doctores adicionales
INSERT INTO users (user_id, user_username, user_password, user_first_name, user_middle_name, user_first_surname, user_last_name, user_state, user_type_id)
VALUES 
    (1000000005, 'ana.martinez@piedrazul.com', 'doctor123', 'Ana', 'María', 'Martínez', 'López', 'ACTIVO', 'CC'),
    (1000000006, 'pedro.gomez@piedrazul.com', 'doctor123', 'Pedro', 'Luis', 'Gómez', 'Ramírez', 'ACTIVO', 'CC'),
    (1000000007, 'laura.torres@piedrazul.com', 'doctor123', 'Laura', 'Isabel', 'Torres', 'Sánchez', 'ACTIVO', 'CC'),
    (1000000008, 'miguel.castro@piedrazul.com', 'doctor123', 'Miguel', 'Ángel', 'Castro', 'Vargas', 'ACTIVO', 'CC')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000005, role_id FROM roles WHERE role_name = 'DOCTOR'
ON CONFLICT DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000006, role_id FROM roles WHERE role_name = 'DOCTOR'
ON CONFLICT DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000007, role_id FROM roles WHERE role_name = 'DOCTOR'
ON CONFLICT DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000008, role_id FROM roles WHERE role_name = 'DOCTOR'
ON CONFLICT DO NOTHING;

-- Pacientes adicionales
INSERT INTO users (user_id, user_username, user_password, user_first_name, user_middle_name, user_first_surname, user_last_name, user_state, user_type_id)
VALUES 
    (1000000010, 'maria.lopez@ejemplo.com', 'paciente123', 'María', 'Elena', 'López', 'García', 'ACTIVO', 'CC'),
    (1000000011, 'jose.hernandez@ejemplo.com', 'paciente123', 'José', 'Antonio', 'Hernández', 'Díaz', 'ACTIVO', 'CC'),
    (1000000012, 'sofia.ramirez@ejemplo.com', 'paciente123', 'Sofía', 'Carolina', 'Ramírez', 'Moreno', 'ACTIVO', 'CC'),
    (1000000013, 'andres.silva@ejemplo.com', 'paciente123', 'Andrés', 'Felipe', 'Silva', 'Rojas', 'ACTIVO', 'TI'),
    (1000000014, 'camila.ortiz@ejemplo.com', 'paciente123', 'Camila', 'Andrea', 'Ortiz', 'Mendoza', 'ACTIVO', 'CC'),
    (1000000015, 'diego.morales@ejemplo.com', 'paciente123', 'Diego', 'Alejandro', 'Morales', 'Cruz', 'ACTIVO', 'CC'),
    (1000000016, 'valentina.ruiz@ejemplo.com', 'paciente123', 'Valentina', 'Isabel', 'Ruiz', 'Jiménez', 'ACTIVO', 'CC'),
    (1000000017, 'santiago.pena@ejemplo.com', 'paciente123', 'Santiago', 'David', 'Peña', 'Vega', 'ACTIVO', 'TI')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000010, role_id FROM roles WHERE role_name = 'PACIENTE'
ON CONFLICT DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000011, role_id FROM roles WHERE role_name = 'PACIENTE'
ON CONFLICT DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000012, role_id FROM roles WHERE role_name = 'PACIENTE'
ON CONFLICT DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000013, role_id FROM roles WHERE role_name = 'PACIENTE'
ON CONFLICT DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000014, role_id FROM roles WHERE role_name = 'PACIENTE'
ON CONFLICT DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000015, role_id FROM roles WHERE role_name = 'PACIENTE'
ON CONFLICT DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000016, role_id FROM roles WHERE role_name = 'PACIENTE'
ON CONFLICT DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000017, role_id FROM roles WHERE role_name = 'PACIENTE'
ON CONFLICT DO NOTHING;

-- Agendadores adicionales
INSERT INTO users (user_id, user_username, user_password, user_first_name, user_first_surname, user_state, user_type_id)
VALUES 
    (1000000020, 'lucia.fernandez@piedrazul.com', 'agendador123', 'Lucía', 'Fernández', 'ACTIVO', 'CC'),
    (1000000021, 'roberto.mendez@piedrazul.com', 'agendador123', 'Roberto', 'Méndez', 'ACTIVO', 'CC')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000020, role_id FROM roles WHERE role_name = 'AGENDADOR'
ON CONFLICT DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000021, role_id FROM roles WHERE role_name = 'AGENDADOR'
ON CONFLICT DO NOTHING;
