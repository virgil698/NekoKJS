// 命令执行 API 示例
// Command Execution Examples

console.info("=== 命令执行 API 示例 / Command Execution Examples ===");

// ===== 基础命令执行 =====
// Basic Command Execution

// 执行单条控制台命令
// Server.runCommand("say Hello from NekoKJS!");

// 以玩家身份执行命令
Events.playerJoin(event => {
    let player = event.getPlayer();
    
    // 让玩家执行命令（会检查权限）
    // Server.runCommandAsPlayer(player, "spawn");
});

// ===== 批量命令执行 =====
// Batch Command Execution

// 执行多条命令
function setupNewPlayer(playerName) {
    let commands = [
        "give " + playerName + " minecraft:diamond 1",
        "give " + playerName + " minecraft:iron_sword 1",
        "give " + playerName + " minecraft:bread 16",
        "xp add " + playerName + " 100 levels"
    ];
    
    let success = Server.runCommands(...commands);
    console.info("执行了 " + success + " 条命令 / Executed " + success + " commands");
}

// 玩家首次加入时给予新手礼包
let firstJoinPlayers = new Set();

Events.playerJoin(event => {
    let player = event.getPlayer();
    
    if (!player.hasPlayedBefore()) {
        Message.send(player, "<gold>欢迎新玩家！正在发放新手礼包...");
        setupNewPlayer(player.getName());
    }
});

// ===== 延迟命令执行 =====
// Delayed Command Execution

// 3 秒后执行命令（60 ticks = 3 seconds）
function announceAfterDelay(message, seconds) {
    let ticks = seconds * 20;
    Server.runCommandLater("say " + message, ticks);
}

// 示例：玩家加入 5 秒后欢迎
Events.playerJoin(event => {
    let player = event.getPlayer();
    let playerName = player.getName();
    
    // 5 秒后发送欢迎消息
    Server.runCommandLater("tellraw " + playerName + " {\"text\":\"欢迎来到服务器！\",\"color\":\"gold\"}", 100);
});

// ===== 定时重复命令 =====
// Repeated Command Execution

// 每 5 分钟自动保存（仅示例，实际使用时取消注释）
// Server.runCommandTimer("save-all", 0, 6000); // 6000 ticks = 5 minutes

// 每小时提醒玩家
// Server.runCommandTimer("say 记得定期保存进度！", 0, 72000); // 72000 ticks = 1 hour

// ===== 条件命令执行 =====
// Conditional Command Execution

// 根据时间执行不同命令
Events.serverTick(() => {
    let time = Server.getWorldTime();
    
    // 每天早上 6:00 清理怪物
    if (time === 0) {
        Server.runCommand("kill @e[type=!player,type=!armor_stand,type=!item_frame]");
        Message.broadcast("<yellow>☀ 早安！已清理夜间怪物");
    }
    
    // 每天晚上 7:00 提醒玩家
    if (time === 13000) {
        Message.broadcast("<blue>🌙 夜幕降临，小心怪物！");
    }
});

// ===== 玩家互动命令 =====
// Player Interaction Commands

// 玩家聊天触发命令
Events.playerChat(event => {
    let player = event.getPlayer();
    let message = event.getMessage().toLowerCase();
    
    // 玩家说 "heal me" 时治疗
    if (message.includes("heal me")) {
        if (player.hasPermission("nekokjs.heal")) {
            Server.runCommandAsPlayer(player, "heal");
            Message.send(player, "<green>✓ 已治疗！");
        } else {
            Message.send(player, "<red>你没有权限使用此功能");
        }
    }
    
    // 玩家说 "spawn" 时传送
    if (message === "spawn" || message === "回城") {
        Server.runCommandAsPlayer(player, "spawn");
    }
});

// ===== 高级命令组合 =====
// Advanced Command Combinations

/**
 * 创建一个临时游戏区域
 */
function createTempArena(centerX, centerY, centerZ, radius) {
    let commands = [
        // 清理区域
        "fill " + (centerX - radius) + " " + centerY + " " + (centerZ - radius) + 
        " " + (centerX + radius) + " " + (centerY + 10) + " " + (centerZ + radius) + " air",
        
        // 创建地板
        "fill " + (centerX - radius) + " " + (centerY - 1) + " " + (centerZ - radius) + 
        " " + (centerX + radius) + " " + (centerY - 1) + " " + (centerZ + radius) + " stone",
        
        // 创建边界
        "fill " + (centerX - radius) + " " + centerY + " " + (centerZ - radius) + 
        " " + (centerX + radius) + " " + (centerY + 5) + " " + (centerZ + radius) + " barrier hollow"
    ];
    
    Server.runCommands(...commands);
    Message.broadcast("<gold>竞技场已创建！");
}

