package com.openwar.charpy.openwarlauncher;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class AuthPageController {

    private static final String CLIENT_ID = "9cef8cab-23eb-49a1-8c71-d3c3b81d98a4";
    private static final String REDIRECT_URI = "http://localhost:3000/auth/redirect";
    private static final String AUTH_URL = "https://login.live.com/oauth20_authorize.srf";
    private static final String SCOPE = "XboxLive.signin offline_access";
    private ViewManager viewManager;
    @FXML
    private Button authButton;

    @FXML
    private ImageView backgroundImage;

    @FXML
    private ImageView icon;

    @FXML
    private Label statusLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private ImageView avatarImageView;

    private boolean isConnected = false;

    @FXML
    private void initialize() {
        Image image = new Image("https://openwar.fr/public/images/background.png");
        Image logo = new Image("https://openwar.fr/public/images/op.png");
        icon.setImage(logo);
        backgroundImage.setImage(image);

        if (isConnected) {
            loadUserInfo();
            authButton.setText("Login");
            authButton.setOnAction(event -> viewManager.loadView("MainPage.fxml"));
        } else {
            statusLabel.setText("Login");
            usernameLabel.setText("Disconnected");
            //avatarImageView.setImage(new Image("path/to/default_avatar.png"));
            authButton.setText("Login with Microsoft");
            authButton.setOnAction(event -> authenticateWithMicrosoft());
        }
    }


    public void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    private void loadUserInfo() {
        String username = "Chapy";
        String avatarUrl = "path/to/user_avatar.png";

        statusLabel.setText("Connected");
        usernameLabel.setText(username);
        avatarImageView.setImage(new Image(avatarUrl));
    }

    private void authenticateWithMicrosoft() {
        try {
            String scope = URLEncoder.encode(SCOPE, StandardCharsets.UTF_8.toString());
            String authLink = AUTH_URL + "?client_id=" + CLIENT_ID
                    + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8.toString())
                    + "&response_type=code"
                    + "&scope=" + scope
                    + "&prompt=select_account";
            System.out.println("Auth Link: " + authLink);
            java.awt.Desktop.getDesktop().browse(new URI(authLink));
            viewManager.loadView("MainPage.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
