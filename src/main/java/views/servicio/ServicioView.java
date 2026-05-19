package views.servicio;

import controllers.ServicioController;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.ServicioModel;
import services.ServicioService;
import views.common.Estado;
import views.common.PrecioFormato;
import views.common.Toast;

import java.util.List;

/**
 * Vista principal del módulo de servicios médicos.
 *
 * Esta clase se encarga exclusivamente de:
 *   - Construir la interfaz gráfica en JavaFX.
 *   - Escuchar eventos generados por el usuario.
 *   - Delegar la lógica al ServicioController.
 *   - Actualizar la UI mediante callbacks.
 *
 * Esta clase NO hace peticiones HTTP, NO crea Tasks
 * ni maneja errores de red. Todo eso lo hace el controlador.
 */
public class ServicioView extends VBox {

    private static final String STYLESHEET = "/styles/servicio/servicio.css";

    /** Controlador encargado de la lógica de negocio. */
    private final ServicioController controller = new ServicioController();

    private final Label                    titulo      = new Label("Gestión de Servicios");
    private final TextField                buscador    = new TextField();
    private final Label                    lblContador = new Label();
    private final Button                   btnNuevo    = new Button("+ Nuevo Servicio");
    private final TableView<ServicioModel> tabla       = new TableView<>();

    /**
     * Constructor de la vista. Configura todo en orden:
     * callbacks → componentes → tabla → layout → eventos → estilos → carga de datos.
     */
    public ServicioView() {
        conectarController();
        configurarComponentes();
        configurarTabla();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
        controller.cargarServicios();
        controller.cargarTipos();
    }

    /**
     * Configura los callbacks que el controlador usa para comunicarse con la vista.
     */
    private void conectarController() {
        controller.setOnDatosActualizados(servicios -> {
            tabla.setItems(FXCollections.observableArrayList(servicios));
            actualizarContador(servicios.size(), servicios.size());
        });

        controller.setOnError(mensaje ->
                new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait()
        );

        controller.setOnExito(mensaje ->
                Toast.success(this, mensaje)
        );
    }

    /**
     * Configura los componentes visuales básicos.
     */
    private void configurarComponentes() {
        titulo.getStyleClass().add("titulo");
        buscador.setPromptText("🔍  Buscar servicio...");
        buscador.setId("buscador");
        lblContador.getStyleClass().add("contador-resultados");
        btnNuevo.setId("btnNuevoServicio");
    }

    /**
     * Construye y configura la tabla con todas sus columnas.
     */
    private void configurarTabla() {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("No hay servicios que coincidan con la búsqueda."));

        // Código
        TableColumn<ServicioModel, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colCodigo.setMinWidth(90);
        colCodigo.setMaxWidth(120);

