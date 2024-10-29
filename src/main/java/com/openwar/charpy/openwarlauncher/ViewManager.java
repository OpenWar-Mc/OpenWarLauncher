package com.openwar.charpy.openwarlauncher;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class ViewManager {
    private StackPane root;

    public ViewManager(StackPane root) {
        this.root = root;
    }

    public void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/openwar/charpy/openwarlauncher/" + fxmlFile));
            Parent view = loader.load();
            Object controller = loader.getController();
            if (controller instanceof AuthPageController) {
                ((AuthPageController) controller).setViewManager(this);
            }

            root.getChildren().clear();
            root.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StackPane getRoot() {
        return root;
    }
}
