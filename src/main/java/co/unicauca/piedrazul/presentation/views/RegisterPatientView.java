package co.unicauca.piedrazul.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Representa la interfaz gráfica para el registro de nuevos pacientes.
 *
 * @author Valentina Añasco
 */
public class RegisterPatientView extends VBox {

    private static final double VIEW_WIDTH = 500;
    private static final double PANEL_WIDTH = 420;

    private static final String COLOR_BLUE = "#2c6cf1";
    private static final String COLOR_LIGHT_BLUE_BG = "#f3f6fa";
    private static final String COLOR_TEXT_GRAY = "#636e72";
    private static final String COLOR_RED_ASTERISK = "#e74c3c";

    private TextField tfFirstName, tfMiddleName, tfFirstSurname, tfLastName;
    private TextField tfEmail, tfPhone, tfDocumentNumber;
    private DatePicker dpBirthDate;
    private ComboBox<String> cbDocumentType, cbGender;
    private PasswordField pfPassword, pfConfirmPassword;

    private TextField tfPasswordShown, tfConfirmShown;

    private CheckBox cbTerms;
    private Button btnCreate;
    private Hyperlink linkLogin;

    private Label lblInfoEmail, lblInfoPhone, lblInfoBirthDate, lblInfoDocType, lblInfoDocNumber, lblInfoPassword;
    private Label lblEyePass, lblEyeConfirm;

    private VBox vboxFirstName, vboxFirstSurname, vboxEmail, vboxPhone, vboxBirthDate, vboxGender,
            vboxDocumentType, vboxDocumentNumber, vboxPassword, vboxConfirmPassword;
    private HBox hboxTerms;
    private final VBox vboxGeneral;

    /**
     * Inicializa los componentes de la vista y configura el layout principal.
     */
    public RegisterPatientView() {
        this.setMinWidth(VIEW_WIDTH);
        this.setMaxWidth(VIEW_WIDTH);
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: " + COLOR_LIGHT_BLUE_BG + ";");
        this.setPadding(new Insets(30, 0, 20, 0));
        this.setSpacing(10);

        VBox header = createHeader();
        header.setPadding(new Insets(0, 0, 15, 0));

        VBox formPanel = new VBox();
        formPanel.setMinWidth(PANEL_WIDTH);
        formPanel.setMaxWidth(PANEL_WIDTH);
        formPanel.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 15px;"
                + "-fx-border-radius: 15px;"
                + "-fx-border-color: #e6e9ef;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);"
                + "-fx-padding: 30px;"
        );

        GridPane formGrid = createFormGrid();
        formPanel.getChildren().add(formGrid);
        VBox.setMargin(formPanel, new Insets(10, 0, 0, 0));

        vboxGeneral = new VBox();
        vboxGeneral.setAlignment(Pos.CENTER);
        VBox.setMargin(vboxGeneral, new Insets(5, 0, 0, 0));

        VBox footer = createFooter();
        footer.setPadding(new Insets(10, 0, 0, 0));

