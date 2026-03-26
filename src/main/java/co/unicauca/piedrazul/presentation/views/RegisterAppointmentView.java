package co.unicauca.piedrazul.presentation.views;

import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.domain.entities.SystemParameter;
import co.unicauca.piedrazul.domain.entities.enums.AppointmentStatus;
import co.unicauca.piedrazul.domain.entities.enums.RoleName;
import co.unicauca.piedrazul.presentation.controllers.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

/**
 * Vista JavaFX para el módulo "Registrar Cita Manual".
 * Solo accesible para roles: ADMIN, DOCTOR, AGENDADOR.
 * El calendario usa el rango de fechas de start_date_schedule / end_date_schedule.
 */
public class RegisterAppointmentView {

    private final ManualAppointmentController appointmentController;
    private final DoctorController doctorController;
    private final AvailabilityController availabilityController;
    private final PatientController patientController;
    private final SystemParameterController parameterController;
    private final String loggedUserRole;

    private Patient selectedPatient;
    private BorderPane root;

    // Controles del formulario
    private TextField     txtDocument;
    private Label         lblPatientResult;
    private ComboBox<Doctor>    cbDoctor;
    private DatePicker          datePicker;
    private ComboBox<LocalTime> cbSlot;
    private Label         lblSlotsInfo;
    private TextArea      txtReason;

    // Rango de fechas leído desde parámetros del sistema
    private LocalDate startDate = LocalDate.now();
    private LocalDate endDate   = LocalDate.now().plusMonths(3);

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Roles con acceso permitido
    private static final List<String> ALLOWED_ROLES = List.of(
            RoleName.ADMIN.name(), RoleName.DOCTOR.name(), RoleName.AGENDADOR.name()
    );

    public RegisterAppointmentView(ManualAppointmentController ac, DoctorController dc,
            AvailabilityController avc, PatientController pc,
            SystemParameterController pmc, String role) {
        this.appointmentController   = ac;
        this.doctorController        = dc;
        this.availabilityController  = avc;
        this.patientController       = pc;
        this.parameterController     = pmc;
        this.loggedUserRole          = role;
        loadDateRange();
        build();
    }

    // ── Carga el rango desde parámetros del sistema ───────────────────────────

    private void loadDateRange() {
        try {
            SystemParameter startParam = parameterController.getSetting("start_date_schedule");
            SystemParameter endParam   = parameterController.getSetting("end_date_schedule");

            if (startParam != null && startParam.getValue() != null
                    && !startParam.getValue().isBlank()) {
                startDate = LocalDate.parse(startParam.getValue().trim(), DATE_FMT);
            }
            if (endParam != null && endParam.getValue() != null
                    && !endParam.getValue().isBlank()) {
                endDate = LocalDate.parse(endParam.getValue().trim(), DATE_FMT);
            }
        } catch (Exception e) {
            System.err.println("⚠ No se pudo leer el rango de fechas: " + e.getMessage());
            // Se usan los valores por defecto definidos arriba
        }
    }

    // ── Construcción de la UI ─────────────────────────────────────────────────

    private void build() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8F9FA;");

        root.setTop(buildHeader());

        if (!ALLOWED_ROLES.contains(loggedUserRole)) {
            root.setCenter(buildAccessDenied());
            return;
        }

