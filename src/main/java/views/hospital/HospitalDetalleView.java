package views.hospital;

import controllers.AreaInternaController;
import controllers.HospitalController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.AreaInternaModel;
import services.HospitalService;
import views.common.Toast;

import java.util.function.Supplier;

/**
 * Vista de detalle de un hospital.
 *
 * <p>Esta clase se encarga exclusivamente de:</p>
 * <ul>
 *     <li>Construir la interfaz gráfica en JavaFX.</li>
 *     <li>Escuchar eventos generados por el usuario.</li>
 *     <li>Delegar la lógica al {@code HospitalController} y {@code AreaInternaController}.</li>
 *     <li>Actualizar la UI mediante callbacks.</li>
 * </ul>
 *
 * <h2>Responsabilidades NO permitidas</h2>
 * <ul>
 *     <li>Consumir servicios HTTP directamente.</li>
 *     <li>Crear o manejar {@code Task}.</li>
 *     <li>Gestionar errores de red.</li>
 * </ul>
 */
public class HospitalDetalleView extends VBox {

    /** Código del hospital cuya información y áreas se muestran. */
    private final String codigoHospital;

    /** Controlador que gestiona las áreas internas de este hospital. */
    private final AreaInternaController areaController;

    /** Controlador usado exclusivamente para cargar el encabezado del hospital. */
    private final HospitalController hospitalController = new HospitalController();

    /** Acción ejecutada al pulsar "Volver"; puede ser {@code null}. */
    private final Runnable onVolver;

    private final Label nombreHospital     = new Label("Cargando...");

    /** Ruta de navegación tipo breadcrumb (ej. "Colombia > Bogotá > Hospital X"). */
    private final Label subtitulo          = new Label("");
    private final Button btnVolver         = new Button("← Volver a hospitales");
    private final Button btnNuevaArea      = new Button("+ Nueva Área");
    private final Label lblTelefono        = new Label("...");
    private final Label lblEstado          = new Label("...");

    /** Título de la sección de áreas; incluye el conteo entre paréntesis al cargar. */
    private final Label lblTituloAreas     = new Label("Áreas del Hospital");

    /** Combo para filtrar la tabla por tipo de área; "Todos" si no hay selección. */
    private final ComboBox<String> cmbTipo = new ComboBox<>();

    /** Tarjeta de estado; se guarda referencia para cambiar su clase CSS según activo/inactivo. */
    private VBox cardEstado;
    private final TableView<AreaInternaModel> tablaAreas = new TableView<>();

    /**
     * Construye la vista, conecta los controladores y lanza la carga de datos.
     *
     * @param codigoHospital Código único del hospital a mostrar.
     * @param onVolver       Acción que se ejecuta al pulsar el botón "Volver"; puede ser {@code null}.
     */
    public HospitalDetalleView(String codigoHospital, Runnable onVolver) {
        this.codigoHospital = codigoHospital;
        this.onVolver       = onVolver;
        this.areaController = new AreaInternaController(codigoHospital);
        conectarControllers();
        configurarComponentes();
        configurarTabla();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
        hospitalController.cargarHospitalPorId(codigoHospital, this::mostrarInfoHospital);
        areaController.cargarAreas();
    }

    /**
     * Constructor alternativo sin acción de volver (compatibilidad).
     *
     * @param codigoHospital Código único del hospital a mostrar.
     */
    public HospitalDetalleView(String codigoHospital) {
        this(codigoHospital, null);
    }

    /**
     * Conecta los callbacks de ambos controladores con la UI.
     */
    private void conectarControllers() {
        areaController.setOnDatosActualizados(areas -> {
            tablaAreas.setItems(FXCollections.observableArrayList(areas));
            lblTituloAreas.setText("Áreas del Hospital (" + areas.size() + ")");
            cmbTipo.getItems().setAll(areaController.getTipos());
        });
        areaController.setOnError(mensaje ->
                new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait());
        areaController.setOnExito(mensaje ->
                Toast.success(this, mensaje));
        hospitalController.setOnError(mensaje ->
                new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait());
    }

    /**
     * Muestra los datos del hospital en el encabezado y las tarjetas de info.
     *
     * @param h Hospital cargado por el controller.
     */
    private void mostrarInfoHospital(models.HospitalModel h) {
        nombreHospital.setText(h.nombre);
        subtitulo.setText("Colombia > " + h.nombreCiudad + " > " + h.nombre);
        lblTelefono.setText(h.telefono);
        boolean activo = Boolean.TRUE.equals(h.estado);
        lblEstado.setText(activo ? "Activo" : "Inactivo");
        lblEstado.getStyleClass().removeAll("valor-activo", "valor-inactivo");
        lblEstado.getStyleClass().add(activo ? "valor-activo" : "valor-inactivo");
        if (cardEstado != null) {
            cardEstado.getStyleClass().removeAll("card-estado-activo", "card-estado-inactivo");
            cardEstado.getStyleClass().add(activo ? "card-estado-activo" : "card-estado-inactivo");
        }
    }

