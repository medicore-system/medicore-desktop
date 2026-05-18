package views.asignacion.asignacionesMedico;

import controllers.AsignacionMedicoController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.AsignacionMedicoModel;
import models.MedicoModel;
import services.AsignacionMedicoService;
import views.common.Toast;

public class AsignacionesMedicoView extends VBox {

    private final AsignacionMedicoController controller = new AsignacionMedicoController();
    private final Runnable onVolver;
    private final MedicoModel medico;

    private final Label titulo      = new Label();
    private final Label lblContador = new Label();
    private final Button btnVolver  = new Button("← Volver a Médicos");
    private final Button btnNueva   = new Button("+ Nueva Asignación");
    private final TableView<AsignacionMedicoModel> tabla = new TableView<>();

    public AsignacionesMedicoView(MedicoModel medico, Runnable onVolver) {
        this.medico   = medico;
        this.onVolver = onVolver;
        conectarController();
        configurarComponentes();
        configurarTabla();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
        controller.cargarPorMedico(medico.getDocumento());
    }

    private void conectarController() {
        controller.setOnDatosActualizados(lista -> {
            tabla.setItems(FXCollections.observableArrayList(lista));
            tabla.refresh();
            actualizarContador(lista.size());
        });

        controller.setOnError(mensaje ->
                new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait()
        );

        controller.setOnExito(mensaje ->
                Toast.success(this, mensaje)
        );
    }

    private void configurarComponentes() {
        titulo.setText("Asignaciones / " + medico.getNombre() + " " + medico.getApellido());
        titulo.getStyleClass().add("titulo");

        lblContador.getStyleClass().add("contador-resultados");

        btnNueva.setId("btnNuevoMedico");
    }

    private void configurarTabla() {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("Este médico no tiene asignaciones activas."));

        TableColumn<AsignacionMedicoModel, String> colHospital = new TableColumn<>("Hospital");
        colHospital.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNombreHospital()));

        TableColumn<AsignacionMedicoModel, String> colHorario = new TableColumn<>("Horario");
        colHorario.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getHorario()));
        colHorario.setMaxWidth(140);

        TableColumn<AsignacionMedicoModel, String> colInicio = new TableColumn<>("Fecha inicio");
        colInicio.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getFechaInicio() != null ? c.getValue().getFechaInicio().toString() : ""));
        colInicio.setMaxWidth(100);

        TableColumn<AsignacionMedicoModel, String> colFin = new TableColumn<>("Fecha fin");
        colFin.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getFechaFin() != null ? c.getValue().getFechaFin().toString() : ""));
        colFin.setMaxWidth(100);

        TableColumn<AsignacionMedicoModel, String> colEstado = new TableColumn<>("Estado");
        colEstado.setMaxWidth(100);
        colEstado.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        Boolean.TRUE.equals(c.getValue().getEstado()) ? "Activo" : "Inactivo"));
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

        TableColumn<AsignacionMedicoModel, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMaxWidth(80);
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnDesactivar = btnIcono("⏻", "btn-icono", "btn-toggle");
            {
                btnDesactivar.setOnAction(e -> {
                    AsignacionMedicoModel a = getTableView().getItems().get(getIndex());
                    controller.desactivar(a.getCodigo());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDesactivar);
            }
        });

        tabla.getColumns().addAll(colHospital, colHorario, colInicio, colFin, colEstado, colAcciones);
    }

    private void configurarLayout() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox barraTitulo = new HBox(titulo, spacer, btnVolver);
        barraTitulo.setAlignment(Pos.CENTER_LEFT);

        HBox barraBotones = new HBox(lblContador, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, btnNueva);
        barraBotones.setAlignment(Pos.CENTER_LEFT);

        setSpacing(15);
        setPadding(new Insets(24));
        getChildren().addAll(barraTitulo, barraBotones, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
    }

    private void registrarEventos() {
        btnVolver.setOnAction(e -> { if (onVolver != null) onVolver.run(); });
        btnNueva.setOnAction(e -> abrirDialogoCrear());
    }

    private void abrirDialogoCrear() {
        new AsignacionFormDialog(
                medico.getDocumento(),
                medico.getCodigoCiudad(),
                body -> {
                    AsignacionMedicoService.AsignacionCreateBody createBody =
                            (AsignacionMedicoService.AsignacionCreateBody) body;
                    controller.crear(createBody, medico.getNombre() + " " + medico.getApellido());
                }
        ).show();
    }

    private void actualizarContador(int total) {
        lblContador.setText(total + (total == 1 ? " asignación" : " asignaciones"));
    }

    private Button btnIcono(String icono, String... clases) {
        Button btn = new Button(icono);
        btn.getStyleClass().addAll(clases);
        return btn;
    }

    private void cargarEstilos() {
        getStyleClass().add("paciente-view");
        try {
            getStylesheets().add(
                    getClass().getResource("/styles/paciente/paciente.css").toExternalForm());
        } catch (Exception ignored) {}
    }
}