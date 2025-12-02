package org.virgil.nekokjs.mixin.bridge;

/**
 * Bridge 管理器，用于管理 Mixin 和插件主类之间的通信桥梁
 * 单例模式，确保全局只有一个实例
 */
public class BridgeManager {
    public static final BridgeManager INSTANCE = new BridgeManager();
    private Bridge bridge;

    private BridgeManager() {}

    public void setBridge(Bridge bridge) {
        this.bridge = bridge;
    }

    public Bridge getBridge() {
        return bridge;
    }
}
