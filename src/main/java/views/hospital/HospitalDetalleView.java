package views.hospital;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.AreaInternaModel;
import services.HospitalService;
import views.areaInterna.AreaFila;
import views.common.Toast;
import views.common.Validacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista de detalle de un hospital.
 * Muestra la información completa del hospital, su estado y las áreas internas
 * que lo conforman. Permite crear y editar áreas, y filtrarlas por tipo.
 */
public class HospitalDetalleView extends VBox {

    private final String codigoHospital;
    private final HospitalService hospitalService = new HospitalService();
    private final Runnable onVolver;

    private List<AreaFila> todasLasAreas = new ArrayList<>();

    private Label nombreHospital;
    private Label subtitulo;
    private Button btnVolver;
    private Button btnNuevaArea;
    private Label lblTelefono;
    private Label lblEstado;
    private Label lblTituloAreas;
    private ComboBox<String> cmbTipo;
    private VBox cardEstado;
    private TableView<AreaFila> tablaAreas;

    /**
     * Constructor principal que recibe el código del hospital y una acción para volver.
     *
     * @param codigoHospital Código único del hospital a mostrar.
     * @param onVolver       Acción que se ejecuta al pulsar el botón "Volver".
     */
    public HospitalDetalleView(String codigoHospital, Runnable onVolver) {
        this.codigoHospital = codigoHospital;
        this.onVolver = onVolver;
        iniciarComponentes();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
    }

    /**
     * Constructor alternativo sin acción de volver (compatibilidad).
     *
     * @param codigoHospital Código único del hospital a mostrar.
     */
    public HospitalDetalleView(String codigoHospital) {
        this(codigoHospital, null);
    }

    /**
     * Inicializa los componentes visuales y dispara la carga de datos desde el servidor.
     */
    private void iniciarComponentes() {
        nombreHospital = new Label("Cargando...");
        nombreHospital.getStyleClass().add("nombre-hospital");

        subtitulo = new Label("");
        subtitulo.getStyleClass().add("subtitulo");

        btnVolver = new Button("← Volver a hospitales");
        btnVolver.setId("btnVolver");

        btnNuevaArea = new Button("+ Nueva Área");
        btnNuevaArea.setId("btnNuevaArea");

        lblTelefono = new Label("...");
        lblEstado   = new Label("...");

        lblTituloAreas = new Label("Áreas del Hospital");
        lblTituloAreas.getStyleClass().add("titulo-areas");

        cmbTipo = new ComboBox<>();
        cmbTipo.setPromptText("Tipo: Todos");
        cmbTipo.setId("cmbTipo");

        tablaAreas = crearTablaAreas();

        cargarInfoHospital();
        cargarAreas();
    }

