package it.unicam.cs.mpgc.jbudget120002.view;

import it.unicam.cs.mpgc.jbudget120002.controller.BaseController;
import it.unicam.cs.mpgc.jbudget120002.controller.LoginController;
import it.unicam.cs.mpgc.jbudget120002.controller.MainController;
import it.unicam.cs.mpgc.jbudget120002.model.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import javafx.scene.image.Image;

public class MainApp extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    private Stage primaryStage;
    private BaseController currentController;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("Family Budget Manager");
        // Set the window icon
        this.primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));

        showLoginView();

        // Add a close request handler to cleanup resources
        primaryStage.setOnCloseRequest(event -> {
            LOGGER.info("Application shutdown initiated");
            cleanup();
            Platform.exit();
        });
    }

    public void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(loader.load(), 900, 850); // Width x Height
            
            // Load the login CSS for modern styling
            scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
            
            primaryStage.setScene(scene);

            LoginController controller = loader.getController();
            controller.setMainApp(this);
            this.currentController = controller;

            primaryStage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to show login view", e);
        }
    }

    public void showMainView(User user) {
        try {
            // Cleanup the previous controller
            if (currentController != null) {
                currentController.cleanup();
            }

            UserSession.getInstance().setLoggedInUser(user);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Scene scene = new Scene(loader.load(), 1024, 768);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/statistics.css").toExternalForm());
            primaryStage.setScene(scene);

            MainController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setUserSession(UserSession.getInstance());
            this.currentController = controller;

            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to show main view", e);
        }
    }

    private void cleanup() {
        try {
            LOGGER.info("Cleaning up application resources");
            if (currentController != null) {
                currentController.cleanup();
            }
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
