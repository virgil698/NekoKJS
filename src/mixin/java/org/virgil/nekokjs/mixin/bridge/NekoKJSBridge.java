package org.virgil.nekokjs.mixin.bridge;

/**
 * Bridge 实现类，用于 Mixin 和插件主类之间的通信
 * 这是 Leaves Mixin 插件开发的标准模式
 * 注意：此类在 mixin 模块中，可以被 Mixin 类访问
 */
public class NekoKJSBridge implements Bridge {
    private Object plugin;

    public NekoKJSBridge(Object plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onServerStarted() {
        // 通过反射调用插件的方法，因为 mixin 模块无法直接访问 main 模块的类
        try {
            plugin.getClass().getMethod("getLogger").invoke(plugin);
            Object logger = plugin.getClass().getMethod("getLogger").invoke(plugin);
            logger.getClass().getMethod("info", String.class).invoke(logger, 
                "服务器已启动，NekoKJS 开始初始化脚本环境...");
            
            Object scriptManager = plugin.getClass().getMethod("getScriptManager").invoke(plugin);
            if (scriptManager != null) {
                scriptManager.getClass().getMethod("onServerStarted").invoke(scriptManager);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServerTick() {
        // 服务器每 tick 执行的逻辑
        try {
            Object eventManager = plugin.getClass().getMethod("getEventManager").invoke(plugin);
            if (eventManager != null) {
                eventManager.getClass().getMethod("onServerTick").invoke(eventManager);
            }
        } catch (Exception e) {
            // 忽略异常，避免影响服务器性能
        }
    }

    @Override
    public Object getPlugin() {
        return plugin;
    }
}