    /**
     * Configura los componentes visuales básicos.
     */
    private void configurarComponentes() {
        nombreHospital.getStyleClass().add("nombre-hospital");
        subtitulo.getStyleClass().add("subtitulo");
        btnVolver.setId("btnVolver");
        btnNuevaArea.setId("btnNuevaArea");
        lblTituloAreas.getStyleClass().add("titulo-areas");
        cmbTipo.setPromptText("Tipo: Todos");
        cmbTipo.setId("cmbTipo");
    }

    /**
     * Configura la tabla de áreas internas con todas sus columnas.
     */
    private void configurarTabla() {
        tablaAreas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaAreas.setPlaceholder(new Label("Este hospital aún no tiene áreas registradas."));

        TableColumn<AreaInternaModel, String> colNombre = new TableColumn<>("Área");
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre));

        TableColumn<AreaInternaModel, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombreAreaInterna));

        TableColumn<AreaInternaModel, String> colDescripcion = new TableColumn<>("Descripción");
        colDescripcion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().descripcion));

        TableColumn<AreaInternaModel, Void> colAcciones = new TableColumn<>("Acción");
        colAcciones.setMaxWidth(80);
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = btnIcono("✎", "btn-icono", "btn-editar");
            private final HBox contenedor  = new HBox(5, btnEditar);
            {
                contenedor.setAlignment(Pos.CENTER_LEFT);
                btnEditar.setOnAction(e ->
                        abrirDialogoEditarArea(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });

        tablaAreas.getColumns().addAll(colNombre, colTipo, colDescripcion, colAcciones);
    }

    /**
     * Construye y organiza el layout principal de la vista.
     */
    private void configurarLayout() {
        VBox infoNombre = new VBox(3, nombreHospital, subtitulo);
        Region spacerHeader = new Region();
        HBox.setHgrow(spacerHeader, Priority.ALWAYS);
        HBox header = new HBox(12, btnVolver, infoNombre, spacerHeader, btnNuevaArea);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header-detalle");

        VBox cardTelefono = crearCard("Teléfono", lblTelefono, false);
        cardEstado        = crearCard("Estado",   lblEstado,   true);
        HBox.setHgrow(cardTelefono, Priority.ALWAYS);
        HBox.setHgrow(cardEstado,   Priority.ALWAYS);
        HBox infoCards = new HBox(12, cardTelefono, cardEstado);

        Region spacerTabla = new Region();
        HBox.setHgrow(spacerTabla, Priority.ALWAYS);
        HBox barraAreas = new HBox(lblTituloAreas, spacerTabla, cmbTipo);
        barraAreas.setAlignment(Pos.CENTER_LEFT);

        setSpacing(16);
        setPadding(new Insets(24));
        getChildren().addAll(header, infoCards, barraAreas, tablaAreas);
        VBox.setVgrow(tablaAreas, Priority.ALWAYS);
    }

    /**
     * Crea una tarjeta de información con etiqueta y valor.
     *
     * @param etiqueta Título descriptivo de la tarjeta (ej. "Teléfono").
     * @param valor    Label que mostrará el dato dinámico.
     * @param esEstado Si es {@code true}, añade la clase base {@code card-estado} para
     *                 permitir colorizado dinámico según el estado del hospital.
     */
    private VBox crearCard(String etiqueta, Label valor, boolean esEstado) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("card-etiqueta");
        VBox card = new VBox(5, lbl, valor);
        card.getStyleClass().add("card-info");
        if (esEstado) card.getStyleClass().add("card-estado");
        return card;
    }

    /**
     * Registra todos los listeners y eventos de la vista.
     */
    private void registrarEventos() {
        btnNuevaArea.setOnAction(e -> abrirDialogoNuevaArea());
        btnVolver.setOnAction(e -> { if (onVolver != null) onVolver.run(); });
        cmbTipo.setOnAction(e -> {
            String tipo = cmbTipo.getValue();
            tablaAreas.setItems(FXCollections.observableArrayList(
                    areaController.filtrarPorTipo(tipo)));
        });
    }

    /**
     * Abre el diálogo para crear una nueva área.
     */
    private void abrirDialogoNuevaArea() {
        new AreaFormDialog(null, body -> {
            HospitalService.AreaCreateBody createBody = (HospitalService.AreaCreateBody) body;
            areaController.crear(createBody, createBody.nombre());
        }, areaController::generarCodigo).show();
    }

    /**
     * Abre el diálogo para editar un área existente.
     *
     * @param area Área a editar.
     */
    private void abrirDialogoEditarArea(AreaInternaModel area) {
        new AreaFormDialog(area, body -> {
            HospitalService.AreaUpdateBody updateBody = (HospitalService.AreaUpdateBody) body;
            areaController.actualizar(area.codigo, updateBody, updateBody.nombre());
        }, (Supplier<String>) null).show();
    }

    /**
     * Crea un botón con un icono Unicode y las clases CSS indicadas.
     *
     * @param icono  Carácter o emoji que se muestra como texto del botón.
     * @param clases Clases CSS que se aplican al botón.
     */
    private Button btnIcono(String icono, String... clases) {
        Button btn = new Button(icono);
        btn.getStyleClass().addAll(clases);
        return btn;
    }

    /**
     * Carga los estilos CSS asociados a la vista.
     */
    private void cargarEstilos() {
        getStyleClass().add("hospital-detalle-view");
        getStylesheets().add(
                getClass().getResource("/styles/hospital/hospitalDetalle.css").toExternalForm());
    }
}
