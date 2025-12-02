package org.virgil.nekokjs.lang;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * 语言管理器
 * 提供快捷的多语言消息访问
 */
public class LanguageManager {
    private final FileConfiguration translations;
    
    public LanguageManager(FileConfiguration translations) {
        this.translations = translations;
    }
    
    /**
     * 获取翻译文本
     * 
     * @param key 语言键
     * @param replacements 替换值 (key1, value1, key2, value2, ...)
     * @return 翻译后的文本
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
    
    // === 插件消息 ===
    
    public String pluginLoading() {
        return getMessage("plugin.loading");
    }
    
    public String pluginStarting() {
        return getMessage("plugin.starting");
    }
    
    public String pluginStarted() {
        return getMessage("plugin.started");
    }
    
    public String pluginStopping() {
        return getMessage("plugin.stopping");
    }
    
    public String pluginStopped() {
        return getMessage("plugin.stopped");
    }
    
    public String pluginScriptsDir(String dir) {
        return getMessage("plugin.scripts-dir", "dir", dir);
    }
    
    public String pluginUseHelp() {
        return getMessage("plugin.use-help");
    }
    
    public String pluginBridgeInitFailed(String error) {
        return getMessage("plugin.bridge-init-failed", "error", error);
    }
    
    // === 脚本消息 ===
    
    public String scriptContextInitialized(String type) {
        return getMessage("script.context-initialized", "type", type);
    }
    
    public String scriptContextInitFailed(String error) {
        return getMessage("script.context-init-failed", "error", error);
    }
    
    public String scriptContextCleaned(String type) {
        return getMessage("script.context-cleaned", "type", type);
    }
    
    public String scriptContextsInitialized() {
        return getMessage("script.contexts-initialized");
    }
    
    public String scriptDirCreated() {
        return getMessage("script.dir-created");
    }
    
    public String scriptAllLoaded(int count) {
        return getMessage("script.all-loaded", "count", count);
    }
    
    public String scriptTypeLoading(String type, int count) {
        return getMessage("script.type-loading", "type", type, "count", count);
    }
    
    public String scriptLoading(String name) {
        return getMessage("script.loading", "name", name);
    }
    
    public String scriptLoaded(String name) {
        return getMessage("script.loaded", "name", name);
    }
    
    public String scriptExecuteFailed(String name, String error) {
        return getMessage("script.execute-failed", "name", name, "error", error);
    }
    
    public String scriptReadFailed(String name, String error) {
        return getMessage("script.read-failed", "name", name, "error", error);
    }
    
    public String scriptFailed(String name, String error) {
        return getMessage("script.failed", "name", name, "error", error);
    }
    
    public String scriptAllReloading() {
        return getMessage("script.all-reloading");
    }
    
    public String scriptAllReloaded() {
        return getMessage("script.all-reloaded");
    }
    
    public String scriptAllUnloaded() {
        return getMessage("script.all-unloaded");
    }
    
    public String scriptServerStarted() {
        return getMessage("script.server-started");
    }
    
    public String scriptExampleStartupFailed(String error) {
        return getMessage("script.example-startup-failed", "error", error);
    }
    
    public String scriptExampleServerFailed(String error) {
        return getMessage("script.example-server-failed", "error", error);
    }
    
    // === 事件消息 ===
    
    public String eventManagerInitialized(int interval) {
        return getMessage("event.manager-initialized", "interval", interval);
    }
    
    public String eventManagerCleaned() {
        return getMessage("event.manager-cleaned");
    }
    
    public String eventServerLoaded(String type) {
        return getMessage("event.server-loaded", "type", type);
    }
    
    public String eventTickFailed(String error) {
        return getMessage("event.tick-failed", "error", error);
    }
    
    public String eventRegistered(String event) {
        return getMessage("event.registered", "event", event);
    }
    
    public String eventNotFound(String event) {
        return getMessage("event.not-found", "event", event);
    }
    
    public String eventRegisterFailed(String error) {
        return getMessage("event.register-failed", "error", error);
    }
    
    public String eventCallbackFailed(String event, String error) {
        return getMessage("event.callback-failed", "event", event, "error", error);
    }
    
    public String eventCallbacksFailed(String event, String error) {
        return getMessage("event.callbacks-failed", "event", event, "error", error);
    }
    
    public String eventListenersCleaned() {
        return getMessage("event.listeners-cleaned");
    }
    
    // === 配置消息 ===
    
    public String configLoaded() {
        return getMessage("config.loaded");
    }
    
    public String configReloaded() {
        return getMessage("config.reloaded");
    }
    
    public String configFileCreated(String file) {
        return getMessage("config.file-created", "file", file);
    }
    
    public String configResourceSaveFailed(String file, String error) {
        return getMessage("config.resource-save-failed", "file", file, "error", error);
    }
    
    public String configDefaultCreateFailed(String error) {
        return getMessage("config.default-create-failed", "error", error);
    }
    
    public String configLibsCreated(String path) {
        return getMessage("config.libs-created", "path", path);
    }
    
    public String configResourcesCreated(String path) {
        return getMessage("config.resources-created", "path", path);
    }
    
    public String configTranslationsCreated(String path) {
        return getMessage("config.translations-created", "path", path);
    }
    
    // === 命令消息 ===
    
    public String commandRegistered() {
        return getMessage("command.registered");
    }
}