        // Nombre
        TableColumn<ServicioModel, String> colNombre = new TableColumn<>();
        colNombre.setGraphic(headerDobleLinea("Nombre del", "Servicio"));
        colNombre.setMinWidth(140);
        colNombre.setPrefWidth(170);
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));

        // Descripción
        TableColumn<ServicioModel, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setMinWidth(120);
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

        // Tipo
        TableColumn<ServicioModel, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo()));
        colTipo.setMinWidth(90);
        colTipo.setMaxWidth(130);

        // Precio
        TableColumn<ServicioModel, String> colPrecio = new TableColumn<>();
        colPrecio.setGraphic(headerDobleLinea("Precio", "(COP)"));
        colPrecio.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPrecio() != null ? PrecioFormato.formatear(c.getValue().getPrecio()) : "—"));
        colPrecio.setMinWidth(90);
        colPrecio.setMaxWidth(120);

        // Estado con badge de color
        TableColumn<ServicioModel, String> colEstado = new TableColumn<>("Estado");
        colEstado.setMinWidth(80);
        colEstado.setMaxWidth(100);
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstadoTexto()));
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.getStyleClass().add("badge-estado"); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                badge.setText(item);
                badge.getStyleClass().removeAll("badge-activo", "badge-inactivo");
                badge.getStyleClass().add(Estado.ACTIVO.equals(item) ? "badge-activo" : "badge-inactivo");
                setGraphic(badge);
                setText(null);
            }
        });

        // Acciones: Ver, Editar, Inactivar
        TableColumn<ServicioModel, ServicioModel> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMinWidth(120);
        colAcciones.setMaxWidth(120);
        colAcciones.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer      = btnIcono("👁", "btn-icono", "btn-ver");
            private final Button btnEditar   = btnIcono("✎",  "btn-icono", "btn-editar");
            private final Button btnInactivar = btnIcono("⏻", "btn-icono", "btn-toggle");
            private final HBox   caja        = new HBox(8, btnVer, btnEditar, btnInactivar);
            { caja.setAlignment(Pos.CENTER); caja.setPadding(new Insets(0, 4, 0, 4)); }

            @Override
            protected void updateItem(ServicioModel s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                btnVer.setOnAction(e      -> abrirDialogoVer(s));
                btnEditar.setOnAction(e   -> abrirDialogoEditar(s));
                btnInactivar.setOnAction(e -> controller.toggleEstado(s));
                setGraphic(caja);
            }
        });

        tabla.getColumns().addAll(colCodigo, colNombre, colDesc, colTipo, colPrecio, colEstado, colAcciones);
    }

    /**
     * Organiza los componentes en el layout principal.
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

    /**
     * Registra los eventos de los componentes interactivos.
     */
    private void registrarEventos() {
        buscador.textProperty().addListener((obs, ant, texto) -> {
            List<ServicioModel> filtrados = controller.filtrar(texto);
            tabla.setItems(FXCollections.observableArrayList(filtrados));
            actualizarContador(filtrados.size(), controller.filtrar("").size());
        });

        btnNuevo.setOnAction(e -> abrirDialogoCrear());
    }

    /** Abre el diálogo para crear un servicio nuevo. */
    private void abrirDialogoCrear() {
        Dialog<ServicioService.ServicioCreateBody> dialog =
                ServicioFormulario.dialogoCrear(controller.getTipos());
        dialog.showAndWait().ifPresent(body -> controller.crear(body, body.nombre()));
    }

    /**
     * Abre el diálogo para editar un servicio.
     *
     * @param servicio servicio a editar
     */
    private void abrirDialogoEditar(ServicioModel servicio) {
        Dialog<ServicioService.ServicioUpdateBody> dialog =
                ServicioFormulario.dialogoEditar(servicio, controller.getTipos());
        dialog.showAndWait().ifPresent(body ->
                controller.actualizar(servicio.getCodigo(), body, body.nombre()));
    }

    /**
     * Abre el diálogo de solo lectura con la información del servicio.
     *
     * @param servicio servicio a mostrar
     */
    private void abrirDialogoVer(ServicioModel servicio) {
        ServicioFormulario.dialogoVer(servicio).showAndWait();
    }

    /**
     * Actualiza el contador de servicios mostrados.
     *
     * @param mostrados cantidad visible en la tabla
     * @param total     cantidad total en memoria
     */
    private void actualizarContador(int mostrados, int total) {
        if (mostrados == total) {
            lblContador.setText(total + (total == 1 ? " servicio" : " servicios"));
        } else {
            lblContador.setText(mostrados + " de " + total + " servicios");
        }
    }

    /**
     * Crea un botón de ícono con estilos personalizados.
     *
     * @param icono  carácter del botón
     * @param clases clases CSS a aplicar
     * @return botón configurado
     */
    private Button btnIcono(String icono, String... clases) {
        Button btn = new Button(icono);
        btn.getStyleClass().addAll(clases);
        return btn;
    }

    /**
     * Crea un Label de dos líneas para usar como header de columna.
     *
     * @param linea1 primera línea del texto
     * @param linea2 segunda línea del texto
     * @return Label centrado con dos líneas
     */
    private Label headerDobleLinea(String linea1, String linea2) {
        Label lbl = new Label(linea1 + "\n" + linea2);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-text-alignment: center; -fx-alignment: center;");
        return lbl;
    }

    /**
     * Carga la hoja de estilos CSS de la vista.
     */
    private void cargarEstilos() {
        getStyleClass().add("servicio-view");
        try {
            getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());
        } catch (Exception ignored) {}
    }
}
