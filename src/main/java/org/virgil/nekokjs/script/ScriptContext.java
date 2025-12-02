package org.virgil.nekokjs.script;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ContextFactory;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.ScriptableObject;
import org.virgil.nekokjs.NekoKJSPlugin;
import org.virgil.nekokjs.api.ConsoleAPI;
import org.virgil.nekokjs.api.EventsAPI;
import org.virgil.nekokjs.api.ScriptLoaderAPI;
import org.virgil.nekokjs.api.ServerAPI;
import org.virgil.nekokjs.lang.LanguageManager;
import org.virgil.nekokjs.script.ScriptPack;

import java.util.logging.Logger;

/**
 * 脚本上下文
 * 为每个脚本提供独立的执行环境和 API 绑定
 */
public class ScriptContext {
    private final NekoKJSPlugin plugin;
    private final ScriptType type;
    private final Logger logger;
    private final ContextFactory contextFactory;
    private final LanguageManager lang;
    private Context context;
    private Scriptable scope;
    private ScriptLoaderAPI scriptLoader;

    public ScriptContext(NekoKJSPlugin plugin, ScriptType type) {
        this.plugin = plugin;
        this.type = type;
        this.logger = plugin.getLogger();
        this.contextFactory = new ContextFactory();
        this.lang = plugin.getConfigManager().getLanguageManager();
        initializeContext();
    }

    private void initializeContext() {
        context = contextFactory.enter();
        try {
            // 初始化作用域
            scope = context.initStandardObjects();
            
            // 注入全局 API
            injectGlobalAPIs();
            
            logger.info(lang.scriptContextInitialized(type.getName()));
        } catch (Exception e) {
            logger.severe(lang.scriptContextInitFailed(e.getMessage()));
            e.printStackTrace();
        }
    }

    private void injectGlobalAPIs() {
        // KubeJS-Rhino 使用 ScriptableObject.putProperty 设置全局属性
        // Console API - 用于日志输出
        ConsoleAPI consoleAPI = new ConsoleAPI(logger);
        ScriptableObject.putProperty(scope, "console", consoleAPI, context);
        
        // Server API - 服务器相关操作
        ServerAPI serverAPI = new ServerAPI(plugin);
        ScriptableObject.putProperty(scope, "Server", serverAPI, context);
        
        // Events API - 事件监听和触发（使用反射）
        EventsAPI eventsAPI = plugin.getEventManager().getEventsAPI();
        ScriptableObject.putProperty(scope, "Events", eventsAPI, context);
        
        // 直接暴露 Bukkit 对象（高级用法）
        ScriptableObject.putProperty(scope, "server", plugin.getServer(), context);
        ScriptableObject.putProperty(scope, "plugin", plugin, context);
    }

    /**
     * 执行脚本
     */
    public Object evaluateScript(String script, String sourceName) {
        Context ctx = contextFactory.enter();
        try {
            // evaluateString 需要 scope, script, sourceName, lineNumber, securityDomain
            return ctx.evaluateString(scope, script, sourceName, 1, null);
        } catch (Exception e) {
            logger.severe(lang.scriptExecuteFailed(sourceName, e.getMessage()));
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 执行脚本包的入口文件，并启用脚本加载器
     */
    public Object evaluatePackScript(String script, String sourceName, ScriptPack pack) {
        // 创建脚本加载器
        this.scriptLoader = new ScriptLoaderAPI(logger, contextFactory, scope, pack);
        ScriptableObject.putProperty(scope, "load", scriptLoader, context);
        ScriptableObject.putProperty(scope, "require", scriptLoader, context);
        
        // 执行入口脚本
        return evaluateScript(script, sourceName);
    }

    public Scriptable getScope() {
        return scope;
    }

    public ScriptType getType() {
        return type;
    }

    public void cleanup() {
        scope = null;
        context = null;
        logger.info(lang.scriptContextCleaned(type.getName()));
    }
}
