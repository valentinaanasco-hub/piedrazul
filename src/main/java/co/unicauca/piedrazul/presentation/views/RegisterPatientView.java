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
 * Vista de registro de pacientes (Crear Cuenta).
 *
 * @author Valentina Añasco
 */
public class RegisterPatientView extends VBox {

    // --- Constantes de layout ---
    private static final double VIEW_WIDTH = 500;
    private static final double PANEL_WIDTH = 420;

    // --- Colores ---
    private static final String COLOR_BLUE = "#2c6cf1";
    private static final String COLOR_LIGHT_BLUE_BG = "#f3f6fa";
    private static final String COLOR_TEXT_GRAY = "#636e72";
    private static final String COLOR_RED_ASTERISK = "#e74c3c";

    // --- Campos del formulario (atributos de instancia para el controlador) ---
    private TextField tfFirstName;
    private TextField tfMiddleName;
    private TextField tfFirstSurname;
    private TextField tfLastName;
    private TextField tfEmail;
    private TextField tfPhone;
    private DatePicker dpBirthDate;
    private ComboBox<String> cbDocumentType;
    private TextField tfDocumentNumber;
    private PasswordField pfPassword;
    private PasswordField pfConfirmPassword;
    private CheckBox cbTerms;
    private Button btnCreate;
    private Hyperlink linkLogin;

    // --- VBox contenedores de cada campo (para inyectar labels de error) ---
    private VBox vboxFirstName;
    private VBox vboxFirstSurname;
    private VBox vboxEmail;
    private VBox vboxPhone;
    private VBox vboxBirthDate;
    private VBox vboxDocumentType;
    private VBox vboxDocumentNumber;
    private VBox vboxPassword;
    private VBox vboxConfirmPassword;
    private HBox hboxTerms;
    private final VBox vboxGeneral;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public RegisterPatientView() {
        this.setMinWidth(VIEW_WIDTH);
        this.setMaxWidth(VIEW_WIDTH);
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: " + COLOR_LIGHT_BLUE_BG + ";");
        this.setPadding(new Insets(30, 0, 20, 0));
        this.setSpacing(10);

        // --- Header ---
        VBox header = createHeader();
        header.setPadding(new Insets(0, 0, 15, 0));

        // --- Panel del formulario ---
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

        // --- Contenedor de error general (fuera del panel) ---
        vboxGeneral = new VBox();
        vboxGeneral.setAlignment(Pos.CENTER);
        VBox.setMargin(vboxGeneral, new Insets(5, 0, 0, 0));

        // --- Footer ---
        VBox footer = createFooter();
        footer.setPadding(new Insets(10, 0, 0, 0));

        this.getChildren().addAll(header, formPanel, vboxGeneral, footer);
    }

    // =========================================================================
    // HEADER
    // =========================================================================
    private VBox createHeader() {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);

        StackPane iconContainer = createEkgIcon();

