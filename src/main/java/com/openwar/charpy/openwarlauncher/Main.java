package com.openwar.charpy.openwarlauncher;

import com.openwar.charpy.openwarlauncher.controller.AuthPageController;
import com.openwar.charpy.openwarlauncher.utils.ViewManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Main extends Application {
    private ViewManager viewManager;

    @Override
    public void start(Stage stage) {
        viewManager = new ViewManager(stage);
        viewManager.showPage("AuthPage.fxml", "OpenWar - Authentication", 400, 640);
    }

    public static void main(String[] args) {
        launch();
    }
}