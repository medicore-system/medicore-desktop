package views.usuario;

import controllers.UsuarioController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.UsuarioModel;
import models.CiudadModel;
import models.EpsModel;
import services.UsuarioService;
import views.common.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Vista principal para la gestión de pacientes.
 *
 * <p>
 * Esta clase se encarga exclusivamente de:
 * </p>
 *
 * <ul>
 *     <li>Construir la interfaz gráfica en JavaFX.</li>
 *     <li>Escuchar eventos generados por el usuario.</li>
 *     <li>Delegar la lógica al {@code UsuarioController}.</li>
 *     <li>Actualizar la UI mediante callbacks.</li>
 * </ul>
 *
 * <h2>Responsabilidades NO permitidas</h2>
 *
 * <ul>
 *     <li>Consumir servicios HTTP directamente.</li>
 *     <li>Crear o manejar {@code Task}.</li>
 *     <li>Gestionar errores de red.</li>
 * </ul>
 */
public class UsuarioView extends VBox {

    /**
     * Controlador encargado de la lógica de negocio.
     */
    private final UsuarioController controller = new UsuarioController();

    /**
     * Caché local de ciudades para reutilizar en formularios.
     */
    private List<CiudadModel> ciudadesCache = new ArrayList<>();

    /**
     * Caché local de EPS para reutilizar en formularios.
     */
    private List<EpsModel>    epsCache      = new ArrayList<>();

    /**
     * Callback utilizado para navegar al detalle de usuario.
     */
    private final Consumer<String> onVerUsuario;

    /**
     * Título principal de la vista.
     */
    private final Label           titulo      = new Label("Gestión de Pacientes");

    /**
     * Campo de búsqueda de usuarios.
     */
    private final TextField       buscador    = new TextField();

    /**
     * Etiqueta que muestra la cantidad de resultados.
     */
    private final Label           lblContador = new Label();

    /**
     * Botón para crear nuevos usuarios.
     */
    private final Button          btnNuevo    = new Button("+ Nuevo Paciente");

    /**
     * Tabla principal de usuarios.
     */
    private final TableView<UsuarioModel> tabla = new TableView<>();

    /**
     * Constructor principal de la vista.
     *
     * @param onVerUsuario callback utilizado al abrir
     *                     el detalle de un usuario
     */
    public UsuarioView(Consumer<String> onVerUsuario) {
        this.onVerUsuario = onVerUsuario;
        conectarController();
        configurarComponentes();
        configurarTabla();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
        controller.cargarUsuarios();
        controller.cargarCiudades(lista -> ciudadesCache = new ArrayList<>(lista));
        controller.cargarEps(lista -> epsCache = new ArrayList<>(lista));
    }

    /**
     * Configura los callbacks enviados por el controlador.
     */
    private void conectarController() {
        controller.setOnDatosActualizados(usuarios -> {
            tabla.setItems(FXCollections.observableArrayList(usuarios));
            actualizarContador(usuarios.size(), usuarios.size());
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

        buscador.setPromptText("🔍  Buscar usuario...");
        buscador.setId("buscador");

        lblContador.getStyleClass().add("contador-resultados");

        btnNuevo.setId("btnNuevoPaciente");
    }

    /**
     * Configura la tabla principal de usuarios.
     */
    private void configurarTabla() {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("No hay usuarios que coincidan con la búsqueda."));

        // Documento
        TableColumn<UsuarioModel, String> colDocumento = new TableColumn<>("Documento");
        colDocumento.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getDocumento()));
        colDocumento.setMaxWidth(130);
        colDocumento.setMinWidth(100);

        // Nombre completo
        TableColumn<UsuarioModel, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().GetNombreCompleto()));

        // Correo
        TableColumn<UsuarioModel, String> colCorreo = new TableColumn<>("Correo");
        colCorreo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getCorreo()));

        // Ciudad
        TableColumn<UsuarioModel, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getCiudad()));
        colCiudad.setMaxWidth(90);

        // EPS
        TableColumn<UsuarioModel, String> colEps = new TableColumn<>("EPS");
        colEps.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getEps()));
        colEps.setMaxWidth(90);

        TableColumn<UsuarioModel, String> colEstado = new TableColumn<>("Estado");
        colEstado.setMaxWidth(100);
        colEstado.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getEstadoTexto()));
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

        TableColumn<UsuarioModel, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMaxWidth(140);
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer    = btnIcono("👁",  "btn-icono", "btn-ver");
            private final Button btnEditar = btnIcono("✎",   "btn-icono", "btn-editar");
            private final Button btnToggle = btnIcono("⏻",   "btn-icono", "btn-toggle");
            private final HBox caja = new HBox(5, btnVer, btnEditar, btnToggle);
            {
                caja.setAlignment(Pos.CENTER_LEFT);

                btnVer.setOnAction(e -> {
                    UsuarioModel u = getTableView().getItems().get(getIndex());
                    onVerUsuario.accept(u.getDocumento());
                });

                btnEditar.setOnAction(e -> {
                    UsuarioModel u = getTableView().getItems().get(getIndex());
                    abrirDialogoEditar(u);
                });

                btnToggle.setOnAction(e -> {
                    UsuarioModel u = getTableView().getItems().get(getIndex());
                    controller.toggleEstado(u.getDocumento(), u.GetNombreCompleto());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : caja);
            }
        });

        tabla.getColumns().addAll(colDocumento, colNombre, colCorreo, colCiudad,
                colEps, colEstado, colAcciones);
    }

    /**
     * Construye y organiza el layout principal.
     */
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

    /**
     * Registra todos los listeners y eventos de la vista.
     */
    private void registrarEventos() {
        buscador.textProperty().addListener((obs, ant, texto) -> {
            List<UsuarioModel> filtrados = controller.filtrar(texto);
            tabla.setItems(FXCollections.observableArrayList(filtrados));
            actualizarContador(filtrados.size(), controller.filtrar("").size());
        });

        btnNuevo.setOnAction(e -> abrirDialogoCrear());
    }

    /**
     * Abre el diálogo para crear un usuario.
     */
    private void abrirDialogoCrear() {
        new UsuarioFormDialog(null, ciudadesCache, epsCache, body -> {
            UsuarioService.UsuarioCreateBody createBody = (UsuarioService.UsuarioCreateBody) body;
            controller.crear(createBody, createBody.nombre());
        }).show();
    }

    /**
     * Abre el diálogo para editar un usuario.
     *
     * @param usuario usuario a editar
     */
    private void abrirDialogoEditar(UsuarioModel usuario) {
        new UsuarioFormDialog(usuario, ciudadesCache, epsCache, body -> {
            UsuarioService.UsuarioUpdateBody updateBody = (UsuarioService.UsuarioUpdateBody) body;
            controller.actualizar(usuario.getDocumento(), updateBody, updateBody.nombre());
        }).show();
    }

    /**
     * Actualiza el contador visual de usuarios.
     *
     * @param mostrados cantidad mostrada
     * @param total cantidad total
     */
    private void actualizarContador(int mostrados, int total) {
        if (mostrados == total) {
            lblContador.setText(total + (total == 1 ? " usuario" : " usuarios"));
        } else {
            lblContador.setText(mostrados + " de " + total + " usuarios");
        }
    }

    /**
     * Crea un botón de icono con estilos personalizados.
     *
     * @param icono texto/icono del botón
     * @param clases clases CSS
     * @return botón configurado
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
        getStyleClass().add("paciente-view");
        try {
            getStylesheets().add(
                    getClass().getResource("/styles/paciente/paciente.css").toExternalForm());
        } catch (Exception ignored) {
        }
    }
}