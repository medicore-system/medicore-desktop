import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        //CREAMOS UN BOTON
        Button button = new Button("Presióname");

        StackPane root = new StackPane(button);

        Scene scene = new Scene(root, 500, 400);

        // AQUI CARGAMOS LA HOJA DE ESTILOS QUE VAMOS A APLICAR PARA EL EJEMPLO
        scene.getStylesheets().add(
                getClass().getResource("styles/app.css").toExternalForm()
        );

        stage.setTitle("Medicore-ADMIN");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}