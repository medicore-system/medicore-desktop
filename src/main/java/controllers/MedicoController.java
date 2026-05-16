package controllers;

import javafx.application.Platform;
import models.CiudadModel;
import models.EspecialidadModel;
import models.MedicoModel;
import services.CiudadService;
import services.EspecialidadService;
import services.MedicoService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
/**
 * Controlador de médicos.
 *
 * Responsabilidades:
 *   - Llamar al MedicoService dentro de Tasks (hilo secundario)
 *   - Mantener el estado local: lista de médicos cargados
 *   - Filtrar la lista sin volver a llamar al servidor
 *   - Notificar a la vista mediante callbacks cuando los datos cambian
 *
 * La vista NO hace HTTP. Solo llama métodos de este controller
 * y reacciona a los callbacks.
 */
public class MedicoController {
    private final MedicoService medicoService = MedicoService.getInstance();
    private final CiudadService ciudadService = CiudadService.getInstance();
    private final EspecialidadService especialidadService = EspecialidadService.getInstance();

    /** Lista completa cargada desde el servidor. Base para el filtrado. */
    private List<MedicoModel> listaMedicos = new ArrayList<>();

    /** Se dispara cuando la lista se carga o recarga exitosamente. */
    private Consumer<List<MedicoModel>> onDatosActualizados;

    /** Se dispara cuando ocurre un error en cualquier operación HTTP. */
    private Consumer<String> onError;

    /** Se dispara cuando una operación (crear/actualizar/toggle) termina bien. */
    private Consumer<String> onExito;

    /**
     * Establece el callback que se ejecutará cuando los datos de médico
     * hayan sido actualizados correctamente.
     *
     * @param cb función callback que recibe la lista actualizada de médicos
     */
    public void setOnDatosActualizados(Consumer<List<MedicoModel>> cb) {
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
     * Carga todos los medicos desde el servidor en un hilo secundario.
     * Al terminar notifica a la vista via onDatosActualizados.
     */
    public void cargarMedicos() {
        medicoService.getAllMedicos().thenAccept(medicos-> Platform.runLater(() -> {
            listaMedicos = new ArrayList<>(medicos);
            if(onDatosActualizados != null) onDatosActualizados.accept(listaMedicos);
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
    public void cargarEspecialidades(Consumer<List<EspecialidadModel>> onListo) {
        especialidadService.getAllEspecialidades().thenAccept(especialidades -> Platform.runLater(() -> onListo.accept(especialidades)))
                .exceptionally(e -> {
                    System.out.println(e.getMessage());
                    Platform.runLater(() -> notificarError("No se pudieron cargar las especialidades"));
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
     * Filtra la lista local sin llamar al servidor.
     * Busca en documento, nombre, apellido, ciudad y eps.
     *
     * @param texto Texto ingresado en el buscador. Si es vacío, devuelve todos.
     * @return Lista filtrada de médicos.
     */
    public List<MedicoModel> filtrar(String texto) {
        if (texto == null || texto.isBlank()) return listaMedicos;
        String textoBuscador = texto.toLowerCase().trim();
        return listaMedicos.stream()
                .filter(medico ->
                        contiene(medico.getDocumento(), textoBuscador) ||
                                contiene(medico.getNombre(),    textoBuscador) ||
                                contiene(medico.getApellido(),  textoBuscador) ||
                                contiene(medico.getNombreEspecialidad(),    textoBuscador) ||
                                contiene(medico.getNombreCiudad(),       textoBuscador) ||
                                contiene(medico.getNombre() + " " + medico.getApellido(), textoBuscador)
                        )
                .toList();
    }

    /**
     * Crea un nuevo medico y recarga la lista al terminar.
     *
     * @param body    Datos del medico a crear.
     * @param nombre  Nombre del medico (solo para el mensaje de éxito).
     */
    public void crear(MedicoService.MedicoCreateBody body, String nombre) {
        System.out.println(body);
        medicoService.crear(body).thenAccept(u -> Platform.runLater(() -> {
            if(onExito != null) onExito.accept("Medico '" + nombre + "'creado correctamente");
            cargarMedicos();
        })).exceptionally(e -> {
            System.out.println(e.getMessage());
            Platform.runLater(() -> notificarError("Error al crear el medico.\n" +
                    "Verifica que el documento no esté duplicado y que la ciudad y especialidad sean válidas."));
            return null;
        });
    }

    /**
     * Actualiza los datos de un medico existente y recarga la lista.
     *
     * @param documento Documento del medico a actualizar.
     * @param body      Nuevos datos.
     * @param nombre    Nombre del medico (para el mensaje de éxito).
     */
    public void actualizar(String documento, MedicoService.MedicoUpdateBody body, String nombre) {
        System.out.println(body);
        medicoService.actualizar(documento, body)
                .thenAccept(u -> Platform.runLater(() -> {
                    if (onExito != null) onExito.accept("Medico '" + nombre + "' actualizado correctamente");
                    cargarMedicos();
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError("Error al actualizar el medico."));
                    return null;
                });
    }

    /**
     * Cambia el estado del medico (habilitar/inhabilitar) y recarga la lista.
     *
     * @param documento Documento del medico.
     * @param nombreCompleto Nombre para el mensaje de éxito.
     */
    public void toggleEstado(String documento, String nombreCompleto) {
        medicoService.toggleEstado(documento)
                .thenAccept(u -> Platform.runLater(() -> {
                    if (onExito != null) onExito.accept("Estado de '" + nombreCompleto + "' actualizado");
                    cargarMedicos();
                }))
                .exceptionally(e -> {
                    System.out.println(e.getMessage());
                    Platform.runLater(() -> notificarError("Error al cambiar el estado del medico."));
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
     * @param campoBusqueda campo donde se realizará la búsqueda
     * @param texto texto a buscar dentro del campo
     * @return {@code true} si el campo contiene el texto;
     *         {@code false} en caso contrario o si el campo es {@code null}
     */
    private boolean contiene(String campoBusqueda, String texto) {
        return campoBusqueda != null && campoBusqueda.toLowerCase().contains(texto);
    }
}
