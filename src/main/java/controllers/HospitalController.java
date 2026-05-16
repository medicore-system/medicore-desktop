package controllers;

import javafx.application.Platform;
import models.HospitalModel;
import services.HospitalService;
import views.hospital.CiudadHospitalModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

/**
 * Controlador de la sección de Hospitales.
 *
 * Centraliza toda la lógica de negocio: llama a {@link HospitalService} en
 * hilos secundarios ({@code CompletableFuture}), mantiene la lista local de
 * hospitales y notifica a la vista mediante callbacks cuando los datos cambian.
 * La vista no accede al servicio ni crea hilos directamente.
 */
public class HospitalController {

    /** Servicio HTTP que comunica con el backend. */
    private final HospitalService service = new HospitalService();

    /** Lista completa de hospitales cargados. Base para filtros locales. */
    private List<HospitalModel> todos = new ArrayList<>();

    /** Callback disparado cuando la lista se carga o recarga exitosamente. */
    private Consumer<List<HospitalModel>> onDatosActualizados;

    /** Callback disparado cuando ocurre un error en cualquier operación HTTP. */
    private Consumer<String> onError;

    /** Callback disparado cuando una operación termina con éxito. */
    private Consumer<String> onExito;

    /**
     * Registra el callback que recibe la lista actualizada de hospitales.
     *
     * @param cb Función que acepta la lista de {@link HospitalModel}.
     */
    public void setOnDatosActualizados(Consumer<List<HospitalModel>> cb) {
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
     * Carga todos los hospitales desde el servidor en un hilo secundario.
     * Al terminar, actualiza {@code todos} y dispara {@code onDatosActualizados}.
     */
    public void cargarHospitales() {
        CompletableFuture.supplyAsync(() -> {
            try { return service.getAll(); }
            catch (Exception e) { throw new CompletionException(e); }
        }).thenAccept(hospitales -> Platform.runLater(() -> {
            todos = new ArrayList<>(hospitales);
            if (onDatosActualizados != null) onDatosActualizados.accept(todos);
        })).exceptionally(e -> {
            Platform.runLater(() -> notificarError(
                    "No se pudieron cargar los hospitales.\n" +
                    "Verifica que el servidor esté corriendo en localhost:8080."));
            return null;
        });
    }

    /**
     * Carga un hospital por su código y lo entrega al callback.
     * Usado por {@code HospitalDetalleView} para mostrar el encabezado.
     *
     * @param codigo  Código único del hospital.
     * @param onListo Callback que recibe el {@link HospitalModel} al terminar.
     */
    public void cargarHospitalPorId(String codigo, Consumer<HospitalModel> onListo) {
        CompletableFuture.supplyAsync(() -> {
            try { return service.getById(codigo); }
            catch (Exception e) { throw new CompletionException(e); }
        }).thenAccept(h -> Platform.runLater(() -> onListo.accept(h)))
        .exceptionally(e -> {
            Platform.runLater(() -> notificarError("No se pudo cargar la información del hospital."));
            return null;
        });
    }

    /**
     * Carga las ciudades disponibles para el combo del formulario.
     *
     * @param onListo Callback que recibe la lista de {@link CiudadHospitalModel}.
     */
    public void cargarCiudades(Consumer<List<CiudadHospitalModel>> onListo) {
        CompletableFuture.supplyAsync(() -> {
            try { return service.getCiudades(); }
            catch (Exception e) { throw new CompletionException(e); }
        }).thenAccept(ciudades -> Platform.runLater(() -> onListo.accept(ciudades)))
        .exceptionally(e -> {
            Platform.runLater(() -> notificarError("No se pudieron cargar las ciudades."));
            return null;
        });
    }

    /**
     * Filtra la lista local sin llamar al servidor.
     * Busca coincidencias en código, nombre, dirección, teléfono y ciudad.
     *
     * @param texto Texto del buscador. Si es vacío o nulo, devuelve todos.
     * @return Lista de hospitales que coinciden con el texto.
     */
    public List<HospitalModel> filtrar(String texto) {
        if (texto == null || texto.isBlank()) return todos;
        String t = texto.toLowerCase().trim();
        return todos.stream()
                .filter(h ->
                        contiene(h.codigo, t) ||
                        contiene(h.nombre, t) ||
                        contiene(h.direccion, t) ||
                        contiene(h.telefono, t) ||
                        contiene(h.nombreCiudad, t))
                .toList();
    }

    /**
     * Envía la petición de creación al servidor y recarga la lista al terminar.
     *
     * @param body   Datos del nuevo hospital.
     * @param nombre Nombre para el mensaje de confirmación.
     */
    public void crear(HospitalService.HospitalCreateBody body, String nombre) {
        CompletableFuture.supplyAsync(() -> {
            try { return service.createHospital(body); }
            catch (Exception e) { throw new CompletionException(e); }
        }).thenAccept(h -> Platform.runLater(() -> {
            if (onExito != null) onExito.accept("Hospital '" + nombre + "' creado correctamente");
            cargarHospitales();
        })).exceptionally(e -> {
            Platform.runLater(() -> notificarError(
                    "Error al crear el hospital.\n" +
                    "Verifica que el código y ciudad sean válidos."));
            return null;
        });
    }

    /**
     * Envía la petición de actualización al servidor y recarga la lista.
     *
     * @param codigo Código del hospital a actualizar.
     * @param body   Nuevos datos del hospital.
     * @param nombre Nombre para el mensaje de confirmación.
     */
    public void actualizar(String codigo, HospitalService.HospitalUpdateBody body, String nombre) {
        CompletableFuture.supplyAsync(() -> {
            try { return service.update(codigo, body); }
            catch (Exception e) { throw new CompletionException(e); }
        }).thenAccept(h -> Platform.runLater(() -> {
            if (onExito != null) onExito.accept("Hospital '" + nombre + "' actualizado correctamente");
            cargarHospitales();
        })).exceptionally(e -> {
            Platform.runLater(() -> notificarError("Error al actualizar el hospital."));
            return null;
        });
    }

    /**
     * Genera el siguiente código disponible con formato {@code HOS###}.
     * Incrementa hasta encontrar uno que no exista en la lista local.
     *
     * @return Código listo para usar, ej. {@code HOS003}.
     */
    public String generarCodigo() {
        int n = todos.size() + 1;
        String candidato = String.format("HOS%03d", n);
        while (codigoYaExiste(candidato)) {
            n++;
            candidato = String.format("HOS%03d", n);
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
        return todos.stream().anyMatch(h -> codigo.equalsIgnoreCase(h.codigo));
    }

    /** Envía el mensaje de error al callback registrado, si existe. */
    private void notificarError(String mensaje) {
        if (onError != null) onError.accept(mensaje);
    }

    /**
     * Comprueba si un campo contiene el texto buscado (sin importar mayúsculas).
     *
     * @param campo Valor del campo a revisar; puede ser {@code null}.
     * @param texto Texto buscado en minúsculas.
     * @return {@code true} si el campo contiene el texto.
     */
    private boolean contiene(String campo, String texto) {
        return campo != null && campo.toLowerCase().contains(texto);
    }
}
