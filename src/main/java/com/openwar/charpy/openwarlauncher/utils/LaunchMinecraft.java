package com.openwar.charpy.openwarlauncher.utils;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LaunchMinecraft {
    private ProgressBar progressBar;

    public LaunchMinecraft(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }
    private static final String MINECRAFT_DIR = String.valueOf(Paths.get(System.getenv("APPDATA"), ".openwar"));

    private static final String LIBRARY_PATH = MINECRAFT_DIR + "/libraries";
    private static final String VERSION_PATH = MINECRAFT_DIR + "/versions/1.12.2";
    private static final String CUSTOM_JAVA = MINECRAFT_DIR + "/CustomJava/bin/java";

    public void startMinecraft(String accessToken, String username, String uuid, int gb, int width, int height) throws IOException {
        List<String> command = new ArrayList<>();
        command.add(CUSTOM_JAVA);
        command.add("-Xms1024M");
        command.add("-Xmx"+gb+"G");
        command.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        command.add("-Djava.library.path=" + VERSION_PATH + "/natives");
        StringBuilder classPathBuilder = new StringBuilder();
        addAllJarsToClasspath(classPathBuilder, new File(LIBRARY_PATH));
        command.add("-cp");
        command.add(classPathBuilder.toString());
        command.add("net.minecraft.launchwrapper.Launch");
        command.add("--gameDir");
        command.add(MINECRAFT_DIR);
        command.add("--width");
        command.add(""+width);
        command.add("--height");
        command.add(""+height);
        command.add("--username");
        command.add(username);
        command.add("--version");
        command.add("1.12.2-forge-1.12.2-14.23.5.2860");
        command.add("--assetsDir");
        command.add(MINECRAFT_DIR + "/assets/");
        command.add("--assetIndex");
        command.add("1.12");
        command.add("--uuid");
        command.add(uuid);
        command.add("--accessToken");
        command.add(accessToken);
        command.add("--tweakClass");
        command.add("net.minecraftforge.fml.common.launcher.FMLTweaker");
        command.add("--versionType");
        command.add("Forge");

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        processBuilder.directory(new File(MINECRAFT_DIR));

        processBuilder.start();

        Stage currentStage = (Stage) progressBar.getScene().getWindow();
        currentStage.close();
        Platform.exit();
    }


    private void addAllJarsToClasspath(StringBuilder classPathBuilder, File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        addAllJarsToClasspath(classPathBuilder, file);
                    } else if (file.getName().endsWith(".jar")) {
                        classPathBuilder.append(file.getAbsolutePath()).append(File.pathSeparator);
                    }
                }
            }
        }
    }
}