-- ============================================================
-- Script de Verificación de Datos de Prueba
-- Ejecutar en cada base de datos para verificar la carga
-- ============================================================

-- ============================================================
-- IDENTITY SERVICE (identity_db)
-- ============================================================
\c identity_db

SELECT '=== USUARIOS POR ROL ===' AS info;
SELECT r.role_name, COUNT(ur.ur_user_id) as total_usuarios
FROM roles r
LEFT JOIN users_roles ur ON r.role_id = ur.ur_role_id
GROUP BY r.role_name
ORDER BY r.role_name;

SELECT '=== TODOS LOS USUARIOS ===' AS info;
SELECT u.user_id, u.user_username, u.user_first_name, u.user_first_surname, u.user_state, r.role_name
FROM users u
JOIN users_roles ur ON u.user_id = ur.ur_user_id
JOIN roles r ON ur.ur_role_id = r.role_id
ORDER BY r.role_name, u.user_id;

-- ============================================================
-- PATIENT SERVICE (patient_db)
-- ============================================================
\c patient_db

SELECT '=== TOTAL PACIENTES ===' AS info;
SELECT COUNT(*) as total_pacientes FROM patients;

SELECT '=== PACIENTES POR GÉNERO ===' AS info;
SELECT pat_gender, COUNT(*) as total
FROM patients
GROUP BY pat_gender;

SELECT '=== TODOS LOS PACIENTES ===' AS info;
SELECT pat_user_id, pat_email, pat_phone, pat_gender, 
       CONCAT(pat_birth_day, '/', pat_birth_month, '/', pat_birth_year) as fecha_nacimiento
FROM patients
ORDER BY pat_user_id;

-- ============================================================
-- MEDICAL STAFF SERVICE (medical_db)
-- ============================================================
\c medical_db

SELECT '=== TOTAL DOCTORES ===' AS info;
SELECT COUNT(*) as total_doctores FROM doctors;

SELECT '=== ESPECIALIDADES DISPONIBLES ===' AS info;
SELECT spec_id, spec_name FROM specialties ORDER BY spec_name;

SELECT '=== DOCTORES CON ESPECIALIDADES ===' AS info;
SELECT d.doct_user_id, d.doct_first_name, d.doct_first_surname, 
       d.doct_professional_id, s.spec_name
FROM doctors d
JOIN doctor_specialties ds ON d.doct_user_id = ds.ds_doct_id
JOIN specialties s ON ds.ds_spec_id = s.spec_id
ORDER BY s.spec_name, d.doct_first_name;

SELECT '=== DOCTORES POR ESPECIALIDAD ===' AS info;
SELECT s.spec_name, COUNT(ds.ds_doct_id) as total_doctores
FROM specialties s
LEFT JOIN doctor_specialties ds ON s.spec_id = ds.ds_spec_id
GROUP BY s.spec_name
ORDER BY total_doctores DESC, s.spec_name;

-- ============================================================
-- CONFIGURATION SERVICE (configuration_db)
-- ============================================================
\c configuration_db

SELECT '=== PARÁMETROS DEL SISTEMA ===' AS info;
SELECT parameter_key, parameter_value, parameter_description
FROM system_parameters;

SELECT '=== DOCTORES CON CONFIGURACIÓN ===' AS info;
SELECT doctor_id, COUNT(*) as dias_configurados
FROM doctor_schedule_configurations
GROUP BY doctor_id
ORDER BY doctor_id;

SELECT '=== CONFIGURACIONES DE HORARIOS ===' AS info;
SELECT doctor_id, day_of_week, start_time, end_time, interval_minutes
FROM doctor_schedule_configurations
ORDER BY doctor_id, day_of_week;

-- ============================================================
-- APPOINTMENT SERVICE (appointment_db)
-- ============================================================
\c appointment_db

SELECT '=== TOTAL CITAS ===' AS info;
SELECT COUNT(*) as total_citas FROM appointments;

SELECT '=== CITAS POR ESTADO ===' AS info;
SELECT appt_status, COUNT(*) as total
FROM appointments
GROUP BY appt_status
ORDER BY appt_status;

SELECT '=== CITAS POR DOCTOR ===' AS info;
SELECT appt_doct_id, COUNT(*) as total_citas
FROM appointments
GROUP BY appt_doct_id
ORDER BY appt_doct_id;

SELECT '=== CITAS AGENDADAS (FUTURAS) ===' AS info;
SELECT appt_id, appt_doct_id, appt_pat_id, appt_date, appt_start_time, appt_status, appt_reason
FROM appointments
WHERE appt_status = 'AGENDADA'
ORDER BY appt_date, appt_start_time;

SELECT '=== CITAS ATENDIDAS ===' AS info;
SELECT appt_id, appt_doct_id, appt_pat_id, appt_date, appt_start_time, appt_status, appt_reason
FROM appointments
WHERE appt_status = 'ATENDIDA'
ORDER BY appt_date DESC, appt_start_time DESC;

SELECT '=== RESUMEN GENERAL ===' AS info;
SELECT 
    (SELECT COUNT(*) FROM appointments WHERE appt_status = 'AGENDADA') as citas_agendadas,
    (SELECT COUNT(*) FROM appointments WHERE appt_status = 'ATENDIDA') as citas_atendidas,
    (SELECT COUNT(*) FROM appointments WHERE appt_status = 'CANCELADA') as citas_canceladas,
    (SELECT COUNT(*) FROM appointments) as total_citas;
