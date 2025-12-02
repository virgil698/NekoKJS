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
 * NekoKJS 插件加载器
 * 在插件加载的最早阶段下载运行时依赖到插件 libs 文件夹
 */
@SuppressWarnings("UnstableApiUsage")
public class NekoKJSPluginLoader implements PluginLoader {
    
    // 依赖信息
    private static final String[][] DEPENDENCIES = {
        {"dev.latvian.mods", "rhino", "2101.2.7-build.81", "https://maven.latvian.dev/releases"},
        {"com.google.code.gson", "gson", "2.10.1", "https://repo1.maven.org/maven2"}
    };
    
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        // 获取插件数据文件夹
        Path dataFolder = classpathBuilder.getContext().getDataDirectory();
        Path libsDir = dataFolder.resolve("libs");
        
        try {
            // 确保 libs 目录存在
            Files.createDirectories(libsDir);
            
            // 下载并加载每个依赖
            for (String[] dep : DEPENDENCIES) {
                String groupId = dep[0];
                String artifactId = dep[1];
                String version = dep[2];
                String repository = dep[3];
                
                String fileName = artifactId + "-" + version + ".jar";
                Path jarPath = libsDir.resolve(fileName);
                
                // 如果文件不存在，下载它
                if (!Files.exists(jarPath)) {
                    System.out.println("[NekoKJS] 下载依赖: " + fileName);
                    downloadDependency(groupId, artifactId, version, repository, jarPath);
                    System.out.println("[NekoKJS] ✓ 下载完成: " + fileName);
                } else {
                    System.out.println("[NekoKJS] 依赖已存在: " + fileName);
                }
                
                // 添加到类路径
                classpathBuilder.addLibrary(new JarLibrary(jarPath));
            }
            
            System.out.println("[NekoKJS] 所有依赖已加载到插件 libs 文件夹");
            
        } catch (IOException e) {
            System.err.println("[NekoKJS] 下载依赖失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 下载单个依赖
     */
    private void downloadDependency(String groupId, String artifactId, String version, 
                                    String repository, Path targetPath) throws IOException {
        String path = groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" 
                     + artifactId + "-" + version + ".jar";
        String downloadUrl = repository + "/" + path;
        
        System.out.println("[NekoKJS]   从: " + downloadUrl);
        
        // 下载到临时文件
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
            System.out.println("[NekoKJS]   大小: " + formatFileSize(fileSize));
            
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
            
            // 移动到目标位置
            Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
        } catch (IOException e) {
            // 清理临时文件
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
            }
            throw e;
        }
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
