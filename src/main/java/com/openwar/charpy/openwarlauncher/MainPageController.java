package com.openwar.charpy.openwarlauncher;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainPageController {

    @FXML
    private ImageView backgroundImage;

    @FXML
    private Button playerButton;


    private PlayerProfile playerProfile;
    public void setPlayerProfile(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }

    @FXML
    public void initialize() {
        backgroundImage.setImage(new Image("https://openwar.fr/public/images/background.png"));
        Path options = Paths.get(System.getenv("APPDATA"), ".openwar\\options.txt");
        if (Files.exists(options)) {
            playerButton.setText("Download");
            installingMinecraft();
        }
        else {
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

    }

    private void handlePlayButtonAction() throws IOException {
        LaunchMinecraft lm = new LaunchMinecraft();
        lm.startMinecraft(playerProfile.getUsername(),playerProfile.getUuid(),playerProfile.getToken());
    }
}
