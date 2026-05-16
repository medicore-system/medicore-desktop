package views.ciudad;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.CiudadModel;
import services.CiudadService;

import java.util.function.Consumer;

/**
 * Diálogo reutilizable para la creación y edición de ciudades.
 *
 * <p>El código es generado automáticamente por el backend al crear.</p>
 *
 * <h2>Modo creación</h2>
 * <pre>new CiudadFormDialog(null, body -> controller.crear(body)).show();</pre>
 *
 * <h2>Modo edición</h2>
 * <pre>new CiudadFormDialog(ciudad, body -> controller.actualizar(ciudad.getCodigo(), body)).show();</pre>
 */
public class CiudadFormDialog {

    /** Ciudad a editar; {@code null} indica modo creación. */
    private final CiudadModel ciudadActual;

    /**
     * Callback ejecutado al confirmar.
     * Recibe {@code CiudadCreateBody} o {@code CiudadUpdateBody}.
     */
    private final Consumer<Object> onConfirmar;

    public CiudadFormDialog(CiudadModel ciudadActual, Consumer<Object> onConfirmar) {
        this.ciudadActual = ciudadActual;
        this.onConfirmar  = onConfirmar;
    }

    public void show() {
        boolean esEdicion = (ciudadActual != null);

        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle(esEdicion ? "Editar Ciudad" : "Nueva Ciudad");
        dialog.setHeaderText(esEdicion
                ? "Editando: " + ciudadActual.getNombre()
                : "Registrar una nueva ciudad");
        dialog.getDialogPane().getStyleClass().add("dialogo-ciudad");

        TextField txtNombre       = campoTexto(esEdicion ? ciudadActual.getNombre()       : "", "Ej: Manizales");
        TextField txtDepartamento = campoTexto(esEdicion ? ciudadActual.getDepartamento() : "", "Ej: Caldas");

        String estadoInicial = esEdicion
                ? (ciudadActual.getStatus() != null ? ciudadActual.getStatus() : "ACTIVE")
                : "ACTIVE";
        ComboBox<String> cmbStatus = comboStatus(estadoInicial);

        Label lblErrores = errorLabel();

        GridPane grid = formularioGrid();
        int fila = 0;
        agregarFila(grid, fila++, "Nombre",       txtNombre);
        agregarFila(grid, fila++, "Departamento", txtDepartamento);
        agregarFila(grid, fila++, "Estado",        cmbStatus);
        grid.add(lblErrores, 0, fila, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(460, 300);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK))
                .setText(esEdicion ? "Guardar cambios" : "Crear ciudad");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL))
                .setText("Cancelar");

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String error = validar(txtNombre, txtDepartamento, cmbStatus);
            if (error != null) { mostrarErrores(lblErrores, error); ev.consume(); }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            String nombre = txtNombre.getText().trim();
            String dep    = txtDepartamento.getText().trim();
            String st     = cmbStatus.getValue();
            if (esEdicion) {
                return new CiudadService.CiudadUpdateBody(nombre, dep, st);
            } else {
                return new CiudadService.CiudadCreateBody(nombre, dep, st);
            }
        });

        cargarEstilos(dialog);
        dialog.showAndWait().ifPresent(onConfirmar);
    }

    private String validar(TextField txtNombre,
                            TextField txtDepartamento,
                            ComboBox<String> cmbStatus) {
        StringBuilder sb = new StringBuilder();
        if (txtNombre.getText().trim().isEmpty())
            sb.append("• El nombre es obligatorio.\n");
        if (txtDepartamento.getText().trim().isEmpty())
            sb.append("• El departamento es obligatorio.\n");
        if (cmbStatus.getValue() == null)
            sb.append("• Debes seleccionar un estado.\n");
        return sb.length() == 0 ? null : sb.toString().trim();
    }

    private ComboBox<String> comboStatus(String inicial) {
        ComboBox<String> cmb = new ComboBox<>();
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setPrefHeight(36);
        cmb.getItems().addAll("ACTIVE", "INACTIVE");
        cmb.setValue(inicial);
        cmb.getStyleClass().add("campo-form");
        return cmb;
    }

    private TextField campoTexto(String inicial, String prompt) {
        TextField tf = new TextField(inicial == null ? "" : inicial);
        tf.setPromptText(prompt);
        tf.getStyleClass().add("campo-form");
        tf.setPrefHeight(36);
        return tf;
    }

    private GridPane formularioGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(22, 24, 18, 24));
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(110);
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

    private Label errorLabel() {
        Label l = new Label();
        l.getStyleClass().add("errores-form");
        l.setVisible(false);
        l.setManaged(false);
        l.setWrapText(true);
        return l;
    }

    private void mostrarErrores(Label lbl, String mensaje) {
        lbl.setText(mensaje);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void cargarEstilos(Dialog<?> dialog) {
        try {
            dialog.getDialogPane().getStylesheets().add(
                    getClass().getResource("/styles/ciudad/ciudad.css").toExternalForm());
        } catch (Exception ignored) {
        }
    }
}
