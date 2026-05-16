package views.usuario;

import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.CiudadModel;
import models.EpsModel;
import models.UsuarioModel;
import services.UsuarioService;
import java.util.List;
import java.util.function.Consumer;

/**
 * Diálogo reutilizable para la creación y edición de usuarios.
 *
 * <p>
 * Esta clase encapsula toda la construcción de la interfaz,
 * validaciones y generación de los cuerpos de petición
 * necesarios para consumir el backend.
 * </p>
 *
 * <h2>Modo creación</h2>
 * <pre>
 * new UsuarioFormDialog(
 *     null,
 *     ciudades,
 *     eps,
 *     body -> controller.crear(body)
 * ).show();
 * </pre>
 *
 * <h2>Modo edición</h2>
 * <pre>
 * new UsuarioFormDialog(
 *     usuario,
 *     ciudades,
 *     eps,
 *     body -> controller.actualizar(body)
 * ).show();
 * </pre>
 *
 * <p>
 * El diálogo no conoce el controlador ni el servicio.
 * Únicamente entrega el resultado mediante un callback.
 * </p>
 */
public class UsuarioFormDialog {

    /**
     * Usuario actualmente editado.
     *
     * <p>
     * Si es {@code null}, el diálogo trabaja en modo creación.
     * </p>
     */
    private final UsuarioModel usuarioActual; // null = modo crear

    /**
     * Lista de ciudades disponibles.
     */
    private final List<CiudadModel> ciudades;

    /**
     * Lista de EPS disponibles.
     */
    private final List<EpsModel> eps;

    /**
     * Callback ejecutado al confirmar el formulario.
     *
     * <p>
     * Recibe un:
     * </p>
     * <ul>
     *     <li>{@code UsuarioCreateBody}</li>
     *     <li>{@code UsuarioUpdateBody}</li>
     * </ul>
     */
    private final Consumer<Object> onConfirmar; // recibe CreateBody o UpdateBody

    /**
     * Constructor del diálogo.
     *
     * @param usuarioActual usuario actual o {@code null} para crear
     * @param ciudades lista de ciudades disponibles
     * @param eps lista de EPS disponibles
     * @param onConfirmar callback ejecutado al confirmar
     */
    public UsuarioFormDialog(UsuarioModel usuarioActual,
                             List<CiudadModel> ciudades,
                             List<EpsModel> eps,
                             Consumer<Object> onConfirmar) {
        this.usuarioActual = usuarioActual;
        this.ciudades      = ciudades;
        this.eps           = eps;
        this.onConfirmar   = onConfirmar;
    }

