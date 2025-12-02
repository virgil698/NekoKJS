package org.virgil.nekokjs.script;

/**
 * 脚本类型枚举
 * 参考 KubeJS 的脚本类型设计
 */
public enum ScriptType {
    /**
     * 启动脚本 - 在插件加载时执行
     * 用于注册事件监听器、自定义物品等
     */
    STARTUP("startup"),
    
    /**
     * 服务端脚本 - 在服务器启动后执行
     * 用于服务器端逻辑、数据包替代等
     */
    SERVER("server");

    private final String name;

    ScriptType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
