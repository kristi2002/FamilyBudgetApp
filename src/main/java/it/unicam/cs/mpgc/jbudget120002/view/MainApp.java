package it.unicam.cs.mpgc.jbudget120002.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Use the classloader to load from /fxml/MainView.fxml on the classpath
        FXMLLoader loader = new FXMLLoader(
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResource("fxml/MainView.fxml")
        );
        Scene scene = new Scene(loader.load(), 1024, 768);
        stage.setTitle("Family Budget Manager");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
