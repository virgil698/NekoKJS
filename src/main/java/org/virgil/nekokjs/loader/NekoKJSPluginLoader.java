package org.virgil.nekokjs.loader;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.JarLibrary;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * NekoKJS Plugin Loader
 * Downloads runtime dependencies to the plugin libs folder at the earliest stage of plugin loading
 */
@SuppressWarnings("UnstableApiUsage")
public class NekoKJSPluginLoader implements PluginLoader {
    
    // Dependencies
    private static final String[][] DEPENDENCIES = {
        {"dev.latvian.mods", "rhino", "2101.2.7-build.81", "https://maven.latvian.dev/releases"},
        {"com.google.code.gson", "gson", "2.10.1", "https://repo1.maven.org/maven2"}
    };
    
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        // Get plugin data folder
        Path dataFolder = classpathBuilder.getContext().getDataDirectory();
        Path libsDir = dataFolder.resolve("libs");
        
        try {
            // Ensure libs directory exists
            Files.createDirectories(libsDir);
            
            // Download and load each dependency
            for (String[] dep : DEPENDENCIES) {
                String groupId = dep[0];
                String artifactId = dep[1];
                String version = dep[2];
                String repository = dep[3];
                
                String fileName = artifactId + "-" + version + ".jar";
                Path jarPath = libsDir.resolve(fileName);
                
                // Download if file doesn't exist
                if (!Files.exists(jarPath)) {
                    System.out.println("[NekoKJS] Downloading dependency: " + fileName);
                    downloadDependency(groupId, artifactId, version, repository, jarPath);
                    System.out.println("[NekoKJS] Downloaded: " + fileName);
                } else {
                    System.out.println("[NekoKJS] Dependency exists: " + fileName);
                }
                
                // Add to classpath
                classpathBuilder.addLibrary(new JarLibrary(jarPath));
            }
            
            System.out.println("[NekoKJS] All dependencies loaded to plugin libs folder");
            
        } catch (IOException e) {
            System.err.println("[NekoKJS] Failed to download dependencies: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Download single dependency
     */
    private void downloadDependency(String groupId, String artifactId, String version, 
                                    String repository, Path targetPath) throws IOException {
        String path = groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" 
                     + artifactId + "-" + version + ".jar";
        String downloadUrl = repository + "/" + path;
        
        System.out.println("[NekoKJS]   From: " + downloadUrl);
        
        // Download to temporary file
        Path tempFile = targetPath.getParent().resolve(targetPath.getFileName() + ".tmp");
        
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("User-Agent", "NekoKJS/1.0");
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("HTTP " + responseCode + ": " + downloadUrl);
            }
            
            long fileSize = conn.getContentLengthLong();
            System.out.println("[NekoKJS]   Size: " + formatFileSize(fileSize));
            
            try (InputStream in = conn.getInputStream();
                 OutputStream out = Files.newOutputStream(tempFile)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalRead = 0;
                
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }
            }
            
            // Move to target location
            Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
        } catch (IOException e) {
            // Clean up temporary file
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
            }
            throw e;
        }
    }
    
    /**
     * Format file size
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
