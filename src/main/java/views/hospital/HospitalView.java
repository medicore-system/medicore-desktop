package views.hospital;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.HospitalModel;
import services.HospitalService;
import views.common.Toast;
import views.common.Validacion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class HospitalView extends VBox {

    private final Consumer<String> onVerHospital;
    private final HospitalService hospitalService = new HospitalService();

    private List<HospitalFila> todosLosHospitales = new ArrayList<>();
    private List<CiudadHospitalModel>  ciudadesCache      = new ArrayList<>();

    private Label titulo;
    private TextField buscador;
    private Label lblContador;
    private Button btnNuevoHospital;
    private TableView<HospitalFila> tabla;

    public HospitalView(Consumer<String> onVerHospital) {
        this.onVerHospital = onVerHospital;
        iniciarComponentes();
        configurarLayout();
        registrarEventos();
        cargarEstilos();
    }

    private void iniciarComponentes() {
        titulo = new Label("Gestión de Hospitales");
        titulo.getStyleClass().add("titulo");

        buscador = new TextField();
        buscador.setPromptText("🔍  Buscar hospital...");
        buscador.setId("buscador");

        lblContador = new Label("");
        lblContador.getStyleClass().add("contador-resultados");

        btnNuevoHospital = new Button("+ Nuevo Hospital");
        btnNuevoHospital.setId("btnNuevoHospital");

        tabla = crearTabla();
        cargarHospitales();
        cargarCiudades();
    }

    private void cargarHospitales() {
        Task<List<HospitalFila>> task = new Task<>() {
            @Override
            protected List<HospitalFila> call() throws Exception {
                return hospitalService.getAll().stream()
                        .map(h -> new HospitalFila(
                                h.codigo,
                                h.nombre,
                                h.direccion != null ? h.direccion : "",
                                h.nombreCiudad,
                                h.telefono,
                                Boolean.TRUE.equals(h.estado) ? "Activo" : "Inactivo",
                                h.codigoCiudad))
                        .toList();
            }
        };
        task.setOnSucceeded(e -> {
            todosLosHospitales = new ArrayList<>(task.getValue());
            aplicarBusqueda(buscador.getText());
        });
        task.setOnFailed(e ->
                mostrarError("No se pudieron cargar los hospitales.\n" +
                             "Verifica que el servidor esté corriendo en localhost:8080."));
        new Thread(task).start();
    }

    private void cargarCiudades() {
        Task<List<CiudadHospitalModel>> task = new Task<>() {
            @Override
            protected List<CiudadHospitalModel> call() throws Exception {
                return hospitalService.getCiudades();
            }
        };
        task.setOnSucceeded(e -> ciudadesCache = new ArrayList<>(task.getValue()));
        new Thread(task).start();
    }

    private TableView<HospitalFila> crearTabla() {
        TableView<HospitalFila> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(new Label("No hay hospitales que coincidan con la búsqueda."));

        TableColumn<HospitalFila, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(c -> c.getValue().codigoProperty());
        colCodigo.setMaxWidth(90);
        colCodigo.setMinWidth(80);

        TableColumn<HospitalFila, Void> colNombre = new TableColumn<>("Hospital");
        colNombre.setPrefWidth(220);
        colNombre.setCellFactory(col -> new TableCell<>() {
            private final Label lblNombre = new Label();
            private final Label lblDir    = new Label();
            private final Label lblMatch  = new Label();
            private final VBox  contenido = new VBox(2, lblNombre, lblDir, lblMatch);
            {
                lblNombre.getStyleClass().add("hospital-nombre-celda");
                lblDir.getStyleClass().add("hospital-dir-celda");
                lblMatch.getStyleClass().add("hospital-match-celda");
                contenido.setPadding(new Insets(4, 0, 4, 0));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    HospitalFila f = getTableView().getItems().get(getIndex());
                    lblNombre.setText(f.getNombre());
                    lblDir.setText(f.getDireccion());
                    String match = f.getCampoCoincidente();
                    if (match == null || match.isEmpty()) {
                        lblMatch.setVisible(false);
                        lblMatch.setManaged(false);
                    } else {
                        lblMatch.setText("Coincide en: " + match);
                        lblMatch.setVisible(true);
                        lblMatch.setManaged(true);
                    }
                    setGraphic(contenido);
                }
            }
        });

        TableColumn<HospitalFila, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setCellValueFactory(c -> c.getValue().ciudadProperty());

        TableColumn<HospitalFila, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(c -> c.getValue().telefonoProperty());

        TableColumn<HospitalFila, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colEstado.setMaxWidth(100);
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.getStyleClass().add("badge-estado"); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    badge.setText(item);
                    badge.getStyleClass().removeAll("badge-activo", "badge-inactivo");
                    badge.getStyleClass().add("Activo".equals(item) ? "badge-activo" : "badge-inactivo");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<HospitalFila, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMaxWidth(110);
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer    = crearBtnIcono("👁", "btn-icono", "btn-ver");
            private final Button btnEditar = crearBtnIcono("✎", "btn-icono", "btn-editar");
            private final HBox contenedor  = new HBox(5, btnVer, btnEditar);
            {
                contenedor.setAlignment(Pos.CENTER_LEFT);
                btnVer.setOnAction(e -> {
                    HospitalFila f = getTableView().getItems().get(getIndex());
                    onVerHospital.accept(f.getCodigo());
                });
                btnEditar.setOnAction(e -> {
                    HospitalFila f = getTableView().getItems().get(getIndex());
                    abrirDialogoEditar(f);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });

        tv.getColumns().addAll(colCodigo, colNombre, colCiudad, colTelefono, colEstado, colAcciones);
        return tv;
    }

    private Button crearBtnIcono(String icono, String... clases) {
        Button btn = new Button(icono);
        btn.getStyleClass().addAll(clases);
        return btn;
    }

    private void abrirDialogoEditar(HospitalFila fila) {
        Dialog<HospitalService.HospitalUpdateBody> dialog = new Dialog<>();
        dialog.setTitle("Editar Hospital");
        dialog.setHeaderText("Editando: " + fila.getNombre());
        dialog.getDialogPane().getStyleClass().add("dialogo-hospital");

        TextField txtNombre    = campoTexto(fila.getNombre(),    "Nombre del hospital");
        TextField txtTelefono  = campoTexto(fila.getTelefono(),  "Ej: (601) 000-0000");
        TextField txtDireccion = campoTexto(fila.getDireccion(), "Dirección física");

        ComboBox<CiudadHospitalModel> cmbCiudad = comboCiudades();
        ciudadesCache.stream()
                .filter(c -> c.codigo != null && c.codigo.equals(fila.getCodigoCiudad()))
                .findFirst()
                .ifPresent(cmbCiudad::setValue);

        ComboBox<String> cmbEstado = comboEstado(fila.getEstado());

        Label lblErrores = new Label();
        lblErrores.getStyleClass().add("errores-form");
        lblErrores.setVisible(false);
        lblErrores.setManaged(false);
        lblErrores.setWrapText(true);

        GridPane grid = formularioGrid();
        agregarFila(grid, 0, "Nombre",    txtNombre);
        agregarFila(grid, 1, "Teléfono",  txtTelefono);
        agregarFila(grid, 2, "Dirección", txtDireccion);
        agregarFila(grid, 3, "Ciudad",    cmbCiudad);
        agregarFila(grid, 4, "Estado",    cmbEstado);
        grid.add(lblErrores, 0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(520, 480);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Guardar cambios");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String err = validarHospitalForm(txtNombre, txtTelefono, txtDireccion, cmbCiudad, cmbEstado);
            if (err != null) { mostrarErroresInline(lblErrores, err); ev.consume(); }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            CiudadHospitalModel c = cmbCiudad.getValue();
            return new HospitalService.HospitalUpdateBody(
                    Validacion.texto(txtNombre), Validacion.texto(txtDireccion),
                    Validacion.texto(txtTelefono), c.codigo,
                    "Activo".equals(cmbEstado.getValue()));
        });

        cargarEstilosEn(dialog);
        dialog.showAndWait().ifPresent(body -> {
            Task<HospitalModel> task = new Task<>() {
                @Override
                protected HospitalModel call() throws Exception {
                    return hospitalService.update(fila.getCodigo(), body);
                }
            };
            task.setOnSucceeded(e -> {
                Toast.success(this, "Hospital '" + body.nombre() + "' actualizado correctamente");
                cargarHospitales();
            });
            task.setOnFailed(e -> {
                Toast.error(this, "Error al actualizar el hospital");
                mostrarError("Error al actualizar el hospital.");
            });
            new Thread(task).start();
        });
    }

    private void configurarLayout() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox barraTitulo = new HBox(titulo, spacer, btnNuevoHospital);
        barraTitulo.setAlignment(Pos.CENTER_LEFT);

        HBox barraBusqueda = new HBox(12, buscador, lblContador);
        barraBusqueda.setAlignment(Pos.CENTER_LEFT);

        setSpacing(15);
        setPadding(new Insets(24));
        getChildren().addAll(barraTitulo, barraBusqueda, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
    }

    private void registrarEventos() {
        buscador.textProperty().addListener((obs, anterior, texto) -> aplicarBusqueda(texto));
        btnNuevoHospital.setOnAction(e -> abrirDialogoCrear());
    }

    /**
     * Aplica el algoritmo de búsqueda inteligente: puntúa cada hospital
     * frente al texto, descarta los que no coinciden cuando hay texto,
     * y los ordena de mayor a menor relevancia.
     */
    private void aplicarBusqueda(String texto) {
        if (texto == null || texto.isBlank()) {
            for (HospitalFila h : todosLosHospitales) h.setCampoCoincidente("");
            tabla.setItems(FXCollections.observableArrayList(todosLosHospitales));
            actualizarContador(todosLosHospitales.size(), todosLosHospitales.size());
            return;
        }
        String t = texto.toLowerCase().trim();
        List<HospitalFila> resultados = todosLosHospitales.stream()
                .peek(h -> h.setCampoCoincidente(detectarCampoCoincidente(h, t)))
                .filter(h -> puntuacion(h, t) > 0)
                .sorted(Comparator.comparingInt((HospitalFila h) -> puntuacion(h, t)).reversed())
                .toList();
        tabla.setItems(FXCollections.observableArrayList(resultados));
        actualizarContador(resultados.size(), todosLosHospitales.size());
    }

    private void actualizarContador(int mostrados, int total) {
        if (mostrados == total) {
            lblContador.setText(total + (total == 1 ? " hospital" : " hospitales"));
        } else {
            lblContador.setText(mostrados + " de " + total + " hospitales");
        }
    }

    private void abrirDialogoCrear() {
        Dialog<HospitalService.HospitalCreateBody> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Hospital");
        dialog.setHeaderText("Registrar un nuevo hospital");
        dialog.getDialogPane().getStyleClass().add("dialogo-hospital");

        TextField txtCodigo    = campoTexto("", "Ej: HOS-004");
        TextField txtNombre    = campoTexto("", "Nombre del hospital");
        TextField txtDireccion = campoTexto("", "Dirección física");
        TextField txtTelefono  = campoTexto("", "Ej: (601) 000-0000");

        ComboBox<CiudadHospitalModel> cmbCiudad = comboCiudades();
        ComboBox<String> cmbEstado = comboEstado("Activo");

        Label lblErrores = new Label();
        lblErrores.getStyleClass().add("errores-form");
        lblErrores.setVisible(false); lblErrores.setManaged(false);
        lblErrores.setWrapText(true);

        GridPane grid = formularioGrid();
        agregarFila(grid, 0, "Código",    txtCodigo);
        agregarFila(grid, 1, "Nombre",    txtNombre);
        agregarFila(grid, 2, "Dirección", txtDireccion);
        agregarFila(grid, 3, "Teléfono",  txtTelefono);
        agregarFila(grid, 4, "Ciudad",    cmbCiudad);
        agregarFila(grid, 5, "Estado",    cmbEstado);
        grid.add(lblErrores, 0, 6, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(520, 540);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Crear hospital");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            StringBuilder sb = new StringBuilder();
            String e1 = Validacion.requerido("Código", Validacion.texto(txtCodigo));
            String e2 = Validacion.formatoCodigo("Código", Validacion.texto(txtCodigo));
            String e3 = codigoYaExiste(Validacion.texto(txtCodigo));
            String resto = validarHospitalForm(txtNombre, txtTelefono, txtDireccion, cmbCiudad, cmbEstado);
            Validacion.marcarInvalido(txtCodigo, e1 != null || e2 != null || e3 != null);
            if (e1 != null) sb.append(e1).append("\n");
            else if (e2 != null) sb.append(e2).append("\n");
            else if (e3 != null) sb.append(e3).append("\n");
            if (resto != null) sb.append(resto);
            if (sb.length() > 0) { mostrarErroresInline(lblErrores, sb.toString().trim()); ev.consume(); }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            CiudadHospitalModel c = cmbCiudad.getValue();
            return new HospitalService.HospitalCreateBody(
                    Validacion.texto(txtCodigo), Validacion.texto(txtNombre),
                    Validacion.texto(txtDireccion), Validacion.texto(txtTelefono),
                    c.codigo, "Activo".equals(cmbEstado.getValue()));
        });

        cargarEstilosEn(dialog);
        dialog.showAndWait().ifPresent(body -> {
            Task<HospitalModel> task = new Task<>() {
                @Override
                protected HospitalModel call() throws Exception {
                    return hospitalService.createHospital(body);
                }
            };
            task.setOnSucceeded(e -> {
                Toast.success(this, "Hospital '" + body.nombre() + "' creado correctamente");
                cargarHospitales();
            });
            task.setOnFailed(e -> {
                Toast.error(this, "Error al crear el hospital");
                mostrarError("Error al crear el hospital.\n" +
                        "Verifica que el código y ciudad sean válidos.");
            });
            new Thread(task).start();
        });
    }

    /**
     * Calcula qué tan relevante es un hospital para el texto buscado.
     * Mayor número = más relevante. Devuelve 0 si no hay coincidencia.
     */
    private int puntuacion(HospitalFila h, String texto) {
        String nombre    = h.getNombre().toLowerCase();
        String direccion = h.getDireccion().toLowerCase();
        String codigo    = h.getCodigo().toLowerCase();
        String telefono  = h.getTelefono().toLowerCase();
        String ciudad    = h.getCiudad().toLowerCase();

        if (nombre.equals(texto))         return 100;
        if (codigo.equals(texto))         return 95;
        if (nombre.startsWith(texto))     return 80;
        if (codigo.startsWith(texto))     return 70;
        if (nombre.contains(texto))       return 60;
        if (ciudad.startsWith(texto))     return 50;
        if (ciudad.contains(texto))       return 40;
        if (direccion.contains(texto))    return 30;
        if (telefono.contains(texto))     return 20;
        if (codigo.contains(texto))       return 15;
        return 0;
    }

    /** Determina cuál campo fue el responsable de la coincidencia. */
    private String detectarCampoCoincidente(HospitalFila h, String texto) {
        if (h.getNombre().toLowerCase().contains(texto))    return "nombre";
        if (h.getCodigo().toLowerCase().contains(texto))    return "código";
        if (h.getCiudad().toLowerCase().contains(texto))    return "ciudad";
        if (h.getDireccion().toLowerCase().contains(texto)) return "dirección";
        if (h.getTelefono().toLowerCase().contains(texto))  return "teléfono";
        return "";
    }

    /* ---------- Helpers de formulario ---------- */

    private TextField campoTexto(String inicial, String prompt) {
        TextField tf = new TextField(inicial == null ? "" : inicial);
        tf.setPromptText(prompt);
        tf.getStyleClass().add("campo-form");
        tf.setPrefHeight(36);
        return tf;
    }

    private ComboBox<CiudadHospitalModel> comboCiudades() {
        ComboBox<CiudadHospitalModel> cmb = new ComboBox<>();
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(36);
        cmb.setPromptText("Selecciona una ciudad");
        cmb.getItems().setAll(ciudadesCache);
        cmb.getStyleClass().add("campo-form");
        return cmb;
    }

    private ComboBox<String> comboEstado(String inicial) {
        ComboBox<String> cmb = new ComboBox<>();
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(36);
        cmb.getItems().addAll("Activo", "Inactivo");
        cmb.setValue(inicial);
        cmb.getStyleClass().add("campo-form");
        return cmb;
    }

    private GridPane formularioGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        grid.setPadding(new Insets(22, 24, 18, 24));
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(110); c0.setHalignment(javafx.geometry.HPos.RIGHT);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS); c1.setFillWidth(true);
        grid.getColumnConstraints().addAll(c0, c1);
        return grid;
    }

    private void agregarFila(GridPane grid, int fila, String etiqueta, javafx.scene.Node control) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("etiqueta-form");
        grid.add(lbl, 0, fila);
        grid.add(control, 1, fila);
        if (control instanceof Region r) GridPane.setHgrow(r, Priority.ALWAYS);
    }

    /**
     * Ejecuta las validaciones comunes para los formularios de hospital.
     * Marca visualmente los controles con error y devuelve un mensaje
     * agrupado, o {@code null} si no hay errores.
     */
    private String validarHospitalForm(TextField txtNombre, TextField txtTelefono,
                                       TextField txtDireccion,
                                       ComboBox<CiudadHospitalModel> cmbCiudad,
                                       ComboBox<String> cmbEstado) {
        Validacion.limpiarEstado(txtNombre, txtTelefono, txtDireccion, cmbCiudad, cmbEstado);
        StringBuilder sb = new StringBuilder();

        String e;
        e = Validacion.requerido("Nombre", Validacion.texto(txtNombre));
        if (e == null) e = Validacion.longitudMax("Nombre", Validacion.texto(txtNombre), 50);
        Validacion.marcarInvalido(txtNombre, e != null);
        if (e != null) sb.append(e).append("\n");

        e = Validacion.requerido("Teléfono", Validacion.texto(txtTelefono));
        if (e == null) e = Validacion.formatoTelefono(Validacion.texto(txtTelefono));
        if (e == null) e = Validacion.longitudMax("Teléfono", Validacion.texto(txtTelefono), 20);
        Validacion.marcarInvalido(txtTelefono, e != null);
        if (e != null) sb.append(e).append("\n");

        e = Validacion.requerido("Dirección", Validacion.texto(txtDireccion));
        if (e == null) e = Validacion.longitudMax("Dirección", Validacion.texto(txtDireccion), 150);
        Validacion.marcarInvalido(txtDireccion, e != null);
        if (e != null) sb.append(e).append("\n");

        e = Validacion.comboSeleccionado("una ciudad", cmbCiudad);
        Validacion.marcarInvalido(cmbCiudad, e != null);
        if (e != null) sb.append(e).append("\n");

        e = Validacion.comboSeleccionado("un estado", cmbEstado);
        Validacion.marcarInvalido(cmbEstado, e != null);
        if (e != null) sb.append(e).append("\n");

        return sb.length() == 0 ? null : sb.toString().trim();
    }

    private String codigoYaExiste(String codigo) {
        if (codigo == null || codigo.isBlank()) return null;
        boolean existe = todosLosHospitales.stream()
                .anyMatch(h -> codigo.equalsIgnoreCase(h.getCodigo()));
        return existe ? "• Ya existe un hospital con el código '" + codigo + "'" : null;
    }

    private void mostrarErroresInline(Label lbl, String mensaje) {
        lbl.setText(mensaje);
        lbl.setVisible(true); lbl.setManaged(true);
    }

    private void cargarEstilosEn(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/hospital/hospital.css").toExternalForm());
    }

    private void cargarEstilos() {
        getStyleClass().add("hospital-view");
        getStylesheets().add(
                getClass().getResource("/styles/hospital/hospital.css").toExternalForm());
    }

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait();
    }
}
