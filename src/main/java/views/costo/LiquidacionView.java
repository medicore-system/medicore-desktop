package views.costo;

import controllers.LiquidacionController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import models.EpsModel;
import models.FacturaResumenModel;
import models.LiquidacionResponse;
import views.common.PrecioFormato;
import views.common.Toast;

import java.time.LocalDate;

public class LiquidacionView extends VBox {

  private final LiquidacionController controller = new LiquidacionController();

  private final ComboBox<EpsModel> cmbEps = new ComboBox<>();
  private final DatePicker dpInicio = new DatePicker();
  private final DatePicker dpFin = new DatePicker();
  private final Button btnGenerar = new Button("⚡ Generar Liquidación");

  private final TableView<FacturaResumenModel> tablaFacturas = new TableView<>();
  private final Label lblTotalBruto = new Label("$ 0.00");
  private final Label lblTotalEps = new Label("$ 0.00");
  private final Label lblTotalCopago = new Label("$ 0.00");

  public LiquidacionView() {
    setSpacing(20);
    setPadding(new Insets(20));
    getStyleClass().add("contenedor-principal");

    configurarFiltros();
    configurarTabla();
    HBox panelResumen = configurarPanelResumen();
    conectarController();

    Label titulo = new Label("Cierre y Generación de Liquidaciones");
    titulo.getStyleClass().add("titulo");

    HBox barraSuperior = new HBox(15, new Label("EPS:"), cmbEps, new Label("Desde:"), dpInicio, new Label("Hasta:"),
        dpFin, btnGenerar);
    barraSuperior.setAlignment(Pos.CENTER_LEFT);

    getChildren().addAll(titulo, barraSuperior, tablaFacturas, panelResumen);
    VBox.setVgrow(tablaFacturas, Priority.ALWAYS);

    controller.cargarListaEps();
  }

  private void configurarFiltros() {
    cmbEps.setPromptText("Seleccione EPS...");
    cmbEps.setConverter(new StringConverter<>() {
      @Override
      public String toString(EpsModel eps) {
        return eps == null ? "" : eps.getNombre();
      }

      @Override
      public EpsModel fromString(String s) {
        return null;
      }
    });

    // Valores por defecto
    dpInicio.setValue(LocalDate.now().withDayOfMonth(1));
    dpFin.setValue(LocalDate.now());

    btnGenerar.getStyleClass().add("button-primario");
    btnGenerar.setOnAction(e -> generarLiquidacion());
  }

  private void configurarTabla() {
    tablaFacturas.setPlaceholder(new Label("No se ha procesado ninguna liquidación."));
    tablaFacturas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    TableColumn<FacturaResumenModel, String> colCodigo = new TableColumn<>("Factura N°");
    colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));

    TableColumn<FacturaResumenModel, String> colFecha = new TableColumn<>("Fecha Emisión");
    colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaFormateada()));

    TableColumn<FacturaResumenModel, String> colDesc = new TableColumn<>("Concepto");
    colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));

    TableColumn<FacturaResumenModel, String> colCosto = new TableColumn<>("Costo Total");
    colCosto.setCellValueFactory(c -> new SimpleStringProperty(PrecioFormato.formatear(c.getValue().getCostoTotal())));
    colCosto.setStyle("-fx-alignment: CENTER-RIGHT;");

    tablaFacturas.getColumns().addAll(colCodigo, colFecha, colDesc, colCosto);
  }

  private HBox configurarPanelResumen() {
    HBox panel = new HBox(30);
    panel.setAlignment(Pos.CENTER_RIGHT);
    panel.setPadding(new Insets(15));
    panel.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5px;");

    lblTotalBruto.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
    lblTotalEps.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2ecc71;");
    lblTotalCopago.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e74c3c;");

    panel.getChildren().addAll(
        new VBox(5, new Label("Total Bruto Procesado:"), lblTotalBruto),
        new VBox(5, new Label("Total a Cobrar EPS:"), lblTotalEps),
        new VBox(5, new Label("Total Copago Pacientes:"), lblTotalCopago));
    return panel;
  }

  private void generarLiquidacion() {
    if (cmbEps.getValue() == null || dpInicio.getValue() == null || dpFin.getValue() == null) {
      new Alert(Alert.AlertType.WARNING, "Debe seleccionar una EPS y un rango de fechas.", ButtonType.OK).show();
      return;
    }

    btnGenerar.setDisable(true);
    btnGenerar.setText("⏳ Procesando...");

    controller.procesarLiquidacion(
        cmbEps.getValue().getCodigo(),
        dpInicio.getValue().toString(),
        dpFin.getValue().toString());
  }

  private void conectarController() {
    controller.setOnEpsCargadas(epsList -> cmbEps.setItems(FXCollections.observableArrayList(epsList)));

    controller.setOnLiquidacionExitosa(res -> {
      btnGenerar.setDisable(false);
      btnGenerar.setText("⚡ Generar Liquidación");

      tablaFacturas.setItems(FXCollections.observableArrayList(res.getFacturas()));
      lblTotalBruto.setText(PrecioFormato.formatear(res.getTotalBruto()));
      lblTotalEps.setText(PrecioFormato.formatear(res.getTotalCoberturaEps()));
      lblTotalCopago.setText(PrecioFormato.formatear(res.getTotalCopagoPaciente()));

      Toast.success(this, "Liquidación " + res.getCodigo() + " generada con éxito.");
    });

    controller.setOnError(msg -> {
      btnGenerar.setDisable(false);
      btnGenerar.setText(" Generar Liquidación");

      // Si el error es la validación de negocio, mostramos una advertencia amigable
      if (msg.contains("No hay facturas pendientes")) {
        new Alert(Alert.AlertType.INFORMATION,
            "No se encontraron facturas pendientes para liquidar con los filtros seleccionados.\n\nRevise la EPS o amplíe el rango de fechas.",
            ButtonType.OK).showAndWait();
      } else {
        // Si es un error de conexión, 500, etc., mostramos el error original
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
      }
    });
  }
}