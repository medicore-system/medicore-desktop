package views.mainLayout;

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import views.asignacion.AsignacionView;
import views.ciudad.CiudadView;
import views.costo.CostoView;
import views.facturacion.FacturacionView;
import views.hospital.HospitalDetalleView;
import views.hospital.HospitalView;
import views.medico.MedicoView;
import views.paciente.PacienteView;
import views.reporte.ReporteView;
import views.servicio.ServicioView;

public class MainLayout extends BorderPane {
    private final VBox sideBar;

    public MainLayout() {
        sideBar = createSideBar();
        setLeft(sideBar);
        setCenter(new CiudadView());
    }

    private VBox createSideBar(){
        //Creamos el menu "sidebar" lateral izquierdo que contendra todos los botones para navegar en el aplicativo
        VBox menu           = new VBox();

        //Creamos los botones que daran apertura a las vistas para el ADMIN
        Button ciudades     = new Button("Ciudades");
        Button hospitales   = new Button("Hospitales");
        Button medicos      = new Button("Medicos");
        Button asignaciones = new Button("Asignaciones");
        Button pacientes    = new Button("Pacientes");
        Button servicios    = new Button("Servicios");
        Button costos       = new Button("Costos");
        Button reportes     = new Button("Reportes");
        Button facturacion  = new Button("Facturacion a EPS");

        //Asignamos "entiramiento" a los diferentes scenes que aparecen segun donde estemos parados
        ciudades.setOnAction    (e -> {setCenter(new CiudadView());});
        hospitales.setOnAction(e -> setCenter(
                new HospitalView(codigo -> setCenter(new HospitalDetalleView(codigo)))
        ));
        medicos.setOnAction     (e -> {setCenter(new MedicoView());});
        asignaciones.setOnAction(e -> {setCenter(new AsignacionView());});
        pacientes.setOnAction   (e -> {setCenter(new PacienteView());});
        servicios.setOnAction   (e -> {setCenter(new ServicioView());});
        costos.setOnAction      (e -> {setCenter(new CostoView());});
        reportes.setOnAction    (e -> {setCenter(new ReporteView());});
        facturacion.setOnAction (e -> {setCenter(new FacturacionView());});

        //Adicionamos los botones al "sidebar" lateral izquierdo
        menu.getChildren().addAll(
          ciudades,
          hospitales,
          medicos,
          asignaciones,
          pacientes,
          servicios,
          costos,
          reportes,
          facturacion
        );

        getStyleClass().add("mainLayout");
        getStylesheets().add(
                getClass()
                        .getResource("/styles/layout/mainLayout.css")
                        .toExternalForm()
        );

        return menu;
    }
}
