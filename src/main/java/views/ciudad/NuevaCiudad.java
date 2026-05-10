package views.ciudad;

import javafx.collections.ObservableList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.scene.layout.*;

import javafx.stage.Modality;
import javafx.stage.Stage;

import models.Ciudad;

/*
 * Ventana utilizada para crear
 * nuevas ciudades.
 */

public class NuevaCiudad extends Stage {

    // =========================
    // COMPONENTES
    // =========================

    private TextField nombreCiudad;

    private ComboBox<String> departamento;

    private ComboBox<String> pais;

    private Button cancelar;

    private Button guardar;

    // Lista principal

    private final ObservableList<Ciudad> listaCiudades;

    /*
     * Constructor principal.
     */

    public NuevaCiudad(
            ObservableList<Ciudad> listaCiudades
    ) {

        this.listaCiudades = listaCiudades;

        iniciarComponentes();

        crearVentana();
    }

    /*
     * Inicializamos componentes.
     */

    private void iniciarComponentes() {

        nombreCiudad = new TextField();

        nombreCiudad.setPromptText(
                "Manizales"
        );

        // Departamento

        departamento = new ComboBox<>();

        departamento.getItems().addAll(
                "Antioquia",
                "Caldas",
                "Cundinamarca"
        );

        departamento.setValue("Antioquia");

        // País

        pais = new ComboBox<>();

        pais.getItems().addAll(
                "Colombia",
                "Mexico",
                "Argentina"
        );

        pais.setValue("Colombia");

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

        setTitle("Nueva Ciudad");

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
                new Label("Nueva Ciudad");

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
     * Eventos de botones.
     */

    private void registrarEventos() {

        cancelar.setOnAction(
                e -> close()
        );

        guardar.setOnAction(
                e -> guardarCiudad()
        );
    }

    /*
     * Guarda la ciudad en la tabla.
     */

    private void guardarCiudad() {

        String codigo =
                "COL-00" + (
                        listaCiudades.size() + 1
                );

        Ciudad nuevaCiudad =
                new Ciudad(
                        codigo,
                        nombreCiudad.getText(),
                        departamento.getValue(),
                        pais.getValue(),
                        0
                );

        listaCiudades.add(nuevaCiudad);

        close();
    }
}
