import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import views.auth.LoginView;
import views.mainLayout.MainLayout;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        //Creamos el nodo raiz que es el login que redirecciona a main layout si es eitoso
        LoginView loginView = new LoginView(stage);
        //Envolvemos en un StackPane para permitir overlays (notificaciones toast, etc.)
        StackPane root = new StackPane(loginView);
        root.setId("toast-overlay");
        //Creamos la scene que guarda el panel a mostrar en el stage
        Scene scene = new Scene(root, 520, 680);
        //Añadimos nuestra hoja de estilos global
        scene.getStylesheets().add(getClass().getResource("styles/app.css").toExternalForm());
        //Añadimos al Stage toda nuestra scene ya lista para mostrar
        stage.setScene(scene);
        stage.setTitle("Medicore");
        stage.getIcons().add(new Image(getClass().getResource("icon/hospital.png").toExternalForm()));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
