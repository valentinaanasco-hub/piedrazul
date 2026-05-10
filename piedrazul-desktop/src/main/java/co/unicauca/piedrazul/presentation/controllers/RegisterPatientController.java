package co.unicauca.piedrazul.presentation.controllers;

import co.unicauca.piedrazul.application.PiedrazulFacade;
import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.domain.entities.enums.UserState;
import co.unicauca.piedrazul.main.DataBaseType;
import co.unicauca.piedrazul.presentation.utils.PasswordFieldUtil;
import co.unicauca.piedrazul.presentation.views.LoginView;
import co.unicauca.piedrazul.presentation.views.RegisterPatientView;
import co.unicauca.piedrazul.presentation.views.SuccessDialog;

import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.time.LocalDate;

/**
 * Gestiona la interacción para el registro de pacientes usando el patrón
 * Facade.
 *
 * @author Valentina Añasco
 */
public class RegisterPatientController {

    private final RegisterPatientView view;
    private final PiedrazulFacade facade;

    private final TextField tfFirstName, tfMiddleName, tfFirstSurname, tfLastName,
            tfEmail, tfPhone, tfDocumentNumber;
    private final DatePicker dpBirthDate;
    private final ComboBox<String> cbDocumentType, cbGender;
    private final PasswordField pfPassword, pfConfirmPassword;
    private final CheckBox cbTerms;
    private final Button btnCreate;

    // --- Labels de error (orden: firstName, firstSurname, email, phone, birthDate,
    //     gender, documentType, documentNumber, password, confirmPassword, terms, general) ---
    private final Label lblErrFirstName, lblErrFirstSurname, lblErrEmail, lblErrPhone,
            lblErrBirthDate, lblErrGender, lblErrDocumentType, lblErrDocumentNumber,
            lblErrPassword, lblErrConfirmPassword, lblErrTerms, lblErrGeneral;

    private static final String STYLE_ERROR = "-fx-background-color: white; -fx-border-color: #e74c3c; -fx-border-radius: 8px; -fx-background-radius: 8px;";
    private static final String STYLE_OK = "-fx-background-color: white; -fx-border-color: #2ecc71; -fx-border-radius: 8px; -fx-background-radius: 8px;";

    public RegisterPatientController(RegisterPatientView view) {
        this.view = view;
        this.facade = PiedrazulFacade.getInstance(DataBaseType.POSTGRESQL);

        this.tfFirstName = view.getTfFirstName();
        this.tfMiddleName = view.getTfMiddleName();
        this.tfFirstSurname = view.getTfFirstSurname();
        this.tfLastName = view.getTfLastName();
        this.tfEmail = view.getTfEmail();
        this.tfPhone = view.getTfPhone();
        this.dpBirthDate = view.getDpBirthDate();
        this.cbDocumentType = view.getCbDocumentType();
        this.cbGender = view.getCbGender();
        this.tfDocumentNumber = view.getTfDocumentNumber();
        this.pfPassword = view.getPfPassword();
        this.pfConfirmPassword = view.getPfConfirmPassword();
        this.cbTerms = view.getCbTerms();
        this.btnCreate = view.getBtnCreate();

        this.lblErrFirstName = buildErrorLabel();
        this.lblErrFirstSurname = buildErrorLabel();
        this.lblErrEmail = buildErrorLabel();
        this.lblErrPhone = buildErrorLabel();
        this.lblErrBirthDate = buildErrorLabel();
        this.lblErrGender = buildErrorLabel();
        this.lblErrDocumentType = buildErrorLabel();
        this.lblErrDocumentNumber = buildErrorLabel();
        this.lblErrPassword = buildErrorLabel();
        this.lblErrConfirmPassword = buildErrorLabel();
        this.lblErrTerms = buildErrorLabel();
        this.lblErrGeneral = buildErrorLabel();

        view.injectErrorLabels(
                lblErrFirstName, lblErrFirstSurname, lblErrEmail, lblErrPhone,
                lblErrBirthDate, lblErrGender, lblErrDocumentType, lblErrDocumentNumber,
                lblErrPassword, lblErrConfirmPassword, lblErrTerms, lblErrGeneral
        );

        setupUIExtras();
        setupEvents();
    }

