package views.asignacion;

import controllers.MedicoController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.MedicoModel;
import views.common.Toast;

import java.util.List;
import java.util.function.Consumer;

public class AsignacionView extends VBox {

    private final MedicoController medicoController = new MedicoController();
    private final Consumer<MedicoModel> onVerAsignaciones;

    private final Label titulo       = new Label("Asignación de Médicos");
    private final TextField buscador = new TextField();
    private final Label lblContador  = new Label();
    private final TableView<MedicoModel> tabla = new TableView<>();

    public AsignacionView(Consumer<MedicoModel> onVerAsignaciones) {
        this.onVerAsignaciones = onVerAsignaciones;
        conectarController();
        configurarComponentes();
        configurarTabla();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
        medicoController.cargarMedicosActivos();
    }

    private void conectarController() {
        medicoController.setOnDatosActualizados(lista -> {
            tabla.setItems(FXCollections.observableArrayList(lista));
            tabla.refresh();
            actualizarContador(lista.size(), lista.size());
        });

        medicoController.setOnError(mensaje ->
                new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait()
        );

        medicoController.setOnExito(mensaje ->
                Toast.success(this, mensaje)
        );
    }

    private void configurarComponentes() {
        titulo.getStyleClass().add("titulo");

        buscador.setPromptText("🔍  Buscar médico...");
        buscador.setId("buscador");

        lblContador.getStyleClass().add("contador-resultados");
    }

    private void configurarTabla() {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("No hay médicos activos."));

        TableColumn<MedicoModel, String> colDocumento = new TableColumn<>("Documento");
        colDocumento.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getDocumento()));
        colDocumento.setMaxWidth(130);
        colDocumento.setMinWidth(100);

        TableColumn<MedicoModel, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getNombre() + " " + c.getValue().getApellido()));

        TableColumn<MedicoModel, String> colEspecialidad = new TableColumn<>("Especialidad");
        colEspecialidad.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNombreEspecialidad()));
        colEspecialidad.setMaxWidth(120);

        TableColumn<MedicoModel, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNombreCiudad()));
        colCiudad.setMaxWidth(100);

        TableColumn<MedicoModel, Void> colAcciones = new TableColumn<>("Asignaciones");
        colAcciones.setMaxWidth(120);
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = btnIcono("📋 Ver", "btn-icono", "btn-ver");
            {
                btnVer.setOnAction(e -> {
                    MedicoModel m = getTableView().getItems().get(getIndex());
                    onVerAsignaciones.accept(m);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnVer);
            }
        });

        tabla.getColumns().addAll(colDocumento, colNombre, colEspecialidad, colCiudad, colAcciones);
    }

    private void configurarLayout() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox barraTitulo = new HBox(titulo, spacer);
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
            List<MedicoModel> filtrados = medicoController.filtrar(texto);
            tabla.setItems(FXCollections.observableArrayList(filtrados));
            actualizarContador(filtrados.size(), medicoController.filtrar("").size());
        });
    }

    private void actualizarContador(int mostrados, int total) {
        if (mostrados == total) {
            lblContador.setText(total + (total == 1 ? " médico" : " médicos"));
        } else {
            lblContador.setText(mostrados + " de " + total + " médicos");
        }
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