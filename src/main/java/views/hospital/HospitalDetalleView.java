package views.hospital;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import views.areaInterna.AreaFila;

/**
 * Vista de detalle de un hospital.
 * Muestra la información completa del hospital y sus áreas internas.
 */
public class HospitalDetalleView extends VBox {

    private final String codigoHospital;

    private Label nombreHospital;
    private Label subtitulo;
    private Button btnNuevaArea;
    private Label lblTelefono;
    private Label lblEstado;
    private TableView<AreaFila> tablaAreas;

    /**
     * Constructor que recibe el código del hospital a mostrar.
     *
     * @param codigoHospital Código del hospital seleccionado.
     */
    public HospitalDetalleView(String codigoHospital) {
        this.codigoHospital = codigoHospital;
        iniciarComponentes();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
    }

    /**
     * Inicializa los componentes visuales de la vista.
     */
    private void iniciarComponentes() {
        //Reemplazar con GET /hospitals/{codigoHospital} mas adelante
        nombreHospital = new Label("Hospital Central Bogotá");
        nombreHospital.getStyleClass().add("nombre-hospital");

        subtitulo = new Label("Colombia > Bogotá D.C. > Hospital Central");
        subtitulo.getStyleClass().add("subtitulo");

        btnNuevaArea = new Button("+ Nueva Área");
        btnNuevaArea.setId("btnNuevaArea");

        lblTelefono = new Label("(601) 382-0000");
        lblEstado   = new Label("Activo");

        tablaAreas = crearTablaAreas();
        tablaAreas.setItems(cargarAreasDePrueba());
    }

    /**
     * Crea y configura la tabla de áreas internas del hospital.
     *
     * @return TableView configurado con las columnas de áreas.
     */
    private TableView<AreaFila> crearTablaAreas() {
        TableView<AreaFila> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<AreaFila, String> colNombre = new TableColumn<>("Área");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<AreaFila, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        TableColumn<AreaFila, String> colCapacidad = new TableColumn<>("Capacidad");
        colCapacidad.setCellValueFactory(new PropertyValueFactory<>("capacidad"));

        TableColumn<AreaFila, String> colDisponibilidad = new TableColumn<>("Disponibilidad");
        colDisponibilidad.setCellValueFactory(new PropertyValueFactory<>("disponibilidad"));
        colDisponibilidad.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Disponible".equals(item)) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<AreaFila, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<AreaFila, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final HBox contenedor  = new HBox(5, btnEditar);

            {
                btnEditar.setOnAction(e -> {
                    AreaFila fila = getTableView().getItems().get(getIndex());
                    //Hacer PUT /areas/{codigo} mas adelante
                    System.out.println("Editar área: " + fila.getNombre());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });

        tv.getColumns().addAll(colNombre, colTipo, colCapacidad, colDisponibilidad, colEstado, colAcciones);
        return tv;
    }

    /**
     * Retorna una lista de áreas de prueba con datos estáticos.
     * Debe reemplazarse con los datos del GET /hospitals/{codigoHospital}.
     *
     * @return Lista observable de áreas de prueba.
     */
    private ObservableList<AreaFila> cargarAreasDePrueba() {
        // Reemplazar con GET /hospitals/{codigoHospital} mas adelante
        return FXCollections.observableArrayList(
                new AreaFila("Consultorio 101", "Consultorio", "4 pax", "Disponible", "Activo"),
                new AreaFila("Quirófano A",     "Quirófano",   "8 pax", "Ocupado",    "Activo")
        );
    }

    /**
     * Organiza los componentes dentro del layout.
     */
    private void configurarLayout() {
        VBox infoNombre = new VBox(3, nombreHospital, subtitulo);
        Region spacer   = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(infoNombre, spacer, btnNuevaArea);
        header.getStyleClass().add("header-detalle");

        VBox cardTelefono = crearCard("Teléfono", lblTelefono);
        VBox cardEstado   = crearCard("Estado", lblEstado);
        HBox infoCards    = new HBox(10, cardTelefono, cardEstado);

        Label tituloAreas = new Label("Áreas del Hospital");
        tituloAreas.getStyleClass().add("titulo-areas");

        setSpacing(15);
        setPadding(new Insets(20));
        getChildren().addAll(header, infoCards, tituloAreas, tablaAreas);
    }

    /**
     * Crea una tarjeta de información con etiqueta y valor.
     *
     * @param etiqueta Texto descriptivo del campo.
     * @param valor    Label con el valor a mostrar.
     * @return VBox con la tarjeta armada.
     */
    private VBox crearCard(String etiqueta, Label valor) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("card-etiqueta");
        VBox card = new VBox(3, lbl, valor);
        card.getStyleClass().add("card-info");
        return card;
    }

    /**
     * Registra los eventos de los componentes interactivos.
     */
    private void registrarEventos() {
        btnNuevaArea.setOnAction(e -> {
            // para crear nueva area, hacer mini ventana
            System.out.println("Nueva área para hospital: " + codigoHospital);
        });
    }

    /**
     * Aplica los estilos CSS a la vista.
     */
    private void cargarEstilos() {
        getStyleClass().add("hospital-detalle-view");
        getStylesheets().add(
                getClass()
                        .getResource("/styles/hospital/hospitalDetalle.css")
                        .toExternalForm()
        );
    }
}