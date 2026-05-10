package views.ciudad;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.stage.Modality;
import javafx.stage.Stage;

import models.Ciudad;

/*
 * Ventana encargada de modificar
 * la información de una ciudad.
 */

public class AccionCiudad extends Stage {

    // =========================
    // COMPONENTES
    // =========================

    private TextField nombreCiudad;

    private ComboBox<String> departamento;

    private ComboBox<String> pais;

    private Button cancelar;

    private Button guardar;

    // Ciudad seleccionada

    private final Ciudad ciudad;

    /*
     * Constructor principal.
     */

    public AccionCiudad(
            Ciudad ciudad
    ) {

        this.ciudad = ciudad;

        iniciarComponentes();

        crearVentana();
    }

    /*
     * Inicializamos componentes.
     */

    private void iniciarComponentes() {

        // Campo nombre

        nombreCiudad = new TextField();

        nombreCiudad.setText(
                ciudad.getNombre()
        );

        // Departamento

        departamento = new ComboBox<>();

        departamento.getItems().addAll(
                "Antioquia",
                "Caldas",
                "Cundinamarca"
        );

        departamento.setValue(
                ciudad.getDepartamento()
        );

        // País

        pais = new ComboBox<>();

        pais.getItems().addAll(
                "Colombia",
                "Mexico",
                "Argentina"
        );

        pais.setValue(
                ciudad.getPais()
        );

        // Botones

        cancelar = new Button("Cancelar");

        cancelar.getStyleClass()
                .add("btn-cancelar");

        guardar = new Button(
                "Guardar Ciudad"
        );

        guardar.getStyleClass()
                .add("btn-guardar");

        registrarEventos();
    }

    /*
     * Creamos la ventana.
     */

    private void crearVentana() {

        BorderPane root = new BorderPane();

        root.setPadding(
                new Insets(25)
        );

        VBox contenido =
                crearContenido();

        HBox botones =
                crearBotones();

        root.setCenter(contenido);

        root.setBottom(botones);

        Scene scene = new Scene(
                root,
                700,
                350
        );

        scene.getStylesheets().add(
                getClass()
                        .getResource(
                                "/styles/ciudad/ciudad.css"
                        )
                        .toExternalForm()
        );

        initModality(
                Modality.APPLICATION_MODAL
        );

        setTitle("Accion Ciudad");

        setScene(scene);
    }

    /*
     * Contenido principal.
     */

    private VBox crearContenido() {

        VBox contenido = new VBox();

        contenido.setSpacing(20);

        // Título

        Label titulo =
                new Label("Editar Ciudad");

        titulo.getStyleClass()
                .add("modal-titulo");

        // Labels

        Label nombreLabel =
                new Label("Nombre de la Ciudad");

        Label depaLabel =
                new Label("Departamento");

        Label paisLabel =
                new Label("Pais");

        // Combos

        HBox combos = new HBox(
                20,

                new VBox(
                        10,
                        depaLabel,
                        departamento
                ),

                new VBox(
                        10,
                        paisLabel,
                        pais
                )
        );

        contenido.getChildren().addAll(
                titulo,
                nombreLabel,
                nombreCiudad,
                combos
        );

        return contenido;
    }

    /*
     * Parte inferior con botones.
     */

    private HBox crearBotones() {

        HBox botones = new HBox(
                15,
                cancelar,
                guardar
        );

        botones.setAlignment(
                Pos.CENTER_RIGHT
        );

        botones.setPadding(
                new Insets(20, 0, 0, 0)
        );

        return botones;
    }

    /*
     * Eventos principales.
     */

    private void registrarEventos() {

        cancelar.setOnAction(
                e -> close()
        );

        guardar.setOnAction(
                e -> guardarCambios()
        );
    }

    /*
     * Guarda cambios de la ciudad.
     */

    private void guardarCambios() {

        ciudad.setNombre(
                nombreCiudad.getText()
        );

        ciudad.setDepartamento(
                departamento.getValue()
        );

        ciudad.setPais(
                pais.getValue()
        );

        close();
    }
}
