package org.virgil.nekokjs.bridge;

import org.virgil.nekokjs.NekoKJSPlugin;
import org.virgil.nekokjs.mixin.bridge.Bridge;

/**
 * Bridge 实现类，位于 main 模块（插件侧）
 * 用于 Mixin 模块调用插件的功能
 * 
 * 注意：这个类在 main 模块中，可以直接访问插件类
 * 但 mixin 模块只能通过 Bridge 接口访问它
 */
public class NekoKJSBridge implements Bridge {
    private final NekoKJSPlugin plugin;

    public NekoKJSBridge(NekoKJSPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onServerStarted() {
        plugin.getLogger().info("服务器已启动，NekoKJS 开始初始化脚本环境...");
        
        // 在服务器启动后执行的逻辑
        if (plugin.getScriptManager() != null) {
            plugin.getScriptManager().onServerStarted();
        }
    }

    @Override
    public void onServerTick() {
        // 服务器每 tick 执行的逻辑
        if (plugin.getEventManager() != null) {
            plugin.getEventManager().onServerTick();
        }
    }

    @Override
    public Object getPlugin() {
        return plugin;
    }
}
