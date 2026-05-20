package views.mainLayout;

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import views.asignacion.AsignacionView;
import views.asignacion.asignacionesMedico.AsignacionesMedicoView;
import views.ciudad.CiudadView;
import views.costo.CostoView;
import views.facturacion.CajaView;
import views.facturacion.FacturacionView;
import views.hospital.HospitalDetalleView;
import views.hospital.HospitalView;
import views.medico.MedicoView;
import views.usuario.UsuarioView;
import views.reporte.ReporteView;
import views.servicio.ServicioView;
import views.usuario.cita.CitasUsuarioView;

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
        Button facturacion  = new Button("Facturacion");

        //Asignamos "entiramiento" a los diferentes scenes que aparecen segun donde estemos parados
        ciudades.setOnAction    (e -> {setCenter(new CiudadView());});
        hospitales.setOnAction  (e -> mostrarHospitales());
        medicos.setOnAction     (e -> {setCenter(new MedicoView());});
        asignaciones.setOnAction(e -> mostrarAsignaciones());
        pacientes.setOnAction   (e -> mostrarUsuarios());
        servicios.setOnAction   (e -> {setCenter(new ServicioView());});
        costos.setOnAction      (e -> {setCenter(new CostoView());});
        reportes.setOnAction    (e -> {setCenter(new ReporteView());});
        facturacion.setOnAction (e -> {setCenter(new CajaView());});

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

    private void mostrarHospitales() {
        setCenter(new HospitalView(codigo ->
                setCenter(new HospitalDetalleView(codigo, this::mostrarHospitales))));
    }

    private void mostrarUsuarios(){
        setCenter(new UsuarioView(codigo ->
                setCenter(new CitasUsuarioView(codigo, this::mostrarUsuarios))));
    }

    private void mostrarAsignaciones() {
        setCenter(new AsignacionView(medico ->
                setCenter(new AsignacionesMedicoView(medico, this::mostrarAsignaciones))));
    }
}
