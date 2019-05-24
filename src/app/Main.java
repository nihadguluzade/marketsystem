package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Alışveriş Uygulaması
 * @version 1.02 09-05-2019 - Added control if product is not available in store anymore.
 * @version 1.01 06-05-2019 - Added control for uniqueness of the newly added products.
 * @version 1.00 16-03-2019 - Initial Release.
 */

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Scene scene = new Scene(new StackPane());

        Manager manager = new Manager(scene);
        manager.showLoginPage(); // open login page

        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/app/resources/icon.png")));
        primaryStage.setTitle("Alışveriş Sistemi");
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

}
