package views.hospital;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.CiudadModel;
import models.HospitalModel;
import services.HospitalService;
import views.common.Validacion;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Diálogo reutilizable para crear y editar hospitales.
 *
 * <p>Si se pasa un {@code HospitalModel} existente, opera en modo edición.
 * Si se pasa {@code null}, opera en modo creación y genera el código
 * automáticamente mediante el {@code Supplier} indicado.</p>
 *
 * @author Juan Sebastián López Guzmán
 * @author Cristian Camilo Salazar Arenas
 */
public class HospitalFormDialog {

    /** Hospital a editar; {@code null} indica modo creación. */
    private final HospitalModel hospitalExistente;

    /** Lista de ciudades disponibles para poblar el combo del formulario. */
    private final List<CiudadModel> ciudades;

    /** Callback invocado con el body resultante al pulsar "OK". */
    private final Consumer<Object> onGuardar;

    /** Genera el código automático en modo creación; {@code null} en modo edición. */
    private final Supplier<String> generarCodigo;

    /**
     * Crea el diálogo vinculado a un hospital existente (edición) o en blanco (creación).
     *
     * @param hospitalExistente Hospital a editar, o {@code null} para crear uno nuevo
     * @param ciudades          Lista de ciudades disponibles para el combo
     * @param onGuardar         Callback que recibe {@link services.HospitalService.HospitalCreateBody}
     *                          o {@link services.HospitalService.HospitalUpdateBody} al confirmar
     * @param generarCodigo     Supplier que devuelve el siguiente código disponible
     *                          en modo creación; {@code null} en modo edición
     */
    public HospitalFormDialog(HospitalModel hospitalExistente,
                              List<CiudadModel> ciudades,
                              Consumer<Object> onGuardar,
                              Supplier<String> generarCodigo) {
        this.hospitalExistente = hospitalExistente;
        this.ciudades          = ciudades;
        this.onGuardar         = onGuardar;
        this.generarCodigo     = generarCodigo;
    }

    /**
     * Construye y muestra el diálogo modal. Al cerrarse con "OK", invoca {@code onGuardar}.
     */
    public void show() {
        boolean esEdicion = (hospitalExistente != null);

        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle(esEdicion ? "Editar Hospital" : "Nuevo Hospital");
        dialog.setHeaderText(esEdicion
                ? "Editando: " + hospitalExistente.nombre
                : "Registrar un nuevo hospital");
        dialog.getDialogPane().getStyleClass().add("dialogo-hospital");

        // En modo creación el código se genera automáticamente; no se le pide al usuario.
        final String codigoGenerado = (!esEdicion && generarCodigo != null) ? generarCodigo.get() : null;

        TextField txtNombre    = campoTexto(esEdicion ? hospitalExistente.nombre    : "", "Nombre del hospital");
        TextField txtDireccion = campoTexto(esEdicion ? hospitalExistente.direccion : "", "Dirección física");
        TextField txtTelefono  = campoTexto(esEdicion ? hospitalExistente.telefono  : "", "Ej: (601) 000-0000");

        ComboBox<CiudadModel> cmbCiudad = comboCiudades();
        if (esEdicion) preseleccionarCiudad(cmbCiudad, hospitalExistente.codigoCiudad);

        String estadoInicial = esEdicion
                ? (Boolean.TRUE.equals(hospitalExistente.estado) ? "Activo" : "Inactivo")
                : "Activo";
        ComboBox<String> cmbEstado = comboEstado(estadoInicial);

        Label lblErrores = errorLabel();

        GridPane grid = formularioGrid();
        int fila = 0;
        agregarFila(grid, fila++, "Nombre",    txtNombre);
        agregarFila(grid, fila++, "Dirección", txtDireccion);
        agregarFila(grid, fila++, "Teléfono",  txtTelefono);
        agregarFila(grid, fila++, "Ciudad",    cmbCiudad);
        agregarFila(grid, fila++, "Estado",    cmbEstado);
        grid.add(lblErrores, 0, fila, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(520, 450);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK))
                .setText(esEdicion ? "Guardar cambios" : "Crear hospital");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String resto = validarCamposComunes(txtNombre, txtTelefono, txtDireccion, cmbCiudad, cmbEstado);
            if (resto != null) { mostrarErrores(lblErrores, resto); ev.consume(); }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            CiudadModel ciudad = cmbCiudad.getValue();
            boolean activo = "Activo".equals(cmbEstado.getValue());
            if (esEdicion) {
                return new HospitalService.HospitalUpdateBody(
                        Validacion.texto(txtNombre), Validacion.texto(txtDireccion),
                        Validacion.texto(txtTelefono), ciudad.getCodigo(), activo);
            }
            return new HospitalService.HospitalCreateBody(
                    codigoGenerado, Validacion.texto(txtNombre),
                    Validacion.texto(txtDireccion), Validacion.texto(txtTelefono),
                    ciudad.getCodigo(), activo);
        });

        cargarEstilos(dialog);
        dialog.showAndWait().ifPresent(onGuardar);
    }

    /**
     * Valida todos los campos del formulario.
     *
     * @return Cadena con todos los errores encontrados, o {@code null} si todo es válido
     */
    private String validarCamposComunes(TextField txtNombre, TextField txtTelefono,
                                        TextField txtDireccion,
                                        ComboBox<CiudadModel> cmbCiudad,
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

    /**
     * Preselecciona en el combo la ciudad que coincida con el código indicado.
     *
     * @param cmb          Combo de ciudades
     * @param codigoCiudad Código de la ciudad a preseleccionar
     */
    private void preseleccionarCiudad(ComboBox<CiudadModel> cmb, String codigoCiudad) {
        if (codigoCiudad == null) return;
        cmb.getItems().stream()
                .filter(c -> codigoCiudad.equals(c.getCodigo()))
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

    /** Crea el combo de ciudades precargado con la lista del controlador. */
    private ComboBox<CiudadModel> comboCiudades() {
        ComboBox<CiudadModel> cmb = new ComboBox<>();
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(36);
        cmb.setPromptText("Selecciona una ciudad");
        cmb.getItems().setAll(ciudades);
        cmb.getStyleClass().add("campo-form");
        return cmb;
    }

    /**
     * Crea el combo de estado con opciones "Activo" e "Inactivo".
     *
     * @param inicial Valor seleccionado por defecto
     */
    private ComboBox<String> comboEstado(String inicial) {
        ComboBox<String> cmb = new ComboBox<>();
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(36);
        cmb.getItems().addAll("Activo", "Inactivo");
        cmb.setValue(inicial);
        cmb.getStyleClass().add("campo-form");
        return cmb;
    }

    /** Crea el {@link GridPane} base del formulario con dos columnas (etiqueta / control). */
    private GridPane formularioGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        grid.setPadding(new Insets(22, 24, 18, 24));
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(110); c0.setHalignment(HPos.RIGHT);
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
                getClass().getResource("/styles/hospital/hospital.css").toExternalForm());
    }
}
