package com.openwar.charpy.openwarlauncher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LaunchMinecraft {

    private static final String MINECRAFT_DIR = "C:/Users/mazin/AppData/Roaming/.openwar";

    private static final String LIBRARY_PATH = MINECRAFT_DIR + "/librariesOP";
    private static final String VERSION_PATH = MINECRAFT_DIR + "/versions/1.12.2";

    public void startMinecraft(String accessToken, String username, String uuid) throws IOException {
        List<String> command = new ArrayList<>();

        command.add("C:/Program Files/Java/jre1.8.0_431/bin/java");
        command.add("-Xms1024M");
        command.add("-Xmx16G");
        command.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        command.add("-Djava.library.path=" + VERSION_PATH + "/natives");

        StringBuilder classPathBuilder = new StringBuilder();
        addAllJarsToClasspath(classPathBuilder, new File(LIBRARY_PATH));
        command.add("-cp");
        System.out.println(classPathBuilder);
        command.add(classPathBuilder.toString());

        command.add("net.minecraft.launchwrapper.Launch");
        command.add("--title");
        command.add("OpenWar - Stable");
        command.add("--width");
        command.add("854");
        command.add("--height");
        command.add("480");
        command.add("--username");
        command.add(username);
        command.add("--version");
        command.add("1.12.2-forge-1.12.2-14.23.5.2860");
        command.add("--gameDir");
        command.add(MINECRAFT_DIR);

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
        processBuilder.start();
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