package co.unicauca.piedrazul.presentation.controllers;

import co.unicauca.piedrazul.domain.entities.Patient;
import co.unicauca.piedrazul.domain.entities.enums.RoleName;
import co.unicauca.piedrazul.domain.entities.enums.UserState;
import co.unicauca.piedrazul.domain.services.PatientService;
import co.unicauca.piedrazul.domain.services.RoleService;
import co.unicauca.piedrazul.domain.services.UserService;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresPatientRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresRoleRepository;
import co.unicauca.piedrazul.infrastructure.repositories.PostgresUserRepository;
import co.unicauca.piedrazul.presentation.views.LoginView;
import co.unicauca.piedrazul.presentation.views.RegisterPatientView;

import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Controlador de la vista de registro de pacientes.
 *
 * Flujo de registro: 1. Validaciones en tiempo real campo por campo 2.
 * UserService.registerUser() → tabla users (username = email, id = número de
 * documento) 3. PatientService.registerPatient() → tabla patients 4.
 * RoleService.assignRole() → rol PACIENTE 5. Navegación a LoginView
 *
 * @author Valentina Añasco
 */
public class RegisterPatientController {

    // --- Vista ---
    private final RegisterPatientView view;

    // --- Campos del formulario ---
    private final TextField tfFirstName;
    private final TextField tfMiddleName;
    private final TextField tfFirstSurname;
    private final TextField tfLastName;
    private final TextField tfEmail;
    private final TextField tfPhone;
    private final DatePicker dpBirthDate;
    private final ComboBox<String> cbDocumentType;
    private final TextField tfDocumentNumber;
    private final PasswordField pfPassword;
    private final PasswordField pfConfirmPassword;
    private final CheckBox cbTerms;
    private final Button btnCreate;

    // --- Labels de error por campo ---
    private final Label lblErrFirstName;
    private final Label lblErrFirstSurname;
    private final Label lblErrEmail;
    private final Label lblErrPhone;
    private final Label lblErrBirthDate;
    private final Label lblErrDocumentType;
    private final Label lblErrDocumentNumber;
    private final Label lblErrPassword;
    private final Label lblErrConfirmPassword;
    private final Label lblErrTerms;
    private final Label lblErrGeneral;

    // --- Servicios ---
    private final UserService userService;
    private final PatientService patientService;
    private final RoleService roleService;

    // --- Estilos de campo ---
    private static final String STYLE_NORMAL
            = "-fx-background-color: white;"
            + "-fx-border-color: #d1d8e0;"
            + "-fx-border-radius: 8px;"
            + "-fx-background-radius: 8px;"
            + "-fx-prompt-text-fill: #a4b0be;"
            + "-fx-text-fill: #2d3436;";

    private static final String STYLE_ERROR
            = "-fx-background-color: white;"
            + "-fx-border-color: #e74c3c;"
            + "-fx-border-radius: 8px;"
            + "-fx-background-radius: 8px;"
            + "-fx-prompt-text-fill: #a4b0be;"
            + "-fx-text-fill: #2d3436;";

