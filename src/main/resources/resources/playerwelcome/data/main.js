// NekoKJS 玩家欢迎消息包 - 主入口
// Player Welcome Message Pack - Main Entry

console.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
console.info("  NekoKJS 玩家欢迎消息包");
console.info("  Player Welcome Message Pack");
console.info("  版本: 1.0.0");
console.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

// ===== 加载配置和工具 =====
console.info("[PlayerWelcome] 正在加载配置...");

// 加载配置文件
const WelcomeConfig = load("data/config.js");

// 加载工具模块
const PlayerStorage = load("data/utils/storage.js");
const MessageFormatter = load("data/utils/formatter.js");

// 加载处理器
const { handlePlayerJoin } = load("data/handlers/join.js");
const { handlePlayerQuit } = load("data/handlers/quit.js");

console.info("[PlayerWelcome] 配置加载完成");
console.info("[PlayerWelcome] 欢迎消息: " + (WelcomeConfig.chatMessages.enabled ? "启用" : "禁用"));
console.info("[PlayerWelcome] 标题消息: " + (WelcomeConfig.titleMessage.enabled ? "启用" : "禁用"));
console.info("[PlayerWelcome] 首次加入特殊消息: " + (WelcomeConfig.firstJoin.enabled ? "启用" : "禁用"));
console.info("[PlayerWelcome] 音效: " + (WelcomeConfig.sounds.enabled ? "启用" : "禁用"));

// ===== 注册事件监听器 =====
console.info("[PlayerWelcome] 正在注册事件监听器...");

// 玩家加入事件
Events.playerJoin(event => {
    try {
        handlePlayerJoin(event, WelcomeConfig, PlayerStorage, MessageFormatter);
    } catch (e) {
        console.error("[PlayerWelcome] 处理玩家加入事件时出错: " + e.message);
        console.error(e.stack);
    }
});

// 玩家退出事件
Events.playerQuit(event => {
    try {
        handlePlayerQuit(event, WelcomeConfig, MessageFormatter);
    } catch (e) {
        console.error("[PlayerWelcome] 处理玩家退出事件时出错: " + e.message);
        console.error(e.stack);
    }
});

console.info("[PlayerWelcome] 事件监听器注册完成");

// ===== 包加载完成 =====
console.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
console.info("  玩家欢迎消息包加载完成！");
console.info("  已注册 2 个事件监听器");
console.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

// ===== 导出模块（可选）=====
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        config: WelcomeConfig,
        storage: PlayerStorage,
        formatter: MessageFormatter
    };
}