    /**
     * Configura tooltips en los íconos de información y visibilidad de
     * contraseñas.
     */
    private void setupUIExtras() {
        Tooltip.install(view.getIconInfoEmail(),
                newTooltip("Será usado como tu nombre de usuario.\nEj: usuario@dominio.com"));

        Tooltip.install(view.getIconInfoPhone(),
                newTooltip("Número de contacto de 7 a 10 dígitos.\nEj: 3001234567"));

        Tooltip.install(view.getIconInfoBirthDate(),
                newTooltip("Ingresa tu fecha en formato dd/MM/yyyy.\nDebes tener entre 1 y 100 años."));

        Tooltip.install(view.getIconInfoDocType(),
                newTooltip("Selecciona el tipo de documento\nde identidad que vas a registrar."));

        Tooltip.install(view.getIconInfoDocNumber(),
                newTooltip("Número de tu documento sin puntos\nni espacios. Entre 6 y 15 dígitos."));

        Tooltip passTooltip = newTooltip("La contraseña debe tener:\n• Mínimo 8 caracteres\n• Al menos un número");
        Tooltip.install(view.getIconInfoPassword(), passTooltip);

        PasswordFieldUtil.configureVisibility(pfPassword, view.getTfPasswordShown(), view.getLblEyePass());
        PasswordFieldUtil.configureVisibility(pfConfirmPassword, view.getTfConfirmShown(), view.getLblEyeConfirm());
    }

    /**
     * Vincula los eventos de la vista con los métodos del controlador.
     */
    private void setupEvents() {
        bindRealtimeValidations();
        btnCreate.setOnAction(e -> handleRegister());
        view.getLinkLogin().setOnAction(e -> navigateToLogin());
    }

    /**
     * Valida los campos al perder el foco, solo si ya contienen texto.
     */
    private void bindRealtimeValidations() {
        tfFirstName.focusedProperty().addListener((obs, old, focus) -> {
            if (!focus && !tfFirstName.getText().isEmpty()) {
                validateFirstName();
            }
        });
        tfFirstSurname.focusedProperty().addListener((obs, old, focus) -> {
            if (!focus && !tfFirstSurname.getText().isEmpty()) {
                validateFirstSurname();
            }
        });
        tfEmail.focusedProperty().addListener((obs, old, focus) -> {
            if (!focus && !tfEmail.getText().isEmpty()) {
                validateEmail();
            }
        });
        tfPhone.focusedProperty().addListener((obs, old, focus) -> {
            if (!focus && !tfPhone.getText().isEmpty()) {
                validatePhone();
            }
        });
        dpBirthDate.valueProperty().addListener((obs, old, val) -> validateBirthDate());
        cbGender.valueProperty().addListener((obs, old, val) -> validateGender());
        cbDocumentType.valueProperty().addListener((obs, old, val) -> validateDocumentType());
        tfDocumentNumber.focusedProperty().addListener((obs, old, focus) -> {
            if (!focus && !tfDocumentNumber.getText().isEmpty()) {
                validateDocumentNumber();
            }
        });
        pfPassword.focusedProperty().addListener((obs, old, focus) -> {
            if (!focus && !pfPassword.getText().isEmpty()) {
                validatePassword();
            }
        });
        pfConfirmPassword.focusedProperty().addListener((obs, old, focus) -> {
            if (!focus && !pfConfirmPassword.getText().isEmpty()) {
                validateConfirmPassword();
            }
        });
    }

    // --- Reglas de validación individuales ---
    private boolean validateFirstName() {
        String val = tfFirstName.getText().trim();
        if (val.isEmpty()) {
            return setError(tfFirstName, lblErrFirstName, "Campo obligatorio");
        }
        if (!val.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            return setError(tfFirstName, lblErrFirstName, "Solo se permiten letras");
        }
        return setOk(tfFirstName, lblErrFirstName);
    }

    private boolean validateFirstSurname() {
        String val = tfFirstSurname.getText().trim();
        if (val.isEmpty()) {
            return setError(tfFirstSurname, lblErrFirstSurname, "Campo obligatorio");
        }
        if (!val.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            return setError(tfFirstSurname, lblErrFirstSurname, "Solo se permiten letras");
        }
        return setOk(tfFirstSurname, lblErrFirstSurname);
    }

    private boolean validateEmail() {
        String val = tfEmail.getText().trim();
        if (!val.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            return setError(tfEmail, lblErrEmail, "Correo inválido");
        }
        return setOk(tfEmail, lblErrEmail);
    }

    private boolean validatePhone() {
        String val = tfPhone.getText().trim();
        if (!val.matches("\\d{7,10}")) {
            return setError(tfPhone, lblErrPhone, "7 a 10 dígitos");
        }
        return setOk(tfPhone, lblErrPhone);
    }

