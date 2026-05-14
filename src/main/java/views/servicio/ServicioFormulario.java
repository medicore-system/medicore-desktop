package views.servicio;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import models.ServicioModel;
import models.TipoServicioModel;
import services.ServicioService;
import views.common.Estado;
import views.common.PrecioFormato;
import views.common.Validacion;

import java.util.List;

/**
 * Construye los pop-ups del módulo de servicios:
 * el de crear, el de editar y el de ver.
 *
 * El código del servicio NO se le pide al usuario al crear,
 * el backend lo genera automáticamente.
 */
public final class ServicioFormulario {

    private static final String STYLESHEET   = "/styles/servicio/servicio.css";
    private static final String CLASE_DIALOGO = "dialogo-servicio";

    private ServicioFormulario() {}

    /**
     * Construye el pop-up para crear un servicio nuevo.
     * No incluye campo de código porque el backend lo genera solo.
     *
     * @param tipos lista de tipos de servicio cargados desde el backend.
     * @return el diálogo listo para llamarle showAndWait().
     */
    public static Dialog<ServicioService.ServicioCreateBody> dialogoCrear(List<TipoServicioModel> tipos) {
        Dialog<ServicioService.ServicioCreateBody> dialog = baseDialog(
                "Nuevo Servicio", "Registrar un nuevo servicio", 520, 460);

        TextField txtNombre = campoTexto("", "Nombre del servicio");
        TextField txtDesc   = campoTexto("", "Descripción del servicio");
        TextField txtPrecio = campoTexto("", "Ej: 80000");
        ComboBox<TipoServicioModel> cmbTipo = comboTipo(tipos, null);
        Label lblErrores = etiquetaErrores();

        GridPane grid = grid();
        agregarFila(grid, 0, "Nombre",      txtNombre);
        agregarFila(grid, 1, "Descripción", txtDesc);
        agregarFila(grid, 2, "Tipo",        cmbTipo);
        agregarFila(grid, 3, "Precio",      txtPrecio);
        grid.add(lblErrores, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Crear servicio");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String error = validarComun(txtNombre, txtDesc, txtPrecio, cmbTipo);
            if (error != null) {
                mostrarErroresInline(lblErrores, error);
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            TipoServicioModel tipoSeleccionado = cmbTipo.getValue();
            return new ServicioService.ServicioCreateBody(
                    Validacion.texto(txtNombre),
                    Validacion.texto(txtDesc),
                    tipoSeleccionado.getId(),
                    PrecioFormato.parsear(Validacion.texto(txtPrecio)));
        });
        return dialog;
    }

    /**
     * Construye el pop-up de edición precargado con los datos del servicio.
     *
     * @param servicio el servicio que se va a editar.
     * @param tipos    lista de tipos de servicio cargados desde el backend.
     * @return el diálogo listo para mostrar.
     */
    public static Dialog<ServicioService.ServicioUpdateBody> dialogoEditar(ServicioModel servicio,
                                                                           List<TipoServicioModel> tipos) {
        Dialog<ServicioService.ServicioUpdateBody> dialog = baseDialog(
                "Editar Servicio", "Editando: " + servicio.getNombre(), 520, 500);

        TextField txtNombre = campoTexto(servicio.getNombre(), "Nombre del servicio");
        TextField txtDesc   = campoTexto(
                servicio.getDescripcion() != null ? servicio.getDescripcion() : "",
                "Descripción del servicio");
        TextField txtPrecio = campoTexto(
                servicio.getPrecio() != null ? String.valueOf(servicio.getPrecio().longValue()) : "",
                "Ej: 80000");
        ComboBox<TipoServicioModel> cmbTipo   = comboTipo(tipos, servicio.getIdTipoServicio());
        ComboBox<String>            cmbEstado = comboEstado(servicio.getEstadoTexto());
        Label lblErrores = etiquetaErrores();

        GridPane grid = grid();
        agregarFila(grid, 0, "Nombre",      txtNombre);
        agregarFila(grid, 1, "Descripción", txtDesc);
        agregarFila(grid, 2, "Tipo",        cmbTipo);
        agregarFila(grid, 3, "Precio",      txtPrecio);
        agregarFila(grid, 4, "Estado",      cmbEstado);
        grid.add(lblErrores, 0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Guardar cambios");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String error = validarComun(txtNombre, txtDesc, txtPrecio, cmbTipo);
            if (error != null) {
                mostrarErroresInline(lblErrores, error);
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            TipoServicioModel tipoSeleccionado = cmbTipo.getValue();
            return new ServicioService.ServicioUpdateBody(
                    Validacion.texto(txtNombre),
                    Validacion.texto(txtDesc),
                    tipoSeleccionado.getId(),
                    PrecioFormato.parsear(Validacion.texto(txtPrecio)),
                    Estado.aBooleano(cmbEstado.getValue()));
        });
        return dialog;
    }

