package com.openwar.charpy.openwarlauncher;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class AuthPageController {

    private static final String CLIENT_ID = "9cef8cab-23eb-49a1-8c71-d3c3b81d98a4";
    private static final String REDIRECT_URI = "http://localhost:3000/auth/redirect";
    private static final String AUTH_URL = "https://login.live.com/oauth20_authorize.srf";
    private static final String SCOPE = "XboxLive.signin offline_access";

    private String accessToken;
    private ViewManager viewManager;

    @FXML
    private Button authButton;

    @FXML
    private ImageView backgroundImage;

    @FXML
    private ImageView icon;

    @FXML
    private Label playerCount;

    @FXML
    private Label statusLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private ImageView avatarImageView;

    private boolean isConnected = false;

    @FXML
    private void initialize() throws Exception {
        setupUI();
        pingServer("90.109.7.236", 25595);
        handleConnection();
    }

    private void setupUI() {
        Image image = new Image("https://openwar.fr/public/images/background.png");
        Image logo = new Image("https://openwar.fr/public/images/op.png");
        icon.setImage(logo);
        backgroundImage.setImage(image);
    }

    private boolean pingServer(String serverAddress, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverAddress, port), 2000);
            return getServerResponse(socket);
        } catch (IOException e) {
            playerCount.setText("Server Offline");
        }
        return false;
    }

    private boolean getServerResponse(Socket socket) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        DataInputStream dis = new DataInputStream(socket.getInputStream());

        dos.write(0xFE);
        dos.flush();

        StringBuilder response = new StringBuilder();
        int character;
        while ((character = dis.read()) != -1) {
            if (character != 0 && character > 16 && character != 255 && character != 23 && character != 24) {
                response.append((char) character);
            }
        }

        String[] data = response.toString().split("§");
        System.out.println(Arrays.toString(data));

        int onlinePlayers = Integer.parseInt(data[data.length - 2].trim());
        int maxPlayers = Integer.parseInt(data[data.length - 1].trim());
        playerCount.setText(onlinePlayers + "/" + maxPlayers + " Players Online");

        return true;
    }

    public void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    private void loadUserInfo(String accessToken) throws Exception {
        MinecraftAuthHelper ma = new MinecraftAuthHelper();
        PlayerProfile profile = ma.authenticateAndFetchPlayerProfile(accessToken);

        System.out.println("Username: " + profile.getUsername());
        System.out.println("Avatar URL: " + profile.getAvatarUrl());
        String username = "Chapy";
        String avatarUrl = "path/to/user_avatar.png";
        Platform.runLater(() -> {
            //statusLabel.setText("Connected");
            //usernameLabel.setText(username);
            //avatarImageView.setImage(new Image(avatarUrl));
        });
    }
    private String readAccessToken() throws IOException {
        Path path = Paths.get(System.getenv("APPDATA"), ".openwar");
        while (!Files.exists(path)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Le thread a été interrompu pendant l'attente du fichier.", e);
            }
        }
        String content = new String(Files.readAllBytes(path));
        JSONObject jsonObject = new JSONObject(content);
        return jsonObject.getString("access_token");
    }

    private void authenticateWithMicrosoft() {
        Task<Void> authTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                startLocalServer(); // Démarrer le serveur local ici
                String scope = URLEncoder.encode(SCOPE, StandardCharsets.UTF_8.toString());
                String authLink = AUTH_URL + "?client_id=" + CLIENT_ID
                        + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8.toString())
                        + "&response_type=code"
                        + "&scope=" + scope
                        + "&prompt=select_account";

                java.awt.Desktop.getDesktop().browse(new URI(authLink));

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
            }

            @Override
            protected void failed() {
                super.failed();
                statusLabel.setText("Authentication failed!");
            }
        };

        Thread authThread = new Thread(authTask);
        authThread.setDaemon(true);
        authThread.start();
    }

    public void startLocalServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/auth/redirect", new AuthHandler(this));
        server.start();
        System.out.println("Local server is running on http://localhost:3000/auth/redirect");
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

                    // Écrire la réponse dans le fichier .openwar
                    //Files.write(Paths.get(System.getenv("APPDATA"), ".openwar"), jsonResponse.toString().getBytes());

                    // Appeler loadUserInfo ici avec le token d'accès
                    System.out.println("ACCESS TOKEN: "+ accessToken);
                    controller.loadUserInfo(accessToken);

                    responseMessage = "Authentication successful! You can close this window.";
                } catch (Exception e) {
                    responseMessage = "Failed to exchange code for token: " + e.getMessage();
                    e.printStackTrace();
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

    private void handleConnection() throws Exception {
        if (isConnected) {
            loadUserInfo(accessToken);
            authButton.setText("Login");
            authButton.setOnAction(event -> viewManager.loadView("MainPage.fxml"));
        } else {
            statusLabel.setText("Login");
            usernameLabel.setText("Disconnected");
            authButton.setText("Login with Microsoft");
            authButton.setOnAction(event -> authenticateWithMicrosoft());
        }
    }


}