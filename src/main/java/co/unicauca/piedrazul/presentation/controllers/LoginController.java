package co.unicauca.piedrazul.presentation.controllers;

import co.unicauca.piedrazul.application.PiedrazulFacade;
import co.unicauca.piedrazul.domain.entities.Role;
import co.unicauca.piedrazul.domain.entities.User;
import co.unicauca.piedrazul.domain.entities.enums.RoleName;
import co.unicauca.piedrazul.main.DataBaseType;
import co.unicauca.piedrazul.presentation.utils.PasswordFieldUtil;
import co.unicauca.piedrazul.presentation.views.LoginView;
import co.unicauca.piedrazul.presentation.views.MainView;
import co.unicauca.piedrazul.presentation.views.RegisterPatientView;

import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Gestiona la interacción del inicio de sesión usando el patrón Facade.
 *
 * @author Valentina Añasco
 */
public class LoginController {

    private final LoginView view;
    private final PiedrazulFacade facade;

    private final TextField tfEmail;
    private final PasswordField pfPassword;
    private final Button btnLogin;

    // --- Labels de error ---
    private final Label lblErrEmail;
    private final Label lblErrPassword;
    private final Label lblErrGeneral;

    private static final String STYLE_ERROR = "-fx-background-color: white; -fx-border-color: #e74c3c; -fx-border-radius: 10px; -fx-background-radius: 10px;";
    private static final String STYLE_OK = "-fx-background-color: white; -fx-border-color: #2ecc71; -fx-border-radius: 10px; -fx-background-radius: 10px;";

    public LoginController(LoginView view) {
        this.view = view;
        this.facade = PiedrazulFacade.getInstance(DataBaseType.POSTGRESQL);

        this.tfEmail = view.getTfEmail();
        this.pfPassword = view.getPfPassword();
        this.btnLogin = view.getBtnLogin();

        this.lblErrEmail = buildErrorLabel();
        this.lblErrPassword = buildErrorLabel();
        this.lblErrGeneral = buildErrorLabel();

        view.injectErrorLabels(lblErrEmail, lblErrPassword, lblErrGeneral);

        setupUIExtras();
        setupEvents();
    }

    /**
     * Configura el toggle de visibilidad de la contraseña.
     */
    private void setupUIExtras() {
        PasswordFieldUtil.configureVisibility(
                pfPassword,
                view.getTfPasswordShown(),
                view.getLblEyePass()
        );
    }

    /**
     * Vincula los eventos de la vista con los métodos del controlador.
     */
    private void setupEvents() {
        bindRealtimeValidations();
        btnLogin.setOnAction(e -> handleLogin());
        view.getLinkRegister().setOnAction(e -> navigateToRegister());
        view.getLinkForgotPassword().setOnAction(e -> handleForgotPassword());
    }

    /**
     * Valida los campos al perder el foco, solo si ya contienen texto.
     */
    private void bindRealtimeValidations() {
        tfEmail.focusedProperty().addListener((obs, old, focus) -> {
            if (!focus && !tfEmail.getText().isEmpty()) {
                validateUsername();
            }
        });

        pfPassword.focusedProperty().addListener((obs, old, focus) -> {
            if (!focus && !pfPassword.getText().isEmpty()) {
                validatePassword();
            }
        });
    }

    // --- Reglas de validación ---
    private boolean validateUsername() {
        String val = tfEmail.getText().trim();
        if (val.isEmpty()) {
            return setError(tfEmail, lblErrEmail, "Campo obligatorio");
        }
        return setOk(tfEmail, lblErrEmail);
    }

    private boolean validatePassword() {
        String val = pfPassword.getText();
        if (val.isEmpty()) {
            return setError(pfPassword, lblErrPassword, "Campo obligatorio");
        }
        return setOk(pfPassword, lblErrPassword);
    }

    /**
     * Ejecuta el flujo de inicio de sesión al presionar el botón. Usa
     * facade.login() que autentica Y carga los roles en una sola llamada.
     */
    private void handleLogin() {
        lblErrGeneral.setVisible(false);

        boolean isValid = validateUsername() & validatePassword();
        if (!isValid) {
            return;
        }

        try {
            User user = facade.login(
                    tfEmail.getText().trim(),
                    pfPassword.getText()
            );
            navigateByRole(user);

        } catch (IllegalArgumentException ex) {
            showGeneralError(ex.getMessage());
        }
    }

    // --- NAVEGACIÓN ---
    /**
     * Redirige al usuario a la vista correspondiente según su rol. PACIENTE →
     * pendiente de implementar. ADMIN, DOCTOR, AGENDADOR → MainView.
     */
    private void navigateByRole(User user) {
        Stage stage = (Stage) view.getScene().getWindow();

        boolean isPatient = user.getRoles() != null && user.getRoles().stream()
                .map(Role::getRoleName)
                .anyMatch(name -> name.equalsIgnoreCase(RoleName.PACIENTE.name()));

        if (isPatient) {
            // TODO: implementar PatientDashboardView cuando esté lista
            showGeneralError("El portal de pacientes aún no está disponible.");
            return;
        }

        String technicalRole = user.getRoles().stream()
                .map(Role::getRoleName)
                .filter(name -> !name.equalsIgnoreCase(RoleName.PACIENTE.name()))
                .findFirst()
                .orElse("USUARIO");

        MainView mainView = new MainView(stage, user, technicalRole, DataBaseType.POSTGRESQL);
        mainView.show();
    }

    /**
     * Obtiene el nombre legible del primer rol no-paciente del usuario.
     */
    private String resolveRoleName(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return "Usuario";
        }

        return user.getRoles().stream()
                .map(Role::getRoleName)
                .filter(name -> !name.equalsIgnoreCase(RoleName.PACIENTE.name()))
                .findFirst()
                .map(name -> switch (name.toUpperCase()) {
            case "ADMIN" ->
                "Administrador";
            case "DOCTOR" ->
                "Médico";
            case "AGENDADOR" ->
                "Agendador";
            default ->
                name;
        })
                .orElse("Usuario");
    }

    private void navigateToRegister() {
        Stage stage = (Stage) view.getScene().getWindow();
        RegisterPatientView registerView = new RegisterPatientView();
        new RegisterPatientController(registerView);

        ScrollPane scrollPane = new ScrollPane(registerView);
        scrollPane.setFitToWidth(true);

        stage.getScene().setRoot(scrollPane);
    }

    private void handleForgotPassword() {
        // TODO: implementar recuperación de contraseña
    }

    // --- HELPERS ---
    private boolean setError(Control field, Label lbl, String msg) {
        field.setStyle(STYLE_ERROR);
        lbl.setText(msg);
        lbl.setVisible(true);
        return false;
    }

    private boolean setOk(Control field, Label lbl) {
        field.setStyle(STYLE_OK);
        lbl.setVisible(false);
        return true;
    }

    private void showGeneralError(String msg) {
        lblErrGeneral.setText(msg);
        lblErrGeneral.setVisible(true);
    }

    private Label buildErrorLabel() {
        Label lbl = new Label();
        lbl.setTextFill(Color.web("#e74c3c"));
        lbl.setStyle("-fx-font-size: 11px;");
        lbl.setVisible(false);
        lbl.setManaged(false);
        lbl.visibleProperty().addListener((o, old, vis) -> lbl.setManaged(vis));
        return lbl;
    }
}
