package com.openwar.charpy.openwarlauncher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LaunchMinecraft {

        public void startMinecraft(String accessToken, String username, String uuid) throws IOException {
            String gameDir = "C:\\Users\\mazin\\AppData\\Roaming\\.minecraft";
            String assetsDir = gameDir + "\\assets";
            String nativesDir = gameDir + "\\natives";
            String forgeJarPath = "C:\\Users\\mazin\\AppData\\Roaming\\.minecraft\\versions\\1.12.2-forge-14.23.5.2860\\1.12.2-forge1.12.2-14.23.5.2860.jar";

            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-Xmx4G"); // Allouer la mémoire, ajuster si nécessaire
            command.add("-Djava.library.path=" + nativesDir); // Chemin pour les bibliothèques natives
            command.add("-cp");
            command.add(forgeJarPath);  // Ajouter le JAR principal de Forge au classpath
            command.add("net.minecraft.client.Main");  // Classe de démarrage pour Forge

            // Arguments de démarrage Minecraft
            command.add("--username");
            command.add(username);
            command.add("--version");
            command.add("1.12.2");
            command.add("--gameDir");
            command.add(gameDir);
            command.add("--assetsDir");
            command.add(assetsDir);
            command.add("--assetIndex");
            command.add("1.12");
            command.add("--uuid");
            command.add(uuid);
            command.add("--accessToken");
            command.add(accessToken);
            command.add("--userType");
            command.add("legacy");
            command.add("--tweakClass");  // Classe spéciale pour lancer Forge
            command.add("net.minecraftforge.fml.common.launcher.FMLTweaker");

            // Création du ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(gameDir));
            processBuilder.inheritIO();

            Process process = processBuilder.start();

            try {
                int exitCode = process.waitFor();
                System.out.println("Minecraft exited with code: " + exitCode);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }