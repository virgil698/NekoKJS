package org.virgil.nekokjs.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.virgil.nekokjs.NekoKJSPlugin;
import org.virgil.nekokjs.lang.LanguageManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Logger;

/**
 * 配置管理器
 * 管理插件的所有配置文件
 */
public class ConfigManager {
    private final NekoKJSPlugin plugin;
    private final Logger logger;
    private final File dataFolder;
    
    // 配置文件
    private FileConfiguration config;
    private FileConfiguration commands;
    private FileConfiguration translations;
    private LanguageManager languageManager;
    
    // 文件夹
    private File libsFolder;
    private File resourcesFolder;
    private File translationsFolder;

    public ConfigManager(NekoKJSPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dataFolder = plugin.getDataFolder();
        
        initializeFolders();
        loadConfigs();
    }

    /**
     * 初始化文件夹结构
     */
    private void initializeFolders() {
        // 创建主数据文件夹
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        // libs 文件夹 - 存放依赖
        libsFolder = new File(dataFolder, "libs");
        if (!libsFolder.exists()) {
            libsFolder.mkdirs();
        }
        
        // resources 文件夹 - KubeJS 脚本存放处
        resourcesFolder = new File(dataFolder, "resources");
        boolean resourcesCreated = false;
        if (!resourcesFolder.exists()) {
            resourcesFolder.mkdirs();
            resourcesCreated = true;
        }
        
        // 如果是首次创建 resources 文件夹，生成示例脚本包
        if (resourcesCreated) {
            createExampleScriptPack();
        }
        
        // translations 文件夹 - 语言文件
        translationsFolder = new File(dataFolder, "translations");
        if (!translationsFolder.exists()) {
            translationsFolder.mkdirs();
        }
    }

    /**
     * 加载所有配置文件
     */
    private void loadConfigs() {
        // 加载 config.yml
        config = loadConfig("config.yml");
        
        // 加载 commands.yml
        commands = loadConfig("commands.yml");
        
        // 初始化所有语言文件
        initializeAllTranslations();
        
        // 加载当前使用的语言文件
        translations = loadTranslation(config.getString("language", "zh_CN"));
        languageManager = new LanguageManager(translations);
        
        logger.info(languageManager.configLoaded());
    }
    
    /**
     * 初始化所有语言文件（确保所有语言文件都被创建）
     */
    private void initializeAllTranslations() {
        String[] languages = {"zh_CN", "en_US"};
        
        for (String language : languages) {
            File langFile = new File(translationsFolder, language + ".yml");
            if (!langFile.exists()) {
                saveResource("translations/" + language + ".yml");
                // Use English here as languageManager is not yet initialized
                logger.info("Created language file: " + language + ".yml");
            }
        }
    }
    
    /**
     * 重新加载配置文件
     */
    public void reloadConfig() {
        logger.info(languageManager != null ? languageManager.configReloading() : "Reloading plugin configuration...");
        
        // 重新加载 config.yml
        config = loadConfig("config.yml");
        
        // 重新加载 commands.yml
        commands = loadConfig("commands.yml");
        
        // 确保所有语言文件存在
        initializeAllTranslations();
        
        // 重新加载当前使用的语言文件
        translations = loadTranslation(config.getString("language", "zh_CN"));
        languageManager = new LanguageManager(translations);
        
        logger.info(languageManager.configConfigReloaded());
    }

