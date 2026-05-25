package views.historial;

import controllers.HistorialDesktopController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.HistorialModel;
import views.common.Toast;

import java.util.List;

/**
 * Vista principal del módulo de historial clínico.
 *
 * <p>Muestra en una tabla todos los historiales clínicos registrados
 * en el sistema y permite registrar nuevos mediante un formulario
 * en diálogo. La búsqueda filtra en tiempo real por código, tipo,
 * nombre del paciente o nombre del médico.</p>
 *
 * <p>Esta clase no realiza peticiones HTTP directamente.
 * Toda la lógica se delega a {@link HistorialDesktopController}.</p>
 */
public class HistorialView extends VBox {

    private static final String STYLESHEET = "/styles/servicio/servicio.css";

    /** Controlador encargado de la lógica de negocio del módulo. */
    private final HistorialDesktopController controller = new HistorialDesktopController();

    private final Label                     titulo      = new Label("Historial Clínico");
    private final TextField                 buscador    = new TextField();
    private final Label                     lblContador = new Label();
    private final Button                    btnNuevo    = new Button("+ Nuevo Historial");
    private final TableView<HistorialModel> tabla       = new TableView<>();

    /**
     * Constructor de la vista.
     *
     * <p>Inicializa los callbacks, los componentes visuales, la tabla,
     * el layout, los eventos y los estilos. Luego dispara la carga
     * inicial de historiales, pacientes y médicos desde el backend.</p>
     */
    public HistorialView() {
        conectarController();
        configurarComponentes();
        configurarTabla();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
        controller.cargarHistoriales();
        controller.cargarPacientes();
        controller.cargarMedicos();
    }

    // ── Callbacks ──────────────────────────────────────────────────────────────

    /**
     * Conecta los callbacks del controlador con las actualizaciones de la vista.
     *
     * <ul>
     *   <li>{@code onDatosActualizados} — reemplaza el contenido de la tabla.</li>
     *   <li>{@code onError} — muestra un diálogo de error.</li>
     *   <li>{@code onExito} — muestra un toast de éxito.</li>
     * </ul>
     */
    private void conectarController() {
        controller.setOnDatosActualizados(lista -> {
            tabla.setItems(FXCollections.observableArrayList(lista));
            actualizarContador(lista.size(), lista.size());
        });

        controller.setOnError(msg ->
                new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait()
        );

        controller.setOnExito(msg -> Toast.success(this, msg));
    }

    // ── Componentes ────────────────────────────────────────────────────────────

    /**
     * Aplica las clases CSS y textos de ayuda a los componentes básicos de la vista.
     */
    private void configurarComponentes() {
        titulo.getStyleClass().add("titulo");
        buscador.setPromptText("🔍  Buscar por código, paciente o médico...");
        buscador.setId("buscador");
        lblContador.getStyleClass().add("contador-resultados");
        btnNuevo.setId("btnNuevoServicio");
    }

    // ── Tabla ──────────────────────────────────────────────────────────────────

    /**
     * Construye y configura la tabla con las columnas:
     * Código, Fecha, Tipo, Paciente, Médico y Descripción.
     */
    private void configurarTabla() {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("No hay historiales clínicos registrados."));

        TableColumn<HistorialModel, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setMinWidth(80);
        colCodigo.setMaxWidth(110);
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));

        TableColumn<HistorialModel, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setMinWidth(100);
        colFecha.setMaxWidth(120);
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha()));

        TableColumn<HistorialModel, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setMinWidth(90);
        colTipo.setMaxWidth(130);
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo()));

        TableColumn<HistorialModel, String> colPaciente = new TableColumn<>("Paciente");
        colPaciente.setMinWidth(130);
        colPaciente.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNombrePaciente() != null ? c.getValue().getNombrePaciente() : ""));

        TableColumn<HistorialModel, String> colMedico = new TableColumn<>("Médico");
        colMedico.setMinWidth(130);
        colMedico.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNombreMedico() != null ? c.getValue().getNombreMedico() : ""));

        TableColumn<HistorialModel, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setMinWidth(150);
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDescripcion() != null ? c.getValue().getDescripcion() : ""));
        colDesc.setCellFactory(col -> new TableCell<>() {
            private final Label lbl = new Label();
            { lbl.setWrapText(true); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); } else { lbl.setText(item); setGraphic(lbl); }
            }
        });

        tabla.getColumns().addAll(colCodigo, colFecha, colTipo, colPaciente, colMedico, colDesc);
    }

    // ── Layout ─────────────────────────────────────────────────────────────────

    /**
     * Organiza los componentes en el layout principal:
     * barra de título con botón, barra de búsqueda con contador y tabla.
     */
    private void configurarLayout() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox barraTitulo   = new HBox(titulo, spacer, btnNuevo);
        barraTitulo.setAlignment(Pos.CENTER_LEFT);

        HBox barraBusqueda = new HBox(12, buscador, lblContador);
        barraBusqueda.setAlignment(Pos.CENTER_LEFT);

        setSpacing(15);
        setPadding(new Insets(24));
        getChildren().addAll(barraTitulo, barraBusqueda, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
    }

    // ── Eventos ────────────────────────────────────────────────────────────────

    /**
     * Registra los listeners de los componentes interactivos:
     * filtrado en tiempo real del buscador y apertura del formulario de creación.
     */
    private void registrarEventos() {
        buscador.textProperty().addListener((obs, ant, texto) -> {
            List<HistorialModel> filtrados = controller.filtrar(texto);
            tabla.setItems(FXCollections.observableArrayList(filtrados));
            actualizarContador(filtrados.size(), controller.filtrar("").size());
        });

        btnNuevo.setOnAction(e -> abrirFormularioCrear());
    }

    /**
     * Abre el diálogo {@link HistorialFormDialog} para registrar un nuevo historial.
     *
     * <p>Pasa las listas de pacientes y médicos ya cargadas en memoria
     * para poblar los ComboBox del formulario sin llamadas adicionales al servidor.</p>
     */
    private void abrirFormularioCrear() {
        new HistorialFormDialog(
                controller.getPacientes(),
                controller.getMedicos(),
                body -> controller.crear(body)
        ).show();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Actualiza el texto del contador de historiales mostrados.
     *
     * @param mostrados cantidad de historiales visibles en la tabla tras el filtro
     * @param total     cantidad total de historiales cargados en memoria
     */
    private void actualizarContador(int mostrados, int total) {
        if (mostrados == total) {
            lblContador.setText(total + (total == 1 ? " historial" : " historiales"));
        } else {
            lblContador.setText(mostrados + " de " + total + " historiales");
        }
    }

    /**
     * Carga la hoja de estilos CSS compartida con el módulo de servicios.
     */
    private void cargarEstilos() {
        getStyleClass().add("servicio-view");
        try {
            getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());
        } catch (Exception ignored) {}
    }
}
