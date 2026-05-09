package views.hospital;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

/**
 * Vista principal para la gestión de hospitales.
 * Muestra una tabla con los hospitales registrados y permite
 * realizar acciones de consulta y edición.
 */
public class HospitalView extends VBox {

    private final Consumer<String> onVerHospital;

    private Label titulo;
    private TextField buscador;
    private TableView<HospitalFila> tabla;

    /**
     * Constructor que inicializa y configura todos los componentes de la vista.
     *
     * @param onVerHospital Callback que recibe el código del hospital al pulsar "Ver".
     */
    public HospitalView(Consumer<String> onVerHospital) {
        this.onVerHospital = onVerHospital;
        iniciarComponentes();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
    }

    /**
     * Inicializa los componentes visuales: título, buscador y tabla.
     */
    private void iniciarComponentes() {
        titulo = new Label("Gestión de Hospitales");
        titulo.getStyleClass().add("titulo");

        buscador = new TextField();
        buscador.setPromptText("Buscar hospital...");
        buscador.setId("buscador");

        tabla = crearTabla();
        tabla.setItems(cargarDatosDePrueba());
    }

    /**
     * Crea y configura la tabla con sus columnas y comportamiento por celda.
     *
     * @return TableView configurado con las columnas de hospital.
     */
    private TableView<HospitalFila> crearTabla() {
        TableView<HospitalFila> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<HospitalFila, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        TableColumn<HospitalFila, String> colNombre = new TableColumn<>("Hospital");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(200);

        TableColumn<HospitalFila, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));

        TableColumn<HospitalFila, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        TableColumn<HospitalFila, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Activo".equals(item)) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<HospitalFila, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer    = new Button("Ver");
            private final Button btnEditar = new Button("Editar");
            private final HBox contenedor  = new HBox(5, btnVer, btnEditar);

            {
                btnVer.setOnAction(e -> {
                    HospitalFila fila = getTableView().getItems().get(getIndex());
                    // Navega a la vista de detalle del hospital
                    onVerHospital.accept(fila.getCodigo());
                });
                btnEditar.setOnAction(e -> {
                    HospitalFila fila = getTableView().getItems().get(getIndex());
                    // TODO: Hacer PUT /hospitals/{codigo}
                    System.out.println("Editar hospital: " + fila.getCodigo());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });

        tv.getColumns().addAll(colCodigo, colNombre, colCiudad, colTelefono, colEstado, colAcciones);
        return tv;
    }

    /**
     * Retorna una lista de hospitales de prueba con datos estáticos.
     * Debe reemplazarse con una llamada GET /hospitals al implementar HTTP.
     *
     * @return Lista observable de hospitales de prueba.
     */
    private ObservableList<HospitalFila> cargarDatosDePrueba() {
        // TODO: Reemplazar con GET /hospitals al implementar HTTP
        return FXCollections.observableArrayList(
                new HospitalFila("HOS-001", "Hospital Central Bogotá",    "Bogotá",   "(601) 382-0000", "Activo"),
                new HospitalFila("HOS-002", "Clínica del Norte Medellín", "Medellín", "(604) 444-1000", "Activo"),
                new HospitalFila("HOS-003", "Hospital Valle del Cauca",   "Cali",     "(602) 394-8888", "Inactivo")
        );
    }

    /**
     * Organiza los componentes dentro del layout con espaciado y padding.
     */
    private void configurarLayout() {
        setSpacing(15);
        setPadding(new Insets(20));
        getChildren().addAll(titulo, buscador, tabla);
    }

    /**
     * Registra los eventos de la vista, como el filtrado por el buscador.
     */
    private void registrarEventos() {
        buscador.setOnKeyReleased(e -> {
            // TODO: Filtrar tabla por nombre al implementar HTTP
            System.out.println("Buscando: " + buscador.getText());
        });
    }

    /**
     * Aplica los estilos CSS a la vista.
     */
    private void cargarEstilos() {
        getStyleClass().add("hospital-view");
        getStylesheets().add(
                getClass()
                        .getResource("/styles/hospital/hospital.css")
                        .toExternalForm()
        );
    }
}