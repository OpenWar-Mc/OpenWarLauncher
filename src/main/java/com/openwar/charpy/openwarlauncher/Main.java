package com.openwar.charpy.openwarlauncher;

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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;


public class Main extends Application {
    private ViewManager viewManager;

    @Override
    public void start(Stage stage) throws IOException {
        StackPane root = new StackPane();
        viewManager = new ViewManager(root);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/openwar/charpy/openwarlauncher/AuthPage.fxml"));
        Parent authPage = loader.load();
        AuthPageController authPageController = loader.getController();
        authPageController.setViewManager(viewManager);
        Path path = Paths.get(System.getenv("APPDATA"), ".openwar");
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println("Directory created at: " + path);

                File dir = path.toFile();
                dir.setReadable(true, false);
                dir.setWritable(true, false);
                dir.setExecutable(true, false);

                Path testFile = path.resolve("test.txt");
                Files.createFile(testFile);
                System.out.println("Test file created at: " + testFile);
                Files.delete(testFile);


            } catch (IOException e) {
                System.err.println("Failed to create directory: " + e.getMessage());
            }
        }
        stage.setTitle("OpenWar - Launcher | Authentication");
        stage.getIcons().add(new Image("https://openwar.fr/public/images/op.png"));
        stage.setResizable(false);
        stage.setScene(new Scene(root, 400, 640));
        root.getChildren().add(authPage);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}