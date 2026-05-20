package views.facturacion;

import controllers.CajaController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import models.FacturaCajaModel;
import views.common.PrecioFormato;
import views.common.Toast;

import java.io.File;

public class CajaView extends VBox {

    private final CajaController controller = new CajaController();
    
    private final TextField txtBuscarDoc = new TextField();
    private final TableView<FacturaCajaModel> tablaCaja = new TableView<>();
    
    // Panel de Detalles
    private final Label lblSubtotal = new Label("$0.00");
    private final Label lblAporteEps = new Label("-$0.00");
    private final Label lblTotalPagar = new Label("$0.00");
    private final Button btnPagar = new Button("💸 Registrar Pago y Generar Recibo");

    public CajaView() {
        setSpacing(20);
        setPadding(new Insets(20));
        getStyleClass().add("contenedor-principal");

        Label titulo = new Label("Caja y Facturación a Pacientes (B2C)");
        titulo.getStyleClass().add("titulo");

        HBox buscador = crearBuscador();
        configurarTabla();
        VBox panelPago = crearPanelPago();

        getChildren().addAll(titulo, buscador, tablaCaja, panelPago);
        VBox.setVgrow(tablaCaja, Priority.ALWAYS);

        registrarEventos();
        conectarController();
    }

    private HBox crearBuscador() {
        HBox box = new HBox(10);
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        txtBuscarDoc.setPromptText("Ingrese Documento del Paciente...");
        txtBuscarDoc.setPrefWidth(250);
        
        Button btnBuscar = new Button("🔍 Buscar Facturas");
        btnBuscar.getStyleClass().add("button-primario");
        btnBuscar.setOnAction(e -> controller.buscarFacturas(txtBuscarDoc.getText()));

        box.getChildren().addAll(new Label("Cédula Paciente:"), txtBuscarDoc, btnBuscar);
        return box;
    }

    private void configurarTabla() {
        tablaCaja.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaCaja.setPlaceholder(new Label("Busque un paciente para ver sus facturas pendientes de pago."));

        TableColumn<FacturaCajaModel, String> colFac = new TableColumn<>("Factura N°");
        colFac.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigoFactura()));

        TableColumn<FacturaCajaModel, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaFormateada()));

        TableColumn<FacturaCajaModel, String> colDesc = new TableColumn<>("Servicio Médico");
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcionServicio()));

        TableColumn<FacturaCajaModel, String> colEps = new TableColumn<>("Convenio EPS");
        colEps.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreEps()));

        tablaCaja.getColumns().addAll(colFac, colFecha, colDesc, colEps);
    }

    private VBox crearPanelPago() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-background-color: #ecf0f1;");
        
        Label tituloPanel = new Label("Detalle Financiero de la Factura Seleccionada");
        tituloPanel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        lblSubtotal.setStyle("-fx-font-size: 14px;");
        lblAporteEps.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 14px;");
        lblTotalPagar.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #c0392b;");
        
        btnPagar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        btnPagar.setDisable(true); // Deshabilitado hasta que seleccionen algo

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.add(new Label("Subtotal del Servicio:"), 0, 0); grid.add(lblSubtotal, 1, 0);
        grid.add(new Label("Cobertura Asumida por EPS:"), 0, 1); grid.add(lblAporteEps, 1, 1);
        grid.add(new Label("VALOR A COBRAR AL PACIENTE:"), 0, 2); grid.add(lblTotalPagar, 1, 2);

        panel.getChildren().addAll(tituloPanel, grid, btnPagar);
        return panel;
    }

    private void registrarEventos() {
        tablaCaja.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                lblSubtotal.setText(PrecioFormato.formatear(newSel.getCostoTotal()));
                lblAporteEps.setText("-" + PrecioFormato.formatear(newSel.getCoberturaEps()));
                lblTotalPagar.setText(PrecioFormato.formatear(newSel.getCopagoAPagar()));
                btnPagar.setDisable(false);
            } else {
                lblSubtotal.setText("$0.00");
                lblAporteEps.setText("-$0.00");
                lblTotalPagar.setText("$0.00");
                btnPagar.setDisable(true);
            }
        });

        btnPagar.setOnAction(e -> {
            FacturaCajaModel seleccionada = tablaCaja.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Guardar Recibo de Caja");
                fileChooser.setInitialFileName("Recibo_Caja_" + seleccionada.getCodigoFactura() + ".pdf");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));

                File file = fileChooser.showSaveDialog(getScene().getWindow());
                if (file != null) {
                    btnPagar.setDisable(true);
                    btnPagar.setText("Procesando pago...");
                    controller.pagarYGenerarRecibo(seleccionada.getCodigoFactura(), file);
                }
            }
        });
    }

    private void conectarController() {
        controller.setOnBusquedaExitosa(facturas -> {
            tablaCaja.setItems(FXCollections.observableArrayList(facturas));
            if (facturas.isEmpty()) {
                Toast.info(this, "El paciente no tiene facturas pendientes de pago.");
            }
        });

        controller.setOnPagoExitoso(msg -> {
            Toast.success(this, msg);
            btnPagar.setText("Registrar Pago y Generar Recibo");
            controller.buscarFacturas(txtBuscarDoc.getText());
        });

        controller.setOnError(msg -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
            alert.showAndWait();
            btnPagar.setDisable(false);
            btnPagar.setText("Registrar Pago y Generar Recibo");
        });
    }
}