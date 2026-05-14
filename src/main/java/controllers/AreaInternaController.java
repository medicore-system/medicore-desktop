package controllers;

import javafx.application.Platform;
import models.AreaInternaModel;
import services.HospitalService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

/**
 * Controlador de las áreas internas de un hospital específico.
 *
 * Centraliza la lógica de áreas: llama a {@link HospitalService} en hilos
 * secundarios, mantiene la lista local y notifica a la vista mediante callbacks.
 * Cada instancia está vinculada a un único hospital a través de {@code codigoHospital}.
 */
public class AreaInternaController {

    /** Servicio HTTP que comunica con el backend. */
    private final HospitalService service = new HospitalService();

    /** Código del hospital al que pertenecen las áreas gestionadas. */
    private final String codigoHospital;

    /** Lista completa de áreas cargadas para este hospital. */
    private List<AreaInternaModel> todas = new ArrayList<>();

    /** Callback disparado cuando la lista se carga o recarga exitosamente. */
    private Consumer<List<AreaInternaModel>> onDatosActualizados;

    /** Callback disparado cuando ocurre un error en cualquier operación HTTP. */
    private Consumer<String> onError;

    /** Callback disparado cuando una operación termina con éxito. */
    private Consumer<String> onExito;

    /**
     * Crea el controlador vinculado al hospital indicado.
     *
     * @param codigoHospital Código del hospital cuyas áreas se gestionarán.
     */
    public AreaInternaController(String codigoHospital) {
        this.codigoHospital = codigoHospital;
    }

    /**
     * Registra el callback que recibe la lista actualizada de áreas.
     *
     * @param cb Función que acepta la lista de {@link AreaInternaModel}.
     */
    public void setOnDatosActualizados(Consumer<List<AreaInternaModel>> cb) {
        this.onDatosActualizados = cb;
    }

    /**
     * Registra el callback que recibe mensajes de error de red o servidor.
     *
     * @param cb Función que acepta el mensaje de error.
     */
    public void setOnError(Consumer<String> cb) {
        this.onError = cb;
    }

    /**
     * Registra el callback que recibe mensajes de éxito tras crear o actualizar.
     *
     * @param cb Función que acepta el mensaje de confirmación.
     */
    public void setOnExito(Consumer<String> cb) {
        this.onExito = cb;
    }

    /**
     * Carga todas las áreas del hospital desde el servidor en un hilo secundario.
     * Al terminar, actualiza {@code todas} y dispara {@code onDatosActualizados}.
     */
    public void cargarAreas() {
        CompletableFuture.supplyAsync(() -> {
            try { return service.getAreas(codigoHospital); }
            catch (Exception e) { throw new CompletionException(e); }
        }).thenAccept(areas -> Platform.runLater(() -> {
            todas = new ArrayList<>(areas);
            if (onDatosActualizados != null) onDatosActualizados.accept(todas);
        })).exceptionally(e -> {
            Platform.runLater(() -> notificarError("No se pudieron cargar las áreas del hospital."));
            return null;
        });
    }

    /**
     * Filtra la lista local por tipo de área sin llamar al servidor.
     *
     * @param tipo Nombre del tipo de área (valor de {@code nombreAreaInterna}).
     *             Si es {@code null}, devuelve todas las áreas.
     * @return Lista de áreas que coinciden con el tipo indicado.
     */
    public List<AreaInternaModel> filtrarPorTipo(String tipo) {
        if (tipo == null) return todas;
        return todas.stream()
                .filter(a -> tipo.equals(a.nombreAreaInterna))
                .toList();
    }

    /**
     * Devuelve los tipos de área disponibles, sin duplicados y en orden alfabético.
     * Se usa para poblar el combo de filtro en la vista de detalle.
     *
     * @return Lista de nombres de tipos de área únicos.
     */
    public List<String> getTipos() {
        return todas.stream()
                .map(a -> a.nombreAreaInterna)
                .filter(t -> t != null)
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * Envía la petición de creación al servidor y recarga la lista al terminar.
     *
     * @param body   Datos de la nueva área.
     * @param nombre Nombre para el mensaje de confirmación.
     */
    public void crear(HospitalService.AreaCreateBody body, String nombre) {
        CompletableFuture.supplyAsync(() -> {
            try { return service.createArea(codigoHospital, body); }
            catch (Exception e) { throw new CompletionException(e); }
        }).thenAccept(a -> Platform.runLater(() -> {
            if (onExito != null) onExito.accept("Área '" + nombre + "' creada correctamente");
            cargarAreas();
        })).exceptionally(e -> {
            Platform.runLater(() -> notificarError("Error al crear el área."));
            return null;
        });
    }

    /**
     * Envía la petición de actualización al servidor y recarga la lista.
     *
     * @param codigoArea Código del área a actualizar.
     * @param body       Nuevos datos del área.
     * @param nombre     Nombre para el mensaje de confirmación.
     */
    public void actualizar(String codigoArea, HospitalService.AreaUpdateBody body, String nombre) {
        CompletableFuture.supplyAsync(() -> {
            try { return service.updateArea(codigoHospital, codigoArea, body); }
            catch (Exception e) { throw new CompletionException(e); }
        }).thenAccept(a -> Platform.runLater(() -> {
            if (onExito != null) onExito.accept("Área '" + nombre + "' actualizada correctamente");
            cargarAreas();
        })).exceptionally(e -> {
            Platform.runLater(() -> notificarError("Error al actualizar el área."));
            return null;
        });
    }

    /**
     * Genera el siguiente código disponible con formato {@code HAI###}.
     * Incrementa hasta encontrar uno que no exista en la lista local.
     *
     * @return Código listo para usar, ej. {@code HAI003}.
     */
    public String generarCodigo() {
        int n = todas.size() + 1;
        String candidato = String.format("HAI%03d", n);
        while (codigoYaExiste(candidato)) {
            n++;
            candidato = String.format("HAI%03d", n);
        }
        return candidato;
    }

    /**
     * Indica si el código ya está en uso en la lista local.
     *
     * @param codigo Código a verificar (ignorando mayúsculas/minúsculas).
     * @return {@code true} si ya existe; {@code false} en caso contrario.
     */
    public boolean codigoYaExiste(String codigo) {
        if (codigo == null || codigo.isBlank()) return false;
        return todas.stream().anyMatch(a -> codigo.equalsIgnoreCase(a.codigo));
    }

    /** Envía el mensaje de error al callback registrado, si existe. */
    private void notificarError(String mensaje) {
        if (onError != null) onError.accept(mensaje);
    }
}
