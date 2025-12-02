package org.virgil.nekokjs.mixin.bridge;

/**
 * Bridge 接口，用于 Mixin 模块和主插件模块之间的通信
 * Mixin 模块无法直接访问主插件类，需要通过 Bridge 进行交互
 */
public interface Bridge {
    /**
     * 服务器启动完成时调用
     */
    void onServerStarted();

    /**
     * 服务器每 tick 调用
     */
    void onServerTick();

    /**
     * 获取插件实例
     */
    Object getPlugin();
}
