package com.openwar.charpy.openwarlauncher;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LocalServer {
    public static void localServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/auth/redirect", new AuthHandler());
        server.start();
    }

    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String responseMessage;
            String authorizationCode = null;

            if (query != null && query.contains("code=")) {
                authorizationCode = query.split("code=")[1];
                try {
                    String tokenResponse = AuthService.exchangeCodeForToken(authorizationCode);
                    JSONObject jsonResponse = new JSONObject(tokenResponse);
                    String accessToken = jsonResponse.getString("access_token");
                    String refreshToken = jsonResponse.getString("refresh_token");
                    String jsonFilePath = "token_response.json";
                    Files.write(Paths.get(jsonFilePath), jsonResponse.toString().getBytes());
                    responseMessage = "Authentication successful!";
                } catch (Exception e) {
                    responseMessage = "Failed to exchange code for token: " + e.getMessage();
                }
            } else {
                responseMessage = "No authorization code received.";
            }

            exchange.sendResponseHeaders(200, responseMessage.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseMessage.getBytes());
            os.close();
        }
    }
}