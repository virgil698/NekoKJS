package org.virgil.nekokjs.script;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 脚本包
 * 表示一个独立的脚本命名空间
 */
public class ScriptPack {
    private final File packDir;
    private final String namespace;
    private final YamlConfiguration config;
    private boolean enabled;
    private int priority;
    
    private String name;
    private String version;
    private List<String> authors;
    private String description;
    private List<String> dependencies;
    
    private File dataDir;
    private String entryPoint;

    public ScriptPack(File packDir) {
        this.packDir = packDir;
        this.namespace = packDir.getName();
        
        // 加载 pack.yml
        File configFile = new File(packDir, "pack.yml");
        if (configFile.exists()) {
            this.config = YamlConfiguration.loadConfiguration(configFile);
            loadConfig();
        } else {
            this.config = new YamlConfiguration();
            this.enabled = true;
            this.priority = 100;
            this.name = namespace;
            this.version = "1.0.0";
            this.authors = new ArrayList<>();
            this.description = "";
            this.dependencies = new ArrayList<>();
        }
        
        // 初始化目录
        initDirectories();
    }
    
    private void loadConfig() {
        // 加载包信息
        this.name = config.getString("pack.name", namespace);
        this.version = config.getString("pack.version", "1.0.0");
        this.authors = config.getStringList("pack.authors");
        this.description = config.getString("pack.description", "");
        this.dependencies = config.getStringList("pack.dependencies");
        
        // 加载设置
        this.enabled = config.getBoolean("loading.enabled", true);
        this.priority = config.getInt("loading.priority", 100);
    }
    
    private void initDirectories() {
        // data 目录
        this.dataDir = new File(packDir, "data");
        
        // 脚本入口文件（支持自定义路径）
        this.entryPoint = config != null ? config.getString("scripts.entry", "data/main.js") : "data/main.js";
    }
    
    /**
     * 获取脚本入口文件
     */
    public File getEntryFile() {
        return new File(packDir, entryPoint);
    }
    
    /**
     * 获取脚本入口路径
     */
    public String getEntryPoint() {
        return entryPoint;
    }
    
    /**
     * 检查包是否有效（存在 data 目录和入口文件）
     */
    public boolean isValid() {
        File entryFile = getEntryFile();
        return dataDir.exists() && dataDir.isDirectory() && entryFile.exists() && entryFile.isFile();
    }

    // Getters
    public File getPackDir() {
        return packDir;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getPriority() {
        return priority;
    }

    public File getDataDir() {
        return dataDir;
    }

    @Override
    public String toString() {
        return name + " v" + version + " [" + namespace + "]";
    }
}
