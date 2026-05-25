package views.auth;

import controllers.AuthController;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.AuthService;
import views.mainLayout.MainLayout;

public class LoginView extends StackPane {

    private final AuthController authController = new AuthController();

    // Campos del formulario
    private final TextField     correoField    = new TextField();
    private final PasswordField passwordField  = new PasswordField();
    private final TextField     passwordVisible = new TextField();
    private final Button        loginBtn       = new Button("Iniciar Sesión");
    private final Label         errorLabel     = new Label();
    private final VBox          loginCard      = new VBox();

    private boolean mostrandoPassword = false;

    public LoginView(Stage stage) {
        getStylesheets().add(getClass().getResource("/styles/auth/LoginView.css").toExternalForm());
        getStyleClass().add("root-pane");

        construirUI();
        configurarCallbacks(stage);
    }


    private void construirUI() {
        // Tarjeta
        loginCard.getStyleClass().add("login-card");
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setSpacing(0);
        loginCard.setMaxWidth(420);

        loginCard.getChildren().addAll(
                crearHeader(),
                crearSeparador(),
                crearFormulario(),
                crearFooter()
        );

        VBox fondo = new VBox(loginCard);
        fondo.setAlignment(Pos.CENTER);
        fondo.getStyleClass().add("background-pane");

        getChildren().add(fondo);
        animarEntrada();
    }

    private VBox crearHeader() {
        VBox header = new VBox(8);
        header.getStyleClass().add("card-header");
        header.setAlignment(Pos.CENTER);

        StackPane iconoCirculo = new StackPane(new Label("🏥"));
        iconoCirculo.getStyleClass().add("icon-circle");
        ((Label) iconoCirculo.getChildren().get(0)).getStyleClass().add("icon-label");

        Label titulo    = new Label("RedHospital");
        Label subtitulo = new Label("Sistema de Gestión Hospitalaria");
        titulo.getStyleClass().add("brand-title");
        subtitulo.getStyleClass().add("brand-subtitle");

        header.getChildren().addAll(iconoCirculo, titulo, subtitulo);
        return header;
    }

    private Separator crearSeparador() {
        Separator sep = new Separator();
        sep.getStyleClass().add("header-separator");
        return sep;
    }

    private VBox crearFormulario() {
        VBox form = new VBox(20);
        form.getStyleClass().add("form-container");

        Label titulo = new Label("Iniciar Sesión");
        titulo.getStyleClass().add("form-title");

        form.getChildren().addAll(
                titulo,
                crearCampoCorreo(),
                crearCampoPassword(),
                crearErrorLabel(),
                loginBtn
        );

        // Estilos del botón
        loginBtn.getStyleClass().add("login-btn");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> handleLogin());

        // Enter en los campos
        correoField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
        passwordVisible.setOnAction(e -> handleLogin());

        return form;
    }

    private VBox crearCampoCorreo() {
        VBox contenedor = new VBox(6);

        Label label = new Label("Correo Electrónico");
        label.getStyleClass().add("field-label");

        correoField.setPromptText("admin@redhospital.com");
        correoField.getStyleClass().add("input-field");
        HBox.setHgrow(correoField, Priority.ALWAYS);

        HBox inputBox = new HBox(10, new Label("✉"), correoField);
        inputBox.getStyleClass().add("input-container");
        inputBox.setAlignment(Pos.CENTER_LEFT);
        ((Label) inputBox.getChildren().get(0)).getStyleClass().add("field-icon");

        contenedor.getChildren().addAll(label, inputBox);
        return contenedor;
    }

    private VBox crearCampoPassword() {
        VBox contenedor = new VBox(6);

        Label label = new Label("Contraseña");
        label.getStyleClass().add("field-label");

        passwordField.setPromptText("••••••••••");
        passwordField.getStyleClass().add("input-field");
        HBox.setHgrow(passwordField, Priority.ALWAYS);

        // Campo texto plano (oculto por defecto)
        passwordVisible.setPromptText("••••••••••");
        passwordVisible.getStyleClass().add("input-field");
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
        passwordVisible.setManaged(false);
        passwordVisible.setVisible(false);
        HBox.setHgrow(passwordVisible, Priority.ALWAYS);

        Button toggleBtn = new Button("👁");
        toggleBtn.getStyleClass().add("toggle-password-btn");
        toggleBtn.setOnAction(e -> togglePassword(toggleBtn));

        HBox inputBox = new HBox(10, new Label("🔒"), passwordField, passwordVisible, toggleBtn);
        inputBox.getStyleClass().add("input-container");
        inputBox.setAlignment(Pos.CENTER_LEFT);
        ((Label) inputBox.getChildren().get(0)).getStyleClass().add("field-icon");

        contenedor.getChildren().addAll(label, inputBox);
        return contenedor;
    }

    private Label crearErrorLabel() {
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        return errorLabel;
    }

    private VBox crearFooter() {
        Label texto = new Label("Sistema exclusivo para personal administrativo autorizado");
        texto.getStyleClass().add("footer-text");
        texto.setWrapText(true);

        VBox footer = new VBox(texto);
        footer.getStyleClass().add("card-footer");
        footer.setAlignment(Pos.CENTER);
        return footer;
    }

    // ── Lógica ────────────────────────────────────────────────────────────

    private void configurarCallbacks(Stage stage) {
        authController.setOnExito(msg -> navegarAMainLayout(stage));
        authController.setOnError(this::mostrarError);
    }

    private void handleLogin() {
        String correo   = correoField.getText().trim();
        String password = passwordField.getText();

        if (correo.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor completa todos los campos.");
            return;
        }
        if (!correo.contains("@")) {
            mostrarError("Ingresa un correo electrónico válido.");
            return;
        }

        ocultarError();
        setLoading(true);
        authController.login(new AuthService.AuthCreateBody(correo, password));
    }

    private void navegarAMainLayout(Stage stage) {
        setLoading(false);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            MainLayout mainLayout = new MainLayout();
            Scene scene = new Scene(mainLayout, stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().addAll(stage.getScene().getStylesheets());
            stage.setWidth(1000);
            stage.setHeight(700);
            stage.centerOnScreen();
            stage.setScene(scene);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), mainLayout);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void togglePassword(Button btn) {
        mostrandoPassword = !mostrandoPassword;
        passwordField.setManaged(!mostrandoPassword);
        passwordField.setVisible(!mostrandoPassword);
        passwordVisible.setManaged(mostrandoPassword);
        passwordVisible.setVisible(mostrandoPassword);
        btn.setText("👁");
        (mostrandoPassword ? passwordVisible : passwordField).requestFocus();
    }

    private void mostrarError(String mensaje) {
        setLoading(false);
        errorLabel.setText(mensaje);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
        animarShake();
    }

    private void ocultarError() {
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }

    private void setLoading(boolean loading) {
        loginBtn.setDisable(loading);
        correoField.setDisable(loading);
        passwordField.setDisable(loading);
        passwordVisible.setDisable(loading);
        loginBtn.setText(loading ? "Iniciando sesión..." : "Iniciar Sesión");
    }

    // ── Animaciones ───────────────────────────────────────────────────────

    private void animarEntrada() {
        loginCard.setOpacity(0);
        loginCard.setTranslateY(-20);

        FadeTransition fade = new FadeTransition(Duration.millis(500), loginCard);
        fade.setFromValue(0); fade.setToValue(1); fade.play();

        TranslateTransition slide = new TranslateTransition(Duration.millis(500), loginCard);
        slide.setFromY(-20); slide.setToY(0); slide.play();
    }

    private void animarShake() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), loginCard);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> loginCard.setTranslateX(0));
        shake.play();
    }
}