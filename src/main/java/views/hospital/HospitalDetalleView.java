package views.hospital;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.AreaInternaModel;
import services.HospitalService;
import views.areaInterna.AreaFila;

import java.util.ArrayList;
import java.util.List;

public class HospitalDetalleView extends VBox {

    private final String codigoHospital;
    private final HospitalService hospitalService = new HospitalService();

    private List<AreaFila> todasLasAreas = new ArrayList<>();

    private Label nombreHospital;
    private Label subtitulo;
    private Button btnNuevaArea;
    private Label lblTelefono;
    private Label lblEstado;
    private Label lblTituloAreas;
    private ComboBox<String> cmbTipo;
    private VBox cardEstado;
    private TableView<AreaFila> tablaAreas;

    public HospitalDetalleView(String codigoHospital) {
        this.codigoHospital = codigoHospital;
        iniciarComponentes();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
    }

    private void iniciarComponentes() {
        nombreHospital = new Label("Cargando...");
        nombreHospital.getStyleClass().add("nombre-hospital");

        subtitulo = new Label("");
        subtitulo.getStyleClass().add("subtitulo");

        btnNuevaArea = new Button("+ Nueva Área");
        btnNuevaArea.setId("btnNuevaArea");

        lblTelefono = new Label("...");
        lblEstado   = new Label("...");

        lblTituloAreas = new Label("Áreas del Hospital");
        lblTituloAreas.getStyleClass().add("titulo-areas");

        cmbTipo = new ComboBox<>();
        cmbTipo.setPromptText("Tipo: Todos");
        cmbTipo.setId("cmbTipo");

        tablaAreas = crearTablaAreas();

        cargarInfoHospital();
        cargarAreas();
    }

