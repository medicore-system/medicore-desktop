package views.hospital;

import controllers.AreaInternaController.TipoArea;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.AreaInternaModel;
import services.HospitalService;
import views.common.Validacion;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Diálogo reutilizable para crear y editar áreas internas de un hospital.
 *
 * <p>Si se pasa un {@link AreaInternaModel} existente, opera en modo edición;
 * si se pasa {@code null}, opera en modo creación y genera el código automáticamente.
 * El tipo de área se selecciona mediante un combo alimentado con los tipos
 * disponibles que ya existen en el sistema, evitando que el usuario tenga
 * que conocer códigos foráneos.</p>
 *
 * @author Juan Sebastián López Guzmán
 * @author Cristian Camilo Salazar Arenas
 */
public class AreaFormDialog {

    /** Área a editar; {@code null} indica modo creación. */
    private final AreaInternaModel areaExistente;

    /** Tipos de área interna disponibles para el combo "Tipo". */
    private final List<TipoArea> tiposDisponibles;

    /** Callback invocado con el body resultante al pulsar "OK". */
    private final Consumer<Object> onGuardar;

    /** Genera el código automático en modo creación; {@code null} en modo edición. */
    private final Supplier<String> generarCodigo;

    /**
     * Crea el diálogo vinculado a un área existente (edición) o en blanco (creación).
     *
     * @param areaExistente    Área a editar, o {@code null} para crear una nueva
     * @param tiposDisponibles Lista de tipos de área disponibles para el combo
     * @param onGuardar        Callback que recibe {@link services.HospitalService.AreaCreateBody}
     *                         o {@link services.HospitalService.AreaUpdateBody} al confirmar
     * @param generarCodigo    Supplier que devuelve el siguiente código disponible
     *                         en modo creación; {@code null} en modo edición
     */
    public AreaFormDialog(AreaInternaModel areaExistente,
                          List<TipoArea> tiposDisponibles,
                          Consumer<Object> onGuardar,
                          Supplier<String> generarCodigo) {
        this.areaExistente    = areaExistente;
        this.tiposDisponibles = tiposDisponibles;
        this.onGuardar        = onGuardar;
        this.generarCodigo    = generarCodigo;
    }

