package co.unicauca.piedrazul.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vista de inicio de sesión de la Red de Servicios Médicos Piedrazul.
 *
 * @author Valentina Añasco
 */
public class LoginView extends VBox {

    private static final double VIEW_WIDTH = 500;
    private static final double PANEL_WIDTH = 420;

    private static final String COLOR_BLUE = "#2c6cf1";
    private static final String COLOR_LIGHT_BLUE_BG = "#f3f6fa";
    private static final String COLOR_TEXT_GRAY = "#636e72";
    private static final String COLOR_TEXT_DARK = "#1e272e";
    private static final String COLOR_RED_ASTERISK = "#e74c3c";

    // --- Componentes del formulario ---
    private TextField tfEmail;
    private PasswordField pfPassword;
    private TextField tfPasswordShown;
    private Label lblEyePass;
    private CheckBox cbRemember;
    private Hyperlink linkForgotPassword;
    private Button btnLogin;
    private Hyperlink linkRegister;

    // --- Contenedores para inyección de errores ---
    private VBox vboxEmail;
    private VBox vboxPassword;
    private VBox vboxGeneral;

    /**
     * Inicializa los componentes de la vista y configura el layout principal.
     */
    public LoginView() {
        this.setMinWidth(VIEW_WIDTH);
        this.setMaxWidth(VIEW_WIDTH);
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: " + COLOR_LIGHT_BLUE_BG + ";");
        this.setPadding(new Insets(60, 0, 40, 0));
        this.setSpacing(25);

        this.getChildren().addAll(createHeader(), createFormPanel(), createFooter());
    }

    // --- HEADER ---
    /**
     * Crea el encabezado con el ícono EKG, título y subtítulo.
     */
    private VBox createHeader() {
        VBox header = new VBox(12);
        header.setAlignment(Pos.CENTER);

        Label title = new Label("Piedrazul");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(COLOR_TEXT_DARK));

        Label subtitle = new Label("Citas Médicas");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitle.setTextFill(Color.web(COLOR_TEXT_GRAY));

