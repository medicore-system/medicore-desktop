package controllers;

import javafx.application.Platform;
import models.CitaModel;
import models.UsuarioModel;
import services.CitaService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CitaController {
    private final CitaService service = CitaService.getInstance();

    /** Lista completa cargada desde el servidor. Base para el filtrado. */
    private List<CitaModel> todos = new ArrayList<>();

    /** Se dispara cuando la lista se carga o recarga exitosamente. */
    private Consumer<List<CitaModel>> onDatosActualizados;

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
    public void setOnDatosActualizados(Consumer<List<CitaModel>> cb) {
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
     * Carga todas las citas de un usuario desde el servidor en un hilo secundario.
     * Al terminar notifica a la vista via onDatosActualizados.
     */
    public void cargarCitas(String documento) {
        service.getCitasPaciente(documento).thenAccept(citas -> Platform.runLater(() -> {
            todos = new ArrayList<>(citas);
            if(onDatosActualizados != null) onDatosActualizados.accept(todos);
        })).exceptionally(e -> {
            Platform.runLater(() -> notificarError("No se puede cargar los usuarios.\n" + "Verifica que el servidor este corriendo en localhost:8080"));
            return null;
        });
    }

    /**
     * Filtra la lista local sin llamar al servidor.
     * Busca en codigo, paciente, especialidad, fecha, medico.
     *
     * @param texto Texto ingresado en el buscador. Si es vacío, devuelve todos.
     * @return Lista filtrada de citas.
     */
    public List<CitaModel> filtrar(String texto) {
        if (texto == null || texto.isBlank()) return todos;
        String t = texto.toLowerCase().trim();
        return todos.stream()
                .filter(u ->
                        contiene(u.getCodigo(),                  t) ||
                                contiene(u.getNombreUsuario(),   t) ||
                                contiene(u.getTipoCita(),        t) ||
                                contiene(u.getFecha().toString(),t) ||
                                contiene(u.getMedico(),          t))
                .toList();
    }

    /**
     * Cambia el estado de la cita  (APROBADA) y recarga la lista.
     *
     * @param codigo codigo de la cita.
     */
    public void aprobar(String codigo) {
        service.aprobar(codigo).thenAccept(u-> Platform.runLater(() -> {
            if (onExito != null) onExito.accept("cita '" + codigo + "' aprobada exitosamete");
        }))
        .exceptionally(e -> {
            Platform.runLater(() -> notificarError("Error al aprobar la cita"));
            return null;
        });
    }

    /**
     * Cambia el estado de la cita  (DENEGADA) y recarga la lista.
     *
     * @param codigo codigo de la cita.
     */
    public void denegar(String codigo) {
        service.denegar(codigo).thenAccept(u-> Platform.runLater(() -> {
                    if (onExito != null) onExito.accept("cita '" + codigo + "' denegada exitosamete");
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError("Error al aprobar la cita"));
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