/**
 * 开始倒计时
 */
function startCountdown(seconds, onComplete) {
    let count = seconds;
    
    let countdownInterval = setInterval(() => {
        if (count > 0) {
            Message.broadcast("<yellow><bold>" + count);
            Server.runCommand("playsound minecraft:block.note_block.hat master @a ~ ~ ~ 1 1");
            count--;
        } else {
            clearInterval(countdownInterval);
            Message.broadcast("<green><bold>开始！");
            Server.runCommand("playsound minecraft:entity.ender_dragon.growl master @a ~ ~ ~ 1 1");
            if (onComplete) onComplete();
        }
    }, 1000);
}

// ===== 命令别名系统 =====
// Command Alias System

let commandAliases = {
    "tpa": "tp {player}",
    "home": "spawn",
    "suicide": "kill {player}",
    "day": "time set day",
    "night": "time set night",
    "clear": "weather clear",
    "rain": "weather rain"
};

Events.playerChat(event => {
    let player = event.getPlayer();
    let message = event.getMessage();
    
    // 检查是否是命令别名
    if (message.startsWith("!")) {
        event.setCancelled(true); // 取消聊天消息
        
        let cmd = message.substring(1).trim();
        let alias = commandAliases[cmd];
        
        if (alias) {
            let finalCmd = alias.replace("{player}", player.getName());
            Server.runCommandAsPlayer(player, finalCmd);
            Message.send(player, "<gray>执行命令: /" + finalCmd);
        } else {
            Message.send(player, "<red>未知的命令别名: " + cmd);
        }
    }
});

// ===== 命令权限检查 =====
// Command Permission Check

/**
 * 安全执行命令（检查权限）
 */
function safeRunCommand(player, command, permission) {
    if (player.hasPermission(permission)) {
        Server.runCommandAsPlayer(player, command);
        return true;
    } else {
        Message.send(player, "<red>你没有权限执行此命令");
        return false;
    }
}

// 使用示例
Events.playerInteract(event => {
    let player = event.getPlayer();
    let item = player.getInventory().getItemInMainHand();
    
    if (item != null && item.getType().toString() === "STICK") {
        // 需要 admin 权限才能使用魔法棒
        if (safeRunCommand(player, "gamemode creative", "nekokjs.admin")) {
            Message.send(player, "<gold>✨ 魔法棒：创造模式已激活");
        }
    }
});

// ===== 命令执行日志 =====
// Command Execution Logging

let commandLog = [];

function logCommand(executor, command, success) {
    let entry = {
        executor: executor,
        command: command,
        success: success,
        timestamp: Server.getCurrentTimeMillis()
    };
    
    commandLog.push(entry);
    
    // 只保留最近 100 条记录
    if (commandLog.length > 100) {
        commandLog.shift();
    }
    
    console.info("[CMD] " + executor + " -> " + command + " (" + (success ? "成功" : "失败") + ")");
}

// 包装命令执行以添加日志
function runCommandWithLog(command, executor) {
    executor = executor || "Console";
    let success = Server.runCommand(command);
    logCommand(executor, command, success);
    return success;
}

// ===== 实用命令函数 =====
// Utility Command Functions

/**
 * 传送所有玩家到指定位置
 */
function teleportAllPlayers(x, y, z) {
    Server.runCommand("tp @a " + x + " " + y + " " + z);
    Message.broadcast("<yellow>所有玩家已传送！");
}

/**
 * 给予所有玩家物品
 */
function giveAllPlayers(item, amount) {
    Server.runCommand("give @a " + item + " " + amount);
    Message.broadcast("<green>已给予所有玩家 " + amount + " 个 " + item);
}

/**
 * 清空所有玩家背包
 */
function clearAllInventories() {
    Server.runCommand("clear @a");
    Message.broadcast("<red>所有玩家背包已清空！");
}

/**
 * 设置所有玩家游戏模式
 */
function setAllGamemode(mode) {
    Server.runCommand("gamemode " + mode + " @a");
    Message.broadcast("<yellow>所有玩家游戏模式已设置为: " + mode);
}

console.info("=== 命令执行 API 示例加载完成 / Command Execution Examples Loaded ===");