    /**
     * 加载配置文件
     */
    private FileConfiguration loadConfig(String fileName) {
        File file = new File(dataFolder, fileName);
        
        // 如果文件不存在，从资源中复制
        if (!file.exists()) {
            saveResource(fileName);
        }
        
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * 加载语言文件
     */
    private FileConfiguration loadTranslation(String language) {
        File file = new File(translationsFolder, language + ".yml");
        
        // 如果文件不存在，从资源中复制
        if (!file.exists()) {
            saveResource("translations/" + language + ".yml");
        }
        
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * 从插件资源中保存文件
     */
    private void saveResource(String resourcePath) {
        try {
            InputStream input = plugin.getResource(resourcePath);
            if (input == null) {
                // 如果资源不存在，创建默认文件
                createDefaultFile(resourcePath);
                return;
            }
            
            File outFile = new File(dataFolder, resourcePath);
            outFile.getParentFile().mkdirs();
            Files.copy(input, outFile.toPath());
            input.close();
            
            // logger.info(languageManager.configFileCreated(resourcePath));
        } catch (IOException e) {
            // logger.warning(languageManager.configResourceSaveFailed(resourcePath, e.getMessage()));
            createDefaultFile(resourcePath);
        }
    }

    /**
     * 创建默认配置文件
     */
    private void createDefaultFile(String fileName) {
        File file = new File(dataFolder, fileName);
        file.getParentFile().mkdirs();
        
        try {
            if (fileName.equals("config.yml")) {
                createDefaultConfig(file);
            } else if (fileName.equals("commands.yml")) {
                createDefaultCommands(file);
            } else if (fileName.startsWith("translations/")) {
                createDefaultTranslation(file);
            }
        } catch (IOException e) {
            logger.severe((languageManager != null ? languageManager.configDefaultCreateFailed(e.getMessage()) : "Failed to create default config file: " + e.getMessage()));
        }
    }

    /**
     * 创建默认 config.yml
     */
    private void createDefaultConfig(File file) throws IOException {
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("language", "zh_CN");
        config.set("debug", false);
        config.set("auto-reload", false);
        config.set("script-timeout", 5000);
        
        config.setComments("language", java.util.List.of(
            "NekoKJS 配置文件",
            "语言设置 (zh_CN, en_US)"
        ));
        config.setComments("debug", java.util.List.of("调试模式"));
        config.setComments("auto-reload", java.util.List.of("自动重载脚本（检测到文件变化时）"));
        config.setComments("script-timeout", java.util.List.of("脚本执行超时时间（毫秒）"));
        
        config.save(file);
    }

    /**
     * 创建默认 commands.yml
     */
    private void createDefaultCommands(File file) throws IOException {
        YamlConfiguration commands = new YamlConfiguration();
        
        commands.set("nekokjs.enabled", true);
        commands.set("nekokjs.aliases", java.util.List.of("nkjs", "kjs"));
        
        commands.setComments("nekokjs.enabled", java.util.List.of(
            "NekoKJS 命令配置",
            "是否启用主命令"
        ));
        commands.setComments("nekokjs.aliases", java.util.List.of("命令别名"));
        
        commands.save(file);
    }

    /**
     * 创建默认语言文件
     */
    private void createDefaultTranslation(File file) throws IOException {
        YamlConfiguration trans = new YamlConfiguration();
        
        // 命令消息
        trans.set("command.no-permission", "§c你没有权限执行此命令！");
        trans.set("command.reload.start", "§e正在重新加载 NekoKJS 脚本...");
        trans.set("command.reload.success", "§a脚本重新加载完成！耗时: {time}ms");
        trans.set("command.reload.failed", "§c脚本重新加载失败: {error}");
        trans.set("command.unknown", "§c未知的子命令！使用 /nekokjs help 查看帮助");
        
        // 信息消息
        trans.set("info.header", "§6========== NekoKJS 信息 ==========");
        trans.set("info.version", "§e版本: §f{version}");
        trans.set("info.authors", "§e作者: §f{authors}");
        trans.set("info.scripts-dir", "§e脚本目录: §f{dir}");
        trans.set("info.tick-count", "§e服务器 Tick 数: §f{count}");
        trans.set("info.footer", "§6================================");
        
        // 帮助消息
        trans.set("help.header", "§6========== NekoKJS 帮助 ==========");
        trans.set("help.reload", "§e/nekokjs reload §7- 重新加载所有脚本");
        trans.set("help.info", "§e/nekokjs info §7- 查看插件信息");
        trans.set("help.help", "§e/nekokjs help §7- 显示此帮助信息");
        trans.set("help.footer", "§6================================");
        
        trans.save(file);
    }

    /**
     * 重载所有配置
     */
    public void reload() {
        loadConfigs();
        logger.info(languageManager.configReloaded());
    }

    /**
     * 获取翻译文本
     */
    public String getMessage(String key, Object... replacements) {
        String message = translations.getString(key, key);
        
        // 替换占位符
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                String placeholder = "{" + replacements[i] + "}";
                String value = String.valueOf(replacements[i + 1]);
                message = message.replace(placeholder, value);
            }
        }
        
        return message;
    }

    // Getters
    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getCommands() {
        return commands;
    }

    public FileConfiguration getTranslations() {
        return translations;
    }

    public File getLibsFolder() {
        return libsFolder;
    }

    public File getResourcesFolder() {
        return resourcesFolder;
    }

    public File getTranslationsFolder() {
        return translationsFolder;
    }
    
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    /**
     * 创建示例脚本包
     */
    private void createExampleScriptPack() {
        try {
            // Use English here as languageManager is not yet initialized
            logger.info("Generating example script pack...");
            
            // 示例脚本包文件列表
            String[] exampleFiles = {
                "resources/example_pack/pack.yml",
                "resources/example_pack/data/main.js",
                "resources/example_pack/data/config.js",
                "resources/example_pack/data/utils/helper.js",
                "resources/example_pack/data/events/player.js",
                "resources/example_pack/data/events/world.js"
            };
            
            for (String resourcePath : exampleFiles) {
                InputStream input = plugin.getResource(resourcePath);
                if (input == null) {
                    logger.warning("Resource file not found: " + resourcePath);
                    continue;
                }
                
                // 目标文件路径 (去掉开头的 "resources/")
                String relativePath = resourcePath.substring("resources/".length());
                File outFile = new File(resourcesFolder, relativePath);
                outFile.getParentFile().mkdirs();
                
                // 复制文件
                Files.copy(input, outFile.toPath());
                input.close();
            }
            
            logger.info("Example script pack generated: example_pack");
        } catch (IOException e) {
            logger.severe("Failed to generate example script pack: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
