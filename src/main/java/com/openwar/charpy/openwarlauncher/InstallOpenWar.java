package com.openwar.charpy.openwarlauncher;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

public class InstallOpenWar {
    private ProgressBar progressBar;
    private static final String MINECRAFT_DIR = String.valueOf(Paths.get(System.getenv("APPDATA")));
    private static final String ZIP_URL = "https://openwar.fr/OPENWAR/openwar.zip";
    private static final String ZIP_FILE = MINECRAFT_DIR + "/openwar.zip";

    public InstallOpenWar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void install(Consumer<Void> onComplete) {
        try {
            downloadFile(ZIP_URL, ZIP_FILE);
            Platform.runLater(() -> progressBar.setProgress(50D));
            extractZip(ZIP_FILE, MINECRAFT_DIR);
            Platform.runLater(() -> progressBar.setProgress(100D));


            File zipFile = new File(ZIP_FILE);
            if (zipFile.exists()) {
                boolean deleted = zipFile.delete();
                if (!deleted) {
                    System.err.println("Erreur lors de la suppression du fichier ZIP : " + ZIP_FILE);
                }
            }

            if (onComplete != null) {
                onComplete.accept(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            progressBar.setProgress(-1D);
        }
    }
    private void downloadFile(String fileURL, String saveDir) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = httpConn.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(saveDir)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;
                int fileSize = httpConn.getContentLength();

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    double progress = totalBytesRead / fileSize * 50D;
                    Platform.runLater(() -> progressBar.setProgress(progress));
                }
            }
        } else {
            throw new IOException("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }

    private void extractZip(String zipFilePath, String destDir) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                // Crée le chemin d'accès complet pour l'extraction
                File newFile = new File(destDir, entry.getName());

                if (entry.isDirectory()) {
                    // Si c'est un dossier, créez-le
                    newFile.mkdirs();
                } else {
                    // Créez le dossier parent si nécessaire
                    new File(newFile.getParent()).mkdirs();

                    // Écrire le contenu du fichier dans le nouveau fichier
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
    }
}