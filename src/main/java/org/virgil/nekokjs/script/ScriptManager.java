package org.virgil.nekokjs.script;

import org.virgil.nekokjs.NekoKJSPlugin;
import org.virgil.nekokjs.lang.LanguageManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 脚本管理器，负责加载、执行和管理 JavaScript 脚本
 * 参考 KubeJS 的脚本管理设计
 */
public class ScriptManager {
    private final NekoKJSPlugin plugin;
    private final File scriptsDir;
    private final Logger logger;
    private final Map<ScriptType, ScriptContext> contexts;
    private final Map<String, File> loadedScripts;
    private final List<ScriptPack> scriptPacks;
    private final LanguageManager lang;

    public ScriptManager(NekoKJSPlugin plugin, File scriptsDir) {
        this.plugin = plugin;
        this.scriptsDir = scriptsDir;
        this.logger = plugin.getLogger();
        this.contexts = new HashMap<>();
        this.loadedScripts = new HashMap<>();
        this.scriptPacks = new ArrayList<>();
        this.lang = plugin.getConfigManager().getLanguageManager();
        
        initializeContexts();
    }

    /**
     * 初始化不同类型的脚本上下文
     */
    private void initializeContexts() {
        for (ScriptType type : ScriptType.values()) {
            contexts.put(type, new ScriptContext(plugin, type));
        }
        logger.info(lang.scriptContextsInitialized());
    }

    /**
     * 加载所有脚本
     * 扫描 resources/ 目录下的所有脚本包
     */
    public void loadAllScripts() {
        if (!scriptsDir.exists()) {
            scriptsDir.mkdirs();
            createExamplePack();
            logger.info(lang.scriptDirCreated());
        }

        // 扫描并加载所有脚本包
        discoverScriptPacks();
        
        // 按优先级排序
        scriptPacks.sort(Comparator.comparingInt(ScriptPack::getPriority));
        
        // 加载每个包的脚本
        for (ScriptPack pack : scriptPacks) {
            if (!pack.isEnabled()) {
                logger.info("跳过已禁用的脚本包: " + pack.toString());
                continue;
            }
            
            logger.info("加载脚本包: " + pack.toString());
            loadPackScripts(pack);
        }
        
        logger.info(lang.scriptAllLoaded(loadedScripts.size()));
    }
    
