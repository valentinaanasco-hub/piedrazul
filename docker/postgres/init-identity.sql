-- ============================================================
-- Identity Service — Script de inicialización
-- Base de datos: identity_db
-- Red de Servicios Médicos de Piedrazul
-- ============================================================

CREATE TABLE IF NOT EXISTS roles (
    role_id SERIAL,
    role_name VARCHAR(50) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (role_id),
    CONSTRAINT uk_role_name UNIQUE (role_name),
    CONSTRAINT ck_role_name_valid CHECK (role_name IN ('ADMIN', 'DOCTOR', 'PACIENTE', 'AGENDADOR'))
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT,
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
    ur_user_id BIGINT NOT NULL,
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
-- ADMINISTRADOR
-- ============================================================
INSERT INTO users (user_id, user_username, user_password, user_first_name, user_first_surname, user_state, user_type_id)
VALUES (1000000001, 'admin@piedrazul.com', 'admin123', 'Admin', 'Sistema', 'ACTIVO', 'CC')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000001, role_id FROM roles WHERE role_name = 'ADMIN' ON CONFLICT DO NOTHING;

-- ============================================================
-- AGENDADORES
-- ============================================================
INSERT INTO users (user_id, user_username, user_password, user_first_name, user_first_surname, user_last_name, user_state, user_type_id)
VALUES
    (1000000002, 'agendador@piedrazul.com',  'agendador123', 'Sofía',  'Aguilar', 'Restrepo', 'ACTIVO', 'CC'),
    (1000000003, 'agendador2@piedrazul.com', 'agendador123', 'Marco',  'Suárez',  'Vargas',   'ACTIVO', 'CC')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000002, role_id FROM roles WHERE role_name = 'AGENDADOR' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 1000000003, role_id FROM roles WHERE role_name = 'AGENDADOR' ON CONFLICT DO NOTHING;

-- ============================================================
-- PROFESIONALES DE SALUD
--   Terapia Neural : Clara Inés Córdoba, José Ignacio García,
--                    Ibis González, Christian González
--   Fisioterapia   : Zarama Velasco
--   Quiropraxia    : Armando Peña
-- ============================================================
INSERT INTO users (user_id, user_username, user_password, user_first_name, user_middle_name, user_first_surname, user_last_name, user_state, user_type_id)
VALUES
    (2000000001, 'clara.cordoba@piedrazul.com',    'doctor123', 'Clara',    'Inés',     'Córdoba',  NULL,      'ACTIVO', 'CC'),
    (2000000002, 'jose.garcia@piedrazul.com',      'doctor123', 'José',     'Ignacio',  'García',   NULL,      'ACTIVO', 'CC'),
    (2000000003, 'ibis.gonzalez@piedrazul.com',    'doctor123', 'Ibis',     NULL,       'González', NULL,      'ACTIVO', 'CC'),
    (2000000004, 'christian.gonzalez@piedrazul.com','doctor123','Christian', NULL,      'González', NULL,      'ACTIVO', 'CC'),
    (2000000005, 'zarama.velasco@piedrazul.com',   'doctor123', 'Zarama',   NULL,       'Velasco',  NULL,      'ACTIVO', 'CC'),
    (2000000006, 'armando.pena@piedrazul.com',     'doctor123', 'Armando',  NULL,       'Peña',     NULL,      'ACTIVO', 'CC')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 2000000001, role_id FROM roles WHERE role_name = 'DOCTOR' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 2000000002, role_id FROM roles WHERE role_name = 'DOCTOR' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 2000000003, role_id FROM roles WHERE role_name = 'DOCTOR' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 2000000004, role_id FROM roles WHERE role_name = 'DOCTOR' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 2000000005, role_id FROM roles WHERE role_name = 'DOCTOR' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 2000000006, role_id FROM roles WHERE role_name = 'DOCTOR' ON CONFLICT DO NOTHING;

-- ============================================================
-- PACIENTES — paciente1@gmail.com … paciente10@gmail.com
--             contraseña: paciente123
-- ============================================================
INSERT INTO users (user_id, user_username, user_password, user_first_name, user_middle_name, user_first_surname, user_last_name, user_state, user_type_id)
VALUES
    (1094567890, 'paciente1@gmail.com',  'paciente123', 'Ana',       'María',    'García',     'López',   'ACTIVO', 'CC'),
    (1023456789, 'paciente2@gmail.com',  'paciente123', 'Carlos',    'Eduardo',  'Rodríguez',  'Torres',  'ACTIVO', 'CC'),
    (52345678,   'paciente3@gmail.com',  'paciente123', 'María',     'Fernanda', 'Martínez',   'Sánchez', 'ACTIVO', 'CC'),
    (71890234,   'paciente4@gmail.com',  'paciente123', 'Luis',      'Alejandro','Hernández',  'Díaz',    'ACTIVO', 'CC'),
    (1067890123, 'paciente5@gmail.com',  'paciente123', 'Laura',     'Sofía',    'Pérez',      'Gómez',   'ACTIVO', 'CC'),
    (43567891,   'paciente6@gmail.com',  'paciente123', 'Pedro',     'José',     'López',      'Castro',  'ACTIVO', 'CC'),
    (1012345678, 'paciente7@gmail.com',  'paciente123', 'Sofía',     'Valentina','Ramírez',    'Ortiz',   'ACTIVO', 'CC'),
    (1056789012, 'paciente8@gmail.com',  'paciente123', 'Andrés',    'Felipe',   'Silva',      'Moreno',  'ACTIVO', 'CC'),
    (1078901234, 'paciente9@gmail.com',  'paciente123', 'Valentina', 'Andrea',   'Ruiz',       'Jiménez', 'ACTIVO', 'CC'),
    (1090123456, 'paciente10@gmail.com', 'paciente123', 'Sebastián', 'Camilo',   'Mora',       'Vargas',  'ACTIVO', 'CC')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 1094567890, role_id FROM roles WHERE role_name = 'PACIENTE' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 1023456789, role_id FROM roles WHERE role_name = 'PACIENTE' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 52345678,   role_id FROM roles WHERE role_name = 'PACIENTE' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 71890234,   role_id FROM roles WHERE role_name = 'PACIENTE' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 1067890123, role_id FROM roles WHERE role_name = 'PACIENTE' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 43567891,   role_id FROM roles WHERE role_name = 'PACIENTE' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 1012345678, role_id FROM roles WHERE role_name = 'PACIENTE' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 1056789012, role_id FROM roles WHERE role_name = 'PACIENTE' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 1078901234, role_id FROM roles WHERE role_name = 'PACIENTE' ON CONFLICT DO NOTHING;
INSERT INTO users_roles (ur_user_id, ur_role_id) SELECT 1090123456, role_id FROM roles WHERE role_name = 'PACIENTE' ON CONFLICT DO NOTHING;

-- ============================================================
-- PACIENTES DEMO — 360 pacientes con nombres y cédulas colombianas
-- Cédulas 8 dígitos: 10,248,579 – 99,488,440  (fórmula n×248579+10M)
-- Sin cuenta Keycloak; existen para ocupar slots de agenda.
-- ============================================================
WITH nombres AS (
    SELECT
        ARRAY[
            'Ana','Beatriz','Carmen','Diana','Elena','Francisca','Gloria',
            'Helena','Isabel','Johanna','Karen','Laura','María','Natalia',
            'Olga','Patricia','Rosa','Sandra','Teresa','Valentina',
            'Adriana','Bibiana','Catalina','Daniela','Esperanza',
            'Fernanda','Gabriela','Ingrid','Juliana','Kelly'
        ] AS f_nombres,
        ARRAY[
            'Andrés','Carlos','David','Eduardo','Felipe','Gabriel',
            'Hernán','Iván','Javier','Juan','Luis','Manuel','Nicolás',
            'Óscar','Pablo','Ricardo','Santiago','Tomás','Víctor','William',
            'Alejandro','Camilo','Diego','Fernando','Gustavo',
            'Héctor','Jorge','Kevin','Leonardo','Mauricio'
        ] AS m_nombres,
        ARRAY[
            'García','Rodríguez','Martínez','López','González','Pérez',
            'Sánchez','Romero','Torres','Flores','Ramírez','Gómez','Díaz',
            'Vargas','Castro','Moreno','Jiménez','Ruiz','Herrera','Medina',
            'Ríos','Aguilar','Blanco','Cabrera','Cárdenas','Córdoba',
            'Espinosa','Figueroa','Guerrero','Henao','Ibáñez','Jaramillo',
            'Lara','Molina','Navarro','Ortega','Pineda','Quintero',
            'Restrepo','Salazar'
        ] AS apellidos
)
INSERT INTO users (user_id, user_username, user_password, user_first_name, user_first_surname, user_last_name, user_state, user_type_id)
SELECT
    10000000 + n * 248579,
    'demo' || n || '@piedrazul.demo',
    'demo',
    CASE WHEN n % 2 = 1
         THEN f_nombres[((n - 1) / 2 % 30) + 1]
         ELSE m_nombres[((n / 2 - 1) % 30) + 1]
    END,
    apellidos[((n - 1) % 40) + 1],
    apellidos[(n % 40) + 1],
    'ACTIVO',
    'CC'
FROM generate_series(1, 360) AS n, nombres
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users_roles (ur_user_id, ur_role_id)
SELECT 10000000 + n * 248579, (SELECT role_id FROM roles WHERE role_name = 'PACIENTE')
FROM generate_series(1, 360) AS n
ON CONFLICT DO NOTHING;
