package views.hospital;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import models.AreaInternaModel;
import services.HospitalService;
import views.areaInterna.AreaFila;

import java.util.List;

/**
 * Vista de detalle de un hospital.
 * Muestra la información completa del hospital y sus áreas internas.
 */
public class HospitalDetalleView extends VBox {

    private final String codigoHospital;
    private final HospitalService hospitalService = new HospitalService();

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
     * Inicializa los componentes con valores vacíos y lanza las cargas del backend.
     */
    private void iniciarComponentes() {
        nombreHospital = new Label("Cargando...");
        nombreHospital.getStyleClass().add("nombre-hospital");

        subtitulo = new Label("");
        subtitulo.getStyleClass().add("subtitulo");

        btnNuevaArea = new Button("+ Nueva Área");
        btnNuevaArea.setId("btnNuevaArea");

        lblTelefono = new Label("...");
        lblEstado   = new Label("...");

        tablaAreas = crearTablaAreas();

        cargarInfoHospital();
        cargarAreas();
    }

    /**
     * Pide al backend los datos del hospital (GET /hospitals/{codigo}) y actualiza los labels.
     */
    private void cargarInfoHospital() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                var h = hospitalService.getById(codigoHospital);
                // updateMessage/updateValue se ejecutan en el hilo de JavaFX
                javafx.application.Platform.runLater(() -> {
                    nombreHospital.setText(h.nombre);
                    subtitulo.setText("Colombia > " + h.nombreCiudad + " > " + h.nombre);
                    lblTelefono.setText(h.telefono);
                    lblEstado.setText(Boolean.TRUE.equals(h.estado) ? "Activo" : "Inactivo");
                });
                return null;
            }
        };
        task.setOnFailed(e -> mostrarError("No se pudo cargar la información del hospital."));
        new Thread(task).start();
    }

    /**
     * Pide al backend las áreas del hospital (GET /hospitals/{codigo}/areas) y llena la tabla.
     */
    private void cargarAreas() {
        Task<List<AreaFila>> task = new Task<>() {
            @Override
            protected List<AreaFila> call() throws Exception {
                return hospitalService.getAreas(codigoHospital).stream()
                        .map(a -> new AreaFila(
                                a.codigo,
                                a.nombre,
                                a.nombreAreaInterna,  // tipo genérico (Consultorio, Quirófano…)
                                "-",                  // capacidad: el backend aún no la devuelve
                                "-",                  // disponibilidad: el backend aún no la devuelve
                                "Activo",
                                a.codigoAreaInterna))
                        .toList();
            }
        };
        task.setOnSucceeded(e ->
                tablaAreas.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e ->
                mostrarError("No se pudieron cargar las áreas del hospital."));
        new Thread(task).start();
    }

    /**
     * Crea y configura la tabla de áreas internas del hospital.
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
                    setStyle("Disponible".equals(item)
                            ? "-fx-text-fill: #16a34a; -fx-font-weight: bold;"
                            : "-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<AreaFila, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<AreaFila, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar  = new Button("Editar");
            private final HBox contenedor   = new HBox(5, btnEditar);

            {
                btnEditar.setOnAction(e -> {
                    AreaFila fila = getTableView().getItems().get(getIndex());
                    abrirDialogoEditarArea(fila);
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
     * Abre un diálogo para editar un área y envía PUT /hospitals/{id}/areas/{areaId}.
     */
    private void abrirDialogoEditarArea(AreaFila fila) {
        Dialog<HospitalService.AreaUpdateBody> dialog = new Dialog<>();
        dialog.setTitle("Editar Área");
        dialog.setHeaderText(fila.getNombre());

        TextField txtNombre      = new TextField(fila.getNombre());
        TextField txtDescripcion = new TextField();
        TextField txtCodAreaInterna = new TextField(fila.getCodigoAreaInterna());

        VBox formulario = new VBox(8,
                new Label("Nombre:"),           txtNombre,
                new Label("Descripción:"),      txtDescripcion,
                new Label("Cód. Área Interna:"), txtCodAreaInterna);
        formulario.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(formulario);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new HospitalService.AreaUpdateBody(
                        txtNombre.getText(),
                        txtDescripcion.getText(),
                        txtCodAreaInterna.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(body -> {
            Task<AreaInternaModel> task = new Task<>() {
                @Override
                protected AreaInternaModel call() throws Exception {
                    return hospitalService.updateArea(codigoHospital, fila.getCodigo(), body);
                }
            };
            task.setOnSucceeded(e -> cargarAreas());
            task.setOnFailed(e   -> mostrarError("Error al actualizar el área."));
            new Thread(task).start();
        });
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
        btnNuevaArea.setOnAction(e -> abrirDialogoNuevaArea());
    }

    /**
     * Abre un diálogo para crear un área y envía POST /hospitals/{id}/areas.
     */
    private void abrirDialogoNuevaArea() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nueva Área");
        dialog.setHeaderText("Agregar área al hospital " + codigoHospital);

        TextField txtCodigo         = new TextField();
        TextField txtNombre         = new TextField();
        TextField txtDescripcion    = new TextField();
        TextField txtCodAreaInterna = new TextField();

        VBox formulario = new VBox(8,
                new Label("Código:"),           txtCodigo,
                new Label("Nombre:"),           txtNombre,
                new Label("Descripción:"),      txtDescripcion,
                new Label("Cód. Área Interna:"), txtCodAreaInterna);
        formulario.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(formulario);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> null); // solo necesitamos el evento OK

        // Reemplazamos el botón OK para capturar los valores antes de cerrar
        dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(
                javafx.event.ActionEvent.ACTION, event -> {
                    Task<Void> task = crearAreaTask(
                            txtCodigo.getText(),
                            txtNombre.getText(),
                            txtDescripcion.getText(),
                            txtCodAreaInterna.getText());
                    new Thread(task).start();
                });

        dialog.showAndWait();
    }

    private Task<Void> crearAreaTask(String codigo, String nombre,
                                     String descripcion, String codAreaInterna) {
        // Construimos el JSON manualmente como record para reutilizar Gson en el servicio
        record AreaRequest(String codigo, String nombre,
                           String descripcion, String codigoAreaInterna) {}

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // POST /hospitals/{id}/areas usando el HttpClient directamente
                // porque HospitalService aún no tiene createArea — lo añadimos aquí
                var gson = new com.google.gson.Gson();
                var body = gson.toJson(new AreaRequest(codigo, nombre, descripcion, codAreaInterna));
                var http = java.net.http.HttpClient.newHttpClient();
                var req  = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:8080/hospitals/"
                                + codigoHospital + "/areas"))
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                        .build();
                var res = http.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() >= 400) {
                    throw new Exception("Error " + res.statusCode() + ": " + res.body());
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> cargarAreas());
        task.setOnFailed(e   -> mostrarError("Error al crear el área."));
        return task;
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

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait();
    }
}
