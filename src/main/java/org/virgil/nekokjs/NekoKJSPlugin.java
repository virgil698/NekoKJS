package org.virgil.nekokjs;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.virgil.nekokjs.command.NekoKJSCommand;
import org.virgil.nekokjs.config.ConfigManager;
import org.virgil.nekokjs.lang.LanguageManager;
import org.virgil.nekokjs.script.ScriptManager;
import org.virgil.nekokjs.event.EventManager;

import java.io.File;
import java.util.logging.Logger;

/**
 * NekoKJS - KubeJS plugin version for Leaves server
 * 基于 Leaves Mixin 实现的 KubeJS 插件版本
 */
public class NekoKJSPlugin extends JavaPlugin {
    private static NekoKJSPlugin instance;
    private ConfigManager configManager;
    private ScriptManager scriptManager;
    private EventManager eventManager;
    private Logger logger;

    @Override
    public void onLoad() {
        instance = this;
        logger = getLogger();
        
        logger.info("NekoKJS is loading...");
        
        // 初始化 Bridge，用于 Mixin 和插件之间的通信
        // 使用反射创建 Bridge 实例，因为 main 模块无法直接访问 mixin 模块的类
        try {
            Class<?> bridgeClass = Class.forName("org.virgil.nekokjs.bridge.NekoKJSBridge");
            Object bridge = bridgeClass.getConstructor(Class.forName("org.virgil.nekokjs.NekoKJSPlugin")).newInstance(this);
            
            Class<?> managerClass = Class.forName("org.virgil.nekokjs.mixin.bridge.BridgeManager");
            Object manager = managerClass.getField("INSTANCE").get(null);
            managerClass.getMethod("setBridge", Class.forName("org.virgil.nekokjs.mixin.bridge.Bridge"))
                       .invoke(manager, bridge);
        } catch (Exception e) {
            logger.severe("Failed to initialize Bridge: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        // 初始化配置管理器（必须先加载配置才能使用语言系统）
        configManager = new ConfigManager(this);
        LanguageManager lang = configManager.getLanguageManager();
        
        logger.info(lang.pluginStarting());
        
        // 使用 resources 文件夹作为脚本目录
        File scriptsDir = configManager.getResourcesFolder();
        
        // 初始化事件管理器（必须在脚本管理器之前，因为 ScriptContext 需要访问 EventsAPI）
        eventManager = new EventManager(this);
        
        // 初始化脚本管理器
        scriptManager = new ScriptManager(this, scriptsDir);
        
        // 注册命令（使用 Paper 命令 API）
        registerCommands();
        
        // 加载所有脚本
        scriptManager.loadAllScripts();
        
        logger.info(lang.pluginStarted());
        logger.info(lang.pluginScriptsDir(scriptsDir.getAbsolutePath()));
        logger.info(lang.pluginUseHelp());
    }

    @Override
    public void onDisable() {
        LanguageManager lang = configManager != null ? configManager.getLanguageManager() : null;
        if (lang != null) {
            logger.info(lang.pluginStopping());
        } else {
            logger.info("NekoKJS is shutting down...");
        }
        
        // 卸载所有脚本
        if (scriptManager != null) {
            scriptManager.unloadAllScripts();
        }
        
        // 清理事件管理器
        if (eventManager != null) {
            eventManager.cleanup();
        }
        
        if (lang != null) {
            logger.info(lang.pluginStopped());
        } else {
            logger.info("NekoKJS has been stopped!");
        }
    }

    public static NekoKJSPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ScriptManager getScriptManager() {
        return scriptManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * 注册命令（使用 Paper 命令 API）
     */
    private void registerCommands() {
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                "nekokjs",
                "NekoKJS main command",
                new NekoKJSCommand(this)
            );
            LanguageManager lang = configManager.getLanguageManager();
            logger.info(lang.commandRegistered());
        });
    }
}
