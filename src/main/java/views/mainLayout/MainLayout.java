package views.mainLayout;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import views.asignacion.AsignacionView;
import views.asignacion.asignacionesMedico.AsignacionesMedicoView;
import views.ciudad.CiudadView;
import views.costo.CostoView;
import views.facturacion.CajaView;
import views.historial.HistorialView;
import views.hospital.HospitalDetalleView;
import views.hospital.HospitalView;
import views.medico.MedicoView;
import views.usuario.UsuarioView;
import views.reporte.ReporteView;
import views.servicio.ServicioView;
import views.usuario.cita.CitasUsuarioView;

public class MainLayout extends BorderPane {

    private final VBox sideBar;
    private final VBox menuContainer = new VBox();
    private boolean expandido = true;
    private Button botonActivo = null;

    public MainLayout() {
        // Cargar los estilos
        getStylesheets().add(getClass().getResource("/styles/layout/mainLayout.css").toExternalForm());
        
        sideBar = createSideBar();
        setLeft(sideBar);
        
        // Vista inicial
        setCenter(new CiudadView());
    }

    private VBox createSideBar() {
        VBox menu = new VBox();
        menu.getStyleClass().add("sidebar-moderno");
        menu.setPrefWidth(250);

        // --- 1. HEADER (Logo y Botón Hamburguesa) ---
        HBox header = new HBox();
        header.getStyleClass().add("header-sidebar");
        header.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("MediCore");
        logo.getStyleClass().add("label-logo");
        HBox.setHgrow(logo, Priority.ALWAYS);
        logo.setMaxWidth(Double.MAX_VALUE);

        Button btnToggle = new Button("☰");
        btnToggle.getStyleClass().add("button-colapsar");
        btnToggle.setOnAction(e -> toggleSidebar(logo));

        header.getChildren().addAll(logo, btnToggle);

        // --- 2. BOTONES DEL MENÚ ---
        menuContainer.setSpacing(5);
        menuContainer.setPadding(new Insets(15, 0, 15, 0));

        Button btnCiudades     = crearBotonMenu("🏙️", "Ciudades");
        Button btnHospitales   = crearBotonMenu("🏥", "Hospitales");
        Button btnMedicos      = crearBotonMenu("👨‍⚕️", "Médicos");
        Button btnAsignaciones = crearBotonMenu("📅", "Asignaciones");
        Button btnPacientes    = crearBotonMenu("🤕", "Pacientes");
        Button btnServicios    = crearBotonMenu("🩺", "Servicios");
        Button btnHistorial    = crearBotonMenu("📋", "Historial Clínico");
        Button btnCostos       = crearBotonMenu("🏢", "Liquidación EPS");
        Button btnFacturacion  = crearBotonMenu("💵", "Caja Pacientes");
        Button btnReportes     = crearBotonMenu("📊", "Reportes BI");

        // Eventos y ruteo
        btnCiudades.setOnAction(e -> { activarBoton(btnCiudades); setCenter(new CiudadView()); });
        btnHospitales.setOnAction(e -> { activarBoton(btnHospitales); mostrarHospitales(); });
        btnMedicos.setOnAction(e -> { activarBoton(btnMedicos); setCenter(new MedicoView()); });
        btnAsignaciones.setOnAction(e -> { activarBoton(btnAsignaciones); mostrarAsignaciones(); });
        btnPacientes.setOnAction(e -> { activarBoton(btnPacientes); mostrarUsuarios(); });
        btnServicios.setOnAction(e -> { activarBoton(btnServicios); setCenter(new ServicioView()); });
        btnHistorial.setOnAction(e -> { activarBoton(btnHistorial); setCenter(new HistorialView()); });
        btnCostos.setOnAction(e -> { activarBoton(btnCostos); setCenter(new CostoView()); });
        btnFacturacion.setOnAction(e -> { activarBoton(btnFacturacion); setCenter(new CajaView()); });
        btnReportes.setOnAction(e -> { activarBoton(btnReportes); setCenter(new ReporteView()); });

        // Marcar el primero como activo por defecto
        activarBoton(btnCiudades);

        // Agregamos los botones al contenedor
        menuContainer.getChildren().addAll(
                btnCiudades, btnHospitales, btnMedicos, btnAsignaciones,
                btnPacientes, btnServicios, btnHistorial, btnCostos, btnFacturacion, btnReportes
        );

        menu.getChildren().addAll(header, menuContainer);
        return menu;
    }

    // Helper: Crea los botones con Icono y Texto separados
    private Button crearBotonMenu(String iconoUnicode, String texto) {
        Button btn = new Button(texto);
        Label iconLabel = new Label(iconoUnicode);
        iconLabel.getStyleClass().add("icon-menu");
        
        btn.setGraphic(iconLabel); // El icono va como un gráfico
        btn.getStyleClass().add("button-menu");
        btn.setUserData(texto); // Guardamos el texto original
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    // Helper: Controla el estado visual "Activo"
    private void activarBoton(Button btnSeleccionado) {
        if (botonActivo != null) {
            botonActivo.getStyleClass().remove("active");
        }
        btnSeleccionado.getStyleClass().add("active");
        botonActivo = btnSeleccionado;
    }

    // Helper: Colapsa o expande el panel
    private void toggleSidebar(Label logo) {
        expandido = !expandido;
        sideBar.setPrefWidth(expandido ? 250 : 70);
        logo.setVisible(expandido);
        logo.setManaged(expandido); 

        for (javafx.scene.Node node : menuContainer.getChildren()) {
            if (node instanceof Button btn) {
                if (expandido) {
                    btn.setText((String) btn.getUserData());
                } else {
                    btn.setText("");
                }
            }
        }
    }

    // -- Métodos de navegación  --
    private void mostrarHospitales() {
        setCenter(new HospitalView(codigo -> setCenter(new HospitalDetalleView(codigo, this::mostrarHospitales))));
    }
    private void mostrarUsuarios(){
        setCenter(new UsuarioView(codigo -> setCenter(new CitasUsuarioView(codigo, this::mostrarUsuarios))));
    }
    private void mostrarAsignaciones() {
        setCenter(new AsignacionView(medico -> setCenter(new AsignacionesMedicoView(medico, this::mostrarAsignaciones))));
    }
}