        header.getChildren().addAll(createEkgIcon(), title, subtitle);
        return header;
    }

    /**
     * Genera el ícono visual del corazón y la línea EKG mediante Canvas.
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

    // --- PANEL DEL FORMULARIO ---
    /**
     * Crea el panel central blanco con todos los campos del formulario.
     */
    private VBox createFormPanel() {
        VBox panel = new VBox(20);
        panel.setMinWidth(PANEL_WIDTH);
        panel.setMaxWidth(PANEL_WIDTH);
        panel.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 20px;"
                + "-fx-border-radius: 20px;"
                + "-fx-border-color: #e6e9ef;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 15, 0, 0, 8);"
                + "-fx-padding: 40px;"
        );

        Label lblTitle = new Label("Iniciar sesión");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web(COLOR_TEXT_DARK));

        // --- Campo correo ---
        vboxEmail = buildFieldGroup("Correo electrónico", true);
        tfEmail = (TextField) getFieldFrom(vboxEmail);
        tfEmail.setPromptText("usuario@gmail.com o nombre de usuario");

        // --- Campo contraseña ---
        vboxPassword = buildPasswordGroup();

        // --- Recordarme / ¿Olvidaste tu contraseña? ---
        HBox rowOptions = buildOptionsRow();

        // --- Error general ---
        vboxGeneral = new VBox();
        vboxGeneral.setAlignment(Pos.CENTER_LEFT);

        // --- Botón iniciar sesión ---
        btnLogin = new Button("Iniciar sesión");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPadding(new Insets(14));
        btnLogin.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        btnLogin.setTextFill(Color.WHITE);
        btnLogin.setCursor(Cursor.HAND);
        btnLogin.setStyle(
                "-fx-background-color: " + COLOR_BLUE + ";"
                + "-fx-background-radius: 10px;"
        );

        panel.getChildren().addAll(lblTitle, vboxEmail, vboxPassword, rowOptions, vboxGeneral, btnLogin);
        return panel;
    }

    /**
     * Construye un grupo de campo estándar con etiqueta y TextField.
     */
    private VBox buildFieldGroup(String labelText, boolean required) {
        VBox group = new VBox(6);

        HBox labelBox = new HBox(3);
        labelBox.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", 13));
        label.setTextFill(Color.web(COLOR_TEXT_GRAY));
        labelBox.getChildren().add(label);

        if (required) {
            Label asterisk = new Label("*");
            asterisk.setTextFill(Color.web(COLOR_RED_ASTERISK));
            labelBox.getChildren().add(asterisk);
        }

        TextField tf = new TextField();
        applyFieldStyle(tf);

        group.getChildren().addAll(labelBox, tf);
        return group;
    }

    /**
     * Construye el grupo del campo de contraseña con toggle de visibilidad.
     */
    private VBox buildPasswordGroup() {
        VBox group = new VBox(6);

        HBox labelBox = new HBox(3);
        labelBox.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Contraseña");
        label.setFont(Font.font("Segoe UI", 13));
        label.setTextFill(Color.web(COLOR_TEXT_GRAY));

        Label asterisk = new Label("*");
        asterisk.setTextFill(Color.web(COLOR_RED_ASTERISK));

        labelBox.getChildren().addAll(label, asterisk);

        pfPassword = new PasswordField();
        tfPasswordShown = new TextField();
        tfPasswordShown.setVisible(false);
        tfPasswordShown.setManaged(false);

        applyFieldStyle(pfPassword);
        applyFieldStyle(tfPasswordShown);

        lblEyePass = new Label("👁");
        lblEyePass.setCursor(Cursor.HAND);
        lblEyePass.setPadding(new Insets(0, 15, 0, 0));
        StackPane.setAlignment(lblEyePass, Pos.CENTER_RIGHT);

        StackPane stack = new StackPane(pfPassword, tfPasswordShown, lblEyePass);
        StackPane.setAlignment(lblEyePass, Pos.CENTER_RIGHT);

        group.getChildren().addAll(labelBox, stack);
        return group;
    }

    /**
     * Construye la fila con el checkbox Recordarme y el link de contraseña
     * olvidada.
     */
    private HBox buildOptionsRow() {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        cbRemember = new CheckBox("Recordarme");
        cbRemember.setFont(Font.font("Segoe UI", 13));
        cbRemember.setTextFill(Color.web(COLOR_TEXT_GRAY));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        linkForgotPassword = new Hyperlink("¿Olvidaste tu contraseña?");
        linkForgotPassword.setStyle("-fx-text-fill: " + COLOR_BLUE + "; -fx-padding: 0;");
        linkForgotPassword.setFont(Font.font("Segoe UI", 13));

        row.getChildren().addAll(cbRemember, spacer, linkForgotPassword);
        return row;
    }

    // --- FOOTER ---
    /**
     * Crea el pie de página con el enlace de navegación al registro.
     */
    private VBox createFooter() {
        VBox footer = new VBox();
        footer.setAlignment(Pos.CENTER);

        HBox hbox = new HBox(5);
        hbox.setAlignment(Pos.CENTER);

        Label label = new Label("¿No tienes cuenta?");
        label.setTextFill(Color.web(COLOR_TEXT_GRAY));
        label.setFont(Font.font("Segoe UI", 13));

        linkRegister = new Hyperlink("Regístrate");
        linkRegister.setStyle("-fx-text-fill: " + COLOR_BLUE + "; -fx-font-weight: bold; -fx-padding: 0;");

        hbox.getChildren().addAll(label, linkRegister);
        footer.getChildren().add(hbox);
        return footer;
    }

    // --- INYECCIÓN DE ERRORES ---
    /**
     * Inserta los labels de error bajo sus campos correspondientes. Orden:
     * email, password, general.
     *
     * @param errEmail
     * @param errPassword
     * @param errGeneral
     */
    public void injectErrorLabels(Label errEmail, Label errPassword, Label errGeneral) {
        vboxEmail.getChildren().add(errEmail);
        vboxPassword.getChildren().add(errPassword);
        vboxGeneral.getChildren().add(errGeneral);
    }

    // --- UTILIDADES ---
    private void applyFieldStyle(Control field) {
        field.setPadding(new Insets(12));
        field.setStyle(
                "-fx-background-color: white;"
                + "-fx-border-color: #d1d8e0;"
                + "-fx-border-radius: 10px;"
                + "-fx-background-radius: 10px;"
                + "-fx-prompt-text-fill: #a4b0be;"
                + "-fx-text-fill: #2d3436;"
        );
    }

    private Control getFieldFrom(VBox group) {
        return (Control) group.getChildren().get(1);
    }

    // --- GETTERS ---
    public TextField getTfEmail() {
        return tfEmail;
    }

    public PasswordField getPfPassword() {
        return pfPassword;
    }

    public TextField getTfPasswordShown() {
        return tfPasswordShown;
    }

    public Label getLblEyePass() {
        return lblEyePass;
    }

    public CheckBox getCbRemember() {
        return cbRemember;
    }

    public Hyperlink getLinkForgotPassword() {
        return linkForgotPassword;
    }

    public Button getBtnLogin() {
        return btnLogin;
    }

    public Hyperlink getLinkRegister() {
        return linkRegister;
    }
}
