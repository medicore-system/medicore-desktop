package views.asignacion.asignacionesMedico;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.AsignacionMedicoService;

import java.util.function.Consumer;

public class AsignacionFormDialog {

    private final String documentoMedico;
    private final String codigoCiudad;
    private final Consumer<Object> onGuardar;

    public AsignacionFormDialog(String documentoMedico,
                                String codigoCiudad,
                                Consumer<Object> onGuardar) {
        this.documentoMedico = documentoMedico;
        this.codigoCiudad    = codigoCiudad;
        this.onGuardar       = onGuardar;
    }

    public void show() {
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle("Nueva Asignación");
        dialog.setHeaderText("Crear asignación de médico");

        // ── Campos ──────────────────────────────────────────────────────
        TextField txtDias = campo("", "Ej: 7");

        TextField txtHospital = campo("", "Opcional — vacío para asignación automática");

        Label lblErrores = new Label();
        lblErrores.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 12px;");
        lblErrores.setVisible(false);
        lblErrores.setManaged(false);
        lblErrores.setWrapText(true);

        // ── Grid ────────────────────────────────────────────────────────
        GridPane grid = crearGrid();
        agregarFila(grid, 0, "Duración (días)", txtDias);
        agregarFila(grid, 1, "Código hospital", txtHospital);
        grid.add(lblErrores, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(440, 240);
        dialog.getDialogPane().getStyleClass().add("dialogo-paciente");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Crear asignación");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        // ── Validación ──────────────────────────────────────────────────
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String error = validar(txtDias);
            if (error != null) {
                lblErrores.setText(error);
                lblErrores.setVisible(true);
                lblErrores.setManaged(true);
                ev.consume();
            }
        });

        // ── Result converter ────────────────────────────────────────────
        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            String hospital = txtHospital.getText().trim();
            return new AsignacionMedicoService.AsignacionCreateBody(
                    documentoMedico,
                    codigoCiudad,
                    Integer.parseInt(txtDias.getText().trim()),
                    hospital.isBlank() ? null : hospital
            );
        });

        cargarEstilos(dialog);
        dialog.showAndWait().ifPresent(onGuardar);
    }

    private String validar(TextField txtDias) {
        StringBuilder sb = new StringBuilder();

        if (txtDias.getText().trim().isEmpty()) {
            sb.append("• La duración en días es obligatoria.\n");
        } else {
            try {
                int dias = Integer.parseInt(txtDias.getText().trim());
                if (dias < 1) sb.append("• La duración mínima es 1 día.\n");
            } catch (NumberFormatException e) {
                sb.append("• La duración debe ser un número entero.\n");
            }
        }

        return sb.length() == 0 ? null : sb.toString().trim();
    }

    private TextField campo(String valor, String prompt) {
        TextField tf = new TextField(valor);
        tf.setPromptText(prompt);
        tf.setPrefHeight(34);
        return tf;
    }

    private GridPane crearGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 24, 16, 24));

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(130);
        c0.setHalignment(HPos.RIGHT);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);

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

    private void cargarEstilos(Dialog<?> dialog) {
        try {
            dialog.getDialogPane().getStylesheets().add(
                    getClass().getResource("/styles/paciente/paciente.css").toExternalForm());
        } catch (Exception ignored) {}
    }
}