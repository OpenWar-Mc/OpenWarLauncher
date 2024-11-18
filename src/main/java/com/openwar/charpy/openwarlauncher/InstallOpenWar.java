package com.openwar.charpy.openwarlauncher;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

public class InstallOpenWar {
    private ProgressBar progressBar;
    private static final String MINECRAFT_DIR = String.valueOf(Paths.get(System.getenv("APPDATA")));
    private static final String ZIP_URL = "https://openwar.fr/OPENWAR/openwar.zip";
    private static final String ZIP_FILE = MINECRAFT_DIR + "/openwar.zip";
    private static final String VERSION_DIR = MINECRAFT_DIR + "/.openwar/versionss/1.12.2";
    private static final String JSON_URL = "https://piston-meta.mojang.com/v1/packages/832d95b9f40699d4961394dcf6cf549e65f15dc5/1.12.2.json";

    public InstallOpenWar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void install(Consumer<Void> onComplete) {
        try {
            downloadFile(ZIP_URL, ZIP_FILE);
            Platform.runLater(() -> progressBar.setProgress(50D));
            extractZip(ZIP_FILE, MINECRAFT_DIR);
            Platform.runLater(() -> progressBar.setProgress(75D));

            Files.createDirectories(Paths.get(VERSION_DIR));
            String jarHash = getJarHash();
            if (jarHash == null) {
                throw new IOException("Impossible de récupérer le hash pour 1.12.2.jar");
            }
            downloadFile("https://launcher.mojang.com/v1/objects/" + jarHash + "/1.12.2.jar", VERSION_DIR + "/1.12.2.jar");
            downloadFile(JSON_URL, VERSION_DIR + "/1.12.2.json");
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
    private String getJarHash() throws IOException {
        String versionDetailsUrl = "https://piston-meta.mojang.com/v1/packages/832d95b9f40699d4961394dcf6cf549e65f15dc5/1.12.2.json";

        URL url = new URL(versionDetailsUrl);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Impossible de récupérer les détails de la version. Code de réponse : " + responseCode);
        }

        StringBuilder jsonResponse = new StringBuilder();
        try (Scanner scanner = new Scanner(httpConn.getInputStream())) {
            while (scanner.hasNextLine()) {
                jsonResponse.append(scanner.nextLine());
            }
        }

        String jsonString = jsonResponse.toString();
        int downloadsIndex = jsonString.indexOf("\"downloads\":");
        if (downloadsIndex == -1) {
            throw new IOException("Downloads non trouvés dans les détails de la version.");
        }

        int clientIndex = jsonString.indexOf("\"client\":", downloadsIndex);
        if (clientIndex == -1) {
            throw new IOException("Client non trouvé dans les détails de la version.");
        }

        int sha1Index = jsonString.indexOf("\"sha1\":", clientIndex);
        if (sha1Index == -1) {
            throw new IOException("SHA1 non trouvé dans les détails de la version.");
        }

        int startIndex = jsonString.indexOf("\"", sha1Index + 7) + 1;
        int endIndex = jsonString.indexOf("\"", startIndex);
        return jsonString.substring(startIndex, endIndex);
    }

    private String getVersionDetails(String versionUrl) throws IOException {
        URL url = new URL(versionUrl);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            StringBuilder jsonResponse = new StringBuilder();
            try (Scanner scanner = new Scanner(httpConn.getInputStream())) {
                while (scanner.hasNextLine()) {
                    jsonResponse.append(scanner.nextLine());
                }
            }
            String jsonString = jsonResponse.toString();
            int downloadsIndex = jsonString.indexOf("\"downloads\":");
            if (downloadsIndex != -1) {
                int clientIndex = jsonString.indexOf("\"client\":", downloadsIndex);
                if (clientIndex != -1) {
                    int sha1Index = jsonString.indexOf("\"sha1\":", clientIndex);
                    if (sha1Index != -1) {
                        int startIndex = jsonString.indexOf("\"", sha1Index + 7) + 1;
                        int endIndex = jsonString.indexOf("\"", startIndex);
                        return jsonString.substring(startIndex, endIndex);
                    }
                }
            }
        } else {
            throw new IOException("Impossible de récupérer les détails de la version. Code de réponse : " + responseCode);
        }
        httpConn.disconnect();
        return null;
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
                    double progress = 50D + (totalBytesRead / (double) fileSize) * 25D;
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
    }
}
