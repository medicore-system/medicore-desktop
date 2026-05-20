package views.reporte;

import controllers.ReporteController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import models.reportes.*;
import views.common.PrecioFormato;

public class ReporteView extends VBox {

    private final ReporteController controller = new ReporteController();

    private final PieChart pieCartera = new PieChart();
    private final BarChart<String, Number> barEspecialidad;
    private final BarChart<String, Number> barHospital;

    private final PieChart pieAtencionesEps = new PieChart();
    private final TableView<ProductividadMedicoModel> tablaProductividad = new TableView<>();

    private final ComboBox<Integer> cmbAnio = new ComboBox<>();
    private final ComboBox<Integer> cmbMes = new ComboBox<>();

    public ReporteView() {
        getStylesheets().add(getClass().getResource("/styles/reporte/reporte.css").toExternalForm());

        setSpacing(10);
        setPadding(new Insets(20));
        getStyleClass().add("contenedor-principal");

        Label titulo = new Label("Dashboard Gerencial (Business Intelligence)");
        titulo.getStyleClass().add("titulo");

        // Configuración inicial de los gráficos de barras (Requieren ejes)
        CategoryAxis xAxisEspecialidad = new CategoryAxis();
        NumberAxis yAxisEspecialidad = new NumberAxis();
        barEspecialidad = new BarChart<>(xAxisEspecialidad, yAxisEspecialidad);
        barEspecialidad.setTitle("Rentabilidad por Especialidad");

        CategoryAxis xAxisHospital = new CategoryAxis();
        NumberAxis yAxisHospital = new NumberAxis();
        barHospital = new BarChart<>(xAxisHospital, yAxisHospital);
        barHospital.setTitle("Ingresos Totales por Sede/Hospital");

        HBox barraFiltros = new HBox(15);
        barraFiltros.getStyleClass().add("barra-filtros");
        barraFiltros.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        cmbAnio.getItems().addAll(2023, 2024, 2025, 2026);
        cmbAnio.setValue(java.time.LocalDate.now().getYear()); // Año actual por defecto

        cmbMes.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        cmbMes.setValue(java.time.LocalDate.now().getMonthValue()); // Mes actual por defecto

        Button btnFiltrar = new Button("Actualizar Dashboards");
        btnFiltrar.getStyleClass().add("button-primario");
        btnFiltrar.setOnAction(e -> actualizarReportes());

        barraFiltros.getChildren().addAll(
                new Label("Filtros Gerenciales - Año:"), cmbAnio,
                new Label("Mes (solo Prod. Médica):"), cmbMes,
                btnFiltrar);

        // Construir la controller.setOnEstadoCarteraCargado
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                crearTabFinanzas(),
                crearTabOperacion());
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        getChildren().addAll(titulo, barraFiltros, tabPane);

        conectarController();
        actualizarReportes(); // Carga inicial
    }

    private void actualizarReportes() {
        int anio = cmbAnio.getValue();
        int mes = cmbMes.getValue();
        controller.cargarTodosLosReportes(anio, mes);
    }

    private Tab crearTabFinanzas() {
        Tab tab = new Tab("Resumen Financiero");
        tab.setClosable(false);

        pieCartera.setTitle("Estado de Cartera (Deuda EPS)");

        // Layout de la pestaña Finanzas (Arriba un gráfico grande, abajo dos medianos)
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        HBox graficosSecundarios = new HBox(15, barHospital, barEspecialidad);
        HBox.setHgrow(barHospital, Priority.ALWAYS);
        HBox.setHgrow(barEspecialidad, Priority.ALWAYS);

        layout.getChildren().addAll(pieCartera, graficosSecundarios);
        VBox.setVgrow(pieCartera, Priority.ALWAYS);

        tab.setContent(layout);
        return tab;
    }

    private Tab crearTabOperacion() {
        Tab tab = new Tab("Rendimiento Operativo");
        tab.setClosable(false);

        pieAtencionesEps.setTitle("Volumen de Pacientes por EPS");

        // Configurar Tabla de Productividad Médica
        configurarTablaProductividad();

        VBox boxTabla = new VBox(5, new Label("Ranking de Productividad Médica"), tablaProductividad);
        VBox.setVgrow(tablaProductividad, Priority.ALWAYS);

        HBox layout = new HBox(20, pieAtencionesEps, boxTabla);
        layout.setPadding(new Insets(15));
        HBox.setHgrow(pieAtencionesEps, Priority.ALWAYS);
        HBox.setHgrow(boxTabla, Priority.ALWAYS);

        tab.setContent(layout);
        return tab;
    }

    private void configurarTablaProductividad() {
        tablaProductividad.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProductividadMedicoModel, String> colNombre = new TableColumn<>("Médico Especialista");
        colNombre.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombreCompleto()));

        TableColumn<ProductividadMedicoModel, Long> colCitas = new TableColumn<>("Citas Completadas en el Mes");
        colCitas.setCellValueFactory(new PropertyValueFactory<>("citasCompletadas"));
        colCitas.setStyle("-fx-alignment: CENTER;");

        tablaProductividad.getColumns().addAll(colNombre, colCitas);
    }

    private void conectarController() {
        controller.setOnEstadoCarteraCargado(datos -> {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (EstadoCarteraModel d : datos) {
                // Usamos getDeudaTotal() en lugar de getDeudaPendiente()
                pieData.add(
                        new PieChart.Data(d.getNombreEps() + " (" + PrecioFormato.formatear(d.getDeudaTotal()) + ")",
                                d.getDeudaTotal().doubleValue()));
            }
            pieCartera.setData(pieData);
        });

        controller.setOnIngresosHospitalCargados(datos -> {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ingresos");
            for (IngresosHospitalModel d : datos) {
                series.getData().add(new XYChart.Data<>(d.getNombreHospital(), d.getIngresosTotales().doubleValue()));
            }
            barHospital.getData().clear();
            barHospital.getData().add(series);
        });

        controller.setOnIngresosEspecialidadCargados(datos -> {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Rentabilidad");
            for (IngresosEspecialidadModel d : datos) {
                series.getData().add(new XYChart.Data<>(d.getNombreEspecialidad(), d.getIngresosTotales().doubleValue()));
            }
            barEspecialidad.getData().clear();
            barEspecialidad.getData().add(series);
        });

        controller.setOnAtencionesEpsCargadas(datos -> {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (AtencionesEpsModel d : datos) {
                pieData.add(new PieChart.Data(d.getNombreEps() + " (" + d.getTotalAtenciones() + " citas)",
                        d.getTotalAtenciones()));
            }
            pieAtencionesEps.setData(pieData);
        });

        controller.setOnProductividadMedicaCargada(datos -> {
            tablaProductividad.setItems(FXCollections.observableArrayList(datos));
        });

        controller.setOnError(msg -> new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).show());
    }
}