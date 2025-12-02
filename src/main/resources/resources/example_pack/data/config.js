// 配置模块
// Configuration Module

console.info("[Config] 配置模块已加载 / Config module loaded");

// 服务器配置
const CONFIG = {
    // 欢迎消息
    welcomeMessage: "§a欢迎来到服务器！",
    
    // 是否启用加入广播
    enableJoinBroadcast: true,
    
    // 是否启用退出广播
    enableQuitBroadcast: true,
    
    // 特殊物品奖励
    rewards: {
        diamond: {
            enabled: true,
            broadcast: true,
            message: "挖到了钻石！"
        },
        netherite: {
            enabled: true,
            broadcast: true,
            message: "发现了远古残骸！"
        }
    },
    
    // 服务器 Tick 间隔（秒）
    tickInterval: 60
};

// 导出配置（在同一作用域中，其他脚本可以直接访问）
console.info("[Config] 配置已加载 / Configuration loaded");
console.info("[Config] 加入广播: " + (CONFIG.enableJoinBroadcast ? "启用" : "禁用"));
