package views.costo;

import controllers.HistorialLiquidacionController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import models.FacturaResumenModel;
import models.LiquidacionResponse;
import views.common.PrecioFormato;

public class HistorialLiquidacionView extends VBox {

  private final HistorialLiquidacionController controller = new HistorialLiquidacionController();
  private final TableView<LiquidacionResponse> tablaMaestra = new TableView<>();
  private final TableView<FacturaResumenModel> tablaDetalle = new TableView<>();

  public HistorialLiquidacionView() {
    setSpacing(15);
    setPadding(new Insets(20));
    getStyleClass().add("contenedor-principal");

    Label tituloMaster = new Label("Historial de Liquidaciones a EPS");
    tituloMaster.getStyleClass().add("titulo");

    Label tituloDetail = new Label("Detalle de Facturas de la Liquidación Seleccionada");
    tituloDetail.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #34495e;");

    configurarTablaMaestra();
    configurarTablaDetalle();
    registrarEventosInteraccion();
    conectarController();

    getChildren().addAll(tituloMaster, tablaMaestra, tituloDetail, tablaDetalle);
    VBox.setVgrow(tablaMaestra, Priority.ALWAYS);
    VBox.setVgrow(tablaDetalle, Priority.ALWAYS);

    controller.cargarHistorial();
  }

  private void configurarTablaMaestra() {
    tablaMaestra.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    tablaMaestra.setPlaceholder(new Label("Cargando historial de liquidaciones..."));

    TableColumn<LiquidacionResponse, String> colCodigo = new TableColumn<>("Liquidación N°");
    colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));

    TableColumn<LiquidacionResponse, String> colCobro = new TableColumn<>("Monto Cobrado a EPS");
    colCobro.setCellValueFactory(
        c -> new SimpleStringProperty(PrecioFormato.formatear(c.getValue().getTotalCoberturaEps())));

    TableColumn<LiquidacionResponse, String> colCopago = new TableColumn<>("Copago Pacientes");
    colCopago.setCellValueFactory(
        c -> new SimpleStringProperty(PrecioFormato.formatear(c.getValue().getTotalCopagoPaciente())));

    TableColumn<LiquidacionResponse, String> colTotal = new TableColumn<>("Total Procesado");
    colTotal.setCellValueFactory(c -> new SimpleStringProperty(PrecioFormato.formatear(c.getValue().getTotalBruto())));

    tablaMaestra.getColumns().addAll(colCodigo, colCobro, colCopago, colTotal);
  }

  private void configurarTablaDetalle() {
    tablaDetalle.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    tablaDetalle.setPlaceholder(
        new Label("Seleccione una liquidación en la tabla de arriba para ver las facturas involucradas."));

    TableColumn<FacturaResumenModel, String> colFac = new TableColumn<>("Factura N°");
    colFac.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));

    TableColumn<FacturaResumenModel, String> colFec = new TableColumn<>("Fecha");
    colFec.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaFormateada()));

    TableColumn<FacturaResumenModel, String> colDesc = new TableColumn<>("Concepto");
    colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));

    TableColumn<FacturaResumenModel, String> colCosto = new TableColumn<>("Costo Base");
    colCosto.setCellValueFactory(c -> new SimpleStringProperty(PrecioFormato.formatear(c.getValue().getCostoTotal())));

    tablaDetalle.getColumns().addAll(colFac, colFec, colDesc, colCosto);
  }

  private void registrarEventosInteraccion() {

    tablaMaestra.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
      if (newSel != null && newSel.getFacturas() != null) {
        tablaDetalle.setItems(FXCollections.observableArrayList(newSel.getFacturas()));
      } else {
        tablaDetalle.getItems().clear();
      }
    });
  }

  private void conectarController() {
    controller.setOnDatosCargados(lista -> {
      tablaMaestra.setItems(FXCollections.observableArrayList(lista));
      if (lista.isEmpty()) {
        tablaMaestra.setPlaceholder(new Label("No se ha generado ninguna liquidación históricamente."));
      }
    });

    controller
        .setOnError(msg -> new Alert(Alert.AlertType.ERROR, "Error cargando historial: " + msg, ButtonType.OK).show());
  }
}