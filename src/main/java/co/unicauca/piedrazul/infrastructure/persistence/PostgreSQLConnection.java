/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class PostgreSQLConnection {
        //Patron singleton
    private static Connection instance;
    
    public static Connection getConnection() throws SQLException{
        if(instance == null){
            String host = "localhost";
            String port = "5432";
            String db = "piedrazul_db";
            String user = "postgres";
            String pass = "postgres";
            
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
            try{
                instance = DriverManager.getConnection(url, user, pass);
                initDatabase(instance);
            }catch(SQLException e){
                System.err.println("Error: Asegurese que Postgress esté encendido");
                throw e;
            }
                    
        }
        return instance;
    }
    private static void initDatabase(Connection conn) {
        // Tabla de roles
        String createRoles = """
            CREATE TABLE IF NOT EXISTS roles (
                role_id SERIAL PRIMARY KEY,
                role_name TEXT NOT NULL UNIQUE CHECK (role_name IN ('ADMIN', 'DOCTOR', 'PACIENTE', 'AGENDADOR'))
            );
            """;

        // Tabla de usuarios base
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY CHECK (user_id > 0),
                user_username TEXT NOT NULL UNIQUE,
                user_password TEXT NOT NULL,
                user_first_name TEXT NOT NULL,
                user_middle_name TEXT,
                user_first_surname TEXT NOT NULL,
                user_last_name TEXT,
                user_state TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (user_state IN ('ACTIVO', 'INACTIVO'))
            );
            """;

        // Tabla intermedia para la relación N:M entre users y roles
        String createUsersRoles = """
            CREATE TABLE IF NOT EXISTS users_roles (
                ur_user_id INTEGER NOT NULL,
                ur_role_id INTEGER NOT NULL,
                PRIMARY KEY (ur_user_id, ur_role_id),
                CONSTRAINT fk_user FOREIGN KEY (ur_user_id) REFERENCES users (user_id) ON DELETE CASCADE,
                CONSTRAINT fk_role FOREIGN KEY (ur_role_id) REFERENCES roles (role_id) ON DELETE CASCADE
            );
            """;

        // Tabla de médicos, extiende users con datos profesionales
        String createDoctors = """
            CREATE TABLE IF NOT EXISTS doctors (
                doct_user_id INTEGER PRIMARY KEY,
                doct_professional_id TEXT NOT NULL UNIQUE,
                CONSTRAINT fk_doctor_user FOREIGN KEY (doct_user_id) REFERENCES users (user_id) ON DELETE CASCADE
            );
            """;

        // Tabla de especialidades médicas
        String createSpecialties = """
            CREATE TABLE IF NOT EXISTS specialties (
                spec_id SERIAL PRIMARY KEY,
                spec_name TEXT NOT NULL UNIQUE
            );
            """;

        // Tabla intermedia para la relación N:M entre médicos y especialidades
        String createDoctorSpecialties = """
            CREATE TABLE IF NOT EXISTS doctor_specialties (
                ds_doct_id INTEGER NOT NULL,
                ds_spec_id INTEGER NOT NULL,
                PRIMARY KEY (ds_doct_id, ds_spec_id),
                CONSTRAINT fk_ds_doctor FOREIGN KEY (ds_doct_id) REFERENCES doctors (doct_user_id) ON DELETE CASCADE,
                CONSTRAINT fk_ds_spec FOREIGN KEY (ds_spec_id) REFERENCES specialties (spec_id) ON DELETE CASCADE
            );
            """;

        // Tabla de pacientes, extiende users con datos personales
        String createPatients = """
            CREATE TABLE IF NOT EXISTS patients (
                pat_user_id INTEGER PRIMARY KEY,
                pat_phone TEXT NOT NULL,
                pat_gender TEXT NOT NULL CHECK (pat_gender IN ('Hombre', 'Mujer', 'Otro')),
                pat_birth_day TEXT,
                pat_birth_month TEXT,
                pat_birth_year TEXT,
                pat_email TEXT,
                CONSTRAINT fk_patient_user FOREIGN KEY (pat_user_id) REFERENCES users (user_id) ON DELETE CASCADE
            );
            """;

        // Tabla de horarios disponibles por médico
        String createDoctorSchedules = """
            CREATE TABLE IF NOT EXISTS doctor_schedules (
                sched_id SERIAL PRIMARY KEY,
                sched_doctor_id INTEGER NOT NULL,
                sched_day_of_week INTEGER NOT NULL CHECK (sched_day_of_week BETWEEN 1 AND 7),
                sched_start_time TEXT NOT NULL,
                sched_end_time TEXT NOT NULL,
                sched_interval_minutes INTEGER NOT NULL CHECK (sched_interval_minutes > 0),
                CONSTRAINT fk_sched_doctor FOREIGN KEY (sched_doctor_id) REFERENCES doctors (doct_user_id) ON DELETE CASCADE
            );
            """;

        // Tabla de citas médicas
        String createAppointments = """
            CREATE TABLE IF NOT EXISTS appointments (
                appt_id SERIAL PRIMARY KEY,
                appt_doct_id INTEGER NOT NULL,
                appt_pat_id INTEGER NOT NULL,
                appt_date TEXT NOT NULL,
                appt_start_time TEXT NOT NULL,
                appt_end_time TEXT NOT NULL,
                appt_status TEXT NOT NULL DEFAULT 'AGENDADA' CHECK (appt_status IN ('AGENDADA', 'CANCELADA', 'ATENDIDA')),
                CONSTRAINT fk_appt_doctor FOREIGN KEY (appt_doct_id) REFERENCES doctors (doct_user_id) ON DELETE CASCADE,
                CONSTRAINT fk_appt_patient FOREIGN KEY (appt_pat_id) REFERENCES patients (pat_user_id) ON DELETE CASCADE
            );
            """;

        // Tabla de parámetros configurables del sistema
        String createSystemParameters = """
            CREATE TABLE IF NOT EXISTS system_parameters (
                parameter_key TEXT PRIMARY KEY,
                parameter_value TEXT NOT NULL
            );
            """;

        // Datos iniciales de roles
        String insertDefaultRoles = """
            INSERT INTO roles (role_name)
            SELECT name FROM (VALUES ('ADMIN'), ('DOCTOR'), ('PACIENTE'), ('AGENDADOR')) AS v(name)
            WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = v.name);
            """;

        // Datos iniciales de especialidades
        String insertDefaultSpecialties = """
            INSERT INTO specialties (spec_name)
            SELECT name FROM (VALUES ('Terapia Neural'), ('Quiropráctica'), ('Fisioterapia')) AS v(name)
            WHERE NOT EXISTS (SELECT 1 FROM specialties WHERE spec_name = v.name);
            """;

        try (Statement stmt = conn.createStatement()) {
            // El orden importa por las llaves foráneas
            stmt.execute(createRoles);
            stmt.execute(createUsers);
            stmt.execute(createUsersRoles);
            stmt.execute(createDoctors);
            stmt.execute(createSpecialties);
            stmt.execute(createDoctorSpecialties);
            stmt.execute(createPatients);
            stmt.execute(createDoctorSchedules);
            stmt.execute(createAppointments);
            stmt.execute(createSystemParameters);

            // Inserta datos por defecto si no existen
            stmt.execute(insertDefaultRoles);
            stmt.execute(insertDefaultSpecialties);

            System.out.println("✅ Base de datos inicializada correctamente");
        } catch (SQLException e) {
            System.err.println("❌ Error al inicializar la base de datos: " + e.getMessage());
        }
    }    
}
