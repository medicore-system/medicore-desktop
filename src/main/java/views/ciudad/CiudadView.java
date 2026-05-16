package views.ciudad;

import controllers.CiudadController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.CiudadModel;
import services.CiudadService;
import views.common.Toast;

import java.util.List;

/**
 * Vista principal para la gestión de ciudades.
 * Delega toda la lógica al {@code CiudadController}.
 */
public class CiudadView extends VBox {

    private final CiudadController controller = new CiudadController();

    private final Label titulo       = new Label("Gestión de Ciudades");
    private final TextField buscador = new TextField();
    private final Label lblContador  = new Label();
    private final Button btnNuevo    = new Button("+ Nueva Ciudad");
    private final TableView<CiudadModel> tabla = new TableView<>();

    public CiudadView() {
        conectarController();
        configurarComponentes();
        configurarTabla();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
        controller.cargarCiudades();
    }

    private void conectarController() {
        controller.setOnDatosActualizados(ciudades -> {
            tabla.setItems(FXCollections.observableArrayList(ciudades));
            tabla.refresh();
            actualizarContador(ciudades.size(), ciudades.size());
        });
        controller.setOnError(mensaje ->
                new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait());
        controller.setOnExito(mensaje ->
                Toast.success(this, mensaje));
    }

    private void configurarComponentes() {
        titulo.getStyleClass().add("titulo");
        buscador.setPromptText("🔍  Buscar ciudad...");
        buscador.setId("buscador");
        lblContador.getStyleClass().add("contador-resultados");
        btnNuevo.setId("btnNuevaCiudad");
    }

    private void configurarTabla() {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("No hay ciudades que coincidan con la búsqueda."));

        TableColumn<CiudadModel, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getCodigo()));
        colCodigo.setMaxWidth(120);
        colCodigo.setMinWidth(90);

        TableColumn<CiudadModel, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));

        TableColumn<CiudadModel, String> colDepartamento = new TableColumn<>("Departamento");
        colDepartamento.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getDepartamento()));

        TableColumn<CiudadModel, String> colEstado = new TableColumn<>("Estado");
        colEstado.setMaxWidth(110);
        colEstado.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.getStyleClass().add("badge-estado"); }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                badge.setText("ACTIVE".equalsIgnoreCase(item) ? "Activo" : "Inactivo");
                badge.getStyleClass().removeAll("badge-activo", "badge-inactivo");
                badge.getStyleClass().add("ACTIVE".equalsIgnoreCase(item) ? "badge-activo" : "badge-inactivo");
                setGraphic(badge);
                setText(null);
            }
        });

        TableColumn<CiudadModel, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMaxWidth(100);
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = btnIcono("✎", "btn-icono", "btn-editar");
            private final HBox caja = new HBox(5, btnEditar);
            {
                caja.setAlignment(Pos.CENTER_LEFT);
                btnEditar.setOnAction(e -> abrirDialogoEditar(
                        getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : caja);
            }
        });

        tabla.getColumns().addAll(colCodigo, colNombre, colDepartamento, colEstado, colAcciones);
    }

    private void configurarLayout() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox barraTitulo = new HBox(titulo, spacer, btnNuevo);
        barraTitulo.setAlignment(Pos.CENTER_LEFT);

        HBox barraBusqueda = new HBox(12, buscador, lblContador);
        barraBusqueda.setAlignment(Pos.CENTER_LEFT);

        setSpacing(15);
        setPadding(new Insets(24));
        getChildren().addAll(barraTitulo, barraBusqueda, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
    }

    private void registrarEventos() {
        buscador.textProperty().addListener((obs, ant, texto) -> {
            List<CiudadModel> filtrados = controller.filtrar(texto);
            tabla.setItems(FXCollections.observableArrayList(filtrados));
            actualizarContador(filtrados.size(), controller.filtrar("").size());
        });
        btnNuevo.setOnAction(e -> abrirDialogoCrear());
    }

    private void abrirDialogoCrear() {
        new CiudadFormDialog(null, body -> {
            CiudadService.CiudadCreateBody createBody = (CiudadService.CiudadCreateBody) body;
            controller.crear(createBody);
        }).show();
    }

    private void abrirDialogoEditar(CiudadModel ciudad) {
        new CiudadFormDialog(ciudad, body -> {
            CiudadService.CiudadUpdateBody updateBody = (CiudadService.CiudadUpdateBody) body;
            controller.actualizar(ciudad.getCodigo(), updateBody);
        }).show();
    }

    private void actualizarContador(int mostrados, int total) {
        if (mostrados == total) {
            lblContador.setText(total + (total == 1 ? " ciudad" : " ciudades"));
        } else {
            lblContador.setText(mostrados + " de " + total + " ciudades");
        }
    }

    private Button btnIcono(String icono, String... clases) {
        Button btn = new Button(icono);
        btn.getStyleClass().addAll(clases);
        return btn;
    }

    private void cargarEstilos() {
        getStyleClass().add("ciudad-view");
        try {
            getStylesheets().add(
                    getClass().getResource("/styles/ciudad/ciudad.css").toExternalForm());
        } catch (Exception ignored) {
        }
    }
}
