package co.unicauca.piedrazul.presentation.views;

import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.presentation.controllers.RegisterAppointmentController;
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
 *
 * Patrón MVC — esta clase es la Vista: - Solo construye y maneja la interfaz
 * gráfica - Llama al controlador para cada acción del usuario - Lee el estado
 * del controlador para actualizar la UI - No tiene lógica de negocio
 *
 * Sin patrón Observer: la vista llama al controlador directamente en cada
 * evento (onAction) y luego lee el nuevo estado para actualizarse. Esto es más
 * simple y suficiente para una UI de un solo hilo (JavaFX).
 *
 * @author Equipo Piedrazul
 */
public class RegisterAppointmentView {

    private final RegisterAppointmentController controller;
    private final String loggedUserRole;

    // ── Controles que necesitan actualizarse desde múltiples métodos ──────────
    private TextField txtDocument;      // búsqueda de paciente
    private Label lblPatientResult; // resultado de la búsqueda
    private ComboBox<Doctor> cbDoctor;   // selector de médico
    private DatePicker datePicker; // selector de fecha
    private ComboBox<LocalTime> cbSlot;     // selector de hora
    private Label lblSlotsInfo;     // "N horarios disponibles"

    // Panel raíz que se monta en el BorderPane principal
    private BorderPane root;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public RegisterAppointmentView(RegisterAppointmentController controller,
            String loggedUserRole) {
        this.controller = controller;
        this.loggedUserRole = loggedUserRole;
        build();
    }

    // ── Construcción de la UI ─────────────────────────────────────────────────
    private void build() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8F9FA;");

        root.setTop(buildHeader());

        // Si no tiene permiso, muestra pantalla de acceso denegado
        if (!loggedUserRole.equals("AGENDADOR") && !loggedUserRole.equals("DOCTOR")) {
            root.setCenter(buildAccessDenied());
            return;
        }