        this.getChildren().addAll(header, formPanel, vboxGeneral, footer);
    }

    /**
     * Crea la sección superior con el logo y títulos.
     */
    private VBox createHeader() {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);

        StackPane iconContainer = createEkgIcon();

        Label lblTitle = new Label("Crear cuenta");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 25));
        lblTitle.setTextFill(Color.web("#1e272e"));

        Label lblSubtitle = new Label("Regístrate para agendar tus citas médicas en Piedrazul");
        lblSubtitle.setFont(Font.font("Segoe UI", 13));
        lblSubtitle.setTextFill(Color.web(COLOR_TEXT_GRAY));

        header.getChildren().addAll(iconContainer, lblTitle, lblSubtitle);
        return header;
    }

    /**
     * Genera el icono visual del corazón y la línea EKG mediante Canvas.
     */
    private StackPane createEkgIcon() {
        StackPane container = new StackPane();
        container.setMinSize(62, 62);
        container.setMaxSize(62, 62);
        container.setStyle("-fx-background-color: " + COLOR_BLUE + "; -fx-background-radius: 14px;");

        Canvas canvas = new Canvas(52, 52);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.WHITE);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        gc.setLineWidth(2.2);
        gc.beginPath();
        double cx = 26, cy = 25, w = 13;
        gc.moveTo(cx, cy + 19);
        gc.bezierCurveTo(cx - 20, cy + 6, cx - 20, cy - 12, cx - w, cy - 12);
        gc.bezierCurveTo(cx - 6, cy - 12, cx, cy - 4, cx, cy - 6);
        gc.bezierCurveTo(cx, cy - 6, cx + 6, cy - 12, cx + w, cy - 12);
        gc.bezierCurveTo(cx + 20, cy - 12, cx + 20, cy + 6, cx, cy + 19);
        gc.closePath();
        gc.stroke();

        gc.setLineWidth(2.0);
        double[] x = {13, 20, 23, 27, 31, 39};
        double[] y = {27, 27, 36, 18, 27, 27};
        gc.strokePolyline(x, y, x.length);

        container.getChildren().add(canvas);
        return container;
    }

    /**
     * Organiza los campos del formulario en una cuadrícula.
     */
    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col, col);

        int r = 0;

        // --- Fila 1: Primer nombre / Apellido ---
        vboxFirstName = buildInputVBox("Primer nombre", true, null);
        tfFirstName = (TextField) getFieldFrom(vboxFirstName);
        grid.add(vboxFirstName, 0, r);

        vboxFirstSurname = buildInputVBox("Primer Apellido", true, null);
        tfFirstSurname = (TextField) getFieldFrom(vboxFirstSurname);
        grid.add(vboxFirstSurname, 1, r++);

        // --- Fila 2: Segundo nombre / Segundo apellido ---
        VBox vboxMiddleName = buildInputVBox("Segundo nombre", false, null);
        tfMiddleName = (TextField) getFieldFrom(vboxMiddleName);
        grid.add(vboxMiddleName, 0, r);

        VBox vboxLastName = buildInputVBox("Segundo apellido", false, null);
        tfLastName = (TextField) getFieldFrom(vboxLastName);
        grid.add(vboxLastName, 1, r++);

        // --- Fila 3: Correo / Teléfono ---
        lblInfoEmail = new Label("ⓘ");
        vboxEmail = buildInputVBox("Correo electrónico", true, lblInfoEmail);
        tfEmail = (TextField) getFieldFrom(vboxEmail);
        grid.add(vboxEmail, 0, r);

        lblInfoPhone = new Label("ⓘ");
        vboxPhone = buildInputVBox("Teléfono", true, lblInfoPhone);
        tfPhone = (TextField) getFieldFrom(vboxPhone);
        grid.add(vboxPhone, 1, r++);

        // --- Fila 4: Fecha de nacimiento / Género ---
        lblInfoBirthDate = new Label("ⓘ");
        vboxBirthDate = buildDatePickerVBox(lblInfoBirthDate);
        dpBirthDate = (DatePicker) getFieldFrom(vboxBirthDate);
        grid.add(vboxBirthDate, 0, r);

        vboxGender = buildGenderComboBoxVBox();
        cbGender = (ComboBox<String>) getFieldFrom(vboxGender);
        grid.add(vboxGender, 1, r++);

        // --- Fila 5: Tipo de documento / Número de documento ---
        lblInfoDocType = new Label("ⓘ");
        vboxDocumentType = buildComboBoxVBox(lblInfoDocType);
        cbDocumentType = (ComboBox<String>) getFieldFrom(vboxDocumentType);
        grid.add(vboxDocumentType, 0, r);

        lblInfoDocNumber = new Label("ⓘ");
        vboxDocumentNumber = buildInputVBox("Número de documento", true, lblInfoDocNumber);
        tfDocumentNumber = (TextField) getFieldFrom(vboxDocumentNumber);
        grid.add(vboxDocumentNumber, 1, r++);

        // --- Fila 6: Contraseña / Confirmar contraseña ---
        lblInfoPassword = new Label("ⓘ");
        vboxPassword = buildPasswordVBox("Contraseña", true, lblInfoPassword, false);
        pfPassword = (PasswordField) getPasswordFieldFrom(vboxPassword);
        grid.add(vboxPassword, 0, r);

        vboxConfirmPassword = buildPasswordVBox("Confirmar contraseña", true, null, true);
        pfConfirmPassword = (PasswordField) getPasswordFieldFrom(vboxConfirmPassword);
        grid.add(vboxConfirmPassword, 1, r++);

        // --- Fila 7: Términos y condiciones ---
        hboxTerms = createCheckboxLinks();
        grid.add(hboxTerms, 0, r++, 2, 1);

        // --- Fila 8: Botón crear cuenta ---
        btnCreate = new Button("Crear cuenta");
        btnCreate.setMaxWidth(Double.MAX_VALUE);
        btnCreate.setPadding(new Insets(12));
        btnCreate.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnCreate.setTextFill(Color.WHITE);
        btnCreate.setStyle("-fx-background-color: " + COLOR_BLUE + "; -fx-background-radius: 8px; -fx-cursor: hand;");
        grid.add(btnCreate, 0, r, 2, 1);

        return grid;
    }

    /**
     * Construye un contenedor vertical para entradas de texto estándar.
     */
    private VBox buildInputVBox(String labelText, boolean required, Label infoIcon) {
        VBox vBox = new VBox(5);
        vBox.getChildren().addAll(buildLabelBox(labelText, required, infoIcon), buildTextField());
        return vBox;
    }

    /**
     * Crea el selector de fecha con formato personalizado.
     */
    private VBox buildDatePickerVBox(Label infoIcon) {
        VBox vBox = new VBox(5);
        DatePicker dp = new DatePicker();
        dp.setPromptText("dd/MM/yyyy");
        dp.setMaxWidth(Double.MAX_VALUE);
        applyStandardFieldStyle(dp);
        dp.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                return (date != null) ? fmt.format(date) : "";
            }

            @Override
            public LocalDate fromString(String text) {
                try {
                    return (text == null || text.isEmpty()) ? null : LocalDate.parse(text, fmt);
                } catch (Exception e) {
                    return null;
                }
            }
        });
        vBox.getChildren().addAll(buildLabelBox("Fecha de nacimiento", true, infoIcon), dp);
        return vBox;
    }

    /**
     * Construye el menú desplegable para tipos de identificación.
     */
    private VBox buildComboBoxVBox(Label infoIcon) {
        VBox vBox = new VBox(5);
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("Cédula de ciudadanía", "Tarjeta de identidad", "Cédula de extranjería", "Pasaporte", "Registro civil");
        combo.setPromptText("Seleccionar...");
        combo.setMaxWidth(Double.MAX_VALUE);
        applyStandardFieldStyle(combo);
        vBox.getChildren().addAll(buildLabelBox("Tipo de documento", true, infoIcon), combo);
        return vBox;
    }

    /**
     * Construye el menú desplegable para el género del paciente.
     */
    private VBox buildGenderComboBoxVBox() {
        VBox vBox = new VBox(5);
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("Hombre", "Mujer", "Otro");
        combo.setPromptText("Seleccionar...");
        combo.setMaxWidth(Double.MAX_VALUE);
        applyStandardFieldStyle(combo);
        vBox.getChildren().addAll(buildLabelBox("Género", true, null), combo);
        return vBox;
    }

    /**
     * Crea el campo de contraseña con soporte para alternar visibilidad.
     */
    private VBox buildPasswordVBox(String labelText, boolean required, Label infoIcon, boolean isConfirm) {
        VBox vBox = new VBox(5);
        PasswordField pf = new PasswordField();
        TextField tfMirror = new TextField();

        applyStandardFieldStyle(pf);
        applyStandardFieldStyle(tfMirror);

        tfMirror.setManaged(false);
        tfMirror.setVisible(false);

        Label lblEye = new Label("👁");
        lblEye.setTextFill(Color.web(COLOR_TEXT_GRAY));
        lblEye.setPadding(new Insets(0, 10, 0, 0));
        lblEye.setStyle("-fx-cursor: hand;");

        if (isConfirm) {
            this.lblEyeConfirm = lblEye;
            this.tfConfirmShown = tfMirror;
        } else {
            this.lblEyePass = lblEye;
            this.tfPasswordShown = tfMirror;
        }

        StackPane stack = new StackPane(pf, tfMirror, lblEye);
        StackPane.setAlignment(lblEye, Pos.CENTER_RIGHT);

        vBox.getChildren().addAll(buildLabelBox(labelText, required, infoIcon), stack);
        return vBox;
    }

    /**
     * Genera la fila de etiquetas (Nombre, Asterisco, Info).
     */
    private HBox buildLabelBox(String labelText, boolean required, Label infoIcon) {
        HBox labelBox = new HBox(3);
        labelBox.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", 12));
        label.setTextFill(Color.web(COLOR_TEXT_GRAY));
        labelBox.getChildren().add(label);

        if (required) {
            Label asterisk = new Label("*");
            asterisk.setTextFill(Color.web(COLOR_RED_ASTERISK));
            labelBox.getChildren().add(asterisk);
        }

        if (infoIcon != null) {
            infoIcon.setTextFill(Color.web(COLOR_TEXT_GRAY));
            infoIcon.setStyle("-fx-cursor: help;");
            labelBox.getChildren().add(infoIcon);
        }

        return labelBox;
    }

    private TextField buildTextField() {
        TextField tf = new TextField();
        applyStandardFieldStyle(tf);
        return tf;
    }

    private void applyStandardFieldStyle(Control field) {
        field.setPadding(new Insets(12));
        field.setStyle("-fx-background-color: white; -fx-border-color: #d1d8e0; -fx-border-radius: 8px; "
                + "-fx-background-radius: 8px; -fx-prompt-text-fill: #a4b0be; -fx-text-fill: #2d3436;");
    }

    private Control getFieldFrom(VBox vBox) {
        return (Control) vBox.getChildren().get(1);
    }

    private PasswordField getPasswordFieldFrom(VBox vBox) {
        StackPane stack = (StackPane) vBox.getChildren().get(1);
        return (PasswordField) stack.getChildren().get(0);
    }

    /**
     * Crea la sección de aceptación de políticas legales.
     */
    private HBox createCheckboxLinks() {
        HBox hbox = new HBox(5);
        hbox.setAlignment(Pos.TOP_LEFT);
        cbTerms = new CheckBox();
        cbTerms.setTranslateY(2);

        TextFlow textFlow = new TextFlow();
        Text t1 = new Text("Acepto los ");
        t1.setFill(Color.web(COLOR_TEXT_GRAY));
        Hyperlink l1 = new Hyperlink("términos y condiciones");
        l1.setStyle("-fx-text-fill: " + COLOR_BLUE + "; -fx-padding: 0;");
        Text t2 = new Text(" y la ");
        t2.setFill(Color.web(COLOR_TEXT_GRAY));
        Hyperlink l2 = new Hyperlink("política de privacidad");
        l2.setStyle("-fx-text-fill: " + COLOR_BLUE + "; -fx-padding: 0;");

        textFlow.getChildren().addAll(t1, l1, t2, l2);
        hbox.getChildren().addAll(cbTerms, textFlow);
        return hbox;
    }

    /**
     * Crea el pie de página con el enlace de navegación al login.
     */
    private VBox createFooter() {
        VBox footer = new VBox();
        footer.setAlignment(Pos.CENTER);
        HBox hbox = new HBox(5);
        hbox.setAlignment(Pos.CENTER);
        Label label = new Label("¿Ya tienes cuenta?");
        label.setTextFill(Color.web(COLOR_TEXT_GRAY));
        linkLogin = new Hyperlink("Inicia sesión");
        linkLogin.setStyle("-fx-text-fill: " + COLOR_BLUE + ";");
        hbox.getChildren().addAll(label, linkLogin);
        footer.getChildren().add(hbox);
        return footer;
    }

    /**
     * Inserta visualmente los mensajes de error bajo cada campo. Orden
     * esperado: firstName, firstSurname, email, phone, birthDate, gender,
     * documentType, documentNumber, password, confirmPassword, terms, general.
     * @param errors
     */
    public void injectErrorLabels(Label... errors) {
        VBox[] containers = {
            vboxFirstName, vboxFirstSurname, vboxEmail, vboxPhone,
            vboxBirthDate, vboxGender, vboxDocumentType, vboxDocumentNumber,
            vboxPassword, vboxConfirmPassword
        };
        for (int i = 0; i < containers.length; i++) {
            containers[i].getChildren().add(errors[i]);
        }
        hboxTerms.getChildren().add(errors[10]);
        vboxGeneral.getChildren().add(errors[11]);
    }

    // --- GETTERS ---
    public TextField getTfFirstName() {
        return tfFirstName;
    }

    public TextField getTfMiddleName() {
        return tfMiddleName;
    }

    public TextField getTfFirstSurname() {
        return tfFirstSurname;
    }

    public TextField getTfLastName() {
        return tfLastName;
    }

    public TextField getTfEmail() {
        return tfEmail;
    }

    public TextField getTfPhone() {
        return tfPhone;
    }

    public DatePicker getDpBirthDate() {
        return dpBirthDate;
    }

    public ComboBox<String> getCbDocumentType() {
        return cbDocumentType;
    }

    public ComboBox<String> getCbGender() {
        return cbGender;
    }

    public TextField getTfDocumentNumber() {
        return tfDocumentNumber;
    }

    public PasswordField getPfPassword() {
        return pfPassword;
    }

    public PasswordField getPfConfirmPassword() {
        return pfConfirmPassword;
    }

    public CheckBox getCbTerms() {
        return cbTerms;
    }

    public Button getBtnCreate() {
        return btnCreate;
    }

    public Hyperlink getLinkLogin() {
        return linkLogin;
    }

    public Label getIconInfoEmail() {
        return lblInfoEmail;
    }

    public Label getIconInfoPhone() {
        return lblInfoPhone;
    }

    public Label getIconInfoBirthDate() {
        return lblInfoBirthDate;
    }

    public Label getIconInfoDocType() {
        return lblInfoDocType;
    }

    public Label getIconInfoDocNumber() {
        return lblInfoDocNumber;
    }

    public Label getIconInfoPassword() {
        return lblInfoPassword;
    }

    public Label getLblEyePass() {
        return lblEyePass;
    }

    public Label getLblEyeConfirm() {
        return lblEyeConfirm;
    }

    public TextField getTfPasswordShown() {
        return tfPasswordShown;
    }

    public TextField getTfConfirmShown() {
        return tfConfirmShown;
    }
}
