// NekoKJS 脚本包入口文件
// Script Pack Entry Point
// 
// 这是脚本包的入口文件，在插件加载时执行
// This is the entry point of the script pack, executed when the plugin loads
//
// ===== 模块化示例 =====
// 你可以使用 load() 或 require() 来加载其他 JS 文件
// You can use load() or require() to load other JS files
//
// 加载的脚本会在同一个上下文中执行，可以共享变量和函数
// Loaded scripts execute in the same context and can share variables and functions

console.info("=================================");
console.info("示例脚本包正在加载...");
console.info("Example Pack Loading...");
console.info("=================================");

// ===== 加载模块 =====
// Load Modules
// 
// 使用 load() 或 require() 加载其他脚本文件
// Use load() or require() to load other script files

console.info("\n[Main] 开始加载模块 / Loading modules...\n");

// 1. 加载配置模块
load("data/config.js");  // 或者使用: require("data/config")

// 2. 加载工具函数模块
load("data/utils/helper.js");

// 3. 加载事件处理模块
load("data/events/player.js");
load("data/events/world.js");

console.info("\n[Main] 所有模块加载完成！/ All modules loaded!\n");

// ===== 服务器信息 =====
// Server Information

console.info("=================================");
console.info("服务器名称 / Server Name: " + Server.getName());
console.info("服务器版本 / Server Version: " + Server.getVersion());
console.info("在线玩家数 / Online Players: " + Server.getOnlinePlayerCount());
console.info("最大玩家数 / Max Players: " + Server.getMaxPlayers());
console.info("=================================");

// ===== 测试加载的模块功能 =====
// Test Loaded Module Functions
//
// 现在可以使用从其他模块加载的函数和变量了
// Now you can use functions and variables loaded from other modules

console.info("\n[Main] 测试模块功能 / Testing module functions...\n");

// 测试来自 config.js 的配置
console.info("[Main] 配置测试 - 欢迎消息: " + CONFIG.welcomeMessage);

// 测试来自 helper.js 的工具函数
// 注意：这些函数已在 player.js 和 world.js 中被使用
console.info("[Main] 工具函数已就绪，可在事件处理中使用");

// ===== 主脚本特有的事件 =====
// Main Script Specific Events

// 玩家交互事件（魔法棒示例）
Events.playerInteract(event => {
    let player = event.getPlayer();
    let action = event.getAction();
    
    if (action.toString().includes("RIGHT_CLICK")) {
        let item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType().toString() === "STICK") {
            player.sendMessage("§6✨ 魔法棒被激活了！/ Magic wand activated!");
            
            // 使用加载的工具函数
            broadcastColored(player.getName() + " 使用了魔法棒！", "§d");
        }
    }
});

// ===== 服务器 Tick 事件 =====
// Server Tick Event
// 每秒触发一次（根据配置的 tick-interval）
// Triggers once per second (based on configured tick-interval)

let tickCounter = 0;

Events.serverTick(() => {
    tickCounter++;
    
    // 使用配置中的间隔
    // Use interval from config
    if (tickCounter % CONFIG.tickInterval === 0) {
        // 定时任务
        let minutes = tickCounter / CONFIG.tickInterval;
        console.info("§7[Tick] 服务器已运行 / Server uptime: §e" + minutes + " §7分钟/minutes");
        
        // 可以在这里添加自动保存、自动备份等功能
        // You can add auto-save, auto-backup, etc. here
    }
});

// ===== 通用事件监听 =====
// Generic Event Listener
// 可以使用完整的事件类名监听任意 Bukkit 事件
// Can listen to any Bukkit event using full class name

/*
Events.on("org.bukkit.event.player.PlayerMoveEvent", event => {
    // 注意：高频事件可能影响性能
    // Warning: High-frequency events may affect performance
    console.info("玩家移动 / Player moved");
});
*/

// ===== 自定义函数 =====
// Custom Functions

/**
 * 给予玩家欢迎礼包
 * Give player a welcome kit
 */
function giveWelcomeKit(player) {
    player.sendMessage("§a已获得新手礼包！/ Received welcome kit!");
    // 在这里添加给予物品的代码
    // Add item giving code here
}

/**
 * 传送玩家到出生点
 * Teleport player to spawn
 */
function teleportToSpawn(player) {
    let spawn = Server.getWorld("world").getSpawnLocation();
    player.teleport(spawn);
    player.sendMessage("§e已传送到出生点！/ Teleported to spawn!");
}

// ===== 模块化说明 =====
// Modular Example Explanation
// 
// 📁 当前脚本包结构 / Current Pack Structure:
//
// example_pack/
//   ├── pack.yml         (包配置 / Pack config)
//   └── data/
//       ├── main.js      (入口文件 / Entry point) ← 你在这里
//       ├── config.js    (配置模块 / Config module)
//       ├── utils/
//       │   └── helper.js    (工具函数 / Utility functions)
//       └── events/
//           ├── player.js    (玩家事件 / Player events)
//           └── world.js     (世界事件 / World events)
//
// 💡 工作原理 / How it works:
//   1. main.js 是唯一的入口文件（在 pack.yml 中配置）
//   2. 使用 load("路径") 或 require("路径") 加载其他模块
//   3. 所有模块在同一个作用域中运行，共享变量和函数
//   4. 模块只会加载一次，重复调用 load() 会被跳过
//
// 🎯 优势 / Benefits:
//   ✓ 代码组织更清晰
//   ✓ 易于维护和扩展
//   ✓ 模块可以重用
//   ✓ 团队协作更方便

console.info("\n" + "=".repeat(50));
console.info("✓ 脚本包初始化完成！");
console.info("✓ Script pack initialized successfully!");
console.info("=".repeat(50) + "\n");
