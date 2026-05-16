package views.medico;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import models.*;
import services.MedicoService;

import java.util.List;
import java.util.function.Consumer;
/**
 * Diálogo encargado de crear o editar médicos dentro del sistema.
 *
 * Permite diligenciar la información básica del médico,
 * validar los datos ingresados y enviar el resultado
 * mediante un callback de confirmación.
 */
public class MedicoFormDialog {

    /**
     * Médico actual a editar.
     * Si es null, el formulario se encuentra en modo creación.
     */
    private final MedicoModel medicoActual;

    /**
     * Lista de ciudades disponibles.
     */
    private final List<CiudadModel> ciudades;

    /**
     * Lista de especialidades disponibles.
     */
    private final List<EspecialidadModel> especialidades;

    /**
     * Callback ejecutado al confirmar el formulario.
     */
    private final Consumer<Object> onConfirmar;

    /**
     * Constructor del diálogo.
     *
     * @param medicoActual usuario actual o {@code null} para crear
     * @param ciudades lista de ciudades disponibles
     * @param especialidades lista de EPS disponibles
     * @param onConfirmar callback ejecutado al confirmar
     */
    public MedicoFormDialog(MedicoModel medicoActual,
                             List<CiudadModel> ciudades,
                             List<EspecialidadModel> especialidades,
                             Consumer<Object> onConfirmar) {
        this.medicoActual   = medicoActual;
        this.ciudades       = ciudades;
        this.especialidades = especialidades;
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
        boolean modoCrear = (medicoActual == null);

        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle(modoCrear ? "Nuevo Médico" : "Editar Médico");
        dialog.setHeaderText(modoCrear
                ? "Registrar un nuevo médico"
                : "Editando: " + medicoActual.getNombre()+ " " + medicoActual.getApellido());

        // ── Campos ──────────────────────────────────────────────────────
        TextField txtDocumento = campo(modoCrear ? "" : medicoActual.getDocumento(), "Ej: 1234567890");
        txtDocumento.setDisable(!modoCrear); // el documento no se puede cambiar al editar

        TextField txtNombre    = campo(modoCrear ? "" : medicoActual.getNombre(),   "Nombre");
        TextField txtApellido  = campo(modoCrear ? "" : medicoActual.getApellido(), "Apellido");
        TextField txtCorreo    = campo(modoCrear ? "" : medicoActual.getEmail(),   "correo@ejemplo.com");
        TextField txtTelefono  = campo(modoCrear ? "" : medicoActual.getTelefono(), "Ej: 3001234567");

        ComboBox<CiudadModel> cmbCiudad = comboCiudades();
        if (!modoCrear) preseleccionarCiudad(cmbCiudad, medicoActual.getNombreCiudad());

        ComboBox<EspecialidadModel> cmbEspecialidad = comboEspecialidad();
        if (!modoCrear) preseleccionarEspecialidad(cmbEspecialidad, medicoActual.getNombreEspecialidad());

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
        agregarFila(grid, fila++, "Especialidad",        cmbEspecialidad);
        agregarFila(grid, fila++, "Teléfono",   txtTelefono);
        agregarFila(grid, fila++, "Correo", txtCorreo);
        agregarFila(grid, fila++, "Ciudad",     cmbCiudad);

        grid.add(lblErrores, 0, fila, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(500, modoCrear ? 560 : 480);
        dialog.getDialogPane().getStyleClass().add("dialogo-paciente");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK))
                .setText(modoCrear ? "Crear médico" : "Guardar cambios");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL))
                .setText("Cancelar");

        // Validación antes de cerrar
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String error = validar(
                    modoCrear,
                    txtDocumento,
                    txtNombre,
                    txtApellido,
                    txtTelefono,
                    txtCorreo,
                    cmbCiudad,
                    cmbEspecialidad
            );
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
            EspecialidadModel    espVal = cmbEspecialidad.getValue();

            if (modoCrear) {
                return new MedicoService.MedicoCreateBody(
                        txtDocumento.getText().trim(),
                        txtNombre.getText().trim(),
                        txtApellido.getText().trim(),
                        txtCorreo.getText().trim(),
                        txtTelefono.getText().trim(),
                        ciudad.getCodigo(),
                        espVal.getId()
                );
            } else {
                return new MedicoService.MedicoUpdateBody(
                        txtNombre.getText().trim(),
                        txtApellido.getText().trim(),
                        espVal.getId(),
                        txtTelefono.getText().trim(),
                        txtCorreo.getText().trim(),
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
     * @param cmbEsp combo de Especialidad
     * @return mensaje de error o {@code null} si todo es válido
     */
    private String validar(boolean modoCrear,
                           TextField txtDocumento,
                           TextField txtNombre,
                           TextField txtApellido,
                           TextField txtTelefono,
                           TextField txtCorreo,
                           ComboBox<CiudadModel> cmbCiudad,
                           ComboBox<EspecialidadModel> cmbEsp) {
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

        if (txtCorreo.getText().trim().isEmpty() ||
                !txtCorreo.getText().trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            sb.append("• El correo no es válido.Recuerde el @ o .\n");
        }

        if (!txtTelefono.getText().trim().matches("\\d{7,15}"))
            sb.append("• El teléfono debe contener entre 7 y 15 dígitos.\n");

        if (cmbCiudad.getValue() == null)
            sb.append("• Debes seleccionar una ciudad.\n");

        if (cmbEsp.getValue() == null)
            sb.append("• Debes seleccionar una especialidad.\n");

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
     * Crea el combo de Especialidades.
     *
     * @return combo configurado
     */
    private ComboBox<EspecialidadModel> comboEspecialidad() {
        ComboBox<EspecialidadModel> cmb = new ComboBox<>();
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(34);
        cmb.setPromptText("Selecciona una especialidad");
        cmb.getItems().setAll(especialidades);
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
     * Preselecciona una especilidad dentro del combo.
     *
     * @param cmb combo de Especilidad
     * @param nombreEspecilidad nombre de la EPS
     */
    private void preseleccionarEspecialidad(ComboBox<EspecialidadModel> cmb, String nombreEspecilidad) {
        cmb.getItems().stream()
                .filter(e -> e.getNombre() != null && e.getNombre().equals(nombreEspecilidad))
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