    /**
     * Carga la información general del hospital desde el servidor en un hilo secundario
     * y actualiza los componentes visuales en el hilo de JavaFX.
     */
    private void cargarInfoHospital() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                var h = hospitalService.getById(codigoHospital);
                javafx.application.Platform.runLater(() -> {
                    nombreHospital.setText(h.nombre);
                    subtitulo.setText("Colombia > " + h.nombreCiudad + " > " + h.nombre);
                    lblTelefono.setText(h.telefono);

                    boolean activo = Boolean.TRUE.equals(h.estado);
                    lblEstado.setText(activo ? "Activo" : "Inactivo");
                    lblEstado.getStyleClass().removeAll("valor-activo", "valor-inactivo");
                    lblEstado.getStyleClass().add(activo ? "valor-activo" : "valor-inactivo");
                    if (cardEstado != null) {
                        cardEstado.getStyleClass().removeAll("card-estado-activo", "card-estado-inactivo");
                        cardEstado.getStyleClass().add(activo ? "card-estado-activo" : "card-estado-inactivo");
                    }
                });
                return null;
            }
        };
        task.setOnFailed(e -> mostrarError("No se pudo cargar la información del hospital."));
        new Thread(task).start();
    }

    /**
     * Carga las áreas internas del hospital desde el servidor en un hilo secundario.
     * Al terminar, actualiza la tabla, el contador de áreas y el filtro por tipo.
     */
    private void cargarAreas() {
        Task<List<AreaFila>> task = new Task<>() {
            @Override
            protected List<AreaFila> call() throws Exception {
                return hospitalService.getAreas(codigoHospital).stream()
                        .map(a -> new AreaFila(
                                a.codigo,
                                a.nombre,
                                a.nombreAreaInterna,
                                a.descripcion != null ? a.descripcion : "",
                                a.codigoAreaInterna))
                        .toList();
            }
        };
        task.setOnSucceeded(e -> {
            todasLasAreas = new ArrayList<>(task.getValue());
            tablaAreas.setItems(FXCollections.observableArrayList(todasLasAreas));
            lblTituloAreas.setText("Áreas del Hospital (" + todasLasAreas.size() + ")");
            List<String> tipos = todasLasAreas.stream()
                    .map(AreaFila::getTipo)
                    .distinct()
                    .sorted()
                    .toList();
            cmbTipo.getItems().setAll(tipos);
        });
        task.setOnFailed(e -> mostrarError("No se pudieron cargar las áreas del hospital."));
        new Thread(task).start();
    }

    /**
     * Crea y configura la tabla de áreas internas con sus columnas y acciones.
     *
     * @return TableView configurado con las columnas de áreas.
     */
    private TableView<AreaFila> crearTablaAreas() {
        TableView<AreaFila> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(new Label("Este hospital aún no tiene áreas registradas."));

        TableColumn<AreaFila, String> colNombre = new TableColumn<>("Área");
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());

        TableColumn<AreaFila, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> c.getValue().tipoProperty());

        TableColumn<AreaFila, String> colDescripcion = new TableColumn<>("Descripción");
        colDescripcion.setCellValueFactory(c -> c.getValue().descripcionProperty());

        TableColumn<AreaFila, Void> colAcciones = new TableColumn<>("Acción");
        colAcciones.setMaxWidth(80);
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = crearBtnIcono("✎", "btn-icono", "btn-editar");
            private final HBox contenedor  = new HBox(5, btnEditar);
            {
                contenedor.setAlignment(Pos.CENTER_LEFT);
                btnEditar.setOnAction(e -> {
                    AreaFila f = getTableView().getItems().get(getIndex());
                    abrirDialogoEditarArea(f);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });

        tv.getColumns().addAll(colNombre, colTipo, colDescripcion, colAcciones);
        return tv;
    }

    /**
     * Crea un botón con ícono y clases CSS aplicadas.
     *
     * @param icono  Texto o símbolo que aparece en el botón.
     * @param clases Clases CSS a aplicar.
     * @return Botón configurado.
     */
    private Button crearBtnIcono(String icono, String... clases) {
        Button btn = new Button(icono);
        btn.getStyleClass().addAll(clases);
        return btn;
    }

    /**
     * Abre un diálogo para editar los datos de un área existente.
     * Al confirmar, envía los cambios al servidor y recarga la tabla.
     *
     * @param fila Fila del área a editar.
     */
    private void abrirDialogoEditarArea(AreaFila fila) {
        Dialog<HospitalService.AreaUpdateBody> dialog = new Dialog<>();
        dialog.setTitle("Editar Área");
        dialog.setHeaderText("Editando: " + fila.getNombre());
        dialog.getDialogPane().getStyleClass().add("dialogo-hospital");

        TextField txtNombre         = campoTexto(fila.getNombre(),            "Nombre del área");
        TextField txtDescripcion    = campoTexto(fila.getDescripcion(),       "Descripción (opcional)");
        TextField txtCodAreaInterna = campoTexto(fila.getCodigoAreaInterna(), "Código del tipo de área");

        Label lblErrores = errorLabel();

        GridPane grid = formularioGrid();
        agregarFila(grid, 0, "Nombre",        txtNombre);
        agregarFila(grid, 1, "Descripción",   txtDescripcion);
        agregarFila(grid, 2, "Tipo (código)", txtCodAreaInterna);
        grid.add(lblErrores, 0, 3, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(520, 420);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Guardar Área");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String err = validarAreaForm(txtNombre, txtDescripcion, txtCodAreaInterna);
            if (err != null) { mostrarErroresInline(lblErrores, err); ev.consume(); }
        });

        dialog.setResultConverter(btn -> btn == ButtonType.OK
                ? new HospitalService.AreaUpdateBody(
                Validacion.texto(txtNombre), Validacion.texto(txtDescripcion),
                Validacion.texto(txtCodAreaInterna))
                : null);

        cargarEstilosEn(dialog);
        dialog.showAndWait().ifPresent(body -> {
            Task<AreaInternaModel> task = new Task<>() {
                @Override
                protected AreaInternaModel call() throws Exception {
                    return hospitalService.updateArea(codigoHospital, fila.getCodigo(), body);
                }
            };
            task.setOnSucceeded(e -> {
                Toast.success(this, "Área '" + body.nombre() + "' actualizada correctamente");
                cargarAreas();
            });
            task.setOnFailed(e -> {
                Toast.error(this, "Error al actualizar el área");
                mostrarError("Error al actualizar el área.");
            });
            new Thread(task).start();
        });
    }

    /**
     * Abre un diálogo para registrar una nueva área en el hospital.
     * Al confirmar, envía los datos al servidor y recarga la tabla.
     */
    private void abrirDialogoNuevaArea() {
        Dialog<HospitalService.AreaCreateBody> dialog = new Dialog<>();
        dialog.setTitle("Nueva Área");
        dialog.setHeaderText("Registrar nueva área en el hospital");
        dialog.getDialogPane().getStyleClass().add("dialogo-hospital");

        TextField txtCodigo         = campoTexto("", "Ej: AREA-004");
        TextField txtNombre         = campoTexto("", "Ej: Consultorio 102");
        TextField txtDescripcion    = campoTexto("", "Descripción (opcional)");
        TextField txtCodAreaInterna = campoTexto("", "Código del tipo de área");

        Label lblErrores = errorLabel();

        GridPane grid = formularioGrid();
        agregarFila(grid, 0, "Código",        txtCodigo);
        agregarFila(grid, 1, "Nombre",        txtNombre);
        agregarFila(grid, 2, "Descripción",   txtDescripcion);
        agregarFila(grid, 3, "Tipo (código)", txtCodAreaInterna);
        grid.add(lblErrores, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(520, 470);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Crear área");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            StringBuilder sb = new StringBuilder();
            String e1 = Validacion.requerido("Código", Validacion.texto(txtCodigo));
            String e2 = Validacion.formatoCodigo("Código", Validacion.texto(txtCodigo));
            String e3 = codigoAreaYaExiste(Validacion.texto(txtCodigo));
            Validacion.marcarInvalido(txtCodigo, e1 != null || e2 != null || e3 != null);
            if (e1 != null) sb.append(e1).append("\n");
            else if (e2 != null) sb.append(e2).append("\n");
            else if (e3 != null) sb.append(e3).append("\n");
            String resto = validarAreaForm(txtNombre, txtDescripcion, txtCodAreaInterna);
            if (resto != null) sb.append(resto);
            if (!sb.isEmpty()) { mostrarErroresInline(lblErrores, sb.toString().trim()); ev.consume(); }
        });

        dialog.setResultConverter(btn -> btn == ButtonType.OK
                ? new HospitalService.AreaCreateBody(
                Validacion.texto(txtCodigo), Validacion.texto(txtNombre),
                Validacion.texto(txtDescripcion), Validacion.texto(txtCodAreaInterna))
                : null);

        cargarEstilosEn(dialog);
        dialog.showAndWait().ifPresent(body -> {
            Task<AreaInternaModel> task = new Task<>() {
                @Override
                protected AreaInternaModel call() throws Exception {
                    return hospitalService.createArea(codigoHospital, body);
                }
            };
            task.setOnSucceeded(e -> {
                Toast.success(this, "Área '" + body.nombre() + "' creada correctamente");
                cargarAreas();
            });
            task.setOnFailed(e -> {
                Toast.error(this, "Error al crear el área");
                mostrarError("Error al crear el área.");
            });
            new Thread(task).start();
        });
    }

    /**
     * Organiza todos los componentes dentro del layout de la vista.
     */
    private void configurarLayout() {
        VBox infoNombre = new VBox(3, nombreHospital, subtitulo);
        Region spacerHeader = new Region();
        HBox.setHgrow(spacerHeader, Priority.ALWAYS);
        HBox header = new HBox(12, btnVolver, infoNombre, spacerHeader, btnNuevaArea);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header-detalle");

        VBox cardTelefono = crearCard("Teléfono", lblTelefono, false);
        cardEstado        = crearCard("Estado",   lblEstado,   true);
        HBox.setHgrow(cardTelefono, Priority.ALWAYS);
        HBox.setHgrow(cardEstado,   Priority.ALWAYS);
        HBox infoCards = new HBox(12, cardTelefono, cardEstado);

        Region spacerTabla = new Region();
        HBox.setHgrow(spacerTabla, Priority.ALWAYS);
        HBox barraAreas = new HBox(lblTituloAreas, spacerTabla, cmbTipo);
        barraAreas.setAlignment(Pos.CENTER_LEFT);

        setSpacing(16);
        setPadding(new Insets(24));
        getChildren().addAll(header, infoCards, barraAreas, tablaAreas);
        VBox.setVgrow(tablaAreas, Priority.ALWAYS);
    }

    /**
     * Crea una tarjeta de información con etiqueta y valor.
     *
     * @param etiqueta Texto descriptivo del campo.
     * @param valor    Label con el valor a mostrar.
     * @param esEstado Indica si la tarjeta corresponde al estado (aplica estilos especiales).
     * @return VBox con la tarjeta armada.
     */
    private VBox crearCard(String etiqueta, Label valor, boolean esEstado) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("card-etiqueta");
        VBox card = new VBox(5, lbl, valor);
        card.getStyleClass().add("card-info");
        if (esEstado) card.getStyleClass().add("card-estado");
        return card;
    }

    /**
     * Registra los eventos de los componentes interactivos de la vista.
     */
    private void registrarEventos() {
        btnNuevaArea.setOnAction(e -> abrirDialogoNuevaArea());

        btnVolver.setOnAction(e -> {
            if (onVolver != null) onVolver.run();
        });

        cmbTipo.setOnAction(e -> {
            String tipo = cmbTipo.getValue();
            if (tipo == null) {
                tablaAreas.setItems(FXCollections.observableArrayList(todasLasAreas));
            } else {
                tablaAreas.setItems(FXCollections.observableArrayList(
                        todasLasAreas.stream()
                                .filter(a -> tipo.equals(a.getTipo()))
                                .toList()));
            }
        });
    }

    /**
     * Crea un campo de texto con valor inicial y texto de ayuda.
     *
     * @param inicial Valor inicial del campo.
     * @param prompt  Texto de ayuda que aparece cuando el campo está vacío.
     * @return TextField configurado.
     */
    private TextField campoTexto(String inicial, String prompt) {
        TextField tf = new TextField(inicial == null ? "" : inicial);
        tf.setPromptText(prompt);
        tf.getStyleClass().add("campo-form");
        tf.setPrefHeight(36);
        return tf;
    }

    /**
     * Crea un GridPane base con dos columnas para los formularios de la vista.
     *
     * @return GridPane configurado con espaciado y restricciones de columna.
     */
    private GridPane formularioGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        grid.setPadding(new Insets(22, 24, 18, 24));
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(130); c0.setHalignment(HPos.RIGHT);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS); c1.setFillWidth(true);
        grid.getColumnConstraints().addAll(c0, c1);
        return grid;
    }

    /**
     * Agrega una fila con etiqueta y control al GridPane del formulario.
     *
     * @param grid     GridPane destino.
     * @param fila     Índice de la fila donde se inserta.
     * @param etiqueta Texto descriptivo del campo.
     * @param control  Componente de entrada a mostrar.
     */
    private void agregarFila(GridPane grid, int fila, String etiqueta, javafx.scene.Node control) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("etiqueta-form");
        grid.add(lbl, 0, fila);
        grid.add(control, 1, fila);
        if (control instanceof Region r) GridPane.setHgrow(r, Priority.ALWAYS);
    }

    /**
     * Crea un Label oculto para mostrar errores de validación dentro del formulario.
     *
     * @return Label configurado para mensajes de error.
     */
    private Label errorLabel() {
        Label l = new Label();
        l.getStyleClass().add("errores-form");
        l.setVisible(false); l.setManaged(false);
        l.setWrapText(true);
        return l;
    }

    /**
     * Valida los campos comunes del formulario de área (nombre, descripción y tipo).
     *
     * @param txtNombre         Campo del nombre del área.
     * @param txtDescripcion    Campo de la descripción.
     * @param txtCodAreaInterna Campo del código del tipo de área.
     * @return Mensaje de error si hay problemas, o {@code null} si todo es válido.
     */
    private String validarAreaForm(TextField txtNombre, TextField txtDescripcion,
                                   TextField txtCodAreaInterna) {
        Validacion.limpiarEstado(txtNombre, txtDescripcion, txtCodAreaInterna);
        StringBuilder sb = new StringBuilder();
        String e;

        e = Validacion.requerido("Nombre", Validacion.texto(txtNombre));
        if (e == null) e = Validacion.longitudMax("Nombre", Validacion.texto(txtNombre), 100);
        Validacion.marcarInvalido(txtNombre, e != null);
        if (e != null) sb.append(e).append("\n");

        e = Validacion.longitudMax("Descripción", Validacion.texto(txtDescripcion), 250);
        Validacion.marcarInvalido(txtDescripcion, e != null);
        if (e != null) sb.append(e).append("\n");

        e = Validacion.requerido("Tipo (código)", Validacion.texto(txtCodAreaInterna));
        if (e == null) e = Validacion.formatoCodigo("Tipo (código)", Validacion.texto(txtCodAreaInterna));
        Validacion.marcarInvalido(txtCodAreaInterna, e != null);
        if (e != null) sb.append(e).append("\n");

        return sb.length() == 0 ? null : sb.toString().trim();
    }

    /**
     * Verifica si ya existe un área con el código ingresado en la lista actual.
     *
     * @param codigo Código a verificar.
     * @return Mensaje de error si ya existe, o {@code null} si está disponible.
     */
    private String codigoAreaYaExiste(String codigo) {
        if (codigo == null || codigo.isBlank()) return null;
        boolean existe = todasLasAreas.stream()
                .anyMatch(a -> codigo.equalsIgnoreCase(a.getCodigo()));
        return existe ? "• Ya existe un área con el código '" + codigo + "'" : null;
    }

    /**
     * Muestra un mensaje de error en el Label de errores del formulario.
     *
     * @param lbl     Label donde se muestra el error.
     * @param mensaje Texto del error a mostrar.
     */
    private void mostrarErroresInline(Label lbl, String mensaje) {
        lbl.setText(mensaje);
        lbl.setVisible(true); lbl.setManaged(true);
    }

    /**
     * Aplica los estilos CSS de esta vista al diálogo indicado.
     *
     * @param dialog Diálogo al que se le aplican los estilos.
     */
    private void cargarEstilosEn(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/hospital/hospitalDetalle.css").toExternalForm());
    }

    /**
     * Aplica los estilos CSS a la vista principal.
     */
    private void cargarEstilos() {
        getStyleClass().add("hospital-detalle-view");
        getStylesheets().add(
                getClass().getResource("/styles/hospital/hospitalDetalle.css").toExternalForm());
    }

    /**
     * Muestra un diálogo de error con el mensaje indicado.
     *
     * @param mensaje Texto del error a mostrar al usuario.
     */
    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait();
    }
}