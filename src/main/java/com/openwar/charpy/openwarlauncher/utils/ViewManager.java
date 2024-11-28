package com.openwar.charpy.openwarlauncher.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewManager {
    private final Stage stage;

    public ViewManager(Stage stage) {
        this.stage = stage;
    }

    public void showPage(String fxmlPath, String title, int width, int height) {
        try {
            StackPane root = new StackPane();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/openwar/charpy/openwarlauncher/" + fxmlPath));
            Parent page = loader.load();
            root.getChildren().add(page);
            stage.setResizable(false);
            stage.setScene(new Scene(root, width, height));
            stage.getIcons().add(new Image("https://openwar.fr/public/images/op.png"));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la page : " + fxmlPath);
        }
    }
}
