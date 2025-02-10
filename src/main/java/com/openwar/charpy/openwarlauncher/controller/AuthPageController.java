package com.openwar.charpy.openwarlauncher.controller;

import com.openwar.charpy.openwarlauncher.utils.AuthService;
import com.openwar.charpy.openwarlauncher.utils.MinecraftAuthHelper;
import com.openwar.charpy.openwarlauncher.utils.PlayerProfile;
import com.openwar.charpy.openwarlauncher.utils.ViewManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AuthPageController {

    private static final String CLIENT_ID = "9cef8cab-23eb-49a1-8c71-d3c3b81d98a4";
    private static final String REDIRECT_URI = "http://localhost:3000/auth/redirect";
    private static final String AUTH_URL = "https://login.live.com/oauth20_authorize.srf";
    private static final String SCOPE = "XboxLive.signin offline_access";
    private HttpServer server;

    @FXML
    private Button authButton;
    @FXML
    private ImageView backgroundImage, icon, avatar;
    @FXML
    private Label playerCount, statusLabel, usernameLabel;

    private boolean isConnected = false;
    private ViewManager viewManager;
    private PlayerProfile playerProfile;

    @FXML
    private void initialize() throws Exception {
       setupUI();
       tryLoadingLocalToken();
       checkServerStatus("90.109.7.236", 25595);
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

    private void tryLoadingLocalToken() throws Exception {
        Path tokenPath = whatOsIsThis();
        if (Files.exists(tokenPath)) {
            String jsonString = new String(Files.readAllBytes(tokenPath));
            JSONObject jsonObject = new JSONObject(jsonString);
            String accessToken = jsonObject.optString("token", null);
            if (accessToken != null) {
                isConnected = minecraftApi(accessToken);
                if (isConnected) {
                    authButton.setText("Continue");
                    authButton.setOnAction(event -> {
                        try {
                            loadMain(playerProfile);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
                else {
                    displayDisconnectedState();
                }
            }
        }
        if (!isConnected) {
            displayDisconnectedState();
        }
    }
    private boolean minecraftApi(String accessToken) {
        MinecraftAuthHelper ma = new MinecraftAuthHelper();
        try {
            playerProfile = ma.authenticateMcAPIOnly(accessToken);
        } catch (Exception e) {
            return false;
        }
        if (playerProfile != null) {
            Platform.runLater(() -> {
                statusLabel.setText("Connected");
                usernameLabel.setText(playerProfile.getUsername());
                avatar.setImage(new Image(playerProfile.getAvatarUrl()));
            });
            return true;
        }
        return false;
    }
    private void setupUI() {
        icon.setImage(new Image(String.valueOf(getClass().getResource("/com/openwar/charpy/openwarlauncher/images/op.png"))));
        backgroundImage.setImage(new Image(String.valueOf(getClass().getResource("/com/openwar/charpy/openwarlauncher/images/background.png"))));
    }
    private void loadMain(PlayerProfile playerProfile) throws IOException {
        Stage currentStage = (Stage) authButton.getScene().getWindow();
        currentStage.close();
        viewManager.showPage("MainPage.fxml", "OpenWar - Launcher | Stable Edition v1.4.7", 1080, 750, playerProfile);
    }

    private void displayDisconnectedState() throws IOException {
        Path tokenPath = whatOsIsThis();
        if (Files.exists(tokenPath)) {
            Files.delete(tokenPath);
        }
        statusLabel.setText("");
        usernameLabel.setText("Disconnected");
        avatar.setImage(new Image(String.valueOf(getClass().getResource("/com/openwar/charpy/openwarlauncher/images/uk.png"))));
        //avatar.setImage(new Image("https://openwar.fr/public/images/uk.png", true));
        authButton.setText("Login with Microsoft");
        authButton.setOnAction(event -> authenticateWithMicrosoft());
    }

    private boolean checkServerStatus(String serverAddress, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverAddress, port), 2000);
            return getServerResponse(socket);
        } catch (IOException e) {
            playerCount.setText("Server Offline");
            return false;
        }
    }

    private boolean getServerResponse(Socket socket) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        DataInputStream dis = new DataInputStream(socket.getInputStream());

        dos.write(0xFE);
        dos.flush();

        StringBuilder response = new StringBuilder();
        int character;
        while ((character = dis.read()) != -1) {
            if (character > 16 && character != 255 && character != 23 && character != 24) {
                response.append((char) character);
            }
        }

        String[] data = response.toString().split("§");
        int onlinePlayers = Integer.parseInt(data[data.length - 2].trim());
        int maxPlayers = Integer.parseInt(data[data.length - 1].trim());
        playerCount.setText(onlinePlayers + "/" + maxPlayers + " Players Online");

        return true;
    }

    private void loadUserInfo(String accessToken) throws Exception {
        MinecraftAuthHelper ma = new MinecraftAuthHelper();
        playerProfile = ma.authenticateAndFetchPlayerProfile(accessToken);
        System.out.println(playerProfile.getUsername()+playerProfile.getUuid());

        Platform.runLater(() -> {
            statusLabel.setText("Connected");
            usernameLabel.setText(playerProfile.getUsername());
            avatar.setImage(new Image(playerProfile.getAvatarUrl()));
            authButton.setText("Continue");
            authButton.setOnAction(event -> {
                try {
                    loadMain(playerProfile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
    private void authenticateWithMicrosoft() {
        Task<Void> authTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                startLocalServer();

                String authLink = AUTH_URL + "?client_id=" + CLIENT_ID
                        + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                        + "&response_type=code"
                        + "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8)
                        + "&prompt=select_account";

                Desktop.getDesktop().browse(new URI(authLink));

                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> statusLabel.setText("Authentication failed!"));
                stopLocalServer();
            }
        };

        new Thread(authTask).setDaemon(true);
        authTask.run();
    }

    public void onAuthSuccess() {
        stopLocalServer();
    }
    public void startLocalServer() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/auth/redirect", new AuthHandler(this));
        server.start();
        System.out.println("Local server started on port 3000.");
    }

    public void stopLocalServer() {
        System.out.println("SERVER STOP CALLED");

        if (server == null) {
            System.out.println("Error: server is null!");
            return;
        }

        server.stop(0);
        System.out.println("Local server stopped.");
    }



    static class AuthHandler implements HttpHandler {
        private final AuthPageController controller;

        public AuthHandler(AuthPageController controller) {
            this.controller = controller;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String responseMessage;

            if (query != null && query.contains("code=")) {
                String authorizationCode = query.split("code=")[1];
                try {
                    String tokenResponse = AuthService.exchangeCodeForToken(authorizationCode);
                    JSONObject jsonResponse = new JSONObject(tokenResponse);
                    String accessToken = jsonResponse.getString("access_token");
                    controller.loadUserInfo(accessToken);
                    responseMessage = "Authentication successful! You can close this window.";
                    controller.onAuthSuccess();

                } catch (Exception e) {
                    responseMessage = "Failed to exchange code for token: " + e.getMessage();
                }
            } else {
                responseMessage = "No authorization code received.";
            }
            exchange.sendResponseHeaders(200, responseMessage.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseMessage.getBytes());
            }
        }
    }

    public void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }
}