        root.setCenter(buildBody());
    }

    /**
     * Encabezado con breadcrumb y título
     */
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

    /**
     * Cuerpo principal: formulario izquierdo + panel de disponibilidad derecho
     */
    private HBox buildBody() {
        HBox body = new HBox(20);
        body.setPadding(new Insets(24, 28, 24, 28));
        body.setAlignment(Pos.TOP_LEFT);

        VBox form = buildForm();
        HBox.setHgrow(form, Priority.ALWAYS);

        VBox availability = buildAvailabilityPanel();
        availability.setMinWidth(210);
        availability.setMaxWidth(210);

        body.getChildren().addAll(form, availability);
        return body;
    }

    // ── Formulario ────────────────────────────────────────────────────────────
    private VBox buildForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: white;"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: #E5E7EB;"
                + "-fx-border-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        Label sectionTitle = new Label("Datos de la cita");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        sectionTitle.setStyle("-fx-text-fill: #374151;");

        // Fila 1: paciente | médico
        HBox row1 = new HBox(16);
        VBox patientBox = buildPatientField();
        VBox doctorBox = buildDoctorField();
        HBox.setHgrow(patientBox, Priority.ALWAYS);
        HBox.setHgrow(doctorBox, Priority.ALWAYS);
        row1.getChildren().addAll(patientBox, doctorBox);

        // Fila 2: fecha | hora
        HBox row2 = new HBox(16);
        VBox dateBox = buildDateField();
        VBox slotBox = buildSlotField();
        HBox.setHgrow(dateBox, Priority.ALWAYS);
        HBox.setHgrow(slotBox, Priority.ALWAYS);
        row2.getChildren().addAll(dateBox, slotBox);

        // Botón registrar
        Button btnRegister = new Button("Registrar Cita");
        btnRegister.setStyle(
                "-fx-background-color: #2563EB; -fx-text-fill: white;"
                + "-fx-font-size: 14px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 11 32;"
                + "-fx-cursor: hand;");
        btnRegister.setOnMouseEntered(e
                -> btnRegister.setStyle(
                        "-fx-background-color: #1D4ED8; -fx-text-fill: white;"
                        + "-fx-font-size: 14px; -fx-font-weight: bold;"
                        + "-fx-background-radius: 8; -fx-padding: 11 32;"
                        + "-fx-cursor: hand;"));
        btnRegister.setOnMouseExited(e
                -> btnRegister.setStyle(
                        "-fx-background-color: #2563EB; -fx-text-fill: white;"
                        + "-fx-font-size: 14px; -fx-font-weight: bold;"
                        + "-fx-background-radius: 8; -fx-padding: 11 32;"
                        + "-fx-cursor: hand;"));

        // Al hacer clic llama al controlador y maneja el resultado
        btnRegister.setOnAction(e -> handleRegister());

        HBox btnRow = new HBox(btnRegister);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        form.getChildren().addAll(sectionTitle, row1, row2, btnRow);
        return form;
    }

    /**
     * Campo de búsqueda de paciente por número de documento
     */
    private VBox buildPatientField() {
        VBox box = new VBox(6);

        Label lbl = fieldLabel("Paciente *");

        // Fila: input + botón buscar
        HBox searchRow = new HBox(6);
        txtDocument = new TextField();
        txtDocument.setPromptText("N° documento...");
        txtDocument.setStyle(fieldStyle());
        HBox.setHgrow(txtDocument, Priority.ALWAYS);

        Button btnSearch = new Button("Buscar");
        btnSearch.setStyle(
                "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;"
                + "-fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 12;"
                + "-fx-cursor: hand; -fx-border-color: #D1D5DB; -fx-border-radius: 6;");

        // Buscar con botón o con Enter
        btnSearch.setOnAction(e -> handlePatientSearch());
        txtDocument.setOnAction(e -> handlePatientSearch());

        searchRow.getChildren().addAll(txtDocument, btnSearch);

        // Etiqueta de resultado (nombre del paciente o error)
        lblPatientResult = new Label("Sin paciente seleccionado");
        lblPatientResult.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        box.getChildren().addAll(lbl, searchRow, lblPatientResult);
        return box;
    }

    /**
     * ComboBox de selección de médico
     */
    private VBox buildDoctorField() {
        VBox box = new VBox(6);

        Label lbl = fieldLabel("Profesional *");

        cbDoctor = new ComboBox<>();
        cbDoctor.setPromptText("Seleccionar profesional...");
        cbDoctor.setMaxWidth(Double.MAX_VALUE);
        cbDoctor.setStyle(fieldStyle());

        // Muestra el nombre completo en el ComboBox
        cbDoctor.setConverter(new StringConverter<Doctor>() {
            @Override
            public String toString(Doctor d) {
                return d == null ? "" : d.getFullName();
            }

            @Override
            public Doctor fromString(String s) {
                return null;
            }
        });

        // Carga médicos activos al construir la vista
        List<Doctor> doctors = controller.loadActiveDoctors();
        cbDoctor.getItems().addAll(doctors);

        // Al cambiar médico: notifica al controlador y actualiza slots
        cbDoctor.setOnAction(e -> {
            Doctor selected = cbDoctor.getValue();
            if (selected != null) {
                controller.onDoctorSelected(selected);
                updateSlotsComboBox(); // actualiza el ComboBox de horas
            }
        });

        box.getChildren().addAll(lbl, cbDoctor);
        return box;
    }

    /**
     * DatePicker con rango restringido por parámetros del sistema
     */
    private VBox buildDateField() {
        VBox box = new VBox(6);

        Label lbl = fieldLabel("Fecha *");

        datePicker = new DatePicker();
        datePicker.setPromptText("Seleccionar fecha...");
        datePicker.setMaxWidth(Double.MAX_VALUE);

        // Lee el rango permitido desde los parámetros del sistema
        LocalDate startDate = controller.getStartDate();
        LocalDate endDate = controller.getEndDate();

        // Deshabilita fechas fuera del rango permitido
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                boolean outOfRange = date.isBefore(startDate) || date.isAfter(endDate);
                setDisable(outOfRange);
                if (outOfRange) {
                    // Visual: fechas deshabilitadas en gris
                    setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #D1D5DB;");
                }
            }
        });

        // Al cambiar fecha: notifica al controlador y actualiza slots
        datePicker.setOnAction(e -> {
            LocalDate selected = datePicker.getValue();
            if (selected != null) {
                controller.onDateSelected(selected);
                updateSlotsComboBox(); // actualiza el ComboBox de horas
            }
        });

        box.getChildren().addAll(lbl, datePicker);
        return box;
    }

    /**
     * ComboBox de slots de hora disponibles
     */
    private VBox buildSlotField() {
        VBox box = new VBox(6);

        Label lbl = fieldLabel("Hora *");

        cbSlot = new ComboBox<>();
        cbSlot.setPromptText("--:--");
        cbSlot.setMaxWidth(Double.MAX_VALUE);
        cbSlot.setStyle(fieldStyle());
        cbSlot.setDisable(true); // deshabilitado hasta tener médico y fecha

        // Muestra la hora en formato "HH:mm"
        cbSlot.setConverter(new StringConverter<LocalTime>() {
            @Override
            public String toString(LocalTime t) {
                return t == null ? "" : t.format(TIME_FMT);
            }

            @Override
            public LocalTime fromString(String s) {
                return null;
            }
        });

        // Al seleccionar hora: notifica al controlador
        cbSlot.setOnAction(e -> {
            LocalTime selected = cbSlot.getValue();
            if (selected != null) {
                controller.onSlotSelected(selected);
            }
        });

        // Info de cuántos slots hay disponibles
        lblSlotsInfo = new Label("Seleccione médico y fecha primero");
        lblSlotsInfo.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");

        box.getChildren().addAll(lbl, cbSlot, lblSlotsInfo);
        return box;
    }

    // ── Panel lateral de disponibilidad ──────────────────────────────────────
    private VBox buildAvailabilityPanel() {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white;"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: #E5E7EB;"
                + "-fx-border-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        Label title = new Label("Vista de disponibilidad");
        title.setFont(Font.font("System", FontWeight.BOLD, 13));
        title.setStyle("-fx-text-fill: #374151;");

        Label info = new Label(
                "Seleccione un profesional y una fecha para ver los horarios disponibles.");
        info.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        info.setWrapText(true);

        // Leyenda de colores
        VBox legend = new VBox(10);
        legend.setPadding(new Insets(10, 0, 0, 0));
        legend.getChildren().addAll(
                legendItem("#16A34A", "Con disponibilidad"),
                legendItem("#DC2626", "Sin disponibilidad"),
                legendItem("#2563EB", "Seleccionado")
        );

        // Nota sobre el rango de fechas habilitado
        VBox rangeBox = new VBox(4);
        rangeBox.setPadding(new Insets(12, 0, 0, 0));
        rangeBox.setStyle(
                "-fx-background-color: #EFF6FF; -fx-background-radius: 6; -fx-padding: 10;");

        Label rangeTitle = new Label("Rango habilitado");
        rangeTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #1D4ED8;");

        // Muestra el rango de fechas del sistema
        String start = controller.getStartDate().toString();
        String end = controller.getEndDate().toString();
        Label rangeVal = new Label(start + "\nal " + end);
        rangeVal.setStyle("-fx-text-fill: #1E40AF; -fx-font-size: 11px;");

        rangeBox.getChildren().addAll(rangeTitle, rangeVal);

        panel.getChildren().addAll(title, info, legend, rangeBox);
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

        Label msg = new Label(
                "Solo los usuarios con rol AGENDADOR o DOCTOR\npueden registrar citas manualmente.");
        msg.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");
        msg.setAlignment(Pos.CENTER);
        msg.setWrapText(true);

        box.getChildren().addAll(icon, title, msg);
        return box;
    }

    // ── Manejadores de eventos ────────────────────────────────────────────────
    /**
     * Busca el paciente por documento y actualiza la etiqueta de resultado
     */
    private void handlePatientSearch() {
        String input = txtDocument.getText().trim();
        if (input.isEmpty()) {
            return;
        }

        try {
            int docId = Integer.parseInt(input);
            Patient patient = controller.findPatientById(docId);

            if (patient != null) {
                // Encontrado: selecciona y muestra nombre en verde
                controller.onPatientSelected(patient);
                lblPatientResult.setText("✓ " + patient.getFullName());
                lblPatientResult.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px;");
            } else {
                lblPatientResult.setText("✗ Paciente no encontrado");
                lblPatientResult.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
            }
        } catch (NumberFormatException ex) {
            lblPatientResult.setText("✗ Ingrese un número válido");
            lblPatientResult.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
        }
    }

    /**
     * Llama al controlador para registrar la cita. Captura la excepción y la
     * muestra como alerta al usuario.
     */
    private void handleRegister() {
        try {
            boolean saved = controller.registerAppointment(loggedUserRole);
            if (saved) {
                showAlert(Alert.AlertType.INFORMATION,
                        "Éxito", "✓ Cita registrada exitosamente");
                clearForm(); // limpia la UI después de guardar
            } else {
                showAlert(Alert.AlertType.ERROR,
                        "Error", "No se pudo guardar la cita. Intente de nuevo.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            // El controlador o el Builder lanzaron un error de validación
            showAlert(Alert.AlertType.WARNING, "Datos incompletos", e.getMessage());
        }
    }

    /**
     * Actualiza el ComboBox de slots leyendo el estado del controlador. Se
     * llama después de que el controlador procesa médico o fecha.
     */
    private void updateSlotsComboBox() {
        List<LocalTime> slots = controller.getAvailableSlots();

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

    /**
     * Limpia todos los campos del formulario tras guardar
     */
    private void clearForm() {
        txtDocument.clear();
        lblPatientResult.setText("Sin paciente seleccionado");
        lblPatientResult.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        cbDoctor.setValue(null);
        datePicker.setValue(null);
        cbSlot.getItems().clear();
        cbSlot.setDisable(true);
        lblSlotsInfo.setText("Seleccione médico y fecha primero");
        lblSlotsInfo.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
        controller.reset(); // limpia también el estado del controlador
    }

    /**
     * Muestra un diálogo de alerta estándar de JavaFX
     */
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

    private String fieldStyle() {
        return "-fx-background-radius: 6; -fx-border-color: #D1D5DB;"
                + "-fx-border-radius: 6; -fx-padding: 7;";
    }

    /**
     * Retorna el panel raíz para montarlo en la ventana principal
     */
    public BorderPane getRoot() {
        return root;
    }
}
