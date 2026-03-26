package co.unicauca.piedrazul.presentation.views;

import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.enums.AppointmentStatus;
import co.unicauca.piedrazul.domain.entities.enums.RoleName;
import co.unicauca.piedrazul.presentation.controllers.ManualAppointmentController;
import co.unicauca.piedrazul.presentation.controllers.DoctorController;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

/**
 * Vista JavaFX para el módulo "Listado de Citas".
 * Solo accesible para roles: ADMIN, DOCTOR, AGENDADOR.
 */
public class ListAppointmentsView {

    private final ManualAppointmentController appointmentController;
    private final DoctorController doctorController;
    private final String loggedUserRole;

    private BorderPane root;
    private TableView<Appointment> table;
    private ComboBox<Doctor> cbDoctorFilter;
    private DatePicker dateFilterPicker;
    private Label lblTotal;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // Roles con acceso permitido
    private static final List<String> ALLOWED_ROLES = List.of(
            RoleName.ADMIN.name(), RoleName.DOCTOR.name(), RoleName.AGENDADOR.name()
    );

    public ListAppointmentsView(ManualAppointmentController appointmentController,
                                DoctorController doctorController,
                                String loggedUserRole) {
        this.appointmentController = appointmentController;
        this.doctorController = doctorController;
        this.loggedUserRole = loggedUserRole;
        build();
    }

    // ── Construcción principal ────────────────────────────────────────────────

    private void build() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8F9FA;");

        if (!ALLOWED_ROLES.contains(loggedUserRole)) {
            root.setCenter(buildAccessDenied());
            return;
        }

