package com.openwar.charpy.openwarlauncher.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MinecraftAuthHelper {


    public PlayerProfile authenticateMcAPIOnly(String mcToken) throws Exception {
        PlayerProfile playerProfile = getPlayerProfile(mcToken);
        return playerProfile;
    }

    public PlayerProfile authenticateAndFetchPlayerProfile(String accessToken) throws Exception {
        String xboxLiveToken = getXboxLiveToken(accessToken);
        String userHash = "";
        userHash = extractUserHash(xboxLiveToken);
        //System.out.println("User Hash: " + userHash);
        //System.out.println(" XBOX LIVE TOKEN : "+xboxLiveToken );
        String xstsToken = getXstsToken(xboxLiveToken);
        //System.out.println(" XSTS TOKEN : "+xstsToken );
        String minecraftToken = getMinecraftToken(xstsToken, xboxLiveToken, userHash);
        //System.out.println(" MINECRAFT TOKEN : "+minecraftToken );
        String accessTokenMc = "";
        accessTokenMc = extractAccessTokenFromJson(minecraftToken);

        Path path = whatOsIsThis();
        Files.createDirectories(path.getParent());
        JSONObject playerData = new JSONObject();
        playerData.put("token", accessTokenMc);
        Files.write(path, playerData.toString(4).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        PlayerProfile playerProfile = getPlayerProfile(accessTokenMc);
        return playerProfile;
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

    private String extractUserHash(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            //System.out.println(jsonObject);
            JSONArray xuiArray = jsonObject.getJSONObject("DisplayClaims").getJSONArray("xui");
            //System.out.println(xuiArray);
            return xuiArray.getJSONObject(0).getString("uhs");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
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
    private String extractAccessTokenFromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getString("access_token");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }



    private String getMinecraftToken(String xstsToken, String xboxLiveToken, String userHash) throws Exception {
        URL url = new URL("https://api.minecraftservices.com/authentication/login_with_xbox");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        xstsToken = extractTokenFromJson(xstsToken);
        xboxLiveToken = extractTokenFromJson(xboxLiveToken);
        //System.out.println("TOKEN XBOX: "+xboxLiveToken);
        //System.out.println("TOKEN XSTS: "+xstsToken);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String payload = "{"
                + "\"identityToken\": \"XBL3.0 x=" + userHash + ";" + xstsToken + "\""
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

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to fetch player profile. Response code: " + responseCode);
        }

        String response = readResponse(connection);
        //System.out.println("Response from Minecraft Profile API: " + response);

        JSONObject jsonObject = new JSONObject(response);
        String username = jsonObject.optString("name", "Unknown");
        String uuid = jsonObject.optString("id", "Unknown");
        String avatarUrl = "https://crafatar.com/renders/head/" + uuid;
        //LaunchMinecraft lm = new LaunchMinecraft();
        return new PlayerProfile(username, avatarUrl,minecraftToken, uuid);
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
