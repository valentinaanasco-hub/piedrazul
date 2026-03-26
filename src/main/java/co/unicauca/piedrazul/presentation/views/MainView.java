package co.unicauca.piedrazul.presentation.views;

import co.unicauca.piedrazul.domain.entities.User;
import co.unicauca.piedrazul.domain.services.*;
import co.unicauca.piedrazul.domain.services.validators.DoctorValidator;
import co.unicauca.piedrazul.domain.services.validators.ManualAppointmentValidator;
import co.unicauca.piedrazul.domain.services.validators.UserValidator;
import co.unicauca.piedrazul.infrastructure.repositories.*;
import co.unicauca.piedrazul.presentation.controllers.RegisterAppointmentController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Gestión de la interfaz principal y navegación.
 * * Se encarga de centralizar la instancia de servicios y repositorios 
 * para asegurar que los controladores reciban las dependencias necesarias.
 */
public class MainView {

    private final Stage stage;
    private final User loggedUser;
    private final String loggedUserRole;

    private BorderPane mainLayout;
    private Button activeNavButton;

    // Servicios del sistema
    private ManualAppointmentService appointmentService;
    private DoctorService doctorService;
    private DoctorScheduleService doctorScheduleService;
    private AvailabilityService availabilityService;
    private PatientService patientService;
    private SystemParameterService parameterService;

    public MainView(Stage stage, User loggedUser, String loggedUserRole) {
        this.stage = stage;
        this.loggedUser = loggedUser;
        this.loggedUserRole = loggedUserRole;
        
        /*
        // Inicialización de validadores
        UserValidator userValidator = new UserValidator();
        DoctorValidator doctorValidator = new DoctorValidator();
        ManualAppointmentValidator manualAppointmentValidator = new ManualAppointmentValidator();

        // Inicialización de infraestructura (repositorios)
        PostgresUserRepository userRepo = new PostgresUserRepository();
        PostgresDoctorRepository doctorRepo = new PostgresDoctorRepository();
        PostgresPatientRepository patientRepo = new PostgresPatientRepository();
        PostgresAppointmentRepository appointmentRepo = new PostgresAppointmentRepository();
        PostgresDoctorScheduleRepository scheduleRepo = new PostgresDoctorScheduleRepository();
        PostgresSystemParameterRepository paramRepo = new PostgresSystemParameterRepository();
        PostgresSpecialtyRepository specialtyRepo = new PostgresSpecialtyRepository();

        // Ensamblaje de servicios
        this.doctorScheduleService = new DoctorScheduleService(scheduleRepo);
        this.availabilityService = new AvailabilityService(scheduleRepo, appointmentRepo);
        this.appointmentService = new ManualAppointmentService(appointmentRepo, doctorRepo, patientRepo, manualAppointmentValidator);
        this.doctorService = new DoctorService(doctorRepo, doctorValidator);
        this.patientService = new PatientService(patientRepo);
        this.parameterService = new SystemParameterService(paramRepo);
        */
    }

    public void show() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #F3F4F6;");
        mainLayout.setLeft(buildSidebar());

        // Carga de la vista por defecto
        showListView();

        Scene scene = new Scene(mainLayout, 1100, 700);
        stage.setTitle("Piedrazul — Citas Médicas");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    // Estructura del menú lateral
    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(245);
        sidebar.setStyle(
                "-fx-background-color: white;"
                + "-fx-border-color: #E5E7EB; -fx-border-width: 0 1 0 0;");

        sidebar.getChildren().add(buildLogo());

        Label sectionLbl = new Label("Administración");
        sectionLbl.setPadding(new Insets(18, 16, 6, 18));
        sectionLbl.setStyle(
                "-fx-text-fill: #9CA3AF; -fx-font-size: 10px; -fx-font-weight: bold;");
        sidebar.getChildren().add(sectionLbl);

        // Opciones de navegación
        Button btnPanel = navButton("⊞", "Panel de Citas", this::showPanelView);
        Button btnList = navButton("☰", "Listar Citas", this::showListView);
        Button btnRegister = navButton("⊕", "Registrar Cita", this::showRegisterView);
        Button btnReschedule = navButton("↺", "Reagendar Cita", this::showRescheduleView);
        Button btnExport = navButton("⬇", "Exportar Citas", this::showExportView);
        Button btnConfig = navButton("⚙", "Configuración", this::showConfigView);

        sidebar.getChildren().addAll(
                btnPanel, btnList, btnRegister,
                btnReschedule, btnExport, btnConfig
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        sidebar.getChildren().add(buildUserFooter());
        return sidebar;
    }

