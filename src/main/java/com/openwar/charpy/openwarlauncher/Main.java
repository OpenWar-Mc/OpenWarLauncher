package com.openwar.charpy.openwarlauncher;

import com.openwar.charpy.openwarlauncher.utils.ViewManager;
import javafx.application.Application;
import javafx.stage.Stage;



public class Main extends Application {
    private ViewManager viewManager;

    @Override
    public void start(Stage stage) {
        viewManager = new ViewManager(stage);
        viewManager.showPage("AuthPage.fxml", "OpenWar - Authentication", 400, 640, null);
    }

    public static void main(String[] args) {
        launch();
    }
}