package co.unicauca.piedrazul.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Objects;

/**
 * Vista de inicio de sesión de la Red de Servicios Médicos Piedrazul.
 *
 * * @author Valentina Añasco
 */
public class LoginView extends VBox {

    private static final double VIEW_WIDTH = 500;
    private static final double PANEL_WIDTH = 420;

    // Paleta de colores consistente con el Registro
    private static final String COLOR_BLUE = "#2c6cf1";
    private static final String COLOR_LIGHT_BLUE_BG = "#f3f6fa";
    private static final String COLOR_TEXT_GRAY = "#636e72";
    private static final String COLOR_TEXT_DARK = "#1e272e";

    // Componentes del formulario
    private TextField tfEmail;
    private PasswordField pfPassword;
    private TextField tfPasswordShown; // Espejo para visibilidad
    private Label lblEyePass;           // Ícono del ojo
    private Button btnLogin;
    private Hyperlink linkRegister;

    public LoginView() {
        this.setMinWidth(VIEW_WIDTH);
        this.setMaxWidth(VIEW_WIDTH);
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: " + COLOR_LIGHT_BLUE_BG + ";");
        this.setPadding(new Insets(60, 0, 40, 0)); // Más espacio superior
        this.setSpacing(25);

        VBox header = createHeader();
        VBox formPanel = createFormPanel();
        VBox footer = createFooter();

        this.getChildren().addAll(header, formPanel, footer);
    }

    /**
     * Crea el encabezado con el ícono y los títulos de la aplicación.
     */
    private VBox createHeader() {
        VBox header = new VBox(12);
        header.setAlignment(Pos.CENTER);

        // Contenedor del ícono (Círculo azul con logo)
        StackPane iconContainer = new StackPane();
        Circle circle = new Circle(35);
        circle.setFill(Color.web(COLOR_BLUE));

        try {
            Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/co/unicauca/piedrazul/presentation/assets/img/icon_white.png")));
            ImageView imageView = new ImageView(logo);
            imageView.setFitWidth(35);
            imageView.setPreserveRatio(true);
            iconContainer.getChildren().addAll(circle, imageView);
        } catch (Exception e) {
            // Fallback si no encuentra la imagen
            iconContainer.getChildren().add(circle);
        }

        Label title = new Label("Piedrazul");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(COLOR_TEXT_DARK));

        Label subtitle = new Label("Citas Médicas");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitle.setTextFill(Color.web(COLOR_TEXT_GRAY));

        header.getChildren().addAll(iconContainer, title, subtitle);
        return header;
    }

    /**
     * Crea el panel central blanco que contiene los campos de entrada.
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

        // Campo de Email
        VBox groupEmail = createFieldGroup("Correo electrónico", tfEmail = new TextField());
        tfEmail.setPromptText("ejemplo@correo.com");

        // Campo de Contraseña con soporte para visibilidad
        VBox groupPass = createPasswordFieldGroup();

        // Botón de Inicio de Sesión
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

        panel.getChildren().addAll(groupEmail, groupPass, btnLogin);
        return panel;
    }

    /**
     * Crea el grupo de campo para la contraseña, incluyendo el toggle de
     * visibilidad.
     */
    private VBox createPasswordFieldGroup() {
        VBox group = new VBox(8);
        Label label = new Label("Contraseña");
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        label.setTextFill(Color.web(COLOR_TEXT_DARK));

        StackPane passContainer = new StackPane();

        pfPassword = new PasswordField();
        tfPasswordShown = new TextField();
        tfPasswordShown.setVisible(false);

        applyFieldStyle(pfPassword);
        applyFieldStyle(tfPasswordShown);

        lblEyePass = new Label("👁"); // Puedes cambiar esto por un ImageView con tu ícono de ojo
        lblEyePass.setCursor(Cursor.HAND);
        lblEyePass.setPadding(new Insets(0, 15, 0, 0));
        StackPane.setAlignment(lblEyePass, Pos.CENTER_RIGHT);

        passContainer.getChildren().addAll(tfPasswordShown, pfPassword, lblEyePass);
        group.getChildren().addAll(label, passContainer);
        return group;
    }

    /**
     * Pie de página para navegación hacia el registro.
     */
    private VBox createFooter() {
        VBox footer = new VBox();
        footer.setAlignment(Pos.CENTER);

        HBox hbox = new HBox(5);
        hbox.setAlignment(Pos.CENTER);

        Label label = new Label("¿Aún no tienes una cuenta?");
        label.setTextFill(Color.web(COLOR_TEXT_GRAY));
        label.setFont(Font.font("Segoe UI", 13));

        linkRegister = new Hyperlink("Regístrate ahora");
        linkRegister.setStyle("-fx-text-fill: " + COLOR_BLUE + "; -fx-font-weight: bold;");
        linkRegister.setUnderline(false);

        hbox.getChildren().addAll(label, linkRegister);
        footer.getChildren().add(hbox);

        return footer;
    }

    // --- HELPERS DE ESTILO ---
    private VBox createFieldGroup(String labelText, Control field) {
        VBox group = new VBox(8);
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        label.setTextFill(Color.web(COLOR_TEXT_DARK));

        applyFieldStyle(field);
        group.getChildren().addAll(label, field);
        return group;
    }

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

    public Button getBtnLogin() {
        return btnLogin;
    }

    public Hyperlink getLinkRegister() {
        return linkRegister;
    }
}