    private HBox buildLogo() {
        HBox box = new HBox(12);
        box.setPadding(new Insets(20, 16, 18, 18));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");

        Label icon = new Label("💙");
        icon.setStyle(
                "-fx-font-size: 20px; -fx-background-color: #2563EB;"
                + "-fx-background-radius: 8; -fx-padding: 7 9;");

        Label appName = new Label("Piedrazul");
        appName.setFont(Font.font("System", FontWeight.BOLD, 15));
        appName.setStyle("-fx-text-fill: #111827;");

        Label appSub = new Label("Citas Médicas");
        appSub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");

        VBox textBox = new VBox(1, appName, appSub);
        box.getChildren().addAll(icon, textBox);
        return box;
    }

    // Configuración de eventos y estilos para botones del menú
    private Button navButton(String icon, String label, Runnable action) {
        Button btn = new Button(icon + "  " + label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(11, 16, 11, 20));
        btn.setFont(Font.font("System", 13));
        btn.setStyle(navStyle(false));

        btn.setOnMouseEntered(e -> {
            if (btn != activeNavButton) {
                btn.setStyle(navHoverStyle());
            }
        });
        btn.setOnMouseExited(e -> {
            if (btn != activeNavButton) {
                btn.setStyle(navStyle(false));
            }
        });

        btn.setOnAction(e -> {
            if (activeNavButton != null) {
                activeNavButton.setStyle(navStyle(false));
            }
            activeNavButton = btn;
            btn.setStyle(navStyle(true));
            action.run();
        });

        return btn;
    }

    private String navStyle(boolean active) {
        if (active) {
            return "-fx-background-color: #EFF6FF; -fx-text-fill: #2563EB;"
                    + "-fx-font-weight: bold; -fx-border-color: #2563EB transparent transparent transparent;"
                    + "-fx-border-width: 0 0 0 3; -fx-background-radius: 0;";
        }
        return "-fx-background-color: transparent; -fx-text-fill: #374151;"
                + "-fx-background-radius: 0;";
    }

    private String navHoverStyle() {
        return "-fx-background-color: #F9FAFB; -fx-text-fill: #111827;"
                + "-fx-background-radius: 0;";
    }

    private HBox buildUserFooter() {
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(14, 16, 14, 18));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 1 0 0 0;");

        // Generar inicial para el avatar
        String initial = (loggedUser.getFirstName() != null
                && !loggedUser.getFirstName().isEmpty())
                ? String.valueOf(loggedUser.getFirstName().charAt(0)).toUpperCase()
                : "U";

        Label avatar = new Label(initial);
        avatar.setStyle(
                "-fx-background-color: #2563EB; -fx-text-fill: white;"
                + "-fx-font-weight: bold; -fx-font-size: 13px;"
                + "-fx-background-radius: 50; -fx-min-width: 34; -fx-min-height: 34;"
                + "-fx-alignment: center;");

        Label nameLabel = new Label(loggedUser.getFullName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #111827;");

        Label roleLabel = new Label(loggedUserRole);
        roleLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");

        VBox info = new VBox(2, nameLabel, roleLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label logoutIcon = new Label("⇥");
        logoutIcon.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 16px; -fx-cursor: hand;");
        Tooltip.install(logoutIcon, new Tooltip("Cerrar sesión"));

        footer.getChildren().addAll(avatar, info, logoutIcon);
        return footer;
    }

    // Controladores de intercambio de paneles centrales
    private void showRegisterView() {
        RegisterAppointmentController controller = new RegisterAppointmentController(
                appointmentService,
                doctorService,
                availabilityService,
                patientService,
                parameterService
        );

        RegisterAppointmentView view
                = new RegisterAppointmentView(controller, loggedUserRole);

        mainLayout.setCenter(view.getRoot());
    }

    private void showListView() {
        mainLayout.setCenter(placeholder("📋", "Listar Citas",
                "Módulo en desarrollo - Requerimiento 1"));
    }

    private void showPanelView() {
        mainLayout.setCenter(placeholder("⊞", "Panel de Citas", "Próximamente"));
    }

    private void showRescheduleView() {
        mainLayout.setCenter(placeholder("↺", "Reagendar Cita", "Próximamente"));
    }

    private void showExportView() {
        mainLayout.setCenter(placeholder("⬇", "Exportar Citas", "Próximamente"));
    }

    private void showConfigView() {
        mainLayout.setCenter(placeholder("⚙", "Configuración", "Próximamente"));
    }

    private VBox placeholder(String icon, String title, String subtitle) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60));

        Label iconLbl = new Label(icon);
        iconLbl.setFont(Font.font(42));

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLbl.setStyle("-fx-text-fill: #374151;");

        Label subLbl = new Label(subtitle);
        subLbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");

        box.getChildren().addAll(iconLbl, titleLbl, subLbl);
        BorderPane.setAlignment(box, Pos.CENTER);
        return box;
    }
}