        root.setTop(buildHeader());
        root.setCenter(buildBody());
    }

    private VBox buildHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(20, 28, 14, 28));
        header.setStyle("-fx-background-color: white;"
                + "-fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");

        Label breadcrumb = new Label("Administración / Listado de Citas");
        breadcrumb.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        Label title = new Label("Listado de Citas");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setStyle("-fx-text-fill: #111827;");

        Label subtitle = new Label("Busca y filtra todas las citas médicas programadas.");
        subtitle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

        header.getChildren().addAll(breadcrumb, title, subtitle);
        return header;
    }

    private VBox buildBody() {
        VBox body = new VBox(16);
        body.setPadding(new Insets(24, 28, 24, 28));

        body.getChildren().addAll(buildFilterCard(), buildTableCard());
        return body;
    }

    // ── Tarjeta de filtros ────────────────────────────────────────────────────

    private VBox buildFilterCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setStyle(cardStyle());

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        // ComboBox de doctor
        VBox doctorBox = new VBox(5);
        Label lblDoc = fieldLabel("Profesional o terapista");
        cbDoctorFilter = new ComboBox<>();
        cbDoctorFilter.setPromptText("Todos los profesionales");
        cbDoctorFilter.setPrefWidth(220);
        cbDoctorFilter.setStyle(fieldStyle());

        // Opción "Todos"
        cbDoctorFilter.getItems().add(null);
        cbDoctorFilter.getItems().addAll(doctorController.getActiveStaff());
        cbDoctorFilter.setConverter(new StringConverter<Doctor>() {
            @Override public String toString(Doctor d) {
                return d == null ? "Todos los profesionales" : d.getFullName();
            }
            @Override public Doctor fromString(String s) { return null; }
        });
        doctorBox.getChildren().addAll(lblDoc, cbDoctorFilter);

        // DatePicker de fecha
        VBox dateBox = new VBox(5);
        Label lblDate = fieldLabel("Fecha");
        dateFilterPicker = new DatePicker();
        dateFilterPicker.setPromptText("mm/dd/yyyy");
        dateFilterPicker.setPrefWidth(180);
        dateFilterPicker.setStyle(fieldStyle());
        dateBox.getChildren().addAll(lblDate, dateFilterPicker);

        // Botón buscar
        Button btnSearch = buildPrimaryButton("🔍  Buscar");
        btnSearch.setOnAction(e -> applyFilters());
        VBox btnBox = new VBox(btnSearch);
        btnBox.setAlignment(Pos.BOTTOM_LEFT);

        // Botón limpiar
        Button btnClear = buildSecondaryButton("Limpiar");
        btnClear.setOnAction(e -> {
            cbDoctorFilter.setValue(null);
            dateFilterPicker.setValue(null);
            loadAllAppointments();
        });
        VBox clearBox = new VBox(btnClear);
        clearBox.setAlignment(Pos.BOTTOM_LEFT);

        row.getChildren().addAll(doctorBox, dateBox, btnBox, clearBox);
        card.getChildren().add(row);
        return card;
    }

    // ── Tarjeta de tabla ──────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private VBox buildTableCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(0));
        card.setStyle(cardStyle());

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setPrefHeight(400);
        table.setPlaceholder(new Label("No se encontraron citas"));

        // Columna Hora
        TableColumn<Appointment, String> colHora = new TableColumn<>("Hora");
        colHora.setCellValueFactory(c -> {
            LocalDate date = c.getValue().getDate();
            return new SimpleStringProperty(
                    date != null ? c.getValue().getStartTime().format(TIME_FMT) : "--:--"
            );
        });
        colHora.setPrefWidth(80);
        colHora.setStyle("-fx-font-weight: bold;");

        // Columna Fecha
        TableColumn<Appointment, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> {
            LocalDate date = c.getValue().getDate();
            return new SimpleStringProperty(date != null ? date.format(DATE_FMT) : "—");
        });
        colFecha.setPrefWidth(100);

        // Columna Paciente
        TableColumn<Appointment, String> colPaciente = new TableColumn<>("Nombre del paciente");
        colPaciente.setCellValueFactory(c -> {
            var p = c.getValue().getPatient();
            return new SimpleStringProperty(p != null ? p.getFullName() : "—");
        });
        colPaciente.setPrefWidth(180);

        // Columna Teléfono
        TableColumn<Appointment, String> colTel = new TableColumn<>("Teléfono de contacto");
        colTel.setCellValueFactory(c -> {
            var p = c.getValue().getPatient();
            return new SimpleStringProperty(p != null ? p.getPhone() : "—");
        });
        colTel.setPrefWidth(150);

        // Columna Doctor
        TableColumn<Appointment, String> colDoctor = new TableColumn<>("Profesional");
        colDoctor.setCellValueFactory(c -> {
            var d = c.getValue().getDoctor();
            return new SimpleStringProperty(d != null ? d.getFullName() : "—");
        });
        colDoctor.setPrefWidth(160);

        // Columna Estado (con badge de color)
        TableColumn<Appointment, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStatus() != null
                        ? c.getValue().getStatus().name() : "—"));
        colEstado.setPrefWidth(120);
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setPadding(new Insets(3, 10, 3, 10));
                    badge.setStyle(badgeStyle(item));
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        table.getColumns().addAll(colHora, colFecha, colPaciente, colTel, colDoctor, colEstado);

        // Footer con total
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 20, 14, 20));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 1 0 0 0;");

        lblTotal = new Label("Total de citas encontradas: 0");
        lblTotal.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnVerTodas = new Button("Ver todas");
        btnVerTodas.setStyle("-fx-background-color: transparent; -fx-text-fill: #2563EB;"
                + "-fx-font-size: 12px; -fx-cursor: hand; -fx-border-color: transparent;");
        btnVerTodas.setOnAction(e -> loadAllAppointments());

        footer.getChildren().addAll(lblTotal, spacer, btnVerTodas);

        card.getChildren().addAll(table, footer);

        // Carga inicial
        loadAllAppointments();

        return card;
    }

    // ── Lógica de carga y filtro ──────────────────────────────────────────────

    private void loadAllAppointments() {
        List<Appointment> all = appointmentController.list();
        updateTable(all);
    }

    private void applyFilters() {
        List<Appointment> all = appointmentController.list();

        Doctor doctorFilter = cbDoctorFilter.getValue();
        LocalDate dateFilter = dateFilterPicker.getValue();

        List<Appointment> filtered = all.stream()
                .filter(a -> doctorFilter == null
                        || (a.getDoctor() != null && a.getDoctor().getId() == doctorFilter.getId()))
                .filter(a -> dateFilter == null
                        || dateFilter.equals(a.getDate()))
                .collect(Collectors.toList());

        updateTable(filtered);
    }

    private void updateTable(List<Appointment> appointments) {
        ObservableList<Appointment> data = FXCollections.observableArrayList(appointments);
        table.setItems(data);
        lblTotal.setText("Total de citas encontradas: " + appointments.size());
    }

    // ── Pantalla de acceso denegado ───────────────────────────────────────────

    private VBox buildAccessDenied() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60));

        Label icon = new Label("🚫");
        icon.setFont(Font.font(52));

        Label title = new Label("Acceso denegado");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #DC2626;");

        Label msg = new Label("Solo los usuarios con rol ADMIN, DOCTOR o AGENDADOR\npueden ver el listado de citas.");
        msg.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");
        msg.setAlignment(Pos.CENTER);
        msg.setWrapText(true);

        box.getChildren().addAll(icon, title, msg);
        return box;
    }

    // ── Helpers de estilo ─────────────────────────────────────────────────────

    private String badgeStyle(String state) {
        String base = "-fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
        try {
            AppointmentStatus s = AppointmentStatus.valueOf(state);
            return switch (s) {
                case AGENDADA    -> base + "-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8;";
                case CANCELADA   -> base + "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;";
                case ATENDIDA    -> base + "-fx-background-color: #F3F4F6; -fx-text-fill: #6B7280;";
                default          -> base + "-fx-background-color: #FEF9C3; -fx-text-fill: #92400E;";
            };
        } catch (Exception e) {
            return base + "-fx-background-color: #F3F4F6; -fx-text-fill: #6B7280;";
        }
    }

    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #374151; -fx-font-weight: bold; -fx-font-size: 12px;");
        return lbl;
    }

    private String cardStyle() {
        return "-fx-background-color: white;"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: #E5E7EB;"
                + "-fx-border-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);";
    }

    private String fieldStyle() {
        return "-fx-background-radius: 6; -fx-border-color: #D1D5DB;"
                + "-fx-border-radius: 6; -fx-padding: 7;";
    }

    private Button buildPrimaryButton(String text) {
        Button btn = new Button(text);
        String base = "-fx-background-color: #2563EB; -fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;";
        String hover = "-fx-background-color: #1D4ED8; -fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private Button buildSecondaryButton(String text) {
        Button btn = new Button(text);
        String base = "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;"
                + "-fx-font-size: 13px; -fx-background-radius: 8;"
                + "-fx-border-color: #D1D5DB; -fx-border-radius: 8;"
                + "-fx-padding: 9 20; -fx-cursor: hand;";
        btn.setStyle(base);
        return btn;
    }

    public BorderPane getRoot() { return root; }
}