    private static final String STYLE_OK
            = "-fx-background-color: white;"
            + "-fx-border-color: #2ecc71;"
            + "-fx-border-radius: 8px;"
            + "-fx-background-radius: 8px;"
            + "-fx-prompt-text-fill: #a4b0be;"
            + "-fx-text-fill: #2d3436;";

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public RegisterPatientController(RegisterPatientView view) {
        this.view = view;

        // --- Obtener referencias desde la vista ---
        this.tfFirstName = view.getTfFirstName();
        this.tfMiddleName = view.getTfMiddleName();
        this.tfFirstSurname = view.getTfFirstSurname();
        this.tfLastName = view.getTfLastName();
        this.tfEmail = view.getTfEmail();
        this.tfPhone = view.getTfPhone();
        this.dpBirthDate = view.getDpBirthDate();
        this.cbDocumentType = view.getCbDocumentType();
        this.tfDocumentNumber = view.getTfDocumentNumber();
        this.pfPassword = view.getPfPassword();
        this.pfConfirmPassword = view.getPfConfirmPassword();
        this.cbTerms = view.getCbTerms();
        this.btnCreate = view.getBtnCreate();

        // --- Crear labels de error ---
        this.lblErrFirstName = buildErrorLabel();
        this.lblErrFirstSurname = buildErrorLabel();
        this.lblErrEmail = buildErrorLabel();
        this.lblErrPhone = buildErrorLabel();
        this.lblErrBirthDate = buildErrorLabel();
        this.lblErrDocumentType = buildErrorLabel();
        this.lblErrDocumentNumber = buildErrorLabel();
        this.lblErrPassword = buildErrorLabel();
        this.lblErrConfirmPassword = buildErrorLabel();
        this.lblErrTerms = buildErrorLabel();
        this.lblErrGeneral = buildErrorLabel();

        // --- Inyectar labels en la vista ---
        view.injectErrorLabels(
                lblErrFirstName, lblErrFirstSurname,
                lblErrEmail, lblErrPhone,
                lblErrBirthDate, lblErrDocumentType,
                lblErrDocumentNumber,
                lblErrPassword, lblErrConfirmPassword,
                lblErrTerms, lblErrGeneral
        );

        // --- Instanciar servicios ---
        this.userService = new UserService(new PostgresUserRepository());
        this.patientService = new PatientService(new PostgresPatientRepository());
        this.roleService = new RoleService(new PostgresRoleRepository());

        // --- Conectar eventos ---
        bindRealtimeValidations();
        bindSubmitButton();
        bindLoginLink();
    }

