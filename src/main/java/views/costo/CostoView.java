package views.costo;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Vista principal del módulo Financiero y de Costos.
 * Actúa como un contenedor de pestañas para organizar
 * la Parametrización y las Liquidaciones.
 */
public class CostoView extends VBox {

    public CostoView() {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("tab-pane");

        // Pestaña 1: Configuración de Tarifas (Lo que acabamos de hacer)
        Tab tabTarifas = new Tab("Parametrización EPS", new TarifaEpsView());
        tabTarifas.setClosable(false);

        // Pestaña 2: Aquí pondremos el Generador de Liquidaciones en la Tarea 2
        Tab tabLiquidaciones = new Tab("Generar Liquidaciones", new LiquidacionView());
        tabLiquidaciones.setClosable(false);

        Tab tabHistorial = new Tab("Historial de Liquidaciones", new HistorialLiquidacionView());

        tabHistorial.setClosable(false);

        tabPane.getTabs().addAll(tabTarifas, tabLiquidaciones, tabHistorial);

        getChildren().add(tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
    }
}