    /**
     * Construye y muestra el diálogo modal. Al cerrarse con "OK", invoca {@code onGuardar}.
     */
    public void show() {
        boolean esEdicion = (areaExistente != null);

        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle(esEdicion ? "Editar Área" : "Nueva Área");
        dialog.setHeaderText(esEdicion
                ? "Editando: " + areaExistente.nombre
                : "Registrar nueva área en el hospital");
        dialog.getDialogPane().getStyleClass().add("dialogo-hospital");

        // En modo creación el código se genera automáticamente; no se le pide al usuario.
        final String codigoGenerado = (!esEdicion && generarCodigo != null) ? generarCodigo.get() : null;

        TextField txtNombre      = campoTexto(esEdicion ? areaExistente.nombre : "", "Nombre del área");
        TextField txtDescripcion = campoTexto(
                esEdicion && areaExistente.descripcion != null ? areaExistente.descripcion : "",
                "Descripción (opcional)");

        ComboBox<TipoArea> cmbTipo = comboTipos();
        if (esEdicion) preseleccionarTipo(cmbTipo, areaExistente.codigoAreaInterna);

        Label lblErrores = errorLabel();

        GridPane grid = formularioGrid();
        int fila = 0;
        agregarFila(grid, fila++, "Nombre",      txtNombre);
        agregarFila(grid, fila++, "Descripción", txtDescripcion);
        agregarFila(grid, fila++, "Tipo",        cmbTipo);
        grid.add(lblErrores, 0, fila, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(520, 380);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK))
                .setText(esEdicion ? "Guardar Área" : "Crear área");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String resto = validarCamposComunes(txtNombre, txtDescripcion, cmbTipo);
            if (resto != null) { mostrarErrores(lblErrores, resto); ev.consume(); }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            String codigoTipo = cmbTipo.getValue().codigo();
            if (esEdicion) {
                return new HospitalService.AreaUpdateBody(
                        Validacion.texto(txtNombre), Validacion.texto(txtDescripcion),
                        codigoTipo);
            }
            return new HospitalService.AreaCreateBody(
                    codigoGenerado, Validacion.texto(txtNombre),
                    Validacion.texto(txtDescripcion), codigoTipo);
        });

        cargarEstilos(dialog);
        dialog.showAndWait().ifPresent(onGuardar);
    }

    /**
     * Valida todos los campos del formulario de área.
     *
     * @return Cadena con todos los errores encontrados, o {@code null} si todo es válido
     */
    private String validarCamposComunes(TextField txtNombre, TextField txtDescripcion,
                                        ComboBox<TipoArea> cmbTipo) {
        Validacion.limpiarEstado(txtNombre, txtDescripcion, cmbTipo);
        StringBuilder sb = new StringBuilder();
        String e;

        e = Validacion.requerido("Nombre", Validacion.texto(txtNombre));
        if (e == null) e = Validacion.longitudMax("Nombre", Validacion.texto(txtNombre), 100);
        Validacion.marcarInvalido(txtNombre, e != null);
        if (e != null) sb.append(e).append("\n");

        e = Validacion.longitudMax("Descripción", Validacion.texto(txtDescripcion), 250);
        Validacion.marcarInvalido(txtDescripcion, e != null);
        if (e != null) sb.append(e).append("\n");

        e = Validacion.comboSeleccionado("un tipo de área", cmbTipo);
        Validacion.marcarInvalido(cmbTipo, e != null);
        if (e != null) sb.append(e).append("\n");

        return sb.length() == 0 ? null : sb.toString().trim();
    }

    /**
     * Preselecciona en el combo el tipo que coincida con el código indicado.
     *
     * @param cmb         Combo de tipos
     * @param codigoTipo  Código del tipo de área a preseleccionar
     */
    private void preseleccionarTipo(ComboBox<TipoArea> cmb, String codigoTipo) {
        if (codigoTipo == null) return;
        cmb.getItems().stream()
                .filter(t -> codigoTipo.equals(t.codigo()))
                .findFirst()
                .ifPresent(cmb::setValue);
    }

    /**
     * Crea un {@link TextField} estilizado con valor inicial y texto de ayuda.
     *
     * @param inicial Valor inicial del campo; si es {@code null} se usa cadena vacía
     * @param prompt  Texto descriptivo mostrado cuando el campo está vacío
     */
    private TextField campoTexto(String inicial, String prompt) {
        TextField tf = new TextField(inicial == null ? "" : inicial);
        tf.setPromptText(prompt);
        tf.getStyleClass().add("campo-form");
        tf.setPrefHeight(36);
        return tf;
    }

    /** Crea el combo de tipos de área precargado con los disponibles del sistema. */
    private ComboBox<TipoArea> comboTipos() {
        ComboBox<TipoArea> cmb = new ComboBox<>();
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(36);
        cmb.setPromptText("Selecciona un tipo");
        cmb.getItems().setAll(tiposDisponibles);
        cmb.getStyleClass().add("campo-form");
        return cmb;
    }

    /** Crea el {@link GridPane} base del formulario con dos columnas (etiqueta / control). */
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
     * Añade una fila de etiqueta + control al grid del formulario.
     *
     * @param grid     Grid destino
     * @param fila     Índice de fila donde insertar
     * @param etiqueta Texto de la etiqueta descriptiva
     * @param control  Control de entrada (TextField, ComboBox, etc.)
     */
    private void agregarFila(GridPane grid, int fila, String etiqueta, javafx.scene.Node control) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("etiqueta-form");
        grid.add(lbl, 0, fila);
        grid.add(control, 1, fila);
        if (control instanceof Region r) GridPane.setHgrow(r, Priority.ALWAYS);
    }

    /** Crea el {@link Label} de errores, inicialmente oculto y sin espacio en el layout. */
    private Label errorLabel() {
        Label l = new Label();
        l.getStyleClass().add("errores-form");
        l.setVisible(false); l.setManaged(false);
        l.setWrapText(true);
        return l;
    }

    /**
     * Hace visible el label de errores y muestra el mensaje indicado.
     *
     * @param lbl     Label de errores creado con {@link #errorLabel()}
     * @param mensaje Texto de error a mostrar
     */
    private void mostrarErrores(Label lbl, String mensaje) {
        lbl.setText(mensaje);
        lbl.setVisible(true); lbl.setManaged(true);
    }

    /** Aplica la hoja de estilos del módulo hospital al panel del diálogo. */
    private void cargarEstilos(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/hospital/hospitalDetalle.css").toExternalForm());
    }
}
