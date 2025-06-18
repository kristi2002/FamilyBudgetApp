package it.unicam.cs.mpgc.jbudget120002.view;

import it.unicam.cs.mpgc.jbudget120002.controller.BaseController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.logging.Logger;
import java.util.logging.Level;
import javafx.scene.image.Image;

public class MainApp extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Use the classloader to load from /fxml/MainView.fxml on the classpath
            FXMLLoader loader = new FXMLLoader(
                    Thread.currentThread()
                            .getContextClassLoader()
                            .getResource("fxml/MainView.fxml")
            );
            Scene scene = new Scene(loader.load(), 1024, 768);
            // Add global stylesheet for positive/negative coloring
            scene.getStylesheets().add(getClass().getResource("/css/statistics.css").toExternalForm());
            stage.setTitle("Family Budget Manager");
            stage.setScene(scene);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            // Set the window icon
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));
            stage.show();

            // Add a close request handler to cleanup resources
            stage.setOnCloseRequest(event -> {
                LOGGER.info("Application shutdown initiated");
                cleanup();
                Platform.exit();
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start application", e);
            throw e;
        }
    }

    private void cleanup() {
        try {
            LOGGER.info("Cleaning up application resources");
            BaseController.closeEntityManagerFactory();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during cleanup", e);
        }
    }

    @Override
    public void stop() {
        cleanup();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
