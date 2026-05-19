package views.costo;

import controllers.TarifaEpsController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import models.EpsModel;
import models.TarifaEpsModel;
import views.common.Toast;

import java.util.Optional;

public class TarifaEpsView extends VBox {

    private final TarifaEpsController controller = new TarifaEpsController();
    
    private final Label titulo = new Label("Parametrización de Tarifas y Coberturas");
    private final ComboBox<EpsModel> cmbEpsFilter = new ComboBox<>();
    private final Button btnNuevaTarifa = new Button("+ Registrar Tarifa");
    private final TableView<TarifaEpsModel> tabla = new TableView<>();

    public TarifaEpsView() {
        setSpacing(20);
        setPadding(new Insets(20));
        getStyleClass().add("contenedor-principal");

        titulo.getStyleClass().add("titulo");
        btnNuevaTarifa.setId("btnNuevoServicio"); // Hereda los estilos verdes del CSS global de tu equipo
        btnNuevaTarifa.setDisable(true);          // Deshabilitado hasta que se escoja una EPS

        // Configuración de la barra de herramientas superior
        cmbEpsFilter.setPromptText("Seleccione una EPS para gestionar...");
        cmbEpsFilter.setMaxWidth(350);
        HBox.setHgrow(cmbEpsFilter, Priority.ALWAYS);

        HBox barraSuperior = new HBox(cmbEpsFilter, btnNuevaTarifa);
        barraSuperior.setAlignment(Pos.CENTER_LEFT);
        barraSuperior.setSpacing(20);

        configurarTabla();
        conectarController();
        registrarEventos();

        getChildren().addAll(titulo, barraSuperior, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        // Carga inicial asíncrona de las EPS
        controller.cargarListaEps();
    }

    private void configurarTabla() {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("Seleccione una EPS en el menú superior para desplegar sus tarifas configuradas."));

        TableColumn<TarifaEpsModel, String> colServicio = new TableColumn<>("Servicio Médico Autorizado");
        colServicio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreServicio()));

        TableColumn<TarifaEpsModel, String> colPorcentaje = new TableColumn<>("Porcentaje Cobertura");
        colPorcentaje.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPorcentajeCobertura().toString() + " %"));
        colPorcentaje.setStyle("-fx-alignment: CENTER;");

        TableColumn<TarifaEpsModel, Void> colAcciones = new TableColumn<>("Acciones de Ajuste");
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Cobertura");
            {
                btnEditar.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-cursor: hand;");
                btnEditar.setOnAction(event -> {
                    TarifaEpsModel seleccion = getTableView().getItems().get(getIndex());
                    abrirFormulario(seleccion);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btnEditar);
            }
        });

        tabla.getColumns().addAll(colServicio, colPorcentaje, colAcciones);
    }

    private void registrarEventos() {
        // Escuchamos el cambio de selección del ComboBox
        cmbEpsFilter.setOnAction(event -> {
            EpsModel epsSeleccionada = cmbEpsFilter.getValue();
            if (epsSeleccionada != null) {
                btnNuevaTarifa.setDisable(false);
                controller.cargarTarifasDeEps(epsSeleccionada.getCodigo());
            }
        });

        // Evento para registrar nueva tarifa (POST)
        btnNuevaTarifa.setOnAction(event -> abrirFormulario(null));
    }

    private void abrirFormulario(TarifaEpsModel tarifa) {
        EpsModel epsSeleccionada = cmbEpsFilter.getValue();
        if (epsSeleccionada == null) return;

        TarifaFormDialog dialog = new TarifaFormDialog(tarifa, epsSeleccionada.getNombre());
        Optional<TarifaEpsModel> resultado = dialog.showAndWait();

        resultado.ifPresent(modeloResultante -> {
            if (tarifa != null) {
                // Es edición (PUT)
                controller.actualizarTarifa(modeloResultante);
            } else {
                // Es nueva tarifa (POST)
                controller.guardarNuevaTarifa(epsSeleccionada.getCodigo(), modeloResultante.getCodigoServicio(), modeloResultante.getPorcentajeCobertura().doubleValue());
            }
        });
    }

    private void conectarController() {
        controller.setOnEpsCargadas(list -> cmbEpsFilter.setItems(FXCollections.observableArrayList(list)));
        
        controller.setOnTarifasCargadas(list -> {
            tabla.setItems(FXCollections.observableArrayList(list));
            if (list.isEmpty()) {
                tabla.setPlaceholder(new Label("Esta EPS no tiene tarifas configuradas aún. ¡Crea una nueva!"));
            }
        });

        controller.setOnError(msg -> new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait());
        controller.setOnExito(msg -> Toast.success(this, msg));
    }
}