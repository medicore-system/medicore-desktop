package views.hospital;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.HospitalModel;
import services.HospitalService;

import java.util.List;
import java.util.function.Consumer;

/**
 * Vista principal para la gestión de hospitales.
 * Muestra una tabla con los hospitales registrados y permite
 * realizar acciones de consulta y edición.
 */
public class HospitalView extends VBox {

    private final Consumer<String> onVerHospital;
    private final HospitalService hospitalService = new HospitalService();

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
        cargarHospitales();
    }

    /**
     * Pide al backend la lista de hospitales (GET /hospitals) en un hilo
     * secundario y actualiza la tabla cuando llega la respuesta.
     * Usar Task evita que la UI se congele durante la petición HTTP.
     */
    private void cargarHospitales() {
        Task<List<HospitalFila>> task = new Task<>() {
            @Override
            protected List<HospitalFila> call() throws Exception {
                return hospitalService.getAll().stream()
                        .map(h -> new HospitalFila(
                                h.codigo,
                                h.nombre,
                                h.nombreCiudad,
                                h.telefono,
                                Boolean.TRUE.equals(h.estado) ? "Activo" : "Inactivo"))
                        .toList();
            }
        };

        // setOnSucceeded se ejecuta en el hilo de JavaFX — seguro para tocar la UI
        task.setOnSucceeded(e ->
                tabla.setItems(FXCollections.observableArrayList(task.getValue())));

        task.setOnFailed(e ->
                mostrarError("No se pudieron cargar los hospitales.\n" +
                             "Verifica que el servidor esté corriendo en localhost:8080."));

        new Thread(task).start();
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
                    setStyle("Activo".equals(item)
                            ? "-fx-text-fill: #16a34a; -fx-font-weight: bold;"
                            : "-fx-text-fill: #dc2626; -fx-font-weight: bold;");
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
                    onVerHospital.accept(fila.getCodigo());
                });

                btnEditar.setOnAction(e -> {
                    HospitalFila fila = getTableView().getItems().get(getIndex());
                    abrirDialogoEditar(fila);
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
     * Abre un diálogo para editar el hospital y envía PUT /hospitals/{codigo}.
     */
    private void abrirDialogoEditar(HospitalFila fila) {
        Dialog<HospitalService.HospitalUpdateBody> dialog = new Dialog<>();
        dialog.setTitle("Editar Hospital");
        dialog.setHeaderText(fila.getNombre());

        TextField txtNombre    = new TextField(fila.getNombre());
        TextField txtTelefono  = new TextField(fila.getTelefono());
        // codigoCiudad y dirección no están en HospitalFila, así que los pedimos al usuario
        TextField txtDireccion = new TextField();
        TextField txtCiudad    = new TextField();

        VBox formulario = new VBox(8,
                new Label("Nombre:"),    txtNombre,
                new Label("Teléfono:"),  txtTelefono,
                new Label("Dirección:"), txtDireccion,
                new Label("Cód. Ciudad:"), txtCiudad);
        formulario.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(formulario);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new HospitalService.HospitalUpdateBody(
                        txtNombre.getText(),
                        txtDireccion.getText(),
                        txtTelefono.getText(),
                        txtCiudad.getText(),
                        "Activo".equals(fila.getEstado()));
            }
            return null;
        });

        dialog.showAndWait().ifPresent(body -> {
            Task<HospitalModel> task = new Task<>() {
                @Override
                protected HospitalModel call() throws Exception {
                    return hospitalService.update(fila.getCodigo(), body);
                }
            };
            task.setOnSucceeded(e -> cargarHospitales());
            task.setOnFailed(e  -> mostrarError("Error al actualizar el hospital."));
            new Thread(task).start();
        });
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
            String texto = buscador.getText().toLowerCase();
            // Filtra la tabla localmente sin nueva petición al servidor
            tabla.setItems(tabla.getItems().filtered(
                    h -> h.getNombre().toLowerCase().contains(texto)));
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

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait();
    }
}
