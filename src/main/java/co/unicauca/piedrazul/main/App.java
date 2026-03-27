//package co.unicauca.piedrazul.main;
//
//import co.unicauca.piedrazul.infrastructure.persistence.PostgreSQLConnection;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import javafx.application.Application;
//import javafx.scene.Scene;
//import javafx.scene.control.Label;
//import javafx.scene.layout.StackPane;
//import javafx.stage.Stage;
//
 ///**
// * @author Valentina Añasco
// * @author Camila Dorado
// * @author Felipe Gutierrez
// * @author Ginner Ortega
// * @author Santiago Solarte
// */
//public class App extends Application {
//
//    private String statusMessage = "Iniciando...";
//
//    @Override
//    public void init() {
//        System.out.println("🚀 Iniciando Piedrazul en la nube...");
//
//        try {
//            // 1. Intentar conectar e inicializar tablas
//            Connection conn = PostgreSQLConnection.getConnection();
//
//            if (conn != null && !conn.isClosed()) {
//                System.out.println("✅ Conexión exitosa a Railway");
//
//                // 2. Prueba de fuego: Insertar un parámetro de sistema de prueba
//                String testSql = "INSERT INTO system_parameters (parameter_key, parameter_value) "
//                        + "VALUES (?, ?) ON CONFLICT (parameter_key) DO UPDATE SET parameter_value = EXCLUDED.parameter_value";
//
//                try (PreparedStatement pstmt = conn.prepareStatement(testSql)) {
//                    pstmt.setString(1, "last_connection_test");
//                    pstmt.setString(2, "Exitosa desde el PC de " + System.getProperty("user.name"));
//                    pstmt.executeUpdate();
//                    System.out.println("💾 Datos de prueba guardados en la nube.");
//                }
//
//                statusMessage = "Conectado a PostgreSQL (Railway)";
//            }
//        } catch (SQLException e) {
//            statusMessage = "Error de conexión: " + e.getMessage();
//            System.err.println("❌ " + statusMessage);
//        }
//    }
//
//    @Override
//    public void start(Stage stage) {
//        Label label = new Label("Estado del Sistema: " + statusMessage);
//        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50;");
//
//        StackPane root = new StackPane(label);
//        Scene scene = new Scene(root, 500, 300);
//
//        stage.setTitle("Piedrazul Medical Network - Cloud Connection Test");
//        stage.setScene(scene);
//        stage.show();
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}

package co.unicauca.piedrazul.main;

import co.unicauca.piedrazul.domain.access.IDoctorRepository;
import co.unicauca.piedrazul.domain.access.IDoctorScheduleRepository;
import co.unicauca.piedrazul.domain.access.IPatientRepository;
import co.unicauca.piedrazul.domain.services.AvailabilityService;
import co.unicauca.piedrazul.domain.services.DoctorService;
import co.unicauca.piedrazul.domain.services.ManualAppointmentService;
import co.unicauca.piedrazul.domain.services.PatientService;
import co.unicauca.piedrazul.domain.services.SystemParameterService;
import co.unicauca.piedrazul.domain.services.interfaces.IDoctorValidator;
import co.unicauca.piedrazul.domain.services.interfaces.IManualAppointmentValidator;
import co.unicauca.piedrazul.domain.services.interfaces.IPatientValidator;
import co.unicauca.piedrazul.domain.services.validators.DoctorValidator;
import co.unicauca.piedrazul.domain.services.validators.ManualAppointmentValidator;
import co.unicauca.piedrazul.domain.services.validators.PatientValidator;
import co.unicauca.piedrazul.domain.services.validators.SystemParameterValidator;
import co.unicauca.piedrazul.infrastructure.persistence.PostgreSQLConnection;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresAppointmentRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresDoctorRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresDoctorScheduleRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresSystemParameterRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresPatientRepository;
import co.unicauca.piedrazul.presentation.controllers.RegisterAppointmentController;
import co.unicauca.piedrazul.presentation.views.RegisterAppointmentView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        try {
            IDoctorRepository doctorRepo = new PostgresDoctorRepository();
            IDoctorValidator doctorVal = new DoctorValidator();

            IManualAppointmentValidator manualVal = new ManualAppointmentValidator();

            IDoctorScheduleRepository docSche = new PostgresDoctorScheduleRepository();

            DoctorService doctorService = new DoctorService(doctorRepo, doctorVal);

            IPatientRepository patientRepo = new PostgresPatientRepository();
            IPatientValidator patientVal = new PatientValidator();
            PatientService patientService = new PatientService(patientRepo, patientVal);

            RegisterAppointmentController controller = new RegisterAppointmentController(
                    new ManualAppointmentService(new PostgresAppointmentRepository(), doctorRepo, patientRepo, manualVal),
                    doctorService,
                    new AvailabilityService(docSche, new PostgresAppointmentRepository()),
                    patientService,
                    new SystemParameterService(new PostgresSystemParameterRepository(), new SystemParameterValidator())
            );

            // 2. Inicializar la Vista con el controlador y un rol de prueba
            // Usamos "DOCTOR" o "AGENDADOR" para que no salte la pantalla de acceso denegado
            RegisterAppointmentView appointmentView = new RegisterAppointmentView(controller, "DOCTOR");

            // 3. Obtener el panel raíz (BorderPane) de la vista
            Scene scene = new Scene(appointmentView.getRoot(), 1100, 700);

            // 4. Configurar el escenario (Stage)
            stage.setTitle("Piedrazul Medical Network - Registro de Citas");
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.show();
        } catch (Exception e) {
            System.err.println("❌ Error crítico al inicializar servicios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