    private boolean validateBirthDate() {
        LocalDate val = dpBirthDate.getValue();
        LocalDate now = LocalDate.now();
        if (val == null) {
            return setError(dpBirthDate, lblErrBirthDate, "Campo obligatorio");
        }
        if (val.isAfter(now.minusYears(1))) {
            return setError(dpBirthDate, lblErrBirthDate, "Mínimo 1 año de edad");
        }
        if (val.isBefore(now.minusYears(100))) {
            return setError(dpBirthDate, lblErrBirthDate, "Máximo 100 años de edad");
        }
        return setOk(dpBirthDate, lblErrBirthDate);
    }

    private boolean validateGender() {
        if (cbGender.getValue() == null) {
            return setError(cbGender, lblErrGender, "Seleccione uno");
        }
        return setOk(cbGender, lblErrGender);
    }

    private boolean validateDocumentType() {
        if (cbDocumentType.getValue() == null) {
            return setError(cbDocumentType, lblErrDocumentType, "Seleccione uno");
        }
        return setOk(cbDocumentType, lblErrDocumentType);
    }

    private boolean validateDocumentNumber() {
        String val = tfDocumentNumber.getText().trim();
        if (!val.matches("\\d{6,15}")) {
            return setError(tfDocumentNumber, lblErrDocumentNumber, "6 a 15 números");
        }
        return setOk(tfDocumentNumber, lblErrDocumentNumber);
    }

    private boolean validatePassword() {
        String val = pfPassword.getText();
        if (val.length() < 8 || !val.matches(".*\\d.*")) {
            return setError(pfPassword, lblErrPassword, "Mín. 8 caracteres y 1 número");
        }
        return setOk(pfPassword, lblErrPassword);
    }

    private boolean validateConfirmPassword() {
        if (!pfConfirmPassword.getText().equals(pfPassword.getText())) {
            return setError(pfConfirmPassword, lblErrConfirmPassword, "No coinciden");
        }
        return setOk(pfConfirmPassword, lblErrConfirmPassword);
    }

    /**
     * Ejecuta el flujo completo de registro al presionar el botón.
     */
    private void handleRegister() {
        lblErrGeneral.setVisible(false);

        boolean isValid = validateFirstName() & validateFirstSurname() & validateEmail()
                & validatePhone() & validateBirthDate() & validateGender()
                & validateDocumentType() & validateDocumentNumber()
                & validatePassword() & validateConfirmPassword();

        if (!isValid || !cbTerms.isSelected()) {
            if (!cbTerms.isSelected()) {
                lblErrTerms.setVisible(true);
            }
            return;
        }

        try {
            LocalDate date = dpBirthDate.getValue();
            int docId = Integer.parseInt(tfDocumentNumber.getText().trim());

            Patient patient = new Patient(
                    tfPhone.getText().trim(),
                    cbGender.getValue(),
                    String.valueOf(date.getDayOfMonth()),
                    String.valueOf(date.getMonthValue()),
                    String.valueOf(date.getYear()),
                    tfEmail.getText().trim(),
                    docId,
                    mapDocumentType(cbDocumentType.getValue()),
                    tfFirstName.getText().trim(),
                    tfMiddleName.getText().trim(),
                    tfFirstSurname.getText().trim(),
                    tfLastName.getText().trim(),
                    tfEmail.getText().trim(),
                    pfPassword.getText(),
                    UserState.ACTIVO,
                    null
            );

            if (facade.registerPatient(patient)) {
                Stage stage = (Stage) view.getScene().getWindow();
                SuccessDialog.show(
                        stage,
                        "¡Cuenta Creada Exitosamente!",
                        "Bienvenido " + patient.getFirstName() + ". Ya puedes iniciar sesión.",
                        this::navigateToLogin
                );
            }

        } catch (IllegalArgumentException ex) {
            showGeneralError(ex.getMessage());
        }
    }

    private void navigateToLogin() {
        Stage stage = (Stage) view.getScene().getWindow();
        LoginView loginView = new LoginView(); 
        new LoginController(loginView);        
        stage.getScene().setRoot(loginView);
    }

    // --- Helpers ---
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

    private Tooltip newTooltip(String text) {
        Tooltip tp = new Tooltip(text);
        tp.setShowDelay(javafx.util.Duration.millis(200));
        return tp;
    }

    private String mapDocumentType(String name) {
        return switch (name) {
            case "Cédula de ciudadanía" ->
                "CC";
            case "Tarjeta de identidad" ->
                "TI";
            case "Cédula de extranjería" ->
                "CE";
            case "Pasaporte" ->
                "PA";
            default ->
                "RC";
        };
    }
}
