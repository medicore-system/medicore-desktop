package controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import models.UsuarioModel;
import services.CiudadService;
import services.EpsService;
import services.UsuarioService;
import models.CiudadModel;
import models.EpsModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controlador de usuarios.
 *
 * Responsabilidades:
 *   - Llamar al UsuarioService dentro de Tasks (hilo secundario)
 *   - Mantener el estado local: lista de usuarios cargados
 *   - Filtrar la lista sin volver a llamar al servidor
 *   - Notificar a la vista mediante callbacks cuando los datos cambian
 *
 * La vista NO hace HTTP. Solo llama métodos de este controller
 * y reacciona a los callbacks.
 */
public class UsuarioController {

    private final UsuarioService service = UsuarioService.getInstance();
    private final EpsService epsService = EpsService.getInstance();
    private final CiudadService  ciudadService = CiudadService.getInstance();

    /** Lista completa cargada desde el servidor. Base para el filtrado. */
    private List<UsuarioModel> todos = new ArrayList<>();

    /** Se dispara cuando la lista se carga o recarga exitosamente. */
    private Consumer<List<UsuarioModel>> onDatosActualizados;

    /** Se dispara cuando ocurre un error en cualquier operación HTTP. */
    private Consumer<String> onError;

    /** Se dispara cuando una operación (crear/actualizar/toggle) termina bien. */
    private Consumer<String> onExito;

    /**
     * Establece el callback que se ejecutará cuando los datos de usuarios
     * hayan sido actualizados correctamente.
     *
     * @param cb función callback que recibe la lista actualizada de usuarios
     */
    public void setOnDatosActualizados(Consumer<List<UsuarioModel>> cb) {
        this.onDatosActualizados = cb;
    }

    /**
     * Establece el callback que se ejecutará cuando ocurra un error
     * durante alguna operación.
     *
     * @param cb función callback que recibe el mensaje de error
     */
    public void setOnError(Consumer<String> cb) {
        this.onError = cb;
    }

    /**
     * Establece el callback que se ejecutará cuando una operación
     * finalice exitosamente.
     *
     * @param cb función callback que recibe el mensaje de éxito
     */
    public void setOnExito(Consumer<String> cb) {
        this.onExito = cb;
    }

    /**
     * Carga todos los usuarios desde el servidor en un hilo secundario.
     * Al terminar notifica a la vista via onDatosActualizados.
     */
    public void cargarUsuarios() {
        service.getAllUsers().thenAccept(usuarios -> Platform.runLater(() -> {
            todos = new ArrayList<>(usuarios);
            if(onDatosActualizados != null) onDatosActualizados.accept(todos);
        })).exceptionally(e -> {
            Platform.runLater(() -> notificarError("No se puede cargar los usuarios.\n" + "Verifica que el servidor este corriendo en localhost:8080"));
            return null;
        });
    }

    /**
     * Carga las ciudades disponibles para los formularios.
     *
     * @param onListo Callback que recibe la lista de ciudades al terminar.
     */
    public void cargarCiudades(Consumer<List<CiudadModel>> onListo) {
        ciudadService.getAllCiudades().thenAccept(ciudades -> Platform.runLater(() -> onListo.accept(ciudades)))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError("No se pudieron cargar las ciudades"));
                    return null;
                });
    }

    /**
     * Carga las EPS disponibles para los formularios.
     *
     * @param onListo Callback que recibe la lista de EPS al terminar.
     */
    public void cargarEps(Consumer<List<EpsModel>> onListo) {
        epsService.getAllEps().thenAccept(eps -> Platform.runLater(() -> onListo.accept(eps)))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError("No se pudieron cargar las Eps"));
                    return null;
                });
    }

    /**
     * Filtra la lista local sin llamar al servidor.
     * Busca en documento, nombre, apellido, ciudad y eps.
     *
     * @param texto Texto ingresado en el buscador. Si es vacío, devuelve todos.
     * @return Lista filtrada de usuarios.
     */
    public List<UsuarioModel> filtrar(String texto) {
        if (texto == null || texto.isBlank()) return todos;
        String t = texto.toLowerCase().trim();
        return todos.stream()
                .filter(u ->
                    contiene(u.getDocumento(), t) ||
                    contiene(u.getNombre(),    t) ||
                    contiene(u.getApellido(),  t) ||
                    contiene(u.getCiudad(),    t) ||
                    contiene(u.getEps(),       t))
                .toList();
    }

    /**
     * Crea un nuevo usuario y recarga la lista al terminar.
     *
     * @param body    Datos del usuario a crear.
     * @param nombre  Nombre del usuario (solo para el mensaje de éxito).
     */
    public void crear(UsuarioService.UsuarioCreateBody body, String nombre) {
        service.crear(body).thenAccept(u -> Platform.runLater(() -> {
            if(onExito != null) onExito.accept("Usuario '" + nombre + "'creado correctamente");
            cargarUsuarios();
        })).exceptionally(e -> {
            Platform.runLater(() -> notificarError("Error al crear el usuario.\n" +
                    "Verifica que el documento no esté duplicado y que la ciudad y EPS sean válidas."));
            return null;
        });
    }

    /**
     * Actualiza los datos de un usuario existente y recarga la lista.
     *
     * @param documento Documento del usuario a actualizar.
     * @param body      Nuevos datos.
     * @param nombre    Nombre del usuario (para el mensaje de éxito).
     */
    public void actualizar(String documento, UsuarioService.UsuarioUpdateBody body, String nombre) {
        service.actualizar(documento, body)
                .thenAccept(u -> Platform.runLater(() -> {
                    if (onExito != null) onExito.accept("Usuario '" + nombre + "' actualizado correctamente");
                    cargarUsuarios();
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError("Error al actualizar el usuario."));
                    return null;
                });
    }

    /**
     * Cambia el estado del usuario (habilitar/inhabilitar) y recarga la lista.
     *
     * @param documento Documento del usuario.
     * @param nombreCompleto Nombre para el mensaje de éxito.
     */
    public void toggleEstado(String documento, String nombreCompleto) {
        service.toggleEstado(documento)
                .thenAccept(u -> Platform.runLater(() -> {
                    if (onExito != null) onExito.accept("Estado de '" + nombreCompleto + "' actualizado");
                    cargarUsuarios();
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError("Error al cambiar el estado del usuario."));
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

    /**
     * Verifica si un texto está contenido dentro de un campo,
     * ignorando diferencias entre mayúsculas y minúsculas.
     *
     * @param campo campo donde se realizará la búsqueda
     * @param texto texto a buscar dentro del campo
     * @return {@code true} si el campo contiene el texto;
     *         {@code false} en caso contrario o si el campo es {@code null}
     */
    private boolean contiene(String campo, String texto) {
        return campo != null && campo.toLowerCase().contains(texto);
    }
}
