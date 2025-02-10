package com.openwar.charpy.openwarlauncher.utils;

import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SettingsManager {
    private static SettingsManager instance;
    private int gb = 6;
    private int width = 854;
    private int height = 480;
    private Path settingsPath;

    private SettingsManager() {
        determineSettingsPath();
        loadSettings();
    }

    public static SettingsManager getInstance() {
        if (instance == null) instance = new SettingsManager();
        return instance;
    }

    private void determineSettingsPath() {
        Path basePath;
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            basePath = Paths.get(System.getenv("APPDATA"), ".openwar");
        } else if (os.contains("mac")) {
            basePath = Paths.get(System.getProperty("user.home"), "Library", "Application Support", ".openwar");
        } else {
            basePath = Paths.get(System.getProperty("user.home"), ".config", ".openwar");
        }

        settingsPath = basePath.resolve("launcher_settings.json");
        try {
            Files.createDirectories(basePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadSettings() {
        try {
            if (Files.exists(settingsPath)) {
                String content = new String(Files.readAllBytes(settingsPath));
                JSONObject json = new JSONObject(content);
                gb = json.optInt("gb", 6);
                width = json.optInt("width", 854);
                height = json.optInt("height", 480);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveSettings() {
        JSONObject json = new JSONObject();
        json.put("gb", gb);
        json.put("width", width);
        json.put("height", height);

        try {
            Files.write(settingsPath, json.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getters/Setters
    public int getGb() { return gb; }
    public void setGb(int gb) { this.gb = gb; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
}