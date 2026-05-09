package views.ciudad;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class CiudadView extends VBox {

    private Label titulo;
    private TextField nombre;
    private Button crear;

    public CiudadView() {
        iniciarComponentes();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
    }

    private void iniciarComponentes() {
        //creamos un label y le adicionamos una clase referenciada en su clase de estilos
        titulo = new Label("hola, introduce tu nombre");
        titulo.getStyleClass().add("titulo");

        //Creamos un campo para ingresar datos y le adicionamos una clase referenciada en su clase de estilos
        nombre = new TextField();
        nombre.getStyleClass().add("nombre");

        //Creamos un boton con su clase para su hoja de estilos
        crear = new Button("Crear");
        crear.setId("crear");
    }

    private void configurarLayout() {
        setSpacing(20);
        getChildren().addAll(
                titulo,
                nombre,
                crear
        );
    }

    private void registrarEventos() {
        crear.setOnAction(e -> {
            System.out.println("Creando evento");
        });
    }

    private void cargarEstilos() {
        getStyleClass().add("ciudad-view");
        getStylesheets().add(
                 getClass()
                .getResource("/styles/ciudad/ciudad.css")
                .toExternalForm()
        );
    }
}
