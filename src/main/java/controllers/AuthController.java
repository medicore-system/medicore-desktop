package controllers;

import javafx.application.Platform;
import models.auth.SessionManager;
import services.AuthService;

import java.util.function.Consumer;
/**
 * Controlador de Auth.
 *
 * Responsabilidades:
 *   - Logica de inicio de sesion por parte de un admin en el sistema
 *
 * La vista NO hace HTTP. Solo llama métodos de este controller
 * y reacciona a los callbacks.
 */
public class AuthController {

    private final AuthService authService = AuthService.getInstance();

    /** Se dispara cuando ocurre un error en cualquier operación HTTP. */
    private Consumer<String> onError;
    /** Se dispara cuando una operación (crear/actualizar/toggle) termina bien. */
    private Consumer<String> onExito;

    /**
     * Establece el callback que se ejecutará cuando ocurra un error
     * durante alguna operación.
     *
     * @param onError función callback que recibe el mensaje de error
     */
    public void setOnError(Consumer<String> onError) {
        this.onError = onError;
    }

    /**
     * Establece el callback que se ejecutará cuando una operación
     * finalice exitosamente.
     *
     * @param onExito función callback que recibe el mensaje de éxito
     */
    public void setOnExito(Consumer<String> onExito) {
        this.onExito = onExito;
    }

    public void login(AuthService.AuthCreateBody body){
        authService.login(body).thenAccept( auth -> Platform.runLater(()->{
            if(onExito != null && auth.getRole().equals("ADMIN")){
                SessionManager.getInstance().setSession(auth.getToken(), auth.getRole());
                onExito.accept("BIENVENIDO");
            }
        })).exceptionally(e -> {
            System.out.println(body);
            Platform.runLater(()-> notificarError(e.getMessage()));
            return null;
        });
    }

    /**
     * Notifica un mensaje de error a través del callback registrado,
     * si existe uno configurado.
     *
     * @param mensaje mensaje descriptivo del error ocurrido
     */
    private void notificarError(String mensaje) {
        if (onError != null) onError.accept(mensaje);
    }

}