    private void cargarInfoHospital() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                var h = hospitalService.getById(codigoHospital);
                javafx.application.Platform.runLater(() -> {
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
                });
                return null;
            }
        };
        task.setOnFailed(e -> mostrarError("No se pudo cargar la información del hospital."));
        new Thread(task).start();
    }

    private void cargarAreas() {
        Task<List<AreaFila>> task = new Task<>() {
            @Override
            protected List<AreaFila> call() throws Exception {
                return hospitalService.getAreas(codigoHospital).stream()
                        .map(a -> new AreaFila(
                                a.codigo,
                                a.nombre,
                                a.nombreAreaInterna,
                                a.descripcion != null ? a.descripcion : "",
                                "Activo",
                                a.codigoAreaInterna))
                        .toList();
            }
        };
        task.setOnSucceeded(e -> {
            todasLasAreas = new ArrayList<>(task.getValue());
            tablaAreas.setItems(FXCollections.observableArrayList(todasLasAreas));
            lblTituloAreas.setText("Áreas del Hospital (" + todasLasAreas.size() + ")");
            List<String> tipos = todasLasAreas.stream()
                    .map(AreaFila::getTipo)
                    .distinct()
                    .sorted()
                    .toList();
            cmbTipo.getItems().setAll(tipos);
        });
        task.setOnFailed(e -> mostrarError("No se pudieron cargar las áreas del hospital."));
        new Thread(task).start();
    }

    /**
     * Crea y configura la tabla de áreas con columnas: Área, Tipo, Descripción, Estado, Acción.
     *
     * @return TableView configurado y listo para recibir datos.
     */
    private TableView<AreaFila> crearTablaAreas() {
        TableView<AreaFila> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<AreaFila, String> colNombre = new TableColumn<>("Área");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());

        TableColumn<AreaFila, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> c.getValue().tipoProperty());

        TableColumn<AreaFila, String> colDescripcion = new TableColumn<>("Descripción");
        colDescripcion.setCellValueFactory(c -> c.getValue().descripcionProperty());

        TableColumn<AreaFila, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colEstado.setMaxWidth(100);
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.getStyleClass().add("badge-estado"); }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    badge.setText(item);
                    badge.getStyleClass().removeAll("badge-activo", "badge-inactivo");
                    badge.getStyleClass().add("Activo".equals(item) ? "badge-activo" : "badge-inactivo");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<AreaFila, Void> colAcciones = new TableColumn<>("Acción");
        colAcciones.setMaxWidth(80);
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = crearBtnIcono("✎", "btn-icono", "btn-editar");
            private final HBox contenedor  = new HBox(5, btnEditar);
            {
                contenedor.setAlignment(Pos.CENTER_LEFT);
                btnEditar.setOnAction(e -> {
                    AreaFila f = getTableView().getItems().get(getIndex());
                    abrirDialogoEditarArea(f);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });

        tv.getColumns().addAll(colNombre, colTipo, colDescripcion, colEstado, colAcciones);
        return tv;
    }

    private Button crearBtnIcono(String icono, String... clases) {
        Button btn = new Button(icono);
        btn.getStyleClass().addAll(clases);
        return btn;
    }

    /**
     * Abre un diálogo para editar el nombre, descripción y tipo de un área
     * y envía la petición PUT /hospitals/{id}/areas/{areaId} al backend.
     *
     * @param fila fila seleccionada en la tabla que representa el área a editar
     */
    private void abrirDialogoEditarArea(AreaFila fila) {
        Dialog<HospitalService.AreaUpdateBody> dialog = new Dialog<>();
        dialog.setTitle("Editar Área");

        TextField txtNombre      = new TextField(fila.getNombre());
        txtNombre.setPromptText("Nombre del área");
        TextField txtDescripcion = new TextField(fila.getDescripcion());
        txtDescripcion.setPromptText("Descripción (opcional)");
        TextField txtCodAreaInterna = new TextField(fila.getCodigoAreaInterna());
        txtCodAreaInterna.setPromptText("Código de tipo de área");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.add(new Label("Nombre"),        0, 0); grid.add(txtNombre,         1, 0);
        grid.add(new Label("Descripción"),   0, 1); grid.add(txtDescripcion,    1, 1);
        grid.add(new Label("Tipo (código)"), 0, 2); grid.add(txtCodAreaInterna, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Guardar Área");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        dialog.setResultConverter(btn -> btn == ButtonType.OK
                ? new HospitalService.AreaUpdateBody(
                        txtNombre.getText(), txtDescripcion.getText(),
                        txtCodAreaInterna.getText())
                : null);

        dialog.showAndWait().ifPresent(body -> {
            Task<AreaInternaModel> task = new Task<>() {
                @Override
                protected AreaInternaModel call() throws Exception {
                    return hospitalService.updateArea(codigoHospital, fila.getCodigo(), body);
                }
            };
            task.setOnSucceeded(e -> cargarAreas());
            task.setOnFailed(e -> mostrarError("Error al actualizar el área."));
            new Thread(task).start();
        });
    }

    private void abrirDialogoNuevaArea() {
        Dialog<HospitalService.AreaCreateBody> dialog = new Dialog<>();
        dialog.setTitle("Nueva Área");

        TextField txtCodigo      = new TextField();
        txtCodigo.setPromptText("Ej: AREA-004");
        TextField txtNombre      = new TextField();
        txtNombre.setPromptText("Ej: Consultorio 102");
        TextField txtDescripcion = new TextField();
        txtDescripcion.setPromptText("Descripción (opcional)");
        TextField txtCodAreaInterna = new TextField();
        txtCodAreaInterna.setPromptText("Código del tipo de área");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.add(new Label("Código"),        0, 0); grid.add(txtCodigo,         1, 0);
        grid.add(new Label("Nombre"),        0, 1); grid.add(txtNombre,         1, 1);
        grid.add(new Label("Descripción"),   0, 2); grid.add(txtDescripcion,    1, 2);
        grid.add(new Label("Tipo (código)"), 0, 3); grid.add(txtCodAreaInterna, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Guardar Área");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        dialog.setResultConverter(btn -> btn == ButtonType.OK
                ? new HospitalService.AreaCreateBody(
                        txtCodigo.getText(), txtNombre.getText(),
                        txtDescripcion.getText(), txtCodAreaInterna.getText())
                : null);

        dialog.showAndWait().ifPresent(body -> {
            Task<AreaInternaModel> task = new Task<>() {
                @Override
                protected AreaInternaModel call() throws Exception {
                    return hospitalService.createArea(codigoHospital, body);
                }
            };
            task.setOnSucceeded(e -> cargarAreas());
            task.setOnFailed(e -> mostrarError("Error al crear el área."));
            new Thread(task).start();
        });
    }

    private void configurarLayout() {
        VBox infoNombre = new VBox(3, nombreHospital, subtitulo);
        Region spacerHeader = new Region();
        HBox.setHgrow(spacerHeader, Priority.ALWAYS);
        HBox header = new HBox(infoNombre, spacerHeader, btnNuevaArea);
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
    }

    private VBox crearCard(String etiqueta, Label valor, boolean esEstado) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("card-etiqueta");
        VBox card = new VBox(5, lbl, valor);
        card.getStyleClass().add("card-info");
        if (esEstado) card.getStyleClass().add("card-estado");
        return card;
    }

    private void registrarEventos() {
        btnNuevaArea.setOnAction(e -> abrirDialogoNuevaArea());

        cmbTipo.setOnAction(e -> {
            String tipo = cmbTipo.getValue();
            if (tipo == null) {
                tablaAreas.setItems(FXCollections.observableArrayList(todasLasAreas));
            } else {
                tablaAreas.setItems(FXCollections.observableArrayList(
                        todasLasAreas.stream()
                                .filter(a -> tipo.equals(a.getTipo()))
                                .toList()));
            }
        });
    }

    private void cargarEstilos() {
        getStyleClass().add("hospital-detalle-view");
        getStylesheets().add(
                getClass().getResource("/styles/hospital/hospitalDetalle.css").toExternalForm());
    }

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait();
    }
}
