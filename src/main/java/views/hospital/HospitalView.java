package views.hospital;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.HospitalModel;
import services.HospitalService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class HospitalView extends VBox {

    private final Consumer<String> onVerHospital;
    private final HospitalService hospitalService = new HospitalService();

    private List<HospitalFila> todosLosHospitales = new ArrayList<>();

    private Label titulo;
    private TextField buscador;
    private Button btnNuevoHospital;
    private TableView<HospitalFila> tabla;

    public HospitalView(Consumer<String> onVerHospital) {
        this.onVerHospital = onVerHospital;
        iniciarComponentes();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
    }

    private void iniciarComponentes() {
        titulo = new Label("Gestión de Hospitales");
        titulo.getStyleClass().add("titulo");

        buscador = new TextField();
        buscador.setPromptText("🔍  Buscar hospital...");
        buscador.setId("buscador");

        btnNuevoHospital = new Button("+ Nuevo Hospital");
        btnNuevoHospital.setId("btnNuevoHospital");

        tabla = crearTabla();
        cargarHospitales();
    }

    private void cargarHospitales() {
        Task<List<HospitalFila>> task = new Task<>() {
            @Override
            protected List<HospitalFila> call() throws Exception {
                return hospitalService.getAll().stream()
                        .map(h -> new HospitalFila(
                                h.codigo,
                                h.nombre,
                                h.direccion != null ? h.direccion : "",
                                h.nombreCiudad,
                                h.telefono,
                                Boolean.TRUE.equals(h.estado) ? "Activo" : "Inactivo"))
                        .toList();
            }
        };
        task.setOnSucceeded(e -> {
            todosLosHospitales = new ArrayList<>(task.getValue());
            tabla.setItems(FXCollections.observableArrayList(todosLosHospitales));
        });
        task.setOnFailed(e ->
                mostrarError("No se pudieron cargar los hospitales.\n" +
                             "Verifica que el servidor esté corriendo en localhost:8080."));
        new Thread(task).start();
    }

    private TableView<HospitalFila> crearTabla() {
        TableView<HospitalFila> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<HospitalFila, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(c -> c.getValue().codigoProperty());
        colCodigo.setMaxWidth(90);
        colCodigo.setMinWidth(80);
        TableColumn<HospitalFila, Void> colNombre = new TableColumn<>("Hospital");
        colNombre.setPrefWidth(220);
        colNombre.setCellFactory(col -> new TableCell<>() {
            private final Label lblNombre = new Label();
            private final Label lblDir    = new Label();
            private final VBox  contenido = new VBox(2, lblNombre, lblDir);
            {
                lblNombre.getStyleClass().add("hospital-nombre-celda");
                lblDir.getStyleClass().add("hospital-dir-celda");
                contenido.setPadding(new Insets(4, 0, 4, 0));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    HospitalFila f = getTableView().getItems().get(getIndex());
                    lblNombre.setText(f.getNombre());
                    lblDir.setText(f.getDireccion());
                    setGraphic(contenido);
                }
            }
        });

        TableColumn<HospitalFila, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setCellValueFactory(c -> c.getValue().ciudadProperty());

        TableColumn<HospitalFila, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(c -> c.getValue().telefonoProperty());

        TableColumn<HospitalFila, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colEstado.setMaxWidth(100);
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                badge.getStyleClass().add("badge-estado");
            }
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

        TableColumn<HospitalFila, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMaxWidth(110);
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer    = crearBtnIcono("👁", "btn-icono", "btn-ver");
            private final Button btnEditar = crearBtnIcono("✎", "btn-icono", "btn-editar");
            private final HBox contenedor  = new HBox(5, btnVer, btnEditar);
            {
                contenedor.setAlignment(Pos.CENTER_LEFT);
                btnVer.setOnAction(e -> {
                    HospitalFila f = getTableView().getItems().get(getIndex());
                    onVerHospital.accept(f.getCodigo());
                });
                btnEditar.setOnAction(e -> {
                    HospitalFila f = getTableView().getItems().get(getIndex());
                    abrirDialogoEditar(f);
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

    private Button crearBtnIcono(String icono, String... clases) {
        Button btn = new Button(icono);
        btn.getStyleClass().addAll(clases);
        return btn;
    }

    private void abrirDialogoEditar(HospitalFila fila) {
        Dialog<HospitalService.HospitalUpdateBody> dialog = new Dialog<>();
        dialog.setTitle("Editar Hospital");
        dialog.setHeaderText(fila.getNombre());

        TextField txtNombre    = new TextField(fila.getNombre());
        TextField txtTelefono  = new TextField(fila.getTelefono());
        TextField txtDireccion = new TextField(fila.getDireccion());
        TextField txtCiudad    = new TextField();

        VBox formulario = new VBox(8,
                new Label("Nombre:"),       txtNombre,
                new Label("Teléfono:"),     txtTelefono,
                new Label("Dirección:"),    txtDireccion,
                new Label("Cód. Ciudad:"),  txtCiudad);
        formulario.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(formulario);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> btn == ButtonType.OK
                ? new HospitalService.HospitalUpdateBody(
                        txtNombre.getText(), txtDireccion.getText(),
                        txtTelefono.getText(), txtCiudad.getText(),
                        "Activo".equals(fila.getEstado()))
                : null);

        dialog.showAndWait().ifPresent(body -> {
            Task<HospitalModel> task = new Task<>() {
                @Override
                protected HospitalModel call() throws Exception {
                    return hospitalService.update(fila.getCodigo(), body);
                }
            };
            task.setOnSucceeded(e -> cargarHospitales());
            task.setOnFailed(e -> mostrarError("Error al actualizar el hospital."));
            new Thread(task).start();
        });
    }

    /**
     * Organiza los componentes dentro del layout con título y botón en la barra superior.
     */
    private void configurarLayout() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox barraTitulo = new HBox(titulo, spacer, btnNuevoHospital);
        barraTitulo.setAlignment(Pos.CENTER_LEFT);

        setSpacing(15);
        setPadding(new Insets(24));
        getChildren().addAll(barraTitulo, buscador, tabla);
    }

    /**
     * Registra los eventos de la vista: buscador con orden por relevancia
     * y botón de nuevo hospital.
     */
    private void registrarEventos() {
        buscador.textProperty().addListener((obs, anterior, texto) -> {
            if (texto == null || texto.isBlank()) {
                tabla.setItems(FXCollections.observableArrayList(todosLosHospitales));
                return;
            }
            String t = texto.toLowerCase().trim();
            List<HospitalFila> ordenados = todosLosHospitales.stream()
                    .sorted(Comparator.comparingInt((HospitalFila h) -> puntuacion(h, t)).reversed())
                    .toList();
            tabla.setItems(FXCollections.observableArrayList(ordenados));
        });

        btnNuevoHospital.setOnAction(e -> abrirDialogoCrear());
    }

    /**
     * Abre un diálogo para registrar un nuevo hospital y envía POST /hospitals.
     */
    private void abrirDialogoCrear() {
        Dialog<HospitalService.HospitalCreateBody> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Hospital");

        TextField txtCodigo    = new TextField();
        txtCodigo.setPromptText("Ej: HOS-004");
        TextField txtNombre    = new TextField();
        txtNombre.setPromptText("Nombre del hospital");
        TextField txtDireccion = new TextField();
        txtDireccion.setPromptText("Dirección");
        TextField txtTelefono  = new TextField();
        txtTelefono.setPromptText("Ej: (601) 000-0000");
        TextField txtCiudad    = new TextField();
        txtCiudad.setPromptText("Código de ciudad");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.add(new Label("Código:"),     0, 0); grid.add(txtCodigo,    1, 0);
        grid.add(new Label("Nombre:"),     0, 1); grid.add(txtNombre,    1, 1);
        grid.add(new Label("Dirección:"),  0, 2); grid.add(txtDireccion, 1, 2);
        grid.add(new Label("Teléfono:"),   0, 3); grid.add(txtTelefono,  1, 3);
        grid.add(new Label("Cód. Ciudad:"),0, 4); grid.add(txtCiudad,    1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Guardar");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        dialog.setResultConverter(btn -> btn == ButtonType.OK
                ? new HospitalService.HospitalCreateBody(
                        txtCodigo.getText(), txtNombre.getText(), txtDireccion.getText(),
                        txtTelefono.getText(), txtCiudad.getText(), true)
                : null);

        dialog.showAndWait().ifPresent(body -> {
            Task<HospitalModel> task = new Task<>() {
                @Override
                protected HospitalModel call() throws Exception {
                    return hospitalService.createHospital(body);
                }
            };
            task.setOnSucceeded(e -> cargarHospitales());
            task.setOnFailed(e -> mostrarError("Error al crear el hospital.\n" +
                    "Verifica que el código y ciudad sean válidos."));
            new Thread(task).start();
        });
    }

    /**
     * Calcula qué tan relevante es un hospital para el texto buscado.
     * Mayor número = más relevante = sube en la tabla.
     */
    private int puntuacion(HospitalFila h, String texto) {
        String nombre = h.getNombre().toLowerCase();
        String ciudad = h.getCiudad().toLowerCase();
        String codigo = h.getCodigo().toLowerCase();
        if (nombre.startsWith(texto))            return 4;
        if (nombre.contains(texto))              return 3;
        if (ciudad.startsWith(texto))            return 2;
        if (ciudad.contains(texto)
                || codigo.contains(texto))       return 1;
        return 0; // no coincide, pero sigue apareciendo al final
    }

    private void cargarEstilos() {
        getStyleClass().add("hospital-view");
        getStylesheets().add(
                getClass().getResource("/styles/hospital/hospital.css").toExternalForm());
    }

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait();
    }
}
