package co.unicauca.piedrazul.presentation.views;

import co.unicauca.piedrazul.application.PiedrazulFacade;
import co.unicauca.piedrazul.domain.entities.User;
import co.unicauca.piedrazul.main.DataBaseType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Vista principal con sidebar de navegación.
 * Integra ListAppointmentsView y RegisterAppointmentView.
 */
public class MainView {

    private final Stage stage;
    private final User loggedUser;
    private final String loggedUserRole;
    private final PiedrazulFacade facade;

    private BorderPane mainLayout;
    private Button activeNavButton;

    public MainView(Stage stage, User loggedUser, String loggedUserRole, DataBaseType dbType) {
        this.stage          = stage;
        this.loggedUser     = loggedUser;
        this.loggedUserRole = loggedUserRole;
        this.facade         = PiedrazulFacade.getInstance(dbType);
    }

    public void show() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #F3F4F6;");
        mainLayout.setLeft(buildSidebar());

        // Vista por defecto al abrir la aplicación
        showListView();

        Scene scene = new Scene(mainLayout, 1150, 720);
        stage.setTitle("Piedrazul — Citas Médicas");
        stage.setScene(scene);
        stage.show();
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: white;"
                + "-fx-border-color: #E5E7EB; -fx-border-width: 0 1 0 0;");

        sidebar.getChildren().add(buildLogo());

        // Separador de sección
        Label sectionLabel = new Label("Administración");
        sectionLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-font-weight: bold;");
        sectionLabel.setPadding(new Insets(14, 0, 4, 20));
        sidebar.getChildren().add(sectionLabel);

        sidebar.getChildren().addAll(
                navButton("⊞", "Panel de Citas",   this::showPanelView),
                navButton("☰", "Listar Citas",      this::showListView),
                navButton("⊕", "Registrar Cita",    this::showRegisterView),
                navButton("↺", "Reagendar Cita",    this::showRescheduleView),
                navButton("⬇", "Exportar Citas",    this::showExportView),
                navButton("⚙", "Configuración",     this::showConfigView)
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(spacer, buildUserFooter());
        return sidebar;
    }

    private HBox buildLogo() {
        HBox box = new HBox(12);
        box.setPadding(new Insets(22, 16, 18, 18));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");

        // Icono cuadrado azul con símbolo de corazón
        Label icon = new Label("♥");
        icon.setFont(Font.font("System", FontWeight.BOLD, 18));
        icon.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white;"
                + "-fx-background-radius: 10; -fx-padding: 8 10;");

        VBox textBox = new VBox(1);
        Label appName = new Label("Piedrazul");
        appName.setFont(Font.font("System", FontWeight.BOLD, 15));
        appName.setStyle("-fx-text-fill: #111827;");
        Label appSub = new Label("Citas Médicas");
        appSub.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
        textBox.getChildren().addAll(appName, appSub);

        box.getChildren().addAll(icon, textBox);
        return box;
    }

    private Button navButton(String icon, String label, Runnable action) {
        Button btn = new Button(icon + "   " + label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(11, 16, 11, 20));
        btn.setFont(Font.font("System", 13));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #374151; -fx-cursor: hand;");

        btn.setOnMouseEntered(e -> {
            if (btn != activeNavButton)
                btn.setStyle("-fx-background-color: #F9FAFB; -fx-text-fill: #111827; -fx-cursor: hand;");
        });
        btn.setOnMouseExited(e -> {
            if (btn != activeNavButton)
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #374151; -fx-cursor: hand;");
        });

        btn.setOnAction(e -> {
            if (activeNavButton != null)
                activeNavButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #374151; -fx-cursor: hand;");
            activeNavButton = btn;
            btn.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #2563EB; -fx-cursor: hand;");
            action.run();
        });
        return btn;
    }

    private HBox buildUserFooter() {
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(14, 16, 14, 18));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 1 0 0 0;");

        // Avatar con inicial del nombre
        String initial = (loggedUser.getFirstName() != null && !loggedUser.getFirstName().isEmpty())
                ? loggedUser.getFirstName().substring(0, 1).toUpperCase() : "U";
        Label avatar = new Label(initial);
        avatar.setFont(Font.font("System", FontWeight.BOLD, 14));
        avatar.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8;"
                + "-fx-background-radius: 20; -fx-padding: 6 10;");

        VBox info = new VBox(1);
        Label nameLabel = new Label(loggedUser.getFullName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        nameLabel.setStyle("-fx-text-fill: #111827;");

        Label roleLabel = new Label(loggedUserRole);
        roleLabel.setStyle("-fx-text-fill: #7C3AED; -fx-font-size: 10px; -fx-font-weight: bold;");

        info.getChildren().addAll(nameLabel, roleLabel);

        // Botón logout
        Button btnLogout = new Button("→");
        btnLogout.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF;"
                + "-fx-font-size: 16px; -fx-cursor: hand; -fx-border-color: transparent;");
        btnLogout.setTooltip(new Tooltip("Cerrar sesión"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        footer.getChildren().addAll(avatar, info, spacer, btnLogout);
        return footer;
    }

    // ── Navegación a vistas ───────────────────────────────────────────────────

    /** Listado de citas — solo ADMIN, DOCTOR, AGENDADOR */
    private void showListView() {
        ListAppointmentsView view = new ListAppointmentsView(
                facade.getAppointmentController(),
                facade.getDoctorController(),
                loggedUserRole
        );
        mainLayout.setCenter(view.getRoot());
    }

    /** Registrar cita — solo ADMIN, DOCTOR, AGENDADOR */
    private void showRegisterView() {
        RegisterAppointmentView view = new RegisterAppointmentView(
                facade.getAppointmentController(),
                facade.getDoctorController(),
                facade.getAvailabilityController(),
                facade.getPatientController(),
                facade.getParameterController(),
                loggedUserRole
        );
        mainLayout.setCenter(view.getRoot());
    }

    private void showPanelView() {
        mainLayout.setCenter(buildPlaceholder("Panel de Citas", "Próximamente: vista de panel"));
    }

    private void showRescheduleView() {
        mainLayout.setCenter(buildPlaceholder("Reagendar Cita", "Próximamente: reagendamiento de citas"));
    }

    private void showExportView() {
        mainLayout.setCenter(buildPlaceholder("Exportar Citas", "Próximamente: exportación a PDF/Excel"));
    }

    private void showConfigView() {
        mainLayout.setCenter(buildPlaceholder("Configuración", "Próximamente: ajustes del sistema"));
    }

    // ── Placeholder para vistas en construcción ───────────────────────────────

    private VBox buildPlaceholder(String title, String message) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60));

        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        lblTitle.setStyle("-fx-text-fill: #374151;");

        Label lblMsg = new Label(message);
        lblMsg.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");

        box.getChildren().addAll(lblTitle, lblMsg);
        return box;
    }
}