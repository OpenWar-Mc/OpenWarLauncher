package com.openwar.charpy.openwarlauncher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.openwar.charpy.openwarlauncher.LocalServer.localServer;

public class Main extends Application {
    private ViewManager viewManager;

    @Override
    public void start(Stage stage) throws IOException {
        StackPane root = new StackPane();
        viewManager = new ViewManager(root);

        stage.setTitle("OpenWar - Launcher | Authentication");
        stage.getIcons().add(new Image("https://openwar.fr/public/images/op.png"));
        stage.setResizable(false);

        // stage.setScene(new Scene(root, 1080, 720));
        stage.setScene(new Scene(root, 400, 640));
        viewManager.loadView("AuthPage.fxml");
        if (!isTokenValid()) {
            localServer();
        }
        stage.show();
    }

    private boolean isTokenValid() {
        Path tokenPath = Path.of("C:\\Users\\mazin\\AppData\\Roaming\\.openwar\\launcher_token.json");
        if (Files.exists(tokenPath)) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        launch();
    }
}