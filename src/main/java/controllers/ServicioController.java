package controllers;

import javafx.application.Platform;
import models.ServicioModel;
import models.TipoServicioModel;
import services.ServicioService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controlador de servicios médicos.
 *
 * Responsabilidades:
 *   - Llamar al ServicioService para hacer las peticiones HTTP.
 *   - Mantener la lista de servicios y tipos cargados en memoria.
 *   - Filtrar la lista sin volver a llamar al servidor.
 *   - Notificar a la vista mediante callbacks cuando los datos cambian.
 *
 * La vista NO hace HTTP. Solo llama métodos de este controlador
 * y reacciona a los callbacks.
 */
public class ServicioController {

    private final ServicioService service = ServicioService.getInstance();

    /** Lista completa de servicios cargada desde el servidor. */
    private List<ServicioModel> todos = new ArrayList<>();

    /** Lista de tipos de servicio disponibles (Consulta, Examen, etc.). */
    private List<TipoServicioModel> tipos = new ArrayList<>();

    /** Se dispara cuando la lista de servicios se carga o recarga exitosamente. */
    private Consumer<List<ServicioModel>> onDatosActualizados;

    /** Se dispara cuando los tipos de servicio se cargan exitosamente. */
    private Consumer<List<TipoServicioModel>> onTiposCargados;

    /** Se dispara cuando ocurre un error en cualquier operación. */
    private Consumer<String> onError;

    /** Se dispara cuando una operación termina bien. */
    private Consumer<String> onExito;

    public void setOnDatosActualizados(Consumer<List<ServicioModel>> cb) { this.onDatosActualizados = cb; }
    public void setOnTiposCargados(Consumer<List<TipoServicioModel>> cb) { this.onTiposCargados = cb; }
    public void setOnError(Consumer<String> cb) { this.onError = cb; }
    public void setOnExito(Consumer<String> cb) { this.onExito = cb; }

    /**
     * Devuelve la lista de tipos cargada en memoria.
     *
     * @return lista de tipos de servicio
     */
    public List<TipoServicioModel> getTipos() {
        return tipos;
    }

    /**
     * Carga todos los servicios desde el servidor.
     * Al terminar notifica a la vista via onDatosActualizados.
     */
    public void cargarServicios() {
        service.getAllServicios().thenAccept(servicios -> Platform.runLater(() -> {
            todos = new ArrayList<>(servicios);
            if (onDatosActualizados != null) onDatosActualizados.accept(todos);
        })).exceptionally(e -> {
            Platform.runLater(() -> notificarError(
                    "No se pueden cargar los servicios.\n" +
                    "Verifica que el servidor esté corriendo en localhost:8080"));
            return null;
        });
    }

    /**
     * Carga los tipos de servicio disponibles desde el backend.
     * Se necesitan para mostrar el ComboBox en el formulario.
     */
    public void cargarTipos() {
        service.getAllTipos().thenAccept(lista -> Platform.runLater(() -> {
            tipos = new ArrayList<>(lista);
            if (onTiposCargados != null) onTiposCargados.accept(tipos);
        })).exceptionally(e -> {
            Platform.runLater(() -> notificarError("No se pudieron cargar los tipos de servicio."));
            return null;
        });
    }

    /**
     * Filtra la lista local sin llamar al servidor.
     * Busca en código, nombre, tipo y descripción.
     *
     * @param texto Texto ingresado en el buscador. Si es vacío, devuelve todos.
     * @return Lista filtrada de servicios.
     */
    public List<ServicioModel> filtrar(String texto) {
        if (texto == null || texto.isBlank()) return todos;
        String t = texto.toLowerCase().trim();
        return todos.stream()
                .filter(s ->
                    contiene(s.getCodigo(),      t) ||
                    contiene(s.getNombre(),      t) ||
                    contiene(s.getTipo(),        t) ||
                    contiene(s.getDescripcion(), t))
                .toList();
    }

    /**
     * Crea un nuevo servicio y recarga la lista al terminar.
     *
     * @param body   Datos del servicio a crear.
     * @param nombre Nombre del servicio (para el mensaje de éxito).
     */
    public void crear(ServicioService.ServicioCreateBody body, String nombre) {
        service.crear(body).thenAccept(s -> Platform.runLater(() -> {
            if (onExito != null) onExito.accept("Servicio '" + nombre + "' creado correctamente");
            cargarServicios();
        })).exceptionally(e -> {
            Platform.runLater(() -> notificarError("Error al crear el servicio.\n" +
                    "Verifica que los datos sean válidos."));
            return null;
        });
    }

    /**
     * Actualiza los datos de un servicio existente y recarga la lista.
     *
     * @param codigo Código del servicio a actualizar.
     * @param body   Nuevos datos.
     * @param nombre Nombre del servicio (para el mensaje de éxito).
     */
    public void actualizar(String codigo, ServicioService.ServicioUpdateBody body, String nombre) {
        service.actualizar(codigo, body).thenAccept(s -> Platform.runLater(() -> {
            if (onExito != null) onExito.accept("Servicio '" + nombre + "' actualizado correctamente");
            cargarServicios();
        })).exceptionally(e -> {
            Platform.runLater(() -> notificarError("Error al actualizar el servicio."));
            return null;
        });
    }

    /**
     * Cambia el estado del servicio.
     * Si está activo lo inactiva usando DELETE.
     * Si está inactivo lo reactiva usando PUT con estado true.
     *
     * @param servicio servicio al que se le cambia el estado.
     */
    public void toggleEstado(ServicioModel servicio) {
        boolean estaActivo = Boolean.TRUE.equals(servicio.getEstado());
        if (estaActivo) {
            service.inactivar(servicio.getCodigo()).thenAccept(v -> Platform.runLater(() -> {
                if (onExito != null) onExito.accept("Servicio '" + servicio.getNombre() + "' inactivado");
                cargarServicios();
            })).exceptionally(e -> {
                Platform.runLater(() -> notificarError("Error al inactivar el servicio."));
                return null;
            });
        } else {
            ServicioService.ServicioUpdateBody body = new ServicioService.ServicioUpdateBody(
                    servicio.getNombre(),
                    servicio.getDescripcion(),
                    servicio.getIdTipoServicio(),
                    servicio.getPrecio(),
                    true
            );
            service.actualizar(servicio.getCodigo(), body).thenAccept(s -> Platform.runLater(() -> {
                if (onExito != null) onExito.accept("Servicio '" + servicio.getNombre() + "' activado");
                cargarServicios();
            })).exceptionally(e -> {
                Platform.runLater(() -> notificarError("Error al activar el servicio."));
                return null;
            });
        }
    }

    /**
     * Notifica un mensaje de error a través del callback registrado.
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
     * @return true si el campo contiene el texto; false si no o si el campo es null
     */
    private boolean contiene(String campo, String texto) {
        return campo != null && campo.toLowerCase().contains(texto);
    }
}