    /**
     * Construye el pop-up de solo lectura con la información del servicio.
     *
     * @param servicio el servicio a mostrar.
     * @return el diálogo listo para mostrar.
     */
    public static Dialog<Void> dialogoVer(ServicioModel servicio) {
        Dialog<Void> dialog = baseDialog(
                "Detalle del Servicio", "Información del servicio", 480, 420);

        GridPane grid = grid();
        agregarFilaSoloLectura(grid, 0, "Identificador", servicio.getCodigo());
        agregarFilaSoloLectura(grid, 1, "Nombre",        servicio.getNombre());
        agregarFilaSoloLectura(grid, 2, "Descripción",   servicio.getDescripcion());
        agregarFilaSoloLectura(grid, 3, "Tipo",          servicio.getTipo());
        agregarFilaSoloLectura(grid, 4, "Precio",
                servicio.getPrecio() != null ? PrecioFormato.formatear(servicio.getPrecio()) : "—");
        agregarFilaSoloLectura(grid, 5, "Estado",        servicio.getEstadoTexto());

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE)).setText("Cerrar");
        return dialog;
    }

    /** Crea la estructura base que comparten los tres diálogos. */
    private static <T> Dialog<T> baseDialog(String titulo, String header, double w, double h) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getStyleClass().add(CLASE_DIALOGO);
        dialog.getDialogPane().setPrefSize(w, h);
        dialog.getDialogPane().getStylesheets().add(
                ServicioFormulario.class.getResource(STYLESHEET).toExternalForm());
        return dialog;
    }

    /**
     * Valida los campos obligatorios del formulario.
     *
     * @return null si todo está bien, o un String con los mensajes de error.
     */
    private static String validarComun(TextField txtNombre, TextField txtDesc,
                                       TextField txtPrecio, ComboBox<TipoServicioModel> cmbTipo) {
        Validacion.limpiarEstado(txtNombre, txtDesc, txtPrecio, cmbTipo);

        List<Regla> reglas = List.of(
                new Regla(txtNombre, () -> primero(
                        Validacion.requerido("Nombre", Validacion.texto(txtNombre)),
                        Validacion.longitudMax("Nombre", Validacion.texto(txtNombre), 50))),
                new Regla(txtDesc, () -> primero(
                        Validacion.requerido("Descripción", Validacion.texto(txtDesc)),
                        Validacion.longitudMax("Descripción", Validacion.texto(txtDesc), 250))),
                new Regla(cmbTipo, () -> cmbTipo.getValue() == null ? "• Selecciona un tipo" : null),
                new Regla(txtPrecio, () -> primero(
                        Validacion.requerido("Precio", Validacion.texto(txtPrecio)),
                        Validacion.numeroPositivo("Precio", Validacion.texto(txtPrecio))))
        );

        StringBuilder sb = new StringBuilder();
        for (Regla r : reglas) {
            String error = r.evaluar().get();
            Validacion.marcarInvalido(r.control(), error != null);
            if (error != null) sb.append(error).append("\n");
        }
        return sb.length() == 0 ? null : sb.toString().trim();
    }

    /** Devuelve el primer mensaje de error que no sea null. */
    private static String primero(String... mensajes) {
        for (String m : mensajes) if (m != null) return m;
        return null;
    }

    /** Empareja un control con su función de validación. */
    private record Regla(Node control, java.util.function.Supplier<String> evaluar) {}

    /** Crea un TextField con valor inicial y texto de ayuda. */
    private static TextField campoTexto(String inicial, String prompt) {
        TextField tf = new TextField(inicial == null ? "" : inicial);
        tf.setPromptText(prompt);
        tf.getStyleClass().add("campo-form");
        tf.setPrefHeight(36);
        return tf;
    }

    /**
     * Crea el ComboBox de tipos de servicio cargados desde el backend.
     * Muestra el nombre de cada tipo y selecciona el que coincida con el idSeleccionado.
     *
     * @param tipos        lista de tipos disponibles
     * @param idSeleccionado id del tipo a preseleccionar (null si ninguno)
     * @return ComboBox configurado
     */
    private static ComboBox<TipoServicioModel> comboTipo(List<TipoServicioModel> tipos,
                                                          Integer idSeleccionado) {
        ComboBox<TipoServicioModel> cmb = new ComboBox<>();
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(36);
        cmb.setPromptText("Selecciona un tipo");
        cmb.getItems().addAll(tipos);
        if (idSeleccionado != null) {
            tipos.stream()
                    .filter(t -> t.getId().equals(idSeleccionado))
                    .findFirst()
                    .ifPresent(cmb::setValue);
        }
        cmb.getStyleClass().add("campo-form");
        return cmb;
    }

    /** Crea el ComboBox de estado del servicio. */
    private static ComboBox<String> comboEstado(String inicial) {
        ComboBox<String> cmb = new ComboBox<>();
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(36);
        cmb.getItems().addAll(Estado.ACTIVO, Estado.INACTIVO);
        cmb.setValue(inicial);
        cmb.getStyleClass().add("campo-form");
        return cmb;
    }

    /** Crea el GridPane base del formulario con dos columnas. */
    private static GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(22, 24, 18, 24));
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(120);
        c0.setHalignment(HPos.RIGHT);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);
        grid.getColumnConstraints().addAll(c0, c1);
        return grid;
    }

    /** Agrega una fila editable al grid. */
    private static void agregarFila(GridPane grid, int fila, String etiqueta, Node control) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("etiqueta-form");
        grid.add(lbl, 0, fila);
        grid.add(control, 1, fila);
        if (control instanceof Region r) GridPane.setHgrow(r, Priority.ALWAYS);
    }

    /** Agrega una fila de solo lectura al grid. */
    private static void agregarFilaSoloLectura(GridPane grid, int fila, String etiqueta, String valor) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("etiqueta-form");
        Label val = new Label(valor == null || valor.isBlank() ? "—" : valor);
        val.getStyleClass().add("valor-solo-lectura");
        val.setWrapText(true);
        grid.add(lbl, 0, fila);
        grid.add(val, 1, fila);
        GridPane.setHgrow(val, Priority.ALWAYS);
    }

    /** Crea el Label de errores, inicialmente oculto. */
    private static Label etiquetaErrores() {
        Label lbl = new Label();
        lbl.getStyleClass().add("errores-form");
        lbl.setVisible(false);
        lbl.setManaged(false);
        lbl.setWrapText(true);
        return lbl;
    }

    /** Muestra los errores dentro del formulario. */
    private static void mostrarErroresInline(Label lbl, String mensaje) {
        lbl.setText(mensaje);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }
}