        Label lblTitle = new Label("Crear cuenta");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 25));
        lblTitle.setTextFill(Color.web("#1e272e"));
        VBox.setMargin(lblTitle, new Insets(5, 0, 0, 0));

        Label lblSubtitle = new Label("Regístrate para agendar tus citas médicas en Piedrazul");
        lblSubtitle.setFont(Font.font("Segoe UI", 13));
        lblSubtitle.setTextFill(Color.web(COLOR_TEXT_GRAY));

        header.getChildren().addAll(iconContainer, lblTitle, lblSubtitle);
        return header;
    }

    private StackPane createEkgIcon() {
        StackPane container = new StackPane();
        container.setMinSize(62, 62);
        container.setMaxSize(62, 62);
        container.setStyle(
                "-fx-background-color: " + COLOR_BLUE + ";"
                + "-fx-background-radius: 14px;"
        );

        Canvas canvas = new Canvas(52, 52);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setStroke(Color.WHITE);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        // --- Corazón: contorno blanco ---
        gc.setLineWidth(2.2);
        gc.beginPath();

        double cx = 26, cy = 25;
        double w = 13;

        gc.moveTo(cx, cy + 19);
        gc.bezierCurveTo(cx - 20, cy + 6, cx - 20, cy - 12, cx - w, cy - 12);
        gc.bezierCurveTo(cx - 6, cy - 12, cx, cy - 4, cx, cy - 6);
        gc.bezierCurveTo(cx, cy - 6, cx + 6, cy - 12, cx + w, cy - 12);
        gc.bezierCurveTo(cx + 20, cy - 12, cx + 20, cy + 6, cx, cy + 19);

        gc.closePath();
        gc.stroke();

        // --- EKG simple: plano → baja → sube → plano ---
        gc.setLineWidth(2.0);
        double[] x = {13, 20, 23, 27, 31, 39};
        double[] y = {27, 27, 36, 18, 27, 27};
        gc.strokePolyline(x, y, x.length);

        container.getChildren().add(canvas);
        return container;
    }

    // =========================================================================
    // FORMULARIO
    // =========================================================================
    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        int row = 0;

        // --- Fila 1: Primer nombre / Apellido ---
        vboxFirstName = buildInputVBox("Primer nombre", true, false);
        tfFirstName = (TextField) getFieldFrom(vboxFirstName);
        grid.add(vboxFirstName, 0, row);

        vboxFirstSurname = buildInputVBox("Apellido", true, false);
        tfFirstSurname = (TextField) getFieldFrom(vboxFirstSurname);
        grid.add(vboxFirstSurname, 1, row);
        row++;

        // --- Fila 2: Segundo nombre / Segundo apellido ---
        VBox vboxMiddleName = buildInputVBox("Segundo nombre", false, false);
        tfMiddleName = (TextField) getFieldFrom(vboxMiddleName);
        grid.add(vboxMiddleName, 0, row);

        VBox vboxLastName = buildInputVBox("Segundo apellido", false, false);
        tfLastName = (TextField) getFieldFrom(vboxLastName);
        grid.add(vboxLastName, 1, row);
        row++;

        // --- Fila 3: Correo / Teléfono ---
        vboxEmail = buildInputVBox("Correo electrónico", true, true);
        tfEmail = (TextField) getFieldFrom(vboxEmail);
        grid.add(vboxEmail, 0, row);

        vboxPhone = buildInputVBox("Teléfono", true, true);
        tfPhone = (TextField) getFieldFrom(vboxPhone);
        grid.add(vboxPhone, 1, row);
        row++;

        // --- Fila 4: Fecha de nacimiento ---
        vboxBirthDate = buildDatePickerVBox();
        dpBirthDate = (DatePicker) getFieldFrom(vboxBirthDate);
        grid.add(vboxBirthDate, 0, row);
        grid.add(new Label(), 1, row);
        row++;

        // --- Fila 5: Tipo de documento / Número de documento ---
        vboxDocumentType = buildComboBoxVBox();
        cbDocumentType = (ComboBox<String>) getFieldFrom(vboxDocumentType);
        grid.add(vboxDocumentType, 0, row);

        vboxDocumentNumber = buildInputVBox("Número de documento", true, true);
        tfDocumentNumber = (TextField) getFieldFrom(vboxDocumentNumber);
        grid.add(vboxDocumentNumber, 1, row);
        row++;

        // --- Fila 6: Contraseña / Confirmar contraseña ---
        vboxPassword = buildPasswordVBox("Contraseña", true, true);
        pfPassword = (PasswordField) getPasswordFieldFrom(vboxPassword);
        grid.add(vboxPassword, 0, row);

        vboxConfirmPassword = buildPasswordVBox("Confirmar contraseña", true, false);
        pfConfirmPassword = (PasswordField) getPasswordFieldFrom(vboxConfirmPassword);
        grid.add(vboxConfirmPassword, 1, row);
        row++;

        // --- Fila 7: Términos y condiciones ---
        hboxTerms = createCheckboxLinks();
        grid.add(hboxTerms, 0, row, 2, 1);
        row++;

        // --- Fila 8: Botón crear cuenta ---
        btnCreate = new Button("Crear cuenta");
        btnCreate.setMaxWidth(Double.MAX_VALUE);
        btnCreate.setPadding(new Insets(12));
        btnCreate.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnCreate.setTextFill(Color.WHITE);
        btnCreate.setStyle(
                "-fx-background-color: " + COLOR_BLUE + ";"
                + "-fx-background-radius: 8px;"
                + "-fx-cursor: hand;"
        );
        grid.add(btnCreate, 0, row, 2, 1);

        return grid;
    }

    // =========================================================================
    // BUILDERS DE CAMPOS
    // =========================================================================
    /**
     * Construye un VBox con etiqueta + TextField estándar.
     */
    private VBox buildInputVBox(String labelText, boolean required, boolean hasInfo) {
        VBox vBox = new VBox(5);
        vBox.getChildren().addAll(buildLabelBox(labelText, required, hasInfo), buildTextField());
        return vBox;
    }

    /**
     * Construye el VBox del DatePicker de fecha de nacimiento.
     */
    private VBox buildDatePickerVBox() {
        VBox vBox = new VBox(5);

        DatePicker dp = new DatePicker();
        dp.setPromptText("dd/MM/yyyy");
        dp.setMaxWidth(Double.MAX_VALUE);
        dp.setStyle(
                "-fx-background-color: white;"
                + "-fx-border-color: #d1d8e0;"
                + "-fx-border-radius: 8px;"
                + "-fx-background-radius: 8px;"
                + "-fx-font-size: 13px;"
        );
        dp.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                return (date != null) ? fmt.format(date) : "";
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(text, fmt);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        vBox.getChildren().addAll(buildLabelBox("Fecha de nacimiento", true, true), dp);
        return vBox;
    }

    /**
     * Construye el VBox del ComboBox de tipo de documento.
     */
    private VBox buildComboBoxVBox() {
        VBox vBox = new VBox(5);

        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(
                "Cédula de ciudadanía",
                "Tarjeta de identidad",
                "Cédula de extranjería",
                "Pasaporte",
                "Registro civil"
        );
        combo.setPromptText("Seleccionar...");
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle(
                "-fx-background-color: white;"
                + "-fx-border-color: #d1d8e0;"
                + "-fx-border-radius: 8px;"
                + "-fx-background-radius: 8px;"
                + "-fx-font-size: 13px;"
        );

        vBox.getChildren().addAll(buildLabelBox("Tipo de documento", true, true), combo);
        return vBox;
    }

    /**
     * Construye el VBox de un campo de contraseña con ícono de ojo.
     */
    private VBox buildPasswordVBox(String labelText, boolean required, boolean hasInfo) {
        VBox vBox = new VBox(5);

        PasswordField pf = new PasswordField();
        applyStandardFieldStyle(pf);

        StackPane stack = new StackPane();
        stack.setAlignment(Pos.CENTER_RIGHT);

        Label lblEye = new Label("👁");
        lblEye.setTextFill(Color.web(COLOR_TEXT_GRAY));
        lblEye.setPadding(new Insets(0, 10, 0, 0));

        stack.getChildren().addAll(pf, lblEye);
        vBox.getChildren().addAll(buildLabelBox(labelText, required, hasInfo), stack);
        return vBox;
    }

    // =========================================================================
    // INYECCIÓN DE LABELS DE ERROR (llamado por el controlador)
    // =========================================================================
    /**
     * Recibe los Labels de error creados por el controlador e los inserta
     * debajo de su campo correspondiente.
     *
     * @param errFirstName
     * @param errFirstSurname
     * @param errEmail
     * @param errPhone
     * @param errBirthDate
     * @param errDocumentType
     * @param errDocumentNumber
     * @param errPassword
     * @param errConfirmPassword
     * @param errTerms
     * @param errGeneral
     */
    public void injectErrorLabels(
            Label errFirstName, Label errFirstSurname,
            Label errEmail, Label errPhone,
            Label errBirthDate, Label errDocumentType,
            Label errDocumentNumber,
            Label errPassword, Label errConfirmPassword,
            Label errTerms, Label errGeneral) {

        vboxFirstName.getChildren().add(errFirstName);
        vboxFirstSurname.getChildren().add(errFirstSurname);
        vboxEmail.getChildren().add(errEmail);
        vboxPhone.getChildren().add(errPhone);
        vboxBirthDate.getChildren().add(errBirthDate);
        vboxDocumentType.getChildren().add(errDocumentType);
        vboxDocumentNumber.getChildren().add(errDocumentNumber);
        vboxPassword.getChildren().add(errPassword);
        vboxConfirmPassword.getChildren().add(errConfirmPassword);
        hboxTerms.getChildren().add(errTerms);
        vboxGeneral.getChildren().add(errGeneral);
    }

    // =========================================================================
    // UTILIDADES INTERNAS
    // =========================================================================
    private HBox buildLabelBox(String labelText, boolean required, boolean hasInfo) {
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

        if (hasInfo) {
            Label info = new Label("ⓘ");
            info.setTextFill(Color.web(COLOR_TEXT_GRAY));
            labelBox.getChildren().add(info);
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
        field.setStyle(
                "-fx-background-color: white;"
                + "-fx-border-color: #d1d8e0;"
                + "-fx-border-radius: 8px;"
                + "-fx-background-radius: 8px;"
                + "-fx-prompt-text-fill: #a4b0be;"
                + "-fx-text-fill: #2d3436;"
        );
    }

    /**
     * Extrae el Control (TextField, DatePicker, ComboBox) de un VBox, asumiendo
     * que siempre está en el índice 1 (después del labelBox).
     */
    private Control getFieldFrom(VBox vBox) {
        return (Control) vBox.getChildren().get(1);
    }

    /**
     * Extrae el PasswordField desde el StackPane que está en el VBox.
     */
    private PasswordField getPasswordFieldFrom(VBox vBox) {
        StackPane stack = (StackPane) vBox.getChildren().get(1);
        return (PasswordField) stack.getChildren().get(0);
    }

    // =========================================================================
    // FOOTER
    // =========================================================================
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

    // =========================================================================
    // GETTERS (para el controlador)
    // =========================================================================
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
}
