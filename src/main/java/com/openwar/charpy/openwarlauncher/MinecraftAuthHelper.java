package com.openwar.charpy.openwarlauncher;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class MinecraftAuthHelper {

    public PlayerProfile authenticateAndFetchPlayerProfile(String accessToken) throws Exception {
        String xboxLiveToken = getXboxLiveToken(accessToken);
        System.out.println(" XBOX LIVE TOKEN : "+xboxLiveToken );
        String xstsToken = getXstsToken(xboxLiveToken);
        System.out.println(" XSTS TOKEN : "+xstsToken );
        String minecraftToken = getMinecraftToken(xstsToken, xboxLiveToken);
        System.out.println(" MINECRAFT TOKEN : "+minecraftToken );
        PlayerProfile playerProfile = getPlayerProfile(minecraftToken);
        return playerProfile;
    }

    private String getXboxLiveToken(String accessToken) throws Exception {
        URL url = new URL("https://user.auth.xboxlive.com/user/authenticate");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String payload = "{"
                + "\"Properties\": {"
                + "\"AuthMethod\": \"RPS\","
                + "\"SiteName\": \"user.auth.xboxlive.com\","
                + "\"RpsTicket\": \"d=" + accessToken + "\""
                + "},"
                + "\"RelyingParty\": \"http://auth.xboxlive.com\","
                + "\"TokenType\": \"JWT\""
                + "}";

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return readResponse(connection);
    }

    private String getXstsToken(String xboxUserToken) throws Exception {
        String jwtToken = extractTokenFromJson(xboxUserToken);
        URL url = new URL("https://xsts.auth.xboxlive.com/xsts/authorize");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String payload = "{"
                + "\"Properties\": {"
                + "\"SandboxId\": \"RETAIL\","
                + "\"UserTokens\": [\"" + jwtToken + "\"]"
                + "},"
                + "\"RelyingParty\": \"rp://api.minecraftservices.com/\","
                + "\"TokenType\": \"JWT\""
                + "}";


        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        return readResponse(connection);
    }

    private String extractTokenFromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getString("Token");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    private String getMinecraftToken(String xstsToken, String xboxLiveToken) throws Exception {
        URL url = new URL("https://api.minecraftservices.com/authentication/login_with_xbox");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        xstsToken = extractTokenFromJson(xstsToken);
        xboxLiveToken = extractTokenFromJson(xboxLiveToken);
        System.out.println("TOKEN XBOX: "+xboxLiveToken);
        System.out.println("TOKEN XSTS: "+xstsToken);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String payload = "{"
                + "\"identityToken\": \"XBL3.0 x=" + xboxLiveToken + ";" + xstsToken + "\""
                + "}";

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        return readResponse(connection);
    }

    private PlayerProfile getPlayerProfile(String minecraftToken) throws Exception {
        URL url = new URL("https://api.minecraftservices.com/minecraft/profile");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + minecraftToken);

        String response = readResponse(connection);
        System.out.println(" response 1 ="+response);
        JSONObject jsonObject = new JSONObject(response);

        String username = jsonObject.getString("name");
        String avatarUrl = "https://crafatar.com/renders/body/" + jsonObject.getString("id");

        return new PlayerProfile(username, avatarUrl);
    }

    private String readResponse(HttpURLConnection connection) throws Exception {
        int responseCode = connection.getResponseCode();
        InputStream inputStream = null;

        if (responseCode == HttpURLConnection.HTTP_OK) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }

        if (inputStream == null) {
            throw new IOException("InputStream is null, response code: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {
            throw new IOException("Error reading response: " + e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return response.toString();
    }

}
