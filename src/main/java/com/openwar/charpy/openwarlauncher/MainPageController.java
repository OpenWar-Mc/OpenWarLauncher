package com.openwar.charpy.openwarlauncher;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.BufferedReader;
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
    private ImageView skinplayer;

    @FXML
    private Label nameplayer;

    @FXML
    private ProgressBar progressBar;
    @FXML
    private TextFlow newsText;

    private PlayerProfile playerProfile;


    public void setPlayerProfile(PlayerProfile playerProfile) {
        System.out.println("PLAYER PROFILE" + playerProfile.getUuid());
        this.playerProfile = playerProfile;
        skinplayer.setImage(new Image("https://crafatar.com/renders/body/" + playerProfile.getUuid()));
        nameplayer.setText(playerProfile.getUsername());
    }
    @FXML
    public void initialize() {
        loadNewsText();
        backgroundImage.setImage(new Image("https://openwar.fr/public/images/background.png"));
        Path options = Paths.get(System.getenv("APPDATA"), ".openwar\\options.txt");
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

    private void handlePlayButtonAction() throws IOException {
        progressBar.setVisible(true);
        LaunchMinecraft lm = new LaunchMinecraft(progressBar);
        lm.startMinecraft(playerProfile.getToken(),playerProfile.getUsername(),playerProfile.getUuid());
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
            newsText.getChildren().add(text);
        }
    }
}
