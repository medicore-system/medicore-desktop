package controllers;

import javafx.application.Platform;
import models.AreaInternaModel;
import models.HospitalModel;
import services.HospitalService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Controlador de las áreas internas de un hospital específico.
 *
 * <p>
 * Centraliza la lógica de áreas: llama al {@link HospitalService} en hilos
 * secundarios, mantiene la lista local y notifica a la vista mediante callbacks.
 * Cada instancia está vinculada a un único hospital a través de {@code codigoHospital}.
 * </p>
 *
 * @author Juan Sebastián López Guzmán
 * @author Cristian Camilo Salazar Arenas
 */
public class AreaInternaController {

    /** Prefijo utilizado para generar nuevos códigos de área interna. */
    private static final String PREFIJO_CODIGO = "HAI";

    /** Servicio HTTP que comunica con el backend. */
    private final HospitalService service = HospitalService.getInstance();

    /** Código del hospital al que pertenecen las áreas gestionadas. */
    private final String codigoHospital;

    /** Lista completa de áreas cargadas para este hospital. */
    private List<AreaInternaModel> todas = new ArrayList<>();

    /**
     * Conjunto de códigos de área usados por cualquier hospital del sistema.
     *
     * <p>El backend define {@code hospital_area_interna.codigo} como clave
     * primaria global, por lo que dos hospitales no pueden compartir el
     * mismo código de área. Esta caché se llena al cargar los tipos
     * disponibles y se utiliza para evitar colisiones al generar nuevos
     * códigos.</p>
     */
    private Set<String> codigosGlobales = new HashSet<>();

    /** Callback disparado cuando la lista se carga o recarga exitosamente. */
    private Consumer<List<AreaInternaModel>> onDatosActualizados;

    /** Callback disparado cuando ocurre un error en cualquier operación HTTP. */
    private Consumer<String> onError;

    /** Callback disparado cuando una operación termina con éxito. */
    private Consumer<String> onExito;

    /**
     * Crea el controlador vinculado al hospital indicado.
     *
     * @param codigoHospital Código del hospital cuyas áreas se gestionarán
     */
    public AreaInternaController(String codigoHospital) {
        this.codigoHospital = codigoHospital;
    }

    /**
     * Registra el callback que recibe la lista actualizada de áreas.
     *
     * @param cb Función que acepta la lista de {@link AreaInternaModel}
     */
    public void setOnDatosActualizados(Consumer<List<AreaInternaModel>> cb) {
        this.onDatosActualizados = cb;
    }

    /**
     * Registra el callback que recibe mensajes de error de red o servidor.
     *
     * @param cb Función que acepta el mensaje de error
     */
    public void setOnError(Consumer<String> cb) {
        this.onError = cb;
    }

    /**
     * Registra el callback que recibe mensajes de éxito tras crear o actualizar.
     *
     * @param cb Función que acepta el mensaje de confirmación
     */
    public void setOnExito(Consumer<String> cb) {
        this.onExito = cb;
    }

    /**
     * Carga todas las áreas del hospital desde el servidor.
     * Al terminar, actualiza la lista local y dispara {@code onDatosActualizados}.
     */
    public void cargarAreas() {
        service.getAreas(codigoHospital)
                .thenAccept(areas -> Platform.runLater(() -> {
                    todas = new ArrayList<>(areas);
                    if (onDatosActualizados != null) onDatosActualizados.accept(todas);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() ->
                            notificarError("No se pudieron cargar las áreas del hospital."));
                    return null;
                });
    }

    /**
     * Carga todos los tipos de área interna disponibles en el sistema.
     *
     * <p>Como el backend no expone un endpoint específico para los tipos,
     * se obtienen recorriendo las áreas registradas en todos los hospitales
     * y extrayendo combinaciones únicas {@code (codigoAreaInterna, nombreAreaInterna)}.
     * Sirve para alimentar el combo "Tipo" del formulario de creación.</p>
     *
     * @param onListo Callback que recibe la lista de tipos únicos
     */
    public void cargarTiposDisponibles(Consumer<List<TipoArea>> onListo) {
        service.getAllHospitals()
                .thenCompose(this::recolectarAreasDeTodos)
                .thenAccept(areas -> Platform.runLater(() -> {
                    codigosGlobales = areas.stream()
                            .map(a -> a.codigo)
                            .filter(c -> c != null && !c.isBlank())
                            .map(c -> c.toUpperCase(Locale.ROOT))
                            .collect(Collectors.toCollection(HashSet::new));
                    onListo.accept(tiposUnicos(areas));
                }))
                .exceptionally(e -> {
                    Platform.runLater(() ->
                            notificarError("No se pudieron cargar los tipos de área."));
                    return null;
                });
    }

    /**
     * Filtra la lista local por tipo de área sin llamar al servidor.
     *
     * @param tipo Nombre del tipo de área (valor de {@code nombreAreaInterna});
     *             si es {@code null}, devuelve todas las áreas
     * @return Lista de áreas que coinciden con el tipo indicado
     */
    public List<AreaInternaModel> filtrarPorTipo(String tipo) {
        if (tipo == null) return todas;
        return todas.stream()
                .filter(a -> tipo.equals(a.nombreAreaInterna))
                .toList();
    }

