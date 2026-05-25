package views.historial;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.MedicoModel;
import models.UsuarioModel;
import services.HistorialService;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/**
 * Diálogo para registrar un nuevo historial clínico.
 *
 * <p>El médico elige: código, fecha, tipo, descripción, paciente y médico.
 * Al confirmar, el resultado se entrega al callback onConfirmar.</p>
 */
public class HistorialFormDialog {

    private static final String STYLESHEET = "/styles/servicio/servicio.css";

    private final List<UsuarioModel> pacientes;
    private final List<MedicoModel>  medicos;
    private final Consumer<HistorialService.HistorialCreateBody> onConfirmar;

    /**
     * @param pacientes    lista de usuarios disponibles como pacientes
     * @param medicos      lista de médicos disponibles
     * @param onConfirmar  callback ejecutado al confirmar con los datos del historial
     */
    public HistorialFormDialog(List<UsuarioModel> pacientes,
                               List<MedicoModel>  medicos,
                               Consumer<HistorialService.HistorialCreateBody> onConfirmar) {
        this.pacientes   = pacientes;
        this.medicos     = medicos;
        this.onConfirmar = onConfirmar;
    }

    /** Construye y muestra el diálogo modal. */
    public void show() {
        Dialog<HistorialService.HistorialCreateBody> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Historial Clínico");
        dialog.setHeaderText("Registrar un nuevo historial clínico");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource(STYLESHEET).toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialogo-servicio");
        dialog.getDialogPane().setPrefSize(560, 520);

        // ── Campos ──────────────────────────────────────────────────────────────
        TextField    txtCodigo  = campo("HC005", "Ej: HC005");
        DatePicker   dpFecha    = new DatePicker(LocalDate.now());
        dpFecha.getStyleClass().add("campo-form");
        dpFecha.setMaxWidth(Double.MAX_VALUE);
        dpFecha.setPrefHeight(36);

        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("Consulta", "Urgencia", "Procedimiento", "Examen", "Control");
        cmbTipo.setPromptText("Selecciona el tipo de atención");
        cmbTipo.getStyleClass().add("campo-form");
        cmbTipo.setMaxWidth(Double.MAX_VALUE);
        cmbTipo.setPrefHeight(36);

        TextArea taDesc = new TextArea();
        taDesc.setPromptText("Describe la atención médica realizada...");
        taDesc.setPrefRowCount(3);
        taDesc.setWrapText(true);
        taDesc.getStyleClass().add("campo-form");

        ComboBox<UsuarioModel> cmbPaciente = new ComboBox<>();
        cmbPaciente.getItems().addAll(pacientes);
        cmbPaciente.setPromptText("Selecciona el paciente");
        cmbPaciente.getStyleClass().add("campo-form");
        cmbPaciente.setMaxWidth(Double.MAX_VALUE);
        cmbPaciente.setPrefHeight(36);
        // Mostrar nombre completo en el ComboBox
        cmbPaciente.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(UsuarioModel u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? "" : u.getNombre() + " " + u.getApellido() + " (" + u.getDocumento() + ")");
            }
        });
        cmbPaciente.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(UsuarioModel u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? "" : u.getNombre() + " " + u.getApellido() + " (" + u.getDocumento() + ")");
            }
        });

        ComboBox<MedicoModel> cmbMedico = new ComboBox<>();
        cmbMedico.getItems().addAll(medicos);
        cmbMedico.setPromptText("Selecciona el médico");
        cmbMedico.getStyleClass().add("campo-form");
        cmbMedico.setMaxWidth(Double.MAX_VALUE);
        cmbMedico.setPrefHeight(36);
        cmbMedico.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(MedicoModel m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty || m == null ? "" : "Dr. " + m.getNombre() + " " + m.getApellido() + " (" + m.getDocumento() + ")");
            }
        });
        cmbMedico.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(MedicoModel m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty || m == null ? "" : "Dr. " + m.getNombre() + " " + m.getApellido() + " (" + m.getDocumento() + ")");
            }
        });

        Label lblErrores = new Label();
        lblErrores.getStyleClass().add("errores-form");
        lblErrores.setVisible(false);
        lblErrores.setManaged(false);
        lblErrores.setWrapText(true);

        // ── Grid ────────────────────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(22, 24, 18, 24));

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(130);
        c0.setHalignment(HPos.RIGHT);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);
        grid.getColumnConstraints().addAll(c0, c1);

        agregarFila(grid, 0, "Código",     txtCodigo);
        agregarFila(grid, 1, "Fecha",      dpFecha);
        agregarFila(grid, 2, "Tipo",       cmbTipo);
        agregarFila(grid, 3, "Descripción",taDesc);
        agregarFila(grid, 4, "Paciente",   cmbPaciente);
        agregarFila(grid, 5, "Médico",     cmbMedico);
        grid.add(lblErrores, 0, 6, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Registrar historial");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        // ── Validación antes de confirmar ────────────────────────────────────────
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String error = validar(txtCodigo, dpFecha, cmbTipo, taDesc, cmbPaciente, cmbMedico);
            if (error != null) {
                lblErrores.setText(error);
                lblErrores.setVisible(true);
                lblErrores.setManaged(true);
                ev.consume();
            }
        });

        // ── Resultado ────────────────────────────────────────────────────────────
        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            return new HistorialService.HistorialCreateBody(
                    txtCodigo.getText().trim(),
                    dpFecha.getValue(),
                    cmbTipo.getValue(),
                    taDesc.getText().trim(),
                    cmbPaciente.getValue().getDocumento(),
                    cmbMedico.getValue().getDocumento()
            );
        });

        dialog.showAndWait().ifPresent(onConfirmar);
    }

    // ── Validación ─────────────────────────────────────────────────────────────

    private String validar(TextField txtCodigo, DatePicker dpFecha,
                           ComboBox<String> cmbTipo, TextArea taDesc,
                           ComboBox<UsuarioModel> cmbPaciente, ComboBox<MedicoModel> cmbMedico) {
        StringBuilder sb = new StringBuilder();

        String codigo = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
        if (codigo.isBlank())          sb.append("• El código del historial es obligatorio\n");
        else if (codigo.length() > 50) sb.append("• El código no puede superar 50 caracteres\n");

        if (dpFecha.getValue() == null) sb.append("• La fecha es obligatoria\n");

        if (cmbTipo.getValue() == null) sb.append("• Selecciona un tipo de atención\n");

        String desc = taDesc.getText() == null ? "" : taDesc.getText().trim();
        if (desc.isBlank())           sb.append("• La descripción es obligatoria\n");
        else if (desc.length() > 200) sb.append("• La descripción no puede superar 200 caracteres\n");

        if (cmbPaciente.getValue() == null) sb.append("• Selecciona un paciente\n");
        if (cmbMedico.getValue()   == null) sb.append("• Selecciona un médico\n");

        return sb.length() == 0 ? null : sb.toString().trim();
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private void agregarFila(GridPane grid, int fila, String etiqueta, javafx.scene.Node control) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("etiqueta-form");
        grid.add(lbl, 0, fila);
        grid.add(control, 1, fila);
        if (control instanceof Region r) GridPane.setHgrow(r, Priority.ALWAYS);
    }

    private TextField campo(String inicial, String prompt) {
        TextField tf = new TextField(inicial);
        tf.setPromptText(prompt);
        tf.getStyleClass().add("campo-form");
        tf.setPrefHeight(36);
        return tf;
    }
}
