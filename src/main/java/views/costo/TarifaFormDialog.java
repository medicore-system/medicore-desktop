package views.costo;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import models.ServicioModel;
import models.TarifaEpsModel;
import services.ServicioService;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.List;

public class TarifaFormDialog extends Dialog<TarifaEpsModel> {

  private final TextField txtPorcentaje = new TextField();
  private ComboBox<ServicioModel> cmbServicio;
  private final boolean esEdicion;

  public TarifaFormDialog(TarifaEpsModel tarifa, String nombreEps) {
    this.esEdicion = (tarifa != null);
    setTitle(esEdicion ? "Editar Cobertura EPS" : "Registrar Nueva Tarifa Cobertura");
    setHeaderText(esEdicion ? "Modificar porcentaje de cobertura actual"
        : "Asignar un porcentaje de cobertura a un servicio para: " + nombreEps);

    ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
    getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 40, 10, 10));

    // Campo EPS (Siempre deshabilitado porque depende de la selección de la
    // pantalla principal)
    TextField txtEps = new TextField(nombreEps);
    txtEps.setDisable(true);
    grid.add(new Label("EPS:"), 0, 0);
    grid.add(txtEps, 1, 0);

    grid.add(new Label("Servicio Médico:"), 0, 1);
    if (esEdicion) {
      TextField txtServicio = new TextField(tarifa.getNombreServicio());
      txtServicio.setDisable(true);
      grid.add(txtServicio, 1, 1);
      txtPorcentaje.setText(tarifa.getPorcentajeCobertura().toString());
    } else {
      cmbServicio = new ComboBox<>();
      cmbServicio.setMaxWidth(Double.MAX_VALUE);

      cmbServicio.setConverter(new StringConverter<ServicioModel>() {
        @Override
        public String toString(ServicioModel servicio) {
          return servicio == null ? "" : servicio.getNombre(); // Mostramos el nombre del servicio
        }

        @Override
        public ServicioModel fromString(String string) {
          return null; // No lo necesitamos porque el ComboBox no es editable por texto
        }
      });

      grid.add(cmbServicio, 1, 1);
      txtPorcentaje.setPromptText("Ej: 85.0");
      // Cargar servicios médicos de forma reactiva asíncrona usando el
      // ServicioService de tu equipo
      ServicioService.getInstance().getAllServicios().thenAccept(servicios -> javafx.application.Platform
          .runLater(() -> cmbServicio.setItems(FXCollections.observableArrayList(servicios))));
    }

    grid.add(new Label("Cobertura (%):"), 0, 2);
    grid.add(txtPorcentaje, 1, 2);

    getDialogPane().setContent(grid);

    // Convertidor de resultados - Validador SOLID sin acoplamientos
    setResultConverter(dialogButton -> {
      if (dialogButton == btnGuardar) {
        try {
          BigDecimal porcentaje = new BigDecimal(txtPorcentaje.getText().trim());
          if (porcentaje.compareTo(BigDecimal.ZERO) < 0 || porcentaje.compareTo(new BigDecimal("100")) > 0) {
            mostrarError("El porcentaje de cobertura debe estar estrictamente entre 0 y 100.");
            return null;
          }

          if (esEdicion) {
            tarifa.setPorcentajeCobertura(porcentaje);
            return tarifa;
          } else {
            ServicioModel servSeleccionado = cmbServicio.getValue();
            if (servSeleccionado == null) {
              mostrarError("Debe seleccionar un servicio médico de la lista.");
              return null;
            }
            TarifaEpsModel nuevaTarifa = new TarifaEpsModel();
            nuevaTarifa.setCodigoServicio(servSeleccionado.getCodigo());
            nuevaTarifa.setPorcentajeCobertura(porcentaje);
            return nuevaTarifa;
          }
        } catch (NumberFormatException e) {
          mostrarError("Por favor, ingrese un valor numérico válido decimal.");
          return null;
        }
      }
      return null;
    });
  }

  private void mostrarError(String mensaje) {
    Alert alert = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
    alert.showAndWait();
  }
}