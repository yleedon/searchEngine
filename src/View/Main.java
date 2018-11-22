package View;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Search Engine");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("View.fxml").openStream());
        Scene scene = new Scene(root, 600*1.5, 400*1.5);

        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("View.css").toExternalForm());
        primaryStage.setScene(scene);
        //--------------

        //--------------
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