    /**
     * 扫描脚本包
     */
    private void discoverScriptPacks() {
        scriptPacks.clear();
        
        File[] packDirs = scriptsDir.listFiles(File::isDirectory);
        if (packDirs == null) {
            return;
        }
        
        for (File packDir : packDirs) {
            try {
                // 检查是否有 pack.yml 文件，没有则跳过（静默）
                File packYml = new File(packDir, "pack.yml");
                if (!packYml.exists()) {
                    continue; // 静默跳过非脚本包的文件夹
                }
                
                ScriptPack pack = new ScriptPack(packDir);
                
                // 检查包是否有效
                if (!pack.isValid()) {
                    File entryFile = pack.getEntryFile();
                    if (!pack.getDataDir().exists()) {
                        logger.warning("跳过脚本包 [" + packDir.getName() + "]: data 目录不存在");
                    } else if (!entryFile.exists()) {
                        logger.warning("跳过脚本包 [" + packDir.getName() + "]: 入口文件不存在: " + pack.getEntryPoint());
                        logger.warning("  -> 请在 pack.yml 中配置正确的入口文件路径，或创建默认的 data/main.js");
                    } else if (!entryFile.isFile()) {
                        logger.warning("跳过脚本包 [" + packDir.getName() + "]: 入口路径不是文件: " + pack.getEntryPoint());
                    } else {
                        logger.warning("跳过脚本包 [" + packDir.getName() + "]: 包无效（未知原因）");
                    }
                    continue;
                }
                
                scriptPacks.add(pack);
                logger.info("发现脚本包: " + pack.toString() + " (入口: " + pack.getEntryPoint() + ")");
            } catch (Exception e) {
                logger.warning("加载脚本包失败 [" + packDir.getName() + "]: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 加载脚本包的入口文件
     */
    private void loadPackScripts(ScriptPack pack) {
        File entryFile = pack.getEntryFile();
        if (!entryFile.exists() || !entryFile.isFile()) {
            logger.warning("脚本包 [" + pack.getNamespace() + "] 的入口文件不存在: " + pack.getEntryPoint());
            return;
        }
        
        // 在 STARTUP 上下文中加载（启动时执行）
        ScriptContext context = contexts.get(ScriptType.STARTUP);
        String scriptId = pack.getNamespace() + ":" + pack.getEntryPoint();
        
        loadPackScript(entryFile, context, scriptId, pack);
    }
    
    /**
     * 加载脚本包的入口脚本文件（启用模块加载功能）
     */
    private void loadPackScript(File scriptFile, ScriptContext context, String scriptId, ScriptPack pack) {
        try {
            String script = Files.readString(scriptFile.toPath());
            // 使用 evaluatePackScript 以启用 load/require 功能
            context.evaluatePackScript(script, scriptId, pack);
            loadedScripts.put(scriptId, scriptFile);
            logger.info(lang.scriptLoaded(scriptId));
        } catch (IOException e) {
            logger.severe(lang.scriptReadFailed(scriptId, e.getMessage()));
        } catch (Exception e) {
            logger.severe(lang.scriptExecuteFailed(scriptId, e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * 重新加载所有脚本
     */
    public void reloadAllScripts() {
        logger.info(lang.scriptAllReloading());
        unloadAllScripts();
        initializeContexts();
        loadAllScripts();
        logger.info(lang.scriptAllReloaded());
    }

    /**
     * 卸载所有脚本
     */
    public void unloadAllScripts() {
        loadedScripts.clear();
        for (ScriptContext context : contexts.values()) {
            context.cleanup();
        }
        contexts.clear();
        logger.info(lang.scriptAllUnloaded());
    }

    /**
     * 服务器启动完成时调用
     */
    public void onServerStarted() {
        logger.info(lang.scriptServerStarted());
        // 服务端脚本已在 loadAllScripts 中加载
    }

    /**
     * 创建示例脚本包
     */
    private void createExamplePack() {
        File examplePackDir = new File(scriptsDir, "example_pack");
        if (examplePackDir.exists()) {
            return;
        }
        
        examplePackDir.mkdirs();
        
        // 创建 pack.yml
        createExamplePackConfig(examplePackDir);
        
        // 创建示例脚本
        createExampleStartupScript(examplePackDir);
        createExampleServerScript(examplePackDir);
    }
    
    /**
     * 创建示例包配置
     */
    private void createExamplePackConfig(File packDir) {
        File configFile = new File(packDir, "pack.yml");
        try {
            String content = """
                # NekoKJS 脚本包配置文件
                pack:
                  name: "Example Pack"
                  namespace: "example_pack"
                  version: "1.0.0"
                  authors:
                    - "NekoKJS"
                  description: "示例脚本包"
                
                loading:
                  enabled: true
                  priority: 100
                  load-on-startup: true
                
                scripts:
                  # 脚本入口文件（相对于包目录）
                  entry: "data/main.js"
                """;
            Files.writeString(configFile.toPath(), content);
        } catch (IOException e) {
            logger.warning("创建示例包配置失败: " + e.getMessage());
        }
    }

    private void createExampleStartupScript(File packDir) {
        File dataDir = new File(packDir, "data");
        dataDir.mkdirs();
        
        File mainFile = new File(dataDir, "main.js");
        if (!mainFile.exists()) {
            try {
                String content = """
                    // NekoKJS 脚本包入口文件
                    // Script Pack Entry Point
                    // 
                    // 此文件在插件加载时执行
                    // This file runs when the plugin loads
                    
                    console.info("=================================");
                    console.info("示例脚本包已加载！");
                    console.info("Example Pack Loaded!");
                    console.info("=================================");
                    
                    // 使用反射式事件监听器
                    // 监听玩家加入事件
                    Events.playerJoin(event => {
                        let player = event.getPlayer();
                        console.info("玩家加入: " + player.getName());
                        player.sendMessage("§a欢迎来到服务器！");
                    });
                    
                    // 监听玩家退出事件
                    Events.playerQuit(event => {
                        let player = event.getPlayer();
                        console.info("玩家退出: " + player.getName());
                    });
                    
                    
                    // ===== 服务器相关 =====
                    // Server Related
                    
                    console.info("服务器名称: " + Server.getName());
                    console.info("服务器版本: " + Server.getVersion());
                    console.info("在线玩家数: " + Server.getOnlinePlayerCount());
                    
                    // 监听方块破坏事件
                    Events.blockBreak(event => {
                        let player = event.getPlayer();
                        let block = event.getBlock();
                        console.info(player.getName() + " 破坏了方块: " + block.getType());
                    });
                    
                    // 服务器 Tick 事件
                    Events.serverTick(() => {
                        // 每秒触发一次
                    });
                    
                    // 通用事件监听（使用完整类名）
                    // Events.on("org.bukkit.event.player.PlayerMoveEvent", event => {
                    //     console.info("玩家移动");
                    // });
                    """;
                Files.writeString(mainFile.toPath(), content);
            } catch (IOException e) {
                logger.warning(lang.scriptExampleStartupFailed(e.getMessage()));
            }
        }
    }

    private void createExampleServerScript(File packDir) {
        // 不再需要单独的 server 脚本，所有逻辑合并到 main.js
    }

    public ScriptContext getContext(ScriptType type) {
        return contexts.get(type);
    }

    public File getScriptsDir() {
        return scriptsDir;
    }
    
    public List<ScriptPack> getScriptPacks() {
        return new ArrayList<>(scriptPacks);
    }
}