        root.setCenter(buildBody());
    }

    private VBox buildHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(20, 28, 14, 28));
        header.setStyle("-fx-background-color: white;"
                + "-fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");

        Label breadcrumb = new Label("Administración / Registrar Cita");
        breadcrumb.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        Label title = new Label("Registrar Cita Manual");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setStyle("-fx-text-fill: #111827;");

        Label subtitle = new Label("Registra una nueva cita médica para un paciente");
        subtitle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

        header.getChildren().addAll(breadcrumb, title, subtitle);
        return header;
    }

    private HBox buildBody() {
        HBox body = new HBox(20);
        body.setPadding(new Insets(24, 28, 24, 28));
        body.setAlignment(Pos.TOP_LEFT);

        VBox form = buildForm();
        HBox.setHgrow(form, Priority.ALWAYS);

        VBox availability = buildAvailabilityPanel();
        availability.setMinWidth(220);
        availability.setMaxWidth(220);

        body.getChildren().addAll(form, availability);
        return body;
    }

    // ── Formulario principal ──────────────────────────────────────────────────

    private VBox buildForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(24));
        form.setStyle(cardStyle());

        Label sectionTitle = new Label("Datos de la cita");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        sectionTitle.setStyle("-fx-text-fill: #374151;");

        // Fila 1: paciente | médico
        HBox row1 = new HBox(16);
        VBox patientBox = buildPatientField();
        VBox doctorBox  = buildDoctorField();
        HBox.setHgrow(patientBox, Priority.ALWAYS);
        HBox.setHgrow(doctorBox,  Priority.ALWAYS);
        row1.getChildren().addAll(patientBox, doctorBox);

        // Fila 2: fecha | hora
        HBox row2 = new HBox(16);
        VBox dateBox = buildDateField();
        VBox slotBox = buildSlotField();
        HBox.setHgrow(dateBox, Priority.ALWAYS);
        HBox.setHgrow(slotBox, Priority.ALWAYS);
        row2.getChildren().addAll(dateBox, slotBox);

        // Fila 3: motivo | notas adicionales
        HBox row3 = new HBox(16);
        VBox reasonBox = buildReasonField();
        HBox.setHgrow(reasonBox, Priority.ALWAYS);
        row3.getChildren().add(reasonBox);

        // Botón registrar
        Button btnRegister = buildPrimaryButton("Registrar Cita");
        btnRegister.setOnAction(e -> handleRegister());

        HBox btnRow = new HBox(btnRegister);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        form.getChildren().addAll(sectionTitle, row1, row2, row3, btnRow);
        return form;
    }

    // ── Campos del formulario ─────────────────────────────────────────────────

    private VBox buildPatientField() {
        VBox box = new VBox(6);
        Label lbl = fieldLabel("Paciente *");

        HBox searchRow = new HBox(6);
        txtDocument = new TextField();
        txtDocument.setPromptText("Buscar paciente...");
        txtDocument.setStyle(fieldStyle());
        HBox.setHgrow(txtDocument, Priority.ALWAYS);

        Button btnSearch = buildSecondaryButton("Buscar");
        btnSearch.setOnAction(e -> handlePatientSearch());
        txtDocument.setOnAction(e -> handlePatientSearch());

        searchRow.getChildren().addAll(txtDocument, btnSearch);

        lblPatientResult = new Label("Sin paciente seleccionado");
        lblPatientResult.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        box.getChildren().addAll(lbl, searchRow, lblPatientResult);
        return box;
    }

    private VBox buildDoctorField() {
        VBox box = new VBox(6);
        Label lbl = fieldLabel("Profesional *");

        cbDoctor = new ComboBox<>();
        cbDoctor.setPromptText("Seleccionar profesional...");
        cbDoctor.setMaxWidth(Double.MAX_VALUE);
        cbDoctor.setStyle(fieldStyle());
        cbDoctor.setConverter(new StringConverter<Doctor>() {
            @Override public String toString(Doctor d) { return d == null ? "" : d.getFullName(); }
            @Override public Doctor fromString(String s) { return null; }
        });

        cbDoctor.getItems().addAll(doctorController.getActiveStaff());
        cbDoctor.setOnAction(e -> {
            if (cbDoctor.getValue() != null) updateSlotsComboBox();
        });

        box.getChildren().addAll(lbl, cbDoctor);
        return box;
    }

    private VBox buildDateField() {
        VBox box = new VBox(6);
        Label lbl = fieldLabel("Fecha *");

        datePicker = new DatePicker();
        datePicker.setPromptText("mm/dd/yyyy");
        datePicker.setMaxWidth(Double.MAX_VALUE);

        // Restringe el calendario al rango de parámetros del sistema
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                boolean outOfRange = date.isBefore(startDate) || date.isAfter(endDate);
                setDisable(outOfRange);
                if (outOfRange) {
                    setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #D1D5DB;");
                }
            }
        });

        datePicker.setOnAction(e -> {
            if (datePicker.getValue() != null) updateSlotsComboBox();
        });

        box.getChildren().addAll(lbl, datePicker);
        return box;
    }

    private VBox buildSlotField() {
        VBox box = new VBox(6);
        Label lbl = fieldLabel("Hora *");

        cbSlot = new ComboBox<>();
        cbSlot.setPromptText("--:--");
        cbSlot.setMaxWidth(Double.MAX_VALUE);
        cbSlot.setStyle(fieldStyle());
        cbSlot.setDisable(true);
        cbSlot.setConverter(new StringConverter<LocalTime>() {
            @Override public String toString(LocalTime t) { return t == null ? "" : t.format(TIME_FMT); }
            @Override public LocalTime fromString(String s) { return null; }
        });

        lblSlotsInfo = new Label("Seleccione médico y fecha primero");
        lblSlotsInfo.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");

        box.getChildren().addAll(lbl, cbSlot, lblSlotsInfo);
        return box;
    }

    private VBox buildReasonField() {
        VBox box = new VBox(6);
        Label lbl = fieldLabel("Motivo de consulta *");

        txtReason = new TextArea();
        txtReason.setPromptText("Describe el motivo de la consulta...");
        txtReason.setPrefRowCount(3);
        txtReason.setMaxWidth(Double.MAX_VALUE);
        txtReason.setStyle(fieldStyle() + "-fx-font-size: 13px;");
        txtReason.setWrapText(true);

        box.getChildren().addAll(lbl, txtReason);
        return box;
    }

    // ── Panel lateral de disponibilidad ──────────────────────────────────────

    private VBox buildAvailabilityPanel() {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(20));
        panel.setStyle(cardStyle());

        Label title = new Label("Vista de disponibilidad");
        title.setFont(Font.font("System", FontWeight.BOLD, 13));
        title.setStyle("-fx-text-fill: #374151;");

        Label info = new Label("Seleccione un profesional y una fecha para ver los horarios disponibles.");
        info.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        info.setWrapText(true);

        // Leyenda de estados de hora
        VBox legend = new VBox(8);
        legend.setPadding(new Insets(8, 0, 0, 0));
        legend.getChildren().addAll(
                legendItem("#6B7280", "Disponible"),
                legendItem("#2563EB", "Seleccionado"),
                legendItem("#9CA3AF", "Ocupado")
        );

        // Separador
        Separator sep = new Separator();
        sep.setStyle("-fx-border-color: #E5E7EB;");

        // Rango de fechas habilitado
        VBox rangeBox = new VBox(4);
        rangeBox.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 6; -fx-padding: 10;");

        Label rangeTitle = new Label("Rango habilitado");
        rangeTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #1D4ED8;");

        Label rangeVal = new Label(startDate.toString() + "\nal " + endDate.toString());
        rangeVal.setStyle("-fx-text-fill: #1E40AF; -fx-font-size: 11px;");
        rangeVal.setWrapText(true);

        rangeBox.getChildren().addAll(rangeTitle, rangeVal);

        panel.getChildren().addAll(title, info, legend, sep, rangeBox);
        return panel;
    }

    private HBox legendItem(String color, String text) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px;");
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #374151; -fx-font-size: 11px;");
        row.getChildren().addAll(dot, lbl);
        return row;
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

        Label msg = new Label("Solo los usuarios con rol ADMIN, DOCTOR o AGENDADOR\npueden registrar citas manualmente.");
        msg.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");
        msg.setAlignment(Pos.CENTER);
        msg.setWrapText(true);

        box.getChildren().addAll(icon, title, msg);
        return box;
    }

    // ── Manejadores de eventos ────────────────────────────────────────────────

    private void handlePatientSearch() {
        String input = txtDocument.getText().trim();
        if (input.isEmpty()) return;

        try {
            int docId = Integer.parseInt(input);
            Patient patient = patientController.getPatientById(docId);

            if (patient != null) {
                this.selectedPatient = patient;
                lblPatientResult.setText("✓ " + patient.getFullName());
                lblPatientResult.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px;");
            } else {
                this.selectedPatient = null;
                lblPatientResult.setText("✗ Paciente no encontrado");
                lblPatientResult.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
            }
        } catch (NumberFormatException ex) {
            this.selectedPatient = null;
            lblPatientResult.setText("✗ Ingrese un número válido");
            lblPatientResult.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
        }
    }

    private void updateSlotsComboBox() {
        Doctor doc   = cbDoctor.getValue();
        LocalDate date = datePicker.getValue();

        if (doc == null || date == null) return;

        List<LocalTime> slots = availabilityController.checkAvailableSlots(doc.getId(), date);
        cbSlot.getItems().clear();
        cbSlot.setValue(null);

        if (slots == null || slots.isEmpty()) {
            cbSlot.setDisable(true);
            lblSlotsInfo.setText("No hay horarios disponibles para esta fecha");
            lblSlotsInfo.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 11px;");
        } else {
            cbSlot.getItems().addAll(slots);
            cbSlot.setDisable(false);
            lblSlotsInfo.setText(slots.size() + " horario(s) disponible(s)");
            lblSlotsInfo.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 11px;");
        }
    }

    private void handleRegister() {
        // Validación de campos obligatorios
        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "Datos incompletos", "Debe buscar y seleccionar un paciente.");
            return;
        }
        if (cbDoctor.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Datos incompletos", "Debe seleccionar un profesional.");
            return;
        }
        if (datePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Datos incompletos", "Debe seleccionar una fecha.");
            return;
        }
        if (cbSlot.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Datos incompletos", "Debe seleccionar una hora.");
            return;
        }
        if (txtReason.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Datos incompletos", "Debe ingresar el motivo de la consulta.");
            return;
        }

        try {
            LocalTime startTime = cbSlot.getValue();

            Appointment appointment = new Appointment(
                    datePicker.getValue(),
                    startTime,
                    startTime,                  // endTime: el servicio calcula la duración real
                    AppointmentStatus.AGENDADA,
                    cbDoctor.getValue(),
                    selectedPatient
            );

            boolean ok = appointmentController.schedule(appointment);

            if (ok) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "✓ Cita registrada exitosamente");
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        appointmentController.getLastErrorMessage() != null
                                ? appointmentController.getLastErrorMessage()
                                : "No se pudo guardar la cita. Intente de nuevo.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error inesperado", e.getMessage());
        }
    }

    private void clearForm() {
        txtDocument.clear();
        selectedPatient = null;
        lblPatientResult.setText("Sin paciente seleccionado");
        lblPatientResult.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        cbDoctor.setValue(null);
        datePicker.setValue(null);
        cbSlot.getItems().clear();
        cbSlot.setDisable(true);
        lblSlotsInfo.setText("Seleccione médico y fecha primero");
        lblSlotsInfo.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
        txtReason.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ── Helpers de estilo ─────────────────────────────────────────────────────

    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #374151; -fx-font-weight: bold; -fx-font-size: 13px;");
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
        String base  = "-fx-background-color: #2563EB; -fx-text-fill: white;"
                + "-fx-font-size: 14px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 11 32; -fx-cursor: hand;";
        String hover = "-fx-background-color: #1D4ED8; -fx-text-fill: white;"
                + "-fx-font-size: 14px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 11 32; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private Button buildSecondaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151;"
                + "-fx-font-size: 12px; -fx-background-radius: 6;"
                + "-fx-border-color: #D1D5DB; -fx-border-radius: 6;"
                + "-fx-padding: 8 12; -fx-cursor: hand;");
        return btn;
    }

    public BorderPane getRoot() { return root; }
}