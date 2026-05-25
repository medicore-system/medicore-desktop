package controllers;

import javafx.application.Platform;
import models.HistorialModel;
import models.MedicoModel;
import models.UsuarioModel;
import services.HistorialService;
import services.MedicoService;
import services.UsuarioService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controlador de historiales clínicos para la aplicación de escritorio.
 *
 * <p>Centraliza la lógica de negocio del módulo, carga los datos necesarios
 * (historiales, pacientes, médicos) y notifica a la vista mediante callbacks.</p>
 */
public class HistorialDesktopController {

    private final HistorialService historialService = HistorialService.getInstance();
    private final UsuarioService usuarioService     = UsuarioService.getInstance();
    private final MedicoService medicoService       = MedicoService.getInstance();

    /** Lista completa de historiales cargada desde el servidor. */
    private List<HistorialModel> todos = new ArrayList<>();

    /** Lista de pacientes (usuarios con rol PACIENTE). */
    private List<UsuarioModel> pacientes = new ArrayList<>();

    /** Lista de médicos activos disponibles. */
    private List<MedicoModel> medicos = new ArrayList<>();

    // ── Callbacks ──────────────────────────────────────────────────────────────

    private Consumer<List<HistorialModel>> onDatosActualizados;
    private Consumer<List<UsuarioModel>>   onPacientesCargados;
    private Consumer<List<MedicoModel>>    onMedicosCargados;
    private Consumer<String>               onError;
    private Consumer<String>               onExito;

    public void setOnDatosActualizados(Consumer<List<HistorialModel>> cb) { this.onDatosActualizados = cb; }
    public void setOnPacientesCargados(Consumer<List<UsuarioModel>>   cb) { this.onPacientesCargados = cb; }
    public void setOnMedicosCargados(Consumer<List<MedicoModel>>      cb) { this.onMedicosCargados   = cb; }
    public void setOnError(Consumer<String>                           cb) { this.onError             = cb; }
    public void setOnExito(Consumer<String>                           cb) { this.onExito             = cb; }

    /** Lista de pacientes ya cargada en memoria. */
    public List<UsuarioModel> getPacientes() { return pacientes; }

    /** Lista de médicos ya cargada en memoria. */
    public List<MedicoModel> getMedicos() { return medicos; }

    // ── Carga de datos ─────────────────────────────────────────────────────────

    /**
     * Carga todos los historiales clínicos desde el servidor.
     */
    public void cargarHistoriales() {
        historialService.getAllHistoriales()
                .thenAccept(lista -> Platform.runLater(() -> {
                    todos = new ArrayList<>(lista);
                    if (onDatosActualizados != null) onDatosActualizados.accept(todos);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError(
                            "No se pueden cargar los historiales.\n" +
                            "Verifica que el servidor esté corriendo en localhost:8080"));
                    return null;
                });
    }

    /**
     * Carga la lista de usuarios para el selector de pacientes.
     */
    public void cargarPacientes() {
        usuarioService.getAllUsers()
                .thenAccept(lista -> Platform.runLater(() -> {
                    pacientes = new ArrayList<>(lista);
                    if (onPacientesCargados != null) onPacientesCargados.accept(pacientes);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError("No se pudo cargar la lista de pacientes."));
                    return null;
                });
    }

    /**
     * Carga la lista de médicos activos para el selector de médicos.
     */
    public void cargarMedicos() {
        medicoService.getAllMedicos()
                .thenAccept(lista -> Platform.runLater(() -> {
                    medicos = new ArrayList<>(lista);
                    if (onMedicosCargados != null) onMedicosCargados.accept(medicos);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError("No se pudo cargar la lista de médicos."));
                    return null;
                });
    }

    // ── Operaciones ────────────────────────────────────────────────────────────

    /**
     * Crea un nuevo historial clínico y recarga la lista al terminar.
     *
     * @param body datos del historial a crear.
     */
    public void crear(HistorialService.HistorialCreateBody body) {
        historialService.crear(body)
                .thenAccept(h -> Platform.runLater(() -> {
                    if (onExito != null) onExito.accept("Historial '" + h.getCodigo() + "' creado correctamente");
                    cargarHistoriales();
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> notificarError(
                            "Error al crear el historial.\n" +
                            "Verifica que el código no esté duplicado y que paciente y médico existan."));
                    return null;
                });
    }

    /**
     * Filtra la lista local por código, paciente o médico sin llamar al servidor.
     *
     * @param texto texto de búsqueda.
     * @return lista filtrada.
     */
    public List<HistorialModel> filtrar(String texto) {
        if (texto == null || texto.isBlank()) return todos;
        String t = texto.toLowerCase().trim();
        return todos.stream()
                .filter(h ->
                        contiene(h.getCodigo(),         t) ||
                        contiene(h.getNombrePaciente(), t) ||
                        contiene(h.getNombreMedico(),   t) ||
                        contiene(h.getTipo(),           t))
                .toList();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void notificarError(String msg) {
        if (onError != null) onError.accept(msg);
    }

    private boolean contiene(String campo, String texto) {
        return campo != null && campo.toLowerCase().contains(texto);
    }
}
