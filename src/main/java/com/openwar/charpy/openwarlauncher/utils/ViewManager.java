package com.openwar.charpy.openwarlauncher.utils;

import com.openwar.charpy.openwarlauncher.controller.AuthPageController;
import com.openwar.charpy.openwarlauncher.controller.MainPageController;
import com.openwar.charpy.openwarlauncher.controller.SettingsPageController;
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

    public void showPage(String fxmlPath, String title, int width, int height, PlayerProfile pf) {
        try {
            StackPane root = new StackPane();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/openwar/charpy/openwarlauncher/" + fxmlPath));
            Parent page = loader.load();
            Object controller = loader.getController();
            System.out.println("controller "+controller);
            if (controller instanceof AuthPageController) {
                System.out.println("AUTH View Manager for "+ controller);
                ((AuthPageController) controller).setViewManager(this);
            } else if (controller instanceof MainPageController) {
                System.out.println("MAIN View Manager for "+ controller);
                ((MainPageController) controller).setViewManager(this);
                ((MainPageController) controller).setPlayerProfile(pf);
            } else if (controller instanceof SettingsPageController) {
                System.out.println("SETTINGS View Manager for "+ controller);
                ((SettingsPageController) controller).setViewManager(this);
            }
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
