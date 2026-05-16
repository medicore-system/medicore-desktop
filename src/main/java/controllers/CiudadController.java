package controllers;

import javafx.application.Platform;
import models.CiudadModel;
import services.CiudadService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controlador de ciudades.
 *
 * Responsabilidades:
 *   - Llamar al CiudadService en hilo secundario
 *   - Mantener el estado local: lista de ciudades cargadas
 *   - Filtrar la lista sin volver a llamar al servidor
 *   - Notificar a la vista mediante callbacks cuando los datos cambian
 *
 * La vista NO hace HTTP. Solo llama métodos de este controller
 * y reacciona a los callbacks.
 */
public class CiudadController {

    private final CiudadService service = CiudadService.getInstance();

    private List<CiudadModel> todos = new ArrayList<>();

    private Consumer<List<CiudadModel>> onDatosActualizados;
    private Consumer<String>            onError;
    private Consumer<String>            onExito;

    public void setOnDatosActualizados(Consumer<List<CiudadModel>> cb) { this.onDatosActualizados = cb; }
    public void setOnError(Consumer<String> cb)                        { this.onError = cb; }
    public void setOnExito(Consumer<String> cb)                        { this.onExito = cb; }

    /**
     * Carga todas las ciudades desde el servidor.
     * Al terminar notifica a la vista via onDatosActualizados.
     */
    public void cargarCiudades() {
        service.getAllCiudades().thenAccept(ciudades -> Platform.runLater(() -> {
            todos = new ArrayList<>(ciudades);
            if (onDatosActualizados != null) onDatosActualizados.accept(todos);
        })).exceptionally(e -> {
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            System.err.println("[CiudadController] Error al cargar ciudades: " + causa.getMessage());
            Platform.runLater(() -> notificarError(
                    "No se pueden cargar las ciudades.\n" + causa.getMessage()));
            return null;
        });
    }

    /**
     * Filtra la lista local sin llamar al servidor.
     * Busca en código, nombre y departamento.
     */
    public List<CiudadModel> filtrar(String texto) {
        if (texto == null || texto.isBlank()) return todos;
        String t = texto.toLowerCase().trim();
        return todos.stream()
                .filter(c ->
                    contiene(c.getCodigo(),      t) ||
                    contiene(c.getNombre(),       t) ||
                    contiene(c.getDepartamento(), t))
                .toList();
    }

    /**
     * Crea una nueva ciudad. El backend genera el código automáticamente.
     *
     * @param body Datos: name, department, status.
     */
    public void crear(CiudadService.CiudadCreateBody body) {
        service.crear(body).thenAccept(c -> Platform.runLater(() -> {
            if (onExito != null) onExito.accept("Ciudad '" + body.name() + "' creada correctamente");
            cargarCiudades();
        })).exceptionally(e -> {
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            System.err.println("[CiudadController] Error al crear ciudad: " + causa.getMessage());
            Platform.runLater(() -> notificarError(
                    "Error al crear la ciudad.\n" + causa.getMessage()));
            return null;
        });
    }

    /**
     * Actualiza los datos de una ciudad existente.
     *
     * @param codigo Código de la ciudad a actualizar.
     * @param body   Nuevos datos: name, department, status.
     */
    public void actualizar(String codigo, CiudadService.CiudadUpdateBody body) {
        service.actualizar(codigo, body).thenAccept(c -> Platform.runLater(() -> {
            if (onExito != null) onExito.accept("Ciudad '" + body.name() + "' actualizada correctamente");
            cargarCiudades();
        })).exceptionally(e -> {
            Platform.runLater(() -> notificarError("Error al actualizar la ciudad."));
            return null;
        });
    }

    private void notificarError(String mensaje) {
        if (onError != null) onError.accept(mensaje);
    }

    private boolean contiene(String campo, String texto) {
        return campo != null && campo.toLowerCase().contains(texto);
    }
}
