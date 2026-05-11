package views.ciudad;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.layout.*;

import models.Ciudad;

/*
 * Vista principal del módulo de ciudades.
 *
 * Aquí mostramos:
 * - encabezado
 * - buscador
 * - tabla de ciudades
 * - acciones
 * - botón nueva ciudad
 */

public class CiudadView extends VBox {

    // =========================
    // COMPONENTES
    // =========================

    private Label titulo;

    private Label subtitulo;

    private TextField buscarCiudad;

    private Button nuevaCiudad;

    private TableView<Ciudad> tablaCiudades;

    // Lista principal

    private ObservableList<Ciudad> listaCiudades;

    // Lista filtrada

    private FilteredList<Ciudad> filtroCiudades;

    /*
     * Constructor principal.
     */

    public CiudadView() {

        iniciarComponentes();

        crearColumnas();

        agregarDatosIniciales();

        configurarLayout();

        registrarEventos();

        cargarEstilos();
    }

    /*
     * Inicializamos componentes.
     */

    private void iniciarComponentes() {

        titulo = new Label("Gestion de Ciudades");

        titulo.getStyleClass().add("titulo");

        subtitulo = new Label(
                "Administración de ciudades de la red hospitalaria"
        );

        subtitulo.getStyleClass().add("subtitulo");

        // Campo buscador

        buscarCiudad = new TextField();

        buscarCiudad.setPromptText(
                "Buscar Ciudad"
        );

        buscarCiudad.getStyleClass()
                .add("buscar");

        // Botón nueva ciudad

        nuevaCiudad = new Button(
                "+ Nueva Ciudad"
        );

        nuevaCiudad.getStyleClass()
                .add("btn-nueva");

        // Tabla

        tablaCiudades = new TableView<>();

        tablaCiudades.getStyleClass()
                .add("tabla");

        tablaCiudades.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        // Inicializamos listas

        listaCiudades =
                FXCollections.observableArrayList();

        filtroCiudades =
                new FilteredList<>(
                        listaCiudades,
                        b -> true
                );
    }

    /*
     * Creamos las columnas de la tabla.
     */

    private void crearColumnas() {

        // Columna código

        TableColumn<Ciudad, String> codigo =
                new TableColumn<>("Codigo");

        codigo.setCellValueFactory(
                new PropertyValueFactory<>("codigo")
        );

        // Columna nombre

        TableColumn<Ciudad, String> nombre =
                new TableColumn<>("Nombre Ciudad");

        nombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre")
        );

        // Columna departamento

        TableColumn<Ciudad, String> departamento =
                new TableColumn<>("Departamento");

        departamento.setCellValueFactory(
                new PropertyValueFactory<>("departamento")
        );

        // Columna país

        TableColumn<Ciudad, String> pais =
                new TableColumn<>("Pais");

        pais.setCellValueFactory(
                new PropertyValueFactory<>("pais")
        );

        // Columna hospitales

        TableColumn<Ciudad, Integer> hospitales =
                new TableColumn<>("Hospitales");

        hospitales.setCellValueFactory(
                new PropertyValueFactory<>("hospitales")
        );

        // Columna acciones

        TableColumn<Ciudad, Void> acciones =
                crearColumnaAcciones();

        tablaCiudades.getColumns().addAll(
                codigo,
                nombre,
                departamento,
                pais,
                hospitales,
                acciones
        );

        // Ordenamiento

        SortedList<Ciudad> sortedData =
                new SortedList<>(filtroCiudades);

        sortedData.comparatorProperty().bind(
                tablaCiudades.comparatorProperty()
        );

