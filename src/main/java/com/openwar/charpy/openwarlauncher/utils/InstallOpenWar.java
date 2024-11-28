package com.openwar.charpy.openwarlauncher.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

public class InstallOpenWar {
    private ProgressBar progressBar;
    private double totalTasks;
    private double completedTasks; 

    private static final String MINECRAFT_DIR = String.valueOf(Paths.get(System.getenv("APPDATA")));
    private static final String ZIP_URL_NATIVE = "https://openwar.fr/OPENWAR/game/natives.zip";
    private static final String ZIP_URL_LIBRARIES = "https://openwar.fr/OPENWAR/game/libraries.zip";
    private static final String ZIP_URL_GAME = "https://openwar.fr/OPENWAR/game/openwar.zip";
    private static final String ZIP_URL_MODS = "https://openwar.fr/OPENWAR/game/mods.zip";
    private static final String ZIP_URL_CONFIG = "https://openwar.fr/OPENWAR/game/config.zip";
    private static final String ZIP_URL_FORGE = "https://openwar.fr/OPENWAR/game/forge.zip";

    private static final String DOWNLOAD_PATH = MINECRAFT_DIR + "/.openwar/download";
    private static final String LIB_PATH = MINECRAFT_DIR + "/.openwar/libraries";
    private static final String ASSETS_DIR = MINECRAFT_DIR + "/.openwar/assets";
    private static final String FORGE_DIR = MINECRAFT_DIR + "/.openwar/versions";
    private static final String VERSION_DIR = MINECRAFT_DIR + "/.openwar/versions/1.12.2";

    private static final String JSON_URL = "https://piston-meta.mojang.com/v1/packages/832d95b9f40699d4961394dcf6cf549e65f15dc5/1.12.2.json";

    public InstallOpenWar(ProgressBar progressBar) {
        this.progressBar = progressBar;
        this.totalTasks = 12; // 6 téléchargements + 6 extractions
        this.completedTasks = 0;
    }

    public void install(Consumer<Void> onComplete) {
        new Thread(() -> {
            try {
                performStep(ZIP_URL_LIBRARIES, DOWNLOAD_PATH, LIB_PATH);
                performStep(ZIP_URL_NATIVE, DOWNLOAD_PATH, VERSION_DIR);
                performStep(ZIP_URL_FORGE, DOWNLOAD_PATH, FORGE_DIR);
                performStep(ZIP_URL_GAME, DOWNLOAD_PATH, MINECRAFT_DIR);
                performStep(ZIP_URL_MODS, DOWNLOAD_PATH, MINECRAFT_DIR);
                performStep(ZIP_URL_CONFIG, DOWNLOAD_PATH, MINECRAFT_DIR);

                File download = new File(DOWNLOAD_PATH);
                download.delete();
                updateProgressBar(100);
                Platform.runLater(() -> progressBar.setVisible(false));

                if (onComplete != null) {
                    onComplete.accept(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> progressBar.setProgress(0.0));
            }
        }).start();
    }

    private void performStep(String fileUrl, String downloadPath, String extractPath) throws IOException {
        downloadFile(fileUrl, downloadPath);
        completedTasks++;
        updateGlobalProgress();

        extractZip(downloadPath, extractPath);
        completedTasks++;
        updateGlobalProgress();
    }

    private void updateGlobalProgress() {
        double progress = (completedTasks / totalTasks) * 100.0;
        updateProgressBar(progress);
    }

    private void updateProgressBar(double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress / 100.0));
    }

    private void downloadFile(String fileURL, String saveDir) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            int contentLength = httpConn.getContentLength();
            File file = new File(saveDir);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (InputStream inputStream = httpConn.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(file)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
            }
            System.out.println("Téléchargé : " + saveDir);
        } else {
            throw new IOException("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }

    private void extractZip(String zipFilePath, String destDir) throws IOException {
        File zipFile = new File(zipFilePath);

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile))) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        }
        System.out.println("\nExtraction terminée !");
    }
}