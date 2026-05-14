package views.hospital;

import controllers.HospitalController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.HospitalModel;
import services.HospitalService;
import views.common.Toast;

import java.util.function.Supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Vista principal para la gestión de hospitales.
 *
 * <p>Esta clase se encarga exclusivamente de:</p>
 * <ul>
 *     <li>Construir la interfaz gráfica en JavaFX.</li>
 *     <li>Escuchar eventos generados por el usuario.</li>
 *     <li>Delegar la lógica al {@code HospitalController}.</li>
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
public class HospitalView extends VBox {

    /** Callback que recibe el código del hospital cuando el usuario pulsa "Ver". */
    private final Consumer<String> onVerHospital;

    /** Controlador que gestiona la lógica y las llamadas al servicio. */
    private final HospitalController controller = new HospitalController();

    /** Ciudades cargadas al inicio, reutilizadas en los formularios de creación y edición. */
    private List<CiudadHospitalModel> ciudadesCache = new ArrayList<>();

    private final Label titulo            = new Label("Gestión de Hospitales");
    private final TextField buscador      = new TextField();

    /** Muestra cuántos hospitales hay en total o cuántos coinciden con el filtro activo. */
    private final Label lblContador       = new Label("");
    private final Button btnNuevoHospital = new Button("+ Nuevo Hospital");
    private final TableView<HospitalModel> tabla = new TableView<>();

    /**
     * Construye la vista, conecta el controlador y carga los datos iniciales.
     *
     * @param onVerHospital Callback que recibe el código del hospital al pulsar "Ver".
     */
    public HospitalView(Consumer<String> onVerHospital) {
        this.onVerHospital = onVerHospital;
        conectarController();
        configurarComponentes();
        configurarTabla();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
        controller.cargarHospitales();
        controller.cargarCiudades(lista -> ciudadesCache = new ArrayList<>(lista));
    }

    /**
     * Conecta los callbacks del controlador con la UI.
     */
    private void conectarController() {
        controller.setOnDatosActualizados(hospitales -> {
            tabla.setItems(FXCollections.observableArrayList(hospitales));
            tabla.refresh();
            actualizarContador(hospitales.size(), hospitales.size());
        });
        controller.setOnError(mensaje ->
                new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait());
        controller.setOnExito(mensaje ->
                Toast.success(this, mensaje));
    }

    /**
     * Configura los componentes visuales básicos.
     */
    private void configurarComponentes() {
        titulo.getStyleClass().add("titulo");
        buscador.setPromptText("🔍  Buscar hospital...");
        buscador.setId("buscador");
        lblContador.getStyleClass().add("contador-resultados");
        btnNuevoHospital.setId("btnNuevoHospital");
    }

    /**
     * Configura la tabla principal de hospitales con todas sus columnas.
     */
    private void configurarTabla() {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("No hay hospitales que coincidan con la búsqueda."));

        TableColumn<HospitalModel, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().codigo));
        colCodigo.setMaxWidth(90);
        colCodigo.setMinWidth(80);

        TableColumn<HospitalModel, String> colNombre = new TableColumn<>("Hospital");
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre));
        colNombre.setPrefWidth(220);

        TableColumn<HospitalModel, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombreCiudad));

        TableColumn<HospitalModel, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().telefono));

        TableColumn<HospitalModel, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(
                Boolean.TRUE.equals(c.getValue().estado) ? "Activo" : "Inactivo"));
        colEstado.setMaxWidth(100);
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.getStyleClass().add("badge-estado"); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                badge.setText(item);
                badge.getStyleClass().removeAll("badge-activo", "badge-inactivo");
                badge.getStyleClass().add("Activo".equals(item) ? "badge-activo" : "badge-inactivo");
                setGraphic(badge);
                setText(null);
            }
        });

        TableColumn<HospitalModel, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMaxWidth(110);
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer    = btnIcono("👁", "btn-icono", "btn-ver");
            private final Button btnEditar = btnIcono("✎",  "btn-icono", "btn-editar");
            private final HBox contenedor  = new HBox(5, btnVer, btnEditar);
            {
                contenedor.setAlignment(Pos.CENTER_LEFT);
                btnVer.setOnAction(e ->
                        onVerHospital.accept(getTableView().getItems().get(getIndex()).codigo));
                btnEditar.setOnAction(e ->
                        abrirDialogoEditar(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });

        tabla.getColumns().addAll(colCodigo, colNombre, colCiudad, colTelefono, colEstado, colAcciones);
    }

    /**
     * Construye y organiza el layout principal.
     */
    private void configurarLayout() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox barraTitulo = new HBox(titulo, spacer, btnNuevoHospital);
        barraTitulo.setAlignment(Pos.CENTER_LEFT);

        HBox barraBusqueda = new HBox(12, buscador, lblContador);
        barraBusqueda.setAlignment(Pos.CENTER_LEFT);

        setSpacing(15);
        setPadding(new Insets(24));
        getChildren().addAll(barraTitulo, barraBusqueda, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
    }

    /**
     * Registra todos los listeners y eventos de la vista.
     */
    private void registrarEventos() {
        buscador.textProperty().addListener((obs, ant, texto) -> {
            List<HospitalModel> filtrados = controller.filtrar(texto);
            tabla.setItems(FXCollections.observableArrayList(filtrados));
            tabla.refresh();
            actualizarContador(filtrados.size(), controller.filtrar("").size());
        });
        btnNuevoHospital.setOnAction(e -> abrirDialogoCrear());
    }

    /**
     * Abre el diálogo para crear un hospital.
     */
    private void abrirDialogoCrear() {
        new HospitalFormDialog(null, ciudadesCache, body -> {
            HospitalService.HospitalCreateBody createBody = (HospitalService.HospitalCreateBody) body;
            controller.crear(createBody, createBody.nombre());
        }, controller::generarCodigo).show();
    }

    /**
     * Abre el diálogo para editar un hospital existente.
     *
     * @param hospital Hospital a editar.
     */
    private void abrirDialogoEditar(HospitalModel hospital) {
        new HospitalFormDialog(hospital, ciudadesCache, body -> {
            HospitalService.HospitalUpdateBody updateBody = (HospitalService.HospitalUpdateBody) body;
            controller.actualizar(hospital.codigo, updateBody, updateBody.nombre());
        }, (Supplier<String>) null).show();
    }

    /**
     * Actualiza el contador visual de hospitales.
     *
     * @param mostrados Cantidad mostrada tras el filtro.
     * @param total     Cantidad total cargada.
     */
    private void actualizarContador(int mostrados, int total) {
        if (mostrados == total) {
            lblContador.setText(total + (total == 1 ? " hospital" : " hospitales"));
        } else {
            lblContador.setText(mostrados + " de " + total + " hospitales");
        }
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
        getStyleClass().add("hospital-view");
        getStylesheets().add(
                getClass().getResource("/styles/hospital/hospital.css").toExternalForm());
    }
}
