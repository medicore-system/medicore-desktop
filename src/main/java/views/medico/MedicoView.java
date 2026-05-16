package views.medico;

import controllers.MedicoController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.CiudadModel;
import models.EspecialidadModel;
import models.MedicoModel;
import models.UsuarioModel;
import services.MedicoService;
import services.UsuarioService;
import views.common.Toast;
import views.usuario.UsuarioFormDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MedicoView extends VBox {
    /**
     * Controlador encargado de la lógica de negocio.
     */
   private final MedicoController medicoController = new MedicoController();

   /**
     * Caché local de ciudades para reutilizar en formularios.
     */
   private List<CiudadModel> ciudadesCache = new ArrayList<>();

    /**
     * Caché local de especialidades para reutilizar en formularios.
     */
   private List<EspecialidadModel> especialidadCache = new ArrayList<>();

    /**
     * Título principal de la vista.
     */
    private final Label titulo      = new Label("Gestión de Médicos");

    /**
     * Campo de búsqueda de médicos.
     */
    private final TextField buscador    = new TextField();

    /**
     * Etiqueta que muestra la cantidad de resultados.
     */
    private final Label           lblContador = new Label();

    /**
     * Botón para crear nuevos médicos.
     */
    private final Button btnNuevo    = new Button("+ Nuevo Médico");

    /**
     * Tabla principal de médicos.
     */
    private final TableView<MedicoModel> tabla = new TableView<>();

    /**
     * Constructor principal de la vista.
     *
     */
    public MedicoView() {
        conectarController();
        configurarComponentes();
        configurarTabla();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
        medicoController.cargarMedicos();
        medicoController.cargarCiudades(lista -> ciudadesCache = new ArrayList<>(lista));
        medicoController.cargarEspecialidades(lista -> especialidadCache = new ArrayList<>(lista));
    }

    /**
     * Configura los callbacks enviados por el controlador.
     */
    private void conectarController() {
        medicoController.setOnDatosActualizados(usuarios -> {
            tabla.setItems(FXCollections.observableArrayList(usuarios));
            tabla.refresh();
            actualizarContador(usuarios.size(), usuarios.size());
        });

        medicoController.setOnError(mensaje ->
                new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait()
        );

        medicoController.setOnExito(mensaje ->
                Toast.success(this, mensaje)
        );
    }

    /**
     * Configura los componentes visuales básicos.
     */
    private void configurarComponentes() {
        titulo.getStyleClass().add("titulo");

        buscador.setPromptText("🔍  Buscar médico...");
        buscador.setId("buscador");

        lblContador.getStyleClass().add("contador-resultados");

        btnNuevo.setId("btnNuevoMedico");
    }

    /**
     * Configura la tabla principal de usuarios.
     * CONSTRAINED_RESIZE_POLICY --> hace que las columnas de una TableView
     * ocupen automáticamente todo el ancho disponible de la tabla.
     */
    private void configurarTabla() {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("No hay medicos que coincidan con la búsqueda."));

        // Documento
        TableColumn<MedicoModel, String> colDocumento = new TableColumn<>("Documento");
        colDocumento.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getDocumento()));
        colDocumento.setMaxWidth(130);
        colDocumento.setMinWidth(100);

        // Nombre completo
        TableColumn<MedicoModel, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre() + " " + c.getValue().getApellido()));

        // Correo
        TableColumn<MedicoModel, String> colCorreo = new TableColumn<>("Correo");
        colCorreo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));

        // Ciudad
        TableColumn<MedicoModel, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNombreCiudad()));
        colCiudad.setMaxWidth(90);

        // EPS
        TableColumn<MedicoModel, String> colEps = new TableColumn<>("Especialidad");
        colEps.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNombreEspecialidad()));
        colEps.setMaxWidth(90);

        TableColumn<MedicoModel, String> colEstado = new TableColumn<>("Estado");
        colEstado.setMaxWidth(100);
        colEstado.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
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

        TableColumn<MedicoModel, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMaxWidth(140);
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = btnIcono("✎",   "btn-icono", "btn-editar");
            private final Button btnToggle = btnIcono("⏻",   "btn-icono", "btn-toggle");
            private final HBox caja = new HBox(5, btnEditar, btnToggle);
            {
                caja.setAlignment(Pos.CENTER_LEFT);

                btnEditar.setOnAction(e -> {
                    MedicoModel m = getTableView().getItems().get(getIndex());
                    abrirDialogoEditar(m);
                });

                btnToggle.setOnAction(e -> {
                    MedicoModel m = getTableView().getItems().get(getIndex());
                    medicoController.toggleEstado(m.getDocumento(), m.getNombre() + " " + m.getApellido());
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
            List<MedicoModel> filtrados = medicoController.filtrar(texto);
            tabla.setItems(FXCollections.observableArrayList(filtrados));
            actualizarContador(filtrados.size(), medicoController.filtrar("").size());
        });

        btnNuevo.setOnAction(e -> abrirDialogoCrear());
    }

    /**
     * Abre el diálogo para crear un médico.
     */
    private void abrirDialogoCrear() {
        new MedicoFormDialog(null, ciudadesCache, especialidadCache, body -> {
            MedicoService.MedicoCreateBody createBody = (MedicoService.MedicoCreateBody) body;
            medicoController.crear(createBody, createBody.nombre());
        }).show();
    }

    /**
     * Abre el diálogo para editar un médico.
     *
     * @param medicoModel usuario a editar
     */
    private void abrirDialogoEditar(MedicoModel medicoModel) {
        new MedicoFormDialog(medicoModel, ciudadesCache, especialidadCache, body -> {
            MedicoService.MedicoUpdateBody updateBody = (MedicoService.MedicoUpdateBody) body;
            medicoController.actualizar(medicoModel.getDocumento(), updateBody, updateBody.nombre());
        }).show();
    }


    /**
     * Actualiza el contador visual de médico.
     *
     * @param mostrados cantidad mostrada
     * @param total cantidad total
     */
    private void actualizarContador(int mostrados, int total) {
        if (mostrados == total) {
            lblContador.setText(total + (total == 1 ? " médico" : " médicos"));
        } else {
            lblContador.setText(mostrados + " de " + total + " médicos");
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
