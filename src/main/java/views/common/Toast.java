package views.common;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

/**
 * Notificación tipo toast con tarjeta moderna: barra lateral de color,
 * icono dentro de un círculo, título + descripción y botón de cerrar.
 *
 * <p>Las tarjetas se apilan en la parte superior central de la ventana
 * para no ocultar la barra lateral u otros controles.</p>
 *
 * Uso típico:
 *   Toast.success(rootNode, "Hospital actualizado correctamente");
 *   Toast.error(rootNode, "No se pudo guardar el cambio");
 */
public final class Toast {

    private Toast() {}

    public static void success(Node anchor, String mensaje) {
        mostrar(anchor, "Éxito", mensaje, "toast-success", "✓");
    }

    public static void error(Node anchor, String mensaje) {
        mostrar(anchor, "Error", mensaje, "toast-error", "✕");
    }

    public static void info(Node anchor, String mensaje) {
        mostrar(anchor, "Información", mensaje, "toast-info", "i");
    }

    public static void warning(Node anchor, String mensaje) {
        mostrar(anchor, "Atención", mensaje, "toast-warning", "!");
    }

    private static void mostrar(Node anchor, String titulo, String descripcion,
                                String estiloExtra, String icono) {
        if (anchor == null) return;

        StackPane overlay = obtenerOverlay(anchor);
        if (overlay == null) return;

        // Círculo con icono
        Label lblIcono = new Label(icono);
        lblIcono.getStyleClass().add("toast-icono");

        StackPane circuloIcono = new StackPane(lblIcono);
        circuloIcono.getStyleClass().add("toast-circulo");

        // Texto principal
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("toast-titulo");

        Label lblDescripcion = new Label(descripcion);
        lblDescripcion.getStyleClass().add("toast-descripcion");
        lblDescripcion.setWrapText(true);
        lblDescripcion.setMaxWidth(280);

        VBox texto = new VBox(2, lblTitulo, lblDescripcion);
        HBox.setHgrow(texto, Priority.ALWAYS);

        // Botón cerrar
        Button btnCerrar = new Button("✕");
        btnCerrar.getStyleClass().add("toast-cerrar");

        // Barra lateral coloreada
        Region barra = new Region();
        barra.getStyleClass().add("toast-barra");

        HBox tarjeta = new HBox(12, circuloIcono, texto, btnCerrar);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.getStyleClass().add("toast-tarjeta");

        HBox toast = new HBox(barra, tarjeta);
        toast.getStyleClass().addAll("toast", estiloExtra);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setMaxWidth(420);
        toast.setPickOnBounds(false);
        HBox.setHgrow(tarjeta, Priority.ALWAYS);

        VBox contenedor = obtenerContenedorToasts(overlay);
        contenedor.getChildren().add(toast);

        // Animaciones: deslizar desde arriba + fade in
        toast.setTranslateY(-20);
        toast.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toast);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), toast);
        slideIn.setFromY(-20); slideIn.setToY(0);

        PauseTransition espera = new PauseTransition(Duration.seconds(3));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(280), toast);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);

        SequentialTransition seq = new SequentialTransition(fadeIn, espera, fadeOut);
        slideIn.play();
        seq.setOnFinished(e -> Platform.runLater(() -> contenedor.getChildren().remove(toast)));
        seq.play();

        btnCerrar.setOnAction(e -> {
            seq.stop();
            FadeTransition cerrar = new FadeTransition(Duration.millis(180), toast);
            cerrar.setFromValue(toast.getOpacity()); cerrar.setToValue(0);
            cerrar.setOnFinished(ev -> contenedor.getChildren().remove(toast));
            cerrar.play();
        });
    }

    private static StackPane obtenerOverlay(Node anchor) {
        Parent root = anchor.getScene() != null ? anchor.getScene().getRoot() : null;
        if (root instanceof StackPane sp) return sp;
        if (root != null) {
            Node existente = root.lookup("#toast-overlay");
            if (existente instanceof StackPane sp) return sp;
        }
        return null;
    }

    private static VBox obtenerContenedorToasts(StackPane overlay) {
        for (Node n : overlay.getChildren()) {
            if ("toast-stack".equals(n.getId()) && n instanceof VBox v) return v;
        }
        VBox stack = new VBox(10);
        stack.setId("toast-stack");
        stack.setMouseTransparent(false);
        stack.setPickOnBounds(false);
        stack.setAlignment(Pos.TOP_CENTER);
        stack.setPadding(new Insets(24, 0, 0, 0));
        StackPane.setAlignment(stack, Pos.TOP_CENTER);
        overlay.getChildren().add(stack);
        return stack;
    }
}