    /**
     * Construye y muestra el diálogo.
     *
     * <p>
     * Si el usuario confirma la operación y las validaciones
     * son exitosas, se ejecuta el callback configurado.
     * </p>
     */
    public void show() {
        boolean modoCrear = (usuarioActual == null);

        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle(modoCrear ? "Nuevo Usuario" : "Editar Usuario");
        dialog.setHeaderText(modoCrear
                ? "Registrar un nuevo usuario"
                : "Editando: " + usuarioActual.GetNombreCompleto());

        // ── Campos ──────────────────────────────────────────────────────
        TextField txtDocumento = campo(modoCrear ? "" : usuarioActual.getDocumento(), "Ej: 1234567890");
        txtDocumento.setDisable(!modoCrear); // el documento no se puede cambiar al editar

        TextField txtNombre    = campo(modoCrear ? "" : usuarioActual.getNombre(),   "Nombre");
        TextField txtApellido  = campo(modoCrear ? "" : usuarioActual.getApellido(), "Apellido");
        TextField txtCorreo    = campo(modoCrear ? "" : usuarioActual.getCorreo(),   "correo@ejemplo.com");
        txtCorreo.setDisable(!modoCrear); // el correo no se edita
        TextField txtTelefono  = campo(modoCrear ? "" : usuarioActual.getTelefono(), "Ej: 3001234567");

        ComboBox<CiudadModel> cmbCiudad = comboCiudades();
        if (!modoCrear) preseleccionarCiudad(cmbCiudad, usuarioActual.getCiudad());

        ComboBox<EpsModel> cmbEps = comboEps();
        if (!modoCrear) preseleccionarEps(cmbEps, usuarioActual.getEps());

        Label lblErrores = new Label();
        lblErrores.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 12px;");
        lblErrores.setVisible(false);
        lblErrores.setManaged(false);
        lblErrores.setWrapText(true);

        // ── Grid ────────────────────────────────────────────────────────
        GridPane grid = crearGrid();
        int fila = 0;
        agregarFila(grid, fila++, "Documento",  txtDocumento);
        agregarFila(grid, fila++, "Nombre",     txtNombre);
        agregarFila(grid, fila++, "Apellido",   txtApellido);
        if (modoCrear) agregarFila(grid, fila++, "Correo", txtCorreo);
        agregarFila(grid, fila++, "Teléfono",   txtTelefono);
        agregarFila(grid, fila++, "Ciudad",     cmbCiudad);
        agregarFila(grid, fila++, "EPS",        cmbEps);
        grid.add(lblErrores, 0, fila, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(500, modoCrear ? 560 : 480);
        dialog.getDialogPane().getStyleClass().add("dialogo-paciente");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK))
                .setText(modoCrear ? "Crear usuario" : "Guardar cambios");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL))
                .setText("Cancelar");

        // Validación antes de cerrar
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String error = validar(modoCrear, txtDocumento, txtNombre, txtApellido,
                    txtTelefono, cmbCiudad, cmbEps);
            if (error != null) {
                lblErrores.setText(error);
                lblErrores.setVisible(true);
                lblErrores.setManaged(true);
                ev.consume(); // no cierra el dialog
            }
        });

        // Convertir campos en el body correcto
        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            CiudadModel ciudad = cmbCiudad.getValue();
            EpsModel    epsVal = cmbEps.getValue();

            if (modoCrear) {
                return new UsuarioService.UsuarioCreateBody(
                        txtDocumento.getText().trim(),
                        txtNombre.getText().trim(),
                        txtApellido.getText().trim(),
                        txtCorreo.getText().trim(),
                        txtTelefono.getText().trim(),
                        epsVal.getCodigo(),
                        ciudad.getCodigo()
                );
            } else {
                return new UsuarioService.UsuarioUpdateBody(
                        txtNombre.getText().trim(),
                        txtApellido.getText().trim(),
                        txtTelefono.getText().trim(),
                        epsVal.getCodigo(),
                        ciudad.getCodigo()
                );
            }
        });

        cargarEstilos(dialog);
        dialog.showAndWait().ifPresent(onConfirmar);
    }

    /**
     * Valida los campos del formulario.
     *
     * @param modoCrear indica si el formulario está en modo creación
     * @param txtDocumento campo documento
     * @param txtNombre campo nombre
     * @param txtApellido campo apellido
     * @param txtTelefono campo teléfono
     * @param cmbCiudad combo de ciudades
     * @param cmbEps combo de EPS
     * @return mensaje de error o {@code null} si todo es válido
     */
    private String validar(boolean modoCrear,
                           TextField txtDocumento, TextField txtNombre,
                           TextField txtApellido,  TextField txtTelefono,
                           ComboBox<CiudadModel> cmbCiudad, ComboBox<EpsModel> cmbEps) {
        StringBuilder sb = new StringBuilder();

        if (modoCrear && (
                txtDocumento.getText().trim().isEmpty() ||
                        !txtDocumento.getText().trim().matches("\\d{7,10}")
        )) {
            sb.append("• El documento debe contener solo números y tener entre 7 y 10 dígitos.\n");
        }

        if (txtNombre.getText().trim().isEmpty()) {
            sb.append("• El nombre es obligatorio.\n");

        } else if (!txtNombre.getText().trim().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{3,}")) {
            sb.append("• El nombre debe tener mínimo 3 letras y no puede contener números ni símbolos.\n");
        }

        if (txtApellido.getText().trim().isEmpty()) {
            sb.append("• El apellido es obligatorio.\n");
        } else if (!txtApellido.getText().trim().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{3,}")) {
            sb.append("• El apellido debe tener mínimo 3 letras y no puede contener números ni símbolos.\n");
        }

        if (txtTelefono.getText().trim().isEmpty()) {
            sb.append("• El teléfono es obligatorio.\n");
        } else if (!txtTelefono.getText().trim().matches("\\d{7,15}")) {
            sb.append("• El teléfono debe contener solo números y tener entre 7 y 15 dígitos.\n");
        }

        if (cmbCiudad.getValue() == null)
            sb.append("• Debes seleccionar una ciudad.\n");

        if (cmbEps.getValue() == null)
            sb.append("• Debes seleccionar una EPS.\n");

        return sb.length() == 0 ? null : sb.toString().trim();
    }

    /**
     * Crea un campo de texto configurado.
     *
     * @param valor valor inicial
     * @param prompt texto placeholder
     * @return campo configurado
     */
    private TextField campo(String valor, String prompt) {
        TextField tf = new TextField(valor == null ? "" : valor);
        tf.setPromptText(prompt);
        tf.setPrefHeight(34);
        return tf;
    }

    /**
     * Crea el combo de ciudades.
     *
     * @return combo configurado
     */
    private ComboBox<CiudadModel> comboCiudades() {
        ComboBox<CiudadModel> cmb = new ComboBox<>();
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(34);
        cmb.setPromptText("Selecciona una ciudad");
        cmb.getItems().setAll(ciudades);
        return cmb;
    }

    /**
     * Crea el combo de EPS.
     *
     * @return combo configurado
     */
    private ComboBox<EpsModel> comboEps() {
        ComboBox<EpsModel> cmb = new ComboBox<>();
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(34);
        cmb.setPromptText("Selecciona una EPS");
        cmb.getItems().setAll(eps);
        return cmb;
    }

    /**
     * Preselecciona una ciudad dentro del combo.
     *
     * @param cmb combo de ciudades
     * @param nombreCiudad nombre de la ciudad
     */
    private void preseleccionarCiudad(ComboBox<CiudadModel> cmb, String nombreCiudad) {
        cmb.getItems().stream()
                .filter(c -> c.getNombre() != null && c.getNombre().equals(nombreCiudad))
                .findFirst()
                .ifPresent(cmb::setValue);
    }

    /**
     * Preselecciona una EPS dentro del combo.
     *
     * @param cmb combo de EPS
     * @param nombreEps nombre de la EPS
     */
    private void preseleccionarEps(ComboBox<EpsModel> cmb, String nombreEps) {
        cmb.getItems().stream()
                .filter(e -> e.getNombre() != null && e.getNombre().equals(nombreEps))
                .findFirst()
                .ifPresent(cmb::setValue);
    }

    /**
     * Construye el layout principal del formulario.
     *
     * @return grid configurado
     */
    private GridPane crearGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 24, 16, 24));

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(100);
        c0.setHalignment(HPos.RIGHT);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);

        grid.getColumnConstraints().addAll(c0, c1);
        return grid;
    }

    /**
     * Agrega una fila al formulario.
     *
     * @param grid grid principal
     * @param fila índice de fila
     * @param etiqueta texto de la etiqueta
     * @param control control asociado
     */
    private void agregarFila(GridPane grid, int fila, String etiqueta, javafx.scene.Node control) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("etiqueta-form");
        grid.add(lbl, 0, fila);
        grid.add(control, 1, fila);
        if (control instanceof Region r) GridPane.setHgrow(r, Priority.ALWAYS);
    }

    /**
     * Carga los estilos CSS del diálogo.
     *
     * @param dialog diálogo objetivo
     */
    private void cargarEstilos(Dialog<?> dialog) {
        try {
            dialog.getDialogPane().getStylesheets().add(
                    getClass().getResource("/styles/paciente/paciente.css").toExternalForm());
        } catch (Exception ignored) {
        }
    }
}
