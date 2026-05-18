package views.usuario.cita;

import controllers.CitaController;
import controllers.UsuarioController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.CitaModel;
import models.CiudadModel;
import models.UsuarioModel;
import views.common.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CitasUsuarioView extends VBox {
    /**
     * Controlador encargado de la lógica de negocio.
     */
    private final CitaController controller = new CitaController();

    /**
     * Callback utilizado para navegar al detalle de usuario.
     */
    private final Runnable onVolver;

    /**
     * Título principal de la vista.
     */
    private final Label         titulo      = new Label("Gestión de Pacientes / Citas");

    /**
     * Campo de búsqueda de usuarios.
     */
    private final TextField buscador    = new TextField();

    /**
     * Etiqueta que muestra la cantidad de resultados.
     */
    private final Label           lblContador = new Label();

    private final Button btnVolver         = new Button("← Volver a Pacientes");

    /**
     * Tabla principal de usuarios.
     */
    private final TableView<CitaModel> tabla = new TableView<>();

    private final String documento_paciente;

    /**
     * Constructor principal de la vista.
     *
     * @param documento documento de identificacion del paciente
     * @param onVolver callback utilizado para volver a la vista de pacinetes
     *
     */
    public CitasUsuarioView(String documento, Runnable onVolver) {
        this.documento_paciente =  documento;
        this.onVolver = onVolver;
        conectarController();
        configurarComponentes();
        configurarTabla();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
        controller.cargarCitas(documento);
    }

    /**
     * Configura los callbacks enviados por el controlador.
     */
    private void conectarController() {
        controller.setOnDatosActualizados(citas -> {
            tabla.setItems(FXCollections.observableArrayList(citas));
            actualizarContador(citas.size(), citas.size());
        });

        controller.setOnError(mensaje ->
                new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait()
        );

        controller.setOnExito(mensaje ->
                Toast.success(this, mensaje)
        );
    }

    private void configurarTabla() {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("No hay citas que coincidan con la búsqueda."));

        // Paciente
        TableColumn<CitaModel, String> colPaciente = new TableColumn<>("Paciente");
        colPaciente.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNombreUsuario()));
        colPaciente.setMaxWidth(130);
        colPaciente.setMinWidth(100);

        // Especialidad
        TableColumn<CitaModel, String> colEspecialidad = new TableColumn<>("Especialidad");
        colEspecialidad.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getTipoCita()));

        // Fecha
        TableColumn<CitaModel, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getFecha().toString()));

        // Medico
        TableColumn<CitaModel, String> colMedico = new TableColumn<>("Medico");
        colMedico.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getMedico()));
        colMedico.setMaxWidth(130);
        colMedico.setMinWidth(100);

        TableColumn<CitaModel, String> colEstado = new TableColumn<>("Estado");
        colEstado.setMaxWidth(100);
        colEstado.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getEstado()));
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.getStyleClass().add("badge-estado"); }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                badge.setText(item);
                badge.getStyleClass().removeAll("badge-aprobada", "badge-pendiente", "badge-denegada");
                badge.getStyleClass().add("APROBADA".equals(item) ? "badge-aprobada" :
                                        "PENDIENTE".equals(item) ? "badge-pendiente" :
                                        "DENEGADA".equals(item) ? "badge-denegada" :
                                        "badge-default");
                setGraphic(badge);
                setText(null);
            }
        });

        TableColumn<CitaModel, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMaxWidth(100);
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnAprobar    = btnIcono("✔",  "btn-icono", "btn-aprobar");
            private final Button btnDenegar = btnIcono("✖",   "btn-icono", "btn-cancelar");
            private final HBox caja = new HBox(5, btnAprobar, btnDenegar);
            {
                caja.setAlignment(Pos.CENTER_LEFT);

                btnAprobar.setOnAction(e -> {
                    CitaModel u = getTableView().getItems().get(getIndex());
                    controller.aprobar(u.getCodigo());
                });

                btnDenegar.setOnAction(e -> {
                    CitaModel u = getTableView().getItems().get(getIndex());
                    controller.denegar(u.getCodigo());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : caja);
            }
        });

        tabla.getColumns().addAll(colPaciente, colEspecialidad, colFecha,
                colEstado, colAcciones);
    }

    /**
     * Construye y organiza el layout principal.
     */
    private void configurarLayout() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox barraTitulo = new HBox(titulo, spacer, btnVolver);
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
            List<CitaModel> filtrados = controller.filtrar(texto);
            tabla.setItems(FXCollections.observableArrayList(filtrados));
            actualizarContador(filtrados.size(), controller.filtrar("").size());
        });

        btnVolver.setOnAction(e -> { if (onVolver != null) onVolver.run(); });
    }

    /**
     * Configura los componentes visuales básicos.
     */
    private void configurarComponentes() {
        titulo.getStyleClass().add("titulo");

        buscador.setPromptText("🔍  Buscar cita...");
        buscador.setId("buscador");

        lblContador.getStyleClass().add("contador-resultados");
    }

    private void actualizarContador(int mostrados, int total) {
        if (mostrados == total) {
            lblContador.setText(total + (total == 1 ? " cita" : " citas"));
        } else {
            lblContador.setText(mostrados + " de " + total + " citas");
        }
    }

    private Button btnIcono(String icono, String... clases) {
        Button btn = new Button(icono);
        btn.getStyleClass().addAll(clases);
        return btn;
    }

    /**
     * Carga los estilos CSS asociados a la vista.
     */
    private void cargarEstilos() {
        getStyleClass().add("cita-view");
        try {
            getStylesheets().add(
                    getClass().getResource("/styles/cita/cita.css").toExternalForm());
        } catch (Exception ignored) {
        }
    }

}
