package com.openwar.charpy.openwarlauncher.utils;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UpdateGame {
    private ProgressBar progressBar;
    private static final String MINECRAFT_DIR = String.valueOf(Paths.get(System.getenv("APPDATA")));
    private static final String MODS_DIR = MINECRAFT_DIR + "/.openwar";
    private static final String ZIP_URL_MODS = "https://openwar.fr/OPENWAR/game/mods.zip";
    private static final String DOWNLOAD_PATH = MINECRAFT_DIR + "/.openwar/download";

    public UpdateGame(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void updateGame(String accessToken, String username, String uuid, int gb, int width, int height) throws IOException {
        new Thread(() -> {
            String fileUrl = ZIP_URL_MODS;
            String localFilePath = MINECRAFT_DIR+ "/.openwar/mods/journeymap-1.12.2-5.7.1.jar";
            Date serverFileDate = getServerFileModificationDate(fileUrl);

            if (serverFileDate != null) {
                File localFile = new File(localFilePath);
                long localFileDate = 0;
                if (localFile.exists()) {
                    localFileDate = localFile.lastModified();
                }
                System.out.println(serverFileDate.getTime() + " | " + localFileDate);
                if (serverFileDate.getTime() > localFileDate) {
                    System.out.println("UPDATING GAMEEEE");
                    updateProgressBar(20);
                    try {
                        System.out.println("DOWNLOAD ");
                        downloadFile(fileUrl, DOWNLOAD_PATH);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("PHASE 2");
                    updateProgressBar(60);
                    try {
                        System.out.println("EXCTRACT");
                        extractZip(DOWNLOAD_PATH, MODS_DIR);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    updateProgressBar(100);
                }
            }
        }).start();
        LaunchMinecraft lm = new LaunchMinecraft(progressBar);
        lm.startMinecraft(accessToken,username,uuid, gb, width, height);
    }

    private void updateProgressBar(double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress / 100.0));
    }

    private static Date getServerFileModificationDate(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            long lastModified = connection.getHeaderFieldDate("Last-Modified", 0);

            if (lastModified == 0) {
                System.out.println("Impossible de récupérer la date de modification du fichier.");
                return null;
            }
            return new Date(lastModified);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void downloadFile(String fileURL, String saveDir) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
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