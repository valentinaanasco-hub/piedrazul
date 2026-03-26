package co.unicauca.piedrazul.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vista de inicio de sesión (solo para pruebas).
 */
public class LoginView extends VBox {

    private static final double VIEW_WIDTH = 500;
    private static final double PANEL_WIDTH = 420;

    // Colores (los mismos que usas)
    private static final String COLOR_BLUE = "#2c6cf1";
    private static final String COLOR_LIGHT_BLUE_BG = "#f3f6fa";
    private static final String COLOR_TEXT_GRAY = "#636e72";

    // Campos
    private TextField tfEmail;
    private PasswordField pfPassword;
    private Button btnLogin;
    private Hyperlink linkRegister;

    public LoginView() {

        this.setMinWidth(VIEW_WIDTH);
        this.setMaxWidth(VIEW_WIDTH);
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: " + COLOR_LIGHT_BLUE_BG + ";");
        this.setPadding(new Insets(40, 0, 20, 0));
        this.setSpacing(15);

        VBox header = createHeader();
        VBox formPanel = createFormPanel();
        VBox footer = createFooter();

        this.getChildren().addAll(header, formPanel, footer);
    }

    // =========================================================================
    // HEADER
    // =========================================================================
    private VBox createHeader() {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);

        Label title = new Label("Iniciar sesión");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 25));
        title.setTextFill(Color.web("#1e272e"));

        Label subtitle = new Label("Accede a tu cuenta en Piedrazul");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(Color.web(COLOR_TEXT_GRAY));

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    // =========================================================================
    // FORMULARIO
    // =========================================================================
    private VBox createFormPanel() {

        VBox panel = new VBox(15);
        panel.setMinWidth(PANEL_WIDTH);
        panel.setMaxWidth(PANEL_WIDTH);

        panel.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 15px;"
                + "-fx-border-radius: 15px;"
                + "-fx-border-color: #e6e9ef;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);"
                + "-fx-padding: 30px;"
        );

        tfEmail = new TextField();
        tfEmail.setPromptText("Correo electrónico");
        applyStyle(tfEmail);

        pfPassword = new PasswordField();
        pfPassword.setPromptText("Contraseña");
        applyStyle(pfPassword);

        btnLogin = new Button("Iniciar sesión");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPadding(new Insets(12));
        btnLogin.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnLogin.setTextFill(Color.WHITE);
        btnLogin.setStyle(
                "-fx-background-color: " + COLOR_BLUE + ";"
                + "-fx-background-radius: 8px;"
                + "-fx-cursor: hand;"
        );

        panel.getChildren().addAll(tfEmail, pfPassword, btnLogin);

        return panel;
    }

    // =========================================================================
    // FOOTER
    // =========================================================================
    private VBox createFooter() {
        VBox footer = new VBox();
        footer.setAlignment(Pos.CENTER);

        HBox hbox = new HBox(5);
        hbox.setAlignment(Pos.CENTER);

        Label label = new Label("¿No tienes cuenta?");
        label.setTextFill(Color.web(COLOR_TEXT_GRAY));

        linkRegister = new Hyperlink("Crear cuenta");
        linkRegister.setStyle("-fx-text-fill: " + COLOR_BLUE + ";");

        hbox.getChildren().addAll(label, linkRegister);
        footer.getChildren().add(hbox);

        return footer;
    }

    // =========================================================================
    // ESTILO CAMPOS
    // =========================================================================
    private void applyStyle(Control field) {
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

    // =========================================================================
    // GETTERS
    // =========================================================================
    public TextField getTfEmail() {
        return tfEmail;
    }

    public PasswordField getPfPassword() {
        return pfPassword;
    }

    public Button getBtnLogin() {
        return btnLogin;
    }

    public Hyperlink getLinkRegister() {
        return linkRegister;
    }
}