    // =========================================================================
    // VALIDACIONES EN TIEMPO REAL
    // =========================================================================
    /**
     * Vincula listeners de foco a cada campo obligatorio para validar en el
     * momento en que el usuario sale del campo.
     */
    private void bindRealtimeValidations() {

        // Primer nombre
        tfFirstName.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateFirstName();
            }
        });

        // Apellido
        tfFirstSurname.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateFirstSurname();
            }
        });

        // Correo
        tfEmail.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateEmail();
            }
        });

        // Teléfono
        tfPhone.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validatePhone();
            }
        });

        // Fecha de nacimiento (también al seleccionar del calendario)
        dpBirthDate.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateBirthDate();
            }
        });
        dpBirthDate.valueProperty().addListener((obs, oldVal, newVal) -> validateBirthDate());

        // Tipo de documento (al seleccionar del combo)
        cbDocumentType.valueProperty().addListener((obs, oldVal, newVal) -> validateDocumentType());

        // Número de documento
        tfDocumentNumber.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateDocumentNumber();
            }
        });

        // Contraseña
        pfPassword.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validatePassword();
            }
        });

        // Confirmar contraseña + revalidar si cambia la contraseña principal
        pfConfirmPassword.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateConfirmPassword();
            }
        });
        pfPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!pfConfirmPassword.getText().isEmpty()) {
                validateConfirmPassword();
            }
        });

        // Términos
        cbTerms.selectedProperty().addListener((obs, oldVal, newVal) -> validateTerms());
    }

    // =========================================================================
    // REGLAS DE VALIDACIÓN
    // =========================================================================
    private boolean validateFirstName() {
        String value = tfFirstName.getText().trim();
        if (value.isEmpty()) {
            return setError(tfFirstName, lblErrFirstName, "El primer nombre es obligatorio");
        }
        if (!value.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            return setError(tfFirstName, lblErrFirstName, "Solo se permiten letras");
        }
        return setOk(tfFirstName, lblErrFirstName);
    }

    private boolean validateFirstSurname() {
        String value = tfFirstSurname.getText().trim();
        if (value.isEmpty()) {
            return setError(tfFirstSurname, lblErrFirstSurname, "El apellido es obligatorio");
        }
        if (!value.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            return setError(tfFirstSurname, lblErrFirstSurname, "Solo se permiten letras");
        }
        return setOk(tfFirstSurname, lblErrFirstSurname);
    }

    private boolean validateEmail() {
        String value = tfEmail.getText().trim();
        if (value.isEmpty()) {
            return setError(tfEmail, lblErrEmail, "El correo es obligatorio");
        }
        if (!value.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            return setError(tfEmail, lblErrEmail, "Formato de correo inválido");
        }
        return setOk(tfEmail, lblErrEmail);
    }

    private boolean validatePhone() {
        String value = tfPhone.getText().trim();
        if (value.isEmpty()) {
            return setError(tfPhone, lblErrPhone, "El teléfono es obligatorio");
        }
        if (!value.matches("\\d{7,10}")) {
            return setError(tfPhone, lblErrPhone, "Debe tener entre 7 y 10 dígitos");
        }
        return setOk(tfPhone, lblErrPhone);
    }

    private boolean validateBirthDate() {
        LocalDate value = dpBirthDate.getValue();
        if (value == null) {
            return setError(dpBirthDate, lblErrBirthDate, "La fecha de nacimiento es obligatoria");
        }
        if (value.isAfter(LocalDate.now().minusYears(1))) {
            return setError(dpBirthDate, lblErrBirthDate, "Fecha de nacimiento inválida");
        }
        return setOk(dpBirthDate, lblErrBirthDate);
    }

    private boolean validateDocumentType() {
        if (cbDocumentType.getValue() == null || cbDocumentType.getValue().isEmpty()) {
            return setError(cbDocumentType, lblErrDocumentType, "Selecciona el tipo de documento");
        }
        return setOk(cbDocumentType, lblErrDocumentType);
    }

    private boolean validateDocumentNumber() {
        String value = tfDocumentNumber.getText().trim();
        if (value.isEmpty()) {
            return setError(tfDocumentNumber, lblErrDocumentNumber, "El número de documento es obligatorio");
        }
        if (!value.matches("\\d+")) {
            return setError(tfDocumentNumber, lblErrDocumentNumber, "Solo se permiten números");
        }
        if (value.length() < 6 || value.length() > 15) {
            return setError(tfDocumentNumber, lblErrDocumentNumber, "Debe tener entre 6 y 15 dígitos");
        }
        return setOk(tfDocumentNumber, lblErrDocumentNumber);
    }

    private boolean validatePassword() {
        String value = pfPassword.getText();
        if (value.isEmpty()) {
            return setError(pfPassword, lblErrPassword, "La contraseña es obligatoria");
        }
        if (value.length() < 8) {
            return setError(pfPassword, lblErrPassword, "Mínimo 8 caracteres");
        }
        if (!value.matches(".*\\d.*")) {
            return setError(pfPassword, lblErrPassword, "Debe contener al menos un número");
        }
        return setOk(pfPassword, lblErrPassword);
    }

    private boolean validateConfirmPassword() {
        String value = pfConfirmPassword.getText();
        if (value.isEmpty()) {
            return setError(pfConfirmPassword, lblErrConfirmPassword, "Confirma tu contraseña");
        }
        if (!value.equals(pfPassword.getText())) {
            return setError(pfConfirmPassword, lblErrConfirmPassword, "Las contraseñas no coinciden");
        }
        return setOk(pfConfirmPassword, lblErrConfirmPassword);
    }

    private boolean validateTerms() {
        if (!cbTerms.isSelected()) {
            lblErrTerms.setText("Debes aceptar los términos y condiciones");
            lblErrTerms.setVisible(true);
            return false;
        }
        lblErrTerms.setText("");
        lblErrTerms.setVisible(false);
        return true;
    }

    /**
     * Ejecuta todas las validaciones a la vez. Usa & (no &&) para mostrar todos
     * los errores simultáneamente.
     */
    private boolean validateAll() {
        boolean ok = true;
        ok &= validateFirstName();
        ok &= validateFirstSurname();
        ok &= validateEmail();
        ok &= validatePhone();
        ok &= validateBirthDate();
        ok &= validateDocumentType();
        ok &= validateDocumentNumber();
        ok &= validatePassword();
        ok &= validateConfirmPassword();
        ok &= validateTerms();
        return ok;
    }

    // =========================================================================
    // ACCIÓN PRINCIPAL: REGISTRAR
    // =========================================================================
    private void bindSubmitButton() {
        btnCreate.setOnAction(e -> handleRegister());
    }

    private void handleRegister() {
        // Limpiar error general previo
        lblErrGeneral.setText("");
        lblErrGeneral.setVisible(false);

        // 1. Validar todos los campos
        if (!validateAll()) {
            return;
        }

        try {
            LocalDate birthDate = dpBirthDate.getValue();

            // El id del usuario ES su número de documento
            int documentId = Integer.parseInt(tfDocumentNumber.getText().trim());

            // 2. Construir entidad Patient
            Patient patient = new Patient(
                    tfPhone.getText().trim(),
                    "", // gender: no está en el prototipo actual
                    String.valueOf(birthDate.getDayOfMonth()),
                    String.valueOf(birthDate.getMonthValue()),
                    String.valueOf(birthDate.getYear()),
                    tfEmail.getText().trim(),
                    documentId, // id = número de documento
                    mapDocumentType(cbDocumentType.getValue()), // CC, TI, CE, PA, RC
                    tfFirstName.getText().trim(),
                    tfMiddleName.getText().trim(),
                    tfFirstSurname.getText().trim(),
                    tfLastName.getText().trim(),
                    tfEmail.getText().trim(), // username = email
                    pfPassword.getText(),
                    UserState.ACTIVO,
                    null // roles: se asignan en paso 4
            );

            // 3. Guardar en tabla users (verifica que el email no esté en uso)
            userService.registerUser(patient);

            // 4. Guardar en tabla patients
            patientService.registerPatient(patient);

            // 5. Asignar rol PACIENTE
            roleService.assignRole(patient.getId(), RoleName.PACIENTE);

            // 6. Navegar al login
            navigateToLogin();

        } catch (NumberFormatException ex) {
            setError(tfDocumentNumber, lblErrDocumentNumber, "El número de documento no es válido");
        } catch (IllegalArgumentException ex) {
            // Errores de negocio: email duplicado, campos faltantes, etc.
            showGeneralError(ex.getMessage());
        } catch (Exception ex) {
            showGeneralError("Error inesperado al registrar. Intenta de nuevo.");
        }
    }

    // =========================================================================
    // NAVEGACIÓN
    // =========================================================================
    private void bindLoginLink() {
        view.getLinkLogin().setOnAction(e -> navigateToLogin());
    }

    private void navigateToLogin() {
        Stage stage = (Stage) view.getScene().getWindow();
        LoginView loginView = new LoginView();
        stage.getScene().setRoot(loginView);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    /**
     * Marca el campo en rojo, muestra el mensaje y retorna false.
     */
    private boolean setError(Control field, Label errorLabel, String message) {
        field.setStyle(STYLE_ERROR);
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        return false;
    }

    /**
     * Marca el campo en verde, oculta el label de error y retorna true.
     */
    private boolean setOk(Control field, Label errorLabel) {
        field.setStyle(STYLE_OK);
        errorLabel.setText("");
        errorLabel.setVisible(false);
        return true;
    }

    private void showGeneralError(String message) {
        lblErrGeneral.setText(message);
        lblErrGeneral.setVisible(true);
    }

    private Label buildErrorLabel() {
        Label lbl = new Label();
        lbl.setTextFill(Color.web("#e74c3c"));
        lbl.setStyle("-fx-font-size: 11px;");
        lbl.setVisible(false);
        // No ocupa espacio en el layout cuando está oculto
        lbl.setManaged(false);
        lbl.visibleProperty().addListener((obs, oldVal, isVisible)
                -> lbl.setManaged(isVisible)
        );
        return lbl;
    }

    // =========================================================================
    // MAPEO TIPO DE DOCUMENTO
    // =========================================================================
    /**
     * Convierte el texto del ComboBox al código corto que espera el campo
     * userTypeId de la entidad User.
     */
    private String mapDocumentType(String displayName) {
        return switch (displayName) {
            case "Cédula de ciudadanía" ->
                "CC";
            case "Tarjeta de identidad" ->
                "TI";
            case "Cédula de extranjería" ->
                "CE";
            case "Pasaporte" ->
                "PA";
            default ->
                displayName;
        };
    }
}