        tablaCiudades.setItems(sortedData);
    }

    /*
     * Creamos la columna de acciones.
     */

    private TableColumn<Ciudad, Void>
    crearColumnaAcciones() {

        TableColumn<Ciudad, Void> acciones =
                new TableColumn<>("Acciones");

        acciones.setCellFactory(columna ->
                new TableCell<>() {

                    private final Button editar =
                            new Button("✎");

                    {

                        editar.getStyleClass()
                                .add("btn-editar");

                        editar.setOnAction(e -> {

                            Ciudad ciudad =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            // Abrimos ventana de acciones

                            AccionCiudad modal =
                                    new AccionCiudad(ciudad);

                            modal.showAndWait();

                            // Refrescamos tabla

                            tablaCiudades.refresh();
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty
                    ) {

                        super.updateItem(item, empty);

                        if (empty) {

                            setGraphic(null);

                        } else {

                            setGraphic(editar);
                        }
                    }
                });

        return acciones;
    }

    /*
     * Datos iniciales de ejemplo.
     */

    private void agregarDatosIniciales() {

        listaCiudades.add(
                new Ciudad(
                        "COL-001",
                        "Medellin",
                        "Antioquia",
                        "Colombia",
                        30
                )
        );

        listaCiudades.add(
                new Ciudad(
                        "COL-002",
                        "Bogota",
                        "Cundinamarca",
                        "Colombia",
                        42
                )
        );
    }

    /*
     * Organización visual de la pantalla.
     */

    private void configurarLayout() {

        // Encabezado

        VBox header = new VBox(
                titulo,
                subtitulo
        );

        header.getStyleClass()
                .add("header-superior");

        header.setSpacing(5);

        header.setPadding(
                new Insets(20, 40, 20, 40)
        );

        // Barra superior

        HBox barraSuperior = new HBox();

        barraSuperior.setAlignment(
                Pos.CENTER_LEFT
        );

        barraSuperior.setSpacing(20);

        Region espacio = new Region();

        HBox.setHgrow(
                espacio,
                Priority.ALWAYS
        );

        barraSuperior.getChildren().addAll(
                buscarCiudad,
                espacio,
                nuevaCiudad
        );

        // Tabla

        VBox contenedorTabla =
                new VBox(tablaCiudades);

        contenedorTabla.getStyleClass()
                .add("contenedor-tabla");

        // Contenido

        VBox contenido = new VBox(
                barraSuperior,
                contenedorTabla
        );

        contenido.setSpacing(25);

        contenido.setPadding(
                new Insets(20)
        );

        // Agregamos todo

        getChildren().addAll(
                header,
                contenido
        );
    }

    /*
     * Eventos principales.
     */

    private void registrarEventos() {

        registrarNuevaCiudad();

        registrarBuscador();
    }

    /*
     * Evento del botón nueva ciudad.
     */

    private void registrarNuevaCiudad() {

        nuevaCiudad.setOnAction(e -> {

            NuevaCiudad modal =
                    new NuevaCiudad(listaCiudades);

            modal.showAndWait();
        });
    }

    /*
     * Evento del buscador.
     */

    private void registrarBuscador() {

        buscarCiudad.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    filtroCiudades.setPredicate(ciudad -> {

                        // Si está vacío mostramos todo

                        if (newValue == null
                                || newValue.isEmpty()) {

                            return true;
                        }

                        String texto =
                                newValue.toLowerCase();

                        // Buscar por nombre

                        if (ciudad.getNombre()
                                .toLowerCase()
                                .contains(texto)) {

                            return true;
                        }

                        // Buscar por código

                        if (ciudad.getCodigo()
                                .toLowerCase()
                                .contains(texto)) {

                            return true;
                        }

                        // Buscar por departamento

                        if (ciudad.getDepartamento()
                                .toLowerCase()
                                .contains(texto)) {

                            return true;
                        }

                        // Buscar por país

                        return ciudad.getPais()
                                .toLowerCase()
                                .contains(texto);
                    });
                }
        );
    }

    /*
     * Cargamos la hoja de estilos.
     */

    private void cargarEstilos() {

        getStyleClass().add("ciudad-view");

        getStylesheets().add(
                getClass()
                        .getResource(
                                "/styles/ciudad/ciudad.css"
                        )
                        .toExternalForm()
        );
    }
}
