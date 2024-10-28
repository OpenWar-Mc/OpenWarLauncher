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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/openwar/charpy/openwarlauncher/AuthPage.fxml"));
            Parent view = loader.load();
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