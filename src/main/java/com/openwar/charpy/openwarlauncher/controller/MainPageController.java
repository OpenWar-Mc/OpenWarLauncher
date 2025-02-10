package com.openwar.charpy.openwarlauncher.controller;

import com.openwar.charpy.openwarlauncher.utils.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainPageController {

    @FXML
    private ImageView backgroundImage;

    @FXML
    private ImageView newsIMG;

    @FXML
    private Button playerButton;
    @FXML
    private Button disconnect;

    @FXML
    private ImageView skinplayer;

    @FXML
    private Label nameplayer;

    @FXML
    private Button settings;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TextFlow newsText;

    private PlayerProfile playerProfile;
    private ViewManager viewManager;

    private int gb = 6;
    private int width = 854;
    private int height = 480;

    public void setGb(int gb) { this.gb = gb;}
    public void setWidth(int width) { this.width = width;}
    public void setHeight(int height) { this.height = height;}

    public void setPlayerProfile(PlayerProfile playerProfile) {
        System.out.println("PLAYER PROFILE" + playerProfile.getUuid());
        this.playerProfile = playerProfile;
        skinplayer.setImage(new Image("https://crafatar.com/renders/body/" + playerProfile.getUuid()));
        nameplayer.setText(playerProfile.getUsername());
    }
    @FXML
    public void initialize() {
        SettingsManager.getInstance().loadSettings();
        loadNewsText();
        backgroundImage.setImage(new Image(String.valueOf(getClass().getResource("/com/openwar/charpy/openwarlauncher/images/background.png"))));
        Path options = Paths.get(System.getenv("APPDATA"), ".openwar\\versions\\1.12.2-forge-14.23.5.2860\\1.12.2-forge-14.23.5.2860.jar");
        settings.setOnAction(event -> {
            handleSettingsAction();
        });
        disconnect.setOnAction(actionEvent -> {
            handleDisconnectButtonAction();
        });
        if (!Files.exists(options)) {
            playerButton.setText("Download");
            playerButton.setOnAction(event -> {
                installingMinecraft();
            });
        } else {
            playerButton.setOnAction(event -> {
                try {
                    handlePlayButtonAction();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void installingMinecraft() {
        progressBar.setVisible(true);
        InstallOpenWar installer = new InstallOpenWar(progressBar);

        new Thread(() -> {
            installer.install(v -> {
                Platform.runLater(() -> {
                    playerButton.setText("Play");
                    playerButton.setOnAction(event -> {
                        try {
                            handlePlayButtonAction();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                });
            });
        }).start();
    }
    public void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    private void handleDisconnectButtonAction() {
        Path auth = whatOsIsThis();
        File profile = auth.toFile();

        System.out.println("Trying to delete: " + profile.getAbsolutePath());
        System.out.println("File exists? " + profile.exists());
        System.out.println("File writable? " + profile.canWrite());

        if (profile.exists() && profile.delete()) {
            System.out.println("Disconnected");
        } else {
            System.out.println("FILE CANNOT BE DELETED! LOL?");
        }

        viewManager.showPage("AuthPage.fxml", "OpenWar - Authentication", 400, 640, null);
    }
    private Path whatOsIsThis() {
        String osName = System.getProperty("os.name").toLowerCase();
        Path appDataPath;

        if (osName.contains("win")) {
            appDataPath = Paths.get(System.getenv("APPDATA"), ".openwar", "launcher_profiles");
        } else if (osName.contains("mac")) {
            appDataPath = Paths.get(System.getProperty("user.home"), "Library", "Application Support", ".openwar", "launcher_profiles");
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            appDataPath = Paths.get(System.getProperty("user.home"), ".config", ".openwar", "launcher_profiles");
        } else {
            throw new UnsupportedOperationException("OS non supporté : " + osName);
        }

        return appDataPath;
    }
    private void handleSettingsAction() {
        viewManager.showModal("SettingsPage.fxml", "Settings", 300, 300);
    }

    private void handlePlayButtonAction() throws IOException {
        progressBar.setVisible(true);
        SettingsManager settings = SettingsManager.getInstance();
        UpdateGame up = new UpdateGame(progressBar);
        up.updateGame(
                playerProfile.getToken(),
                playerProfile.getUsername(),
                playerProfile.getUuid(),
                settings.getGb(),
                settings.getWidth(),
                settings.getHeight()
        );
    }

    private void loadNewsText() {
        String urlIMG = "https://openwar.fr/public/news/newsIMG.png";
        newsIMG.setImage(new Image(urlIMG));
        String urlString = "https://openwar.fr/public/news/news.txt";
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder content = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                    Platform.runLater(() -> displayNewsText(content.toString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void displayNewsText(String newsContent) {
        newsText.getChildren().clear();
        for (String line : newsContent.split("\n")) {
            Text text = new Text(line + "\n");
            text.setStyle("-fx-fill: white;");
            text.setFont(Font.font(16));
            newsText.getChildren().add(text);
        }
    }
}