    /**
     * Devuelve los tipos de área disponibles en este hospital, sin duplicados
     * y en orden alfabético. Se usa para poblar el combo de filtro en la vista.
     *
     * @return Lista de nombres de tipos de área únicos
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
     * @param body   Datos de la nueva área
     * @param nombre Nombre para el mensaje de confirmación
     */
    public void crear(HospitalService.AreaCreateBody body, String nombre) {
        service.crearArea(codigoHospital, body)
                .thenAccept(a -> Platform.runLater(() -> {
                    if (a != null && a.codigo != null && !a.codigo.isBlank()) {
                        codigosGlobales.add(a.codigo.toUpperCase(Locale.ROOT));
                    }
                    if (onExito != null)
                        onExito.accept("Área '" + nombre + "' creada correctamente");
                    cargarAreas();
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError("Error al crear el área."));
                    return null;
                });
    }

    /**
     * Envía la petición de actualización al servidor y recarga la lista.
     *
     * @param codigoArea Código del área a actualizar
     * @param body       Nuevos datos del área
     * @param nombre     Nombre para el mensaje de confirmación
     */
    public void actualizar(String codigoArea, HospitalService.AreaUpdateBody body, String nombre) {
        service.actualizarArea(codigoHospital, codigoArea, body)
                .thenAccept(a -> Platform.runLater(() -> {
                    if (onExito != null)
                        onExito.accept("Área '" + nombre + "' actualizada correctamente");
                    cargarAreas();
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError("Error al actualizar el área."));
                    return null;
                });
    }

    /**
     * Genera el siguiente código disponible con formato {@code HAI###}.
     *
     * <p>El código del área es clave primaria global en el backend
     * ({@code hospital_area_interna.codigo}), por lo que se verifica
     * contra los códigos del hospital actual y contra los códigos
     * usados por cualquier otro hospital del sistema. Esto evita que
     * un código generado para un hospital sobrescriba el área de otro
     * hospital al persistirse.</p>
     *
     * @return Código listo para usar, p. ej. {@code HAI003}
     */
    public String generarCodigo() {
        int n = Math.max(todas.size(), codigosGlobales.size()) + 1;
        String candidato = String.format("%s%03d", PREFIJO_CODIGO, n);
        while (codigoYaExiste(candidato)) {
            n++;
            candidato = String.format("%s%03d", PREFIJO_CODIGO, n);
        }
        return candidato;
    }

    /**
     * Indica si el código ya está en uso, tanto en la lista local del
     * hospital actual como en la caché global de códigos del sistema.
     *
     * @param codigo Código a verificar (ignorando mayúsculas/minúsculas)
     * @return {@code true} si ya existe; {@code false} en caso contrario
     */
    public boolean codigoYaExiste(String codigo) {
        if (codigo == null || codigo.isBlank()) return false;
        String normalizado = codigo.toUpperCase(Locale.ROOT);
        if (codigosGlobales.contains(normalizado)) return true;
        return todas.stream().anyMatch(a -> codigo.equalsIgnoreCase(a.codigo));
    }

    /**
     * Lanza en paralelo una petición por hospital para obtener todas sus áreas
     * y concatena los resultados en una sola lista. Si la consulta de algún
     * hospital falla, su lista se considera vacía para no abortar el resto.
     *
     * @param hospitales Lista de hospitales sobre los que iterar
     * @return Future con todas las áreas combinadas
     */
    private CompletableFuture<List<AreaInternaModel>> recolectarAreasDeTodos(List<HospitalModel> hospitales) {
        List<CompletableFuture<List<AreaInternaModel>>> futuros = hospitales.stream()
                .map(h -> service.getAreas(h.codigo).exceptionally(e -> List.of()))
                .toList();
        return CompletableFuture.allOf(futuros.toArray(new CompletableFuture[0]))
                .thenApply(v -> futuros.stream()
                        .flatMap(f -> f.join().stream())
                        .toList());
    }

    /**
     * Reduce la lista de áreas a tipos únicos, ordenados por nombre.
     *
     * @param areas Áreas obtenidas del backend
     * @return Lista de tipos sin duplicados
     */
    private List<TipoArea> tiposUnicos(List<AreaInternaModel> areas) {
        Map<String, TipoArea> porCodigo = new LinkedHashMap<>();
        for (AreaInternaModel a : areas) {
            if (a.codigoAreaInterna == null) continue;
            porCodigo.putIfAbsent(a.codigoAreaInterna,
                    new TipoArea(a.codigoAreaInterna, a.nombreAreaInterna));
        }
        return porCodigo.values().stream()
                .sorted((t1, t2) -> safe(t1.nombre()).compareToIgnoreCase(safe(t2.nombre())))
                .toList();
    }

    /** Devuelve cadena vacía si el valor es {@code null}, útil para ordenamientos. */
    private String safe(String valor) {
        return valor == null ? "" : valor;
    }

    /** Envía el mensaje de error al callback registrado, si existe. */
    private void notificarError(String mensaje) {
        if (onError != null) onError.accept(mensaje);
    }

    /**
     * Tipo de área interna disponible para el formulario de creación.
     *
     * @param codigo Código del tipo (FK hacia {@code area_interna})
     * @param nombre Nombre legible del tipo (p. ej. "urgencias")
     */
    public record TipoArea(String codigo, String nombre) {
        @Override
        public String toString() {
            return nombre == null || nombre.isBlank() ? codigo : nombre;
        }
    }
}
