package it.unicam.cs.mpgc.jbudget120002.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import java.io.IOException;

public class ViewUtils {

    /** Replaces the center of the given BorderPane with the loaded FXML view. */
    public static void loadView(BorderPane parent, String fxmlPath) {
        try {
            Pane pane = FXMLLoader.load(ViewUtils.class.getResource(fxmlPath));
            parent.setCenter(pane);
        } catch (IOException e) {
            e.printStackTrace();
            // You might want to show a dialog here
        }
    